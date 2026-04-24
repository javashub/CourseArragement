package com.lyk.coursearrange.schedule.engine;

import com.lyk.coursearrange.schedule.engine.model.SchedulingAssignment;
import com.lyk.coursearrange.schedule.engine.model.SchedulingClassroom;
import com.lyk.coursearrange.schedule.engine.model.SchedulingConstraintSummary;
import com.lyk.coursearrange.schedule.engine.model.SchedulingEngineRequest;
import com.lyk.coursearrange.schedule.engine.model.SchedulingExecutionResult;
import com.lyk.coursearrange.schedule.engine.model.SchedulingFailureCode;
import com.lyk.coursearrange.schedule.engine.model.SchedulingTask;
import com.lyk.coursearrange.schedule.engine.model.UnscheduledTaskDetail;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 第一阶段可行解生成器。
 */
public class FeasibleSolutionBuilder {

    /**
     * 局部重排只尝试最近少量任务，避免第一阶段搜索失控。
     */
    private static final int MAX_REARRANGE_CANDIDATES = 4;
    private static final int MAX_REARRANGE_TASK_COUNT = 2;
    private static final int DENSE_WORKLOAD_TASK_THRESHOLD = 40;
    private static final int DEFAULT_MAX_CANDIDATES_PER_CHUNK = 24;
    private static final int DENSE_MAX_CANDIDATES_PER_CHUNK = 8;

    /**
     * 候选评分权重：优先稳态可排，不追求复杂最优。
     */
    private static final double TEACHER_DAY_LOAD_WEIGHT = 4D;
    private static final double CLASS_DAY_LOAD_WEIGHT = 8D;
    private static final double PERIOD_PREFERENCE_WEIGHT = 1.5D;
    private static final double CAPACITY_FIT_WEIGHT = 0.15D;
    private static final double CROSS_DAY_DISTRIBUTION_BONUS = 6D;

    private final SchedulingConstraintEvaluator constraintEvaluator;
    private final SchedulingFailureReporter failureReporter;

    public FeasibleSolutionBuilder(SchedulingConstraintEvaluator constraintEvaluator,
                                   SchedulingFailureReporter failureReporter) {
        this.constraintEvaluator = constraintEvaluator;
        this.failureReporter = failureReporter;
    }

    public SchedulingExecutionResult build(SchedulingEngineRequest request) {
        List<SchedulingTask> tasks = sortTasks(request.getTasks(), request);
        List<PlacedTask> placedTasks = new ArrayList<>();
        List<UnscheduledTaskDetail> unscheduledTasks = new ArrayList<>();
        SchedulingConstraintEvaluator.SchedulingState state = new SchedulingConstraintEvaluator.SchedulingState();
        for (SchedulingTask task : tasks) {
            TaskPlacement placement = placeTask(task, request, state);
            if (placement.success()) {
                placedTasks.add(new PlacedTask(task, placement.pendingAssignments()));
            } else {
                RearrangementResult rearrangementResult = shouldTryLocalRearrangement(request)
                        ? tryLocalRearrangement(task, request, placedTasks)
                        : RearrangementResult.failure();
                if (rearrangementResult.success()) {
                    state = rearrangementResult.state();
                    placedTasks = rearrangementResult.placedTasks();
                } else {
                    unscheduledTasks.add(placement.failureDetail());
                }
            }
        }
        List<SchedulingAssignment> assignments = expandAssignments(placedTasks);
        int taskCount = tasks.size();
        int scheduledTaskCount = Math.max(taskCount - unscheduledTasks.size(), 0);
        double successRate = taskCount == 0
                ? 0D
                : BigDecimal.valueOf((double) scheduledTaskCount * 100 / taskCount)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
        List<SchedulingConstraintSummary> constraintSummary = failureReporter.summarize(unscheduledTasks);
        return SchedulingExecutionResult.builder()
                .taskCount(taskCount)
                .scheduledTaskCount(scheduledTaskCount)
                .unscheduledTaskCount(unscheduledTasks.size())
                .successRate(successRate)
                .assignments(assignments)
                .unscheduledTasks(unscheduledTasks)
                .constraintSummary(constraintSummary)
                .build();
    }

    private List<SchedulingTask> sortTasks(List<SchedulingTask> tasks, SchedulingEngineRequest request) {
        if (tasks == null || tasks.isEmpty()) {
            return List.of();
        }
        return tasks.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator
                        .comparing(SchedulingTask::isFixedTime).reversed()
                        .thenComparing(task -> task.isNeedContinuous() ? 1 : 0, Comparator.reverseOrder())
                        .thenComparing(task -> task.isNeedSpecialRoom() ? 1 : 0, Comparator.reverseOrder())
                        .thenComparing(task -> estimateCandidateCount(task, request))
                        .thenComparing(SchedulingTask::getPriorityLevel, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(SchedulingTask::getTaskId, Comparator.nullsLast(Long::compareTo)))
                .toList();
    }

    private int estimateCandidateCount(SchedulingTask task, SchedulingEngineRequest request) {
        List<Integer> chunkSizes = splitChunkSizes(task);
        if (task.isFixedTime()) {
            return task.getFixedTimeSlots() == null ? 0 : task.getFixedTimeSlots().size();
        }
        return java.util.stream.IntStream.range(0, chunkSizes.size())
                .map(index -> candidateSlotBlocks(task, request.getTimeSlotCodes(), chunkSizes, index).size())
                .sum();
    }

    private TaskPlacement placeTask(SchedulingTask task,
                                    SchedulingEngineRequest request,
                                    SchedulingConstraintEvaluator.SchedulingState state) {
        List<Integer> chunkSizes = splitChunkSizes(task);
        List<PendingAssignment> pendingAssignments = new ArrayList<>();
        Map<SchedulingFailureCode, Integer> failureCounter = new LinkedHashMap<>();
        boolean success = placeChunk(task, chunkSizes, 0, request, state, pendingAssignments, failureCounter);
        if (!success) {
            return TaskPlacement.failure(failureReporter.buildFailureDetail(task, failureCounter));
        }
        return TaskPlacement.success(pendingAssignments);
    }

    private boolean placeChunk(SchedulingTask task,
                               List<Integer> chunkSizes,
                               int chunkIndex,
                               SchedulingEngineRequest request,
                               SchedulingConstraintEvaluator.SchedulingState state,
                               List<PendingAssignment> pendingAssignments,
                               Map<SchedulingFailureCode, Integer> failureCounter) {
        if (chunkIndex >= chunkSizes.size()) {
            return true;
        }
        int chunkSize = chunkSizes.get(chunkIndex);
        List<CandidatePlacement> candidates = rankedCandidates(task, request, state, chunkSizes, chunkIndex, failureCounter);
        if (candidates.isEmpty()) {
            return false;
        }
        for (CandidatePlacement candidate : candidates) {
            PendingAssignment pending = new PendingAssignment(candidate.slotCodes(), candidate.classroom());
            pendingAssignments.add(pending);
            state.reserve(task, candidate.classroom(), candidate.slotCodes(), constraintEvaluator);
            boolean placed = placeChunk(task, chunkSizes, chunkIndex + 1, request, state, pendingAssignments, failureCounter);
            if (placed) {
                return true;
            }
            state.release(task, candidate.classroom(), candidate.slotCodes(), constraintEvaluator);
            pendingAssignments.remove(pendingAssignments.size() - 1);
        }
        return false;
    }

    private List<CandidatePlacement> rankedCandidates(SchedulingTask task,
                                                      SchedulingEngineRequest request,
                                                      SchedulingConstraintEvaluator.SchedulingState state,
                                                      List<Integer> chunkSizes,
                                                      int chunkIndex,
                                                      Map<SchedulingFailureCode, Integer> failureCounter) {
        List<List<String>> candidateBlocks = candidateSlotBlocks(task, request.getTimeSlotCodes(), chunkSizes, chunkIndex);
        if (candidateBlocks.isEmpty()) {
            if (task.isFixedTime()) {
                incrementFailure(failureCounter, SchedulingFailureCode.FIXED_TIME_UNSATISFIED);
            } else {
                incrementFailure(failureCounter, task.isNeedContinuous()
                        ? SchedulingFailureCode.CONTINUOUS_SLOT_UNAVAILABLE
                        : SchedulingFailureCode.NO_AVAILABLE_TIME_SLOT);
            }
            return List.of();
        }
        Set<Integer> allWeekdays = resolveWeekdays(request.getTimeSlotCodes());
        List<CandidatePlacement> candidates = new ArrayList<>();
        for (List<String> slotCodes : candidateBlocks) {
            List<SchedulingClassroom> classrooms = constraintEvaluator.selectCandidateClassrooms(task, request.getClassrooms(), state, slotCodes);
            if (classrooms.isEmpty()) {
                incrementFailure(failureCounter, task.isNeedSpecialRoom()
                        ? SchedulingFailureCode.SPECIAL_ROOM_UNAVAILABLE
                        : SchedulingFailureCode.NO_AVAILABLE_TIME_SLOT);
                continue;
            }
            for (SchedulingClassroom classroom : classrooms) {
                SchedulingConstraintEvaluator.EvaluationResult evaluation = constraintEvaluator.evaluate(task, slotCodes, classroom, state);
                if (!evaluation.success()) {
                    evaluation.failures().forEach(code -> incrementFailure(failureCounter, code));
                    continue;
                }
                double score = scoreCandidate(task, classroom, slotCodes, state, allWeekdays);
                candidates.add(new CandidatePlacement(slotCodes, classroom, score));
            }
        }
        return candidates.stream()
                .sorted(Comparator
                        .comparingDouble(CandidatePlacement::score)
                        .thenComparing(candidate -> candidate.slotCodes().isEmpty() ? "" : candidate.slotCodes().get(0))
                        .thenComparing(candidate -> candidate.classroom().getClassroomCode(), Comparator.nullsLast(String::compareTo)))
                .limit(resolveCandidateLimit(request))
                .toList();
    }

    private boolean shouldTryLocalRearrangement(SchedulingEngineRequest request) {
        return taskCount(request) < DENSE_WORKLOAD_TASK_THRESHOLD;
    }

    private long resolveCandidateLimit(SchedulingEngineRequest request) {
        return taskCount(request) >= DENSE_WORKLOAD_TASK_THRESHOLD
                ? DENSE_MAX_CANDIDATES_PER_CHUNK
                : DEFAULT_MAX_CANDIDATES_PER_CHUNK;
    }

    private int taskCount(SchedulingEngineRequest request) {
        return request == null || request.getTasks() == null ? 0 : request.getTasks().size();
    }

    /**
     * 候选评分只用于第一阶段排序，所有硬约束仍由 evaluator 严格校验。
     */
    private double scoreCandidate(SchedulingTask task,
                                  SchedulingClassroom classroom,
                                  List<String> slotCodes,
                                  SchedulingConstraintEvaluator.SchedulingState state,
                                  Set<Integer> allWeekdays) {
        double score = 0D;
        Map<Integer, Long> grouped = slotCodes.stream()
                .collect(Collectors.groupingBy(constraintEvaluator::resolveWeekday, Collectors.counting()));
        for (Map.Entry<Integer, Long> entry : grouped.entrySet()) {
            int weekday = entry.getKey();
            int unitCount = entry.getValue().intValue();
            int teacherLoadAfter = state.teacherDayLoad(task.getTeacherNo(), weekday) + unitCount;
            int classLoadAfter = state.classDayLoad(task.getClassNo(), weekday) + unitCount;
            score += teacherLoadAfter * teacherLoadAfter * TEACHER_DAY_LOAD_WEIGHT;
            score += classLoadAfter * classLoadAfter * CLASS_DAY_LOAD_WEIGHT;
            if (state.classDayLoad(task.getClassNo(), weekday) == 0
                    && state.classHasLoadOnOtherDay(task.getClassNo(), weekday, allWeekdays)) {
                score -= CROSS_DAY_DISTRIBUTION_BONUS;
            }
        }
        for (String slotCode : slotCodes) {
            score += periodPenalty(constraintEvaluator.resolvePeriod(slotCode)) * PERIOD_PREFERENCE_WEIGHT;
        }
        int seatCount = classroom.getSeatCount() == null ? 0 : classroom.getSeatCount();
        score += Math.abs(seatCount - safeStudentCount(task)) * CAPACITY_FIT_WEIGHT;
        return score;
    }

    private int safeStudentCount(SchedulingTask task) {
        return task.getStudentCount() == null ? 0 : task.getStudentCount();
    }

    private double periodPenalty(int periodNo) {
        if (periodNo <= 2) {
            return 0D;
        }
        return periodNo - 2;
    }

    private Set<Integer> resolveWeekdays(List<String> slotCodes) {
        if (slotCodes == null || slotCodes.isEmpty()) {
            return Set.of();
        }
        return slotCodes.stream()
                .map(constraintEvaluator::resolveWeekday)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * 第一阶段有限重排：只挪动少量近期非固定任务，避免 first-fit 早早锁死。
     */
    private RearrangementResult tryLocalRearrangement(SchedulingTask currentTask,
                                                      SchedulingEngineRequest request,
                                                      List<PlacedTask> placedTasks) {
        if (placedTasks.isEmpty()) {
            return RearrangementResult.failure();
        }
        List<PlacedTask> candidates = pickRearrangementCandidates(currentTask, placedTasks);
        if (candidates.isEmpty()) {
            return RearrangementResult.failure();
        }
        for (List<PlacedTask> movingGroup : enumerateMovingGroups(candidates)) {
            List<PlacedTask> baseline = new ArrayList<>(placedTasks);
            baseline.removeAll(movingGroup);
            SchedulingConstraintEvaluator.SchedulingState state = rebuildState(baseline);
            TaskPlacement currentPlacement = placeTask(currentTask, request, state);
            if (!currentPlacement.success()) {
                continue;
            }
            List<PlacedTask> rebuilt = new ArrayList<>(baseline);
            rebuilt.add(new PlacedTask(currentTask, currentPlacement.pendingAssignments()));
            boolean allMoved = true;
            for (PlacedTask item : movingGroup) {
                TaskPlacement movedPlacement = placeTask(item.task(), request, state);
                if (!movedPlacement.success()) {
                    allMoved = false;
                    break;
                }
                rebuilt.add(new PlacedTask(item.task(), movedPlacement.pendingAssignments()));
            }
            if (allMoved) {
                return RearrangementResult.success(state, rebuilt);
            }
        }
        return RearrangementResult.failure();
    }

    private List<PlacedTask> pickRearrangementCandidates(SchedulingTask currentTask, List<PlacedTask> placedTasks) {
        List<PlacedTask> related = new ArrayList<>();
        List<PlacedTask> others = new ArrayList<>();
        for (int i = placedTasks.size() - 1; i >= 0; i--) {
            PlacedTask item = placedTasks.get(i);
            if (item.task().isFixedTime()) {
                continue;
            }
            if (isRelatedTask(currentTask, item.task())) {
                related.add(item);
            } else {
                others.add(item);
            }
        }
        List<PlacedTask> ordered = new ArrayList<>();
        related.forEach(ordered::add);
        others.forEach(ordered::add);
        return ordered.stream()
                .limit(MAX_REARRANGE_CANDIDATES)
                .toList();
    }

    private boolean isRelatedTask(SchedulingTask left, SchedulingTask right) {
        return Objects.equals(left.getClassNo(), right.getClassNo())
                || Objects.equals(left.getTeacherNo(), right.getTeacherNo());
    }

    private List<List<PlacedTask>> enumerateMovingGroups(List<PlacedTask> candidates) {
        if (candidates.isEmpty()) {
            return List.of();
        }
        List<List<PlacedTask>> groups = new ArrayList<>();
        for (int i = 0; i < candidates.size(); i++) {
            groups.add(List.of(candidates.get(i)));
        }
        if (MAX_REARRANGE_TASK_COUNT >= 2) {
            for (int i = 0; i < candidates.size(); i++) {
                for (int j = i + 1; j < candidates.size(); j++) {
                    groups.add(List.of(candidates.get(i), candidates.get(j)));
                }
            }
        }
        return groups;
    }

    private SchedulingConstraintEvaluator.SchedulingState rebuildState(List<PlacedTask> placedTasks) {
        SchedulingConstraintEvaluator.SchedulingState state = new SchedulingConstraintEvaluator.SchedulingState();
        for (PlacedTask placedTask : placedTasks) {
            for (PendingAssignment pending : placedTask.pendingAssignments()) {
                state.reserve(placedTask.task(), pending.classroom(), pending.slotCodes(), constraintEvaluator);
            }
        }
        return state;
    }

    private List<SchedulingAssignment> expandAssignments(List<PlacedTask> placedTasks) {
        List<SchedulingAssignment> assignments = new ArrayList<>();
        for (PlacedTask placedTask : placedTasks) {
            for (PendingAssignment item : placedTask.pendingAssignments()) {
                for (String slotCode : item.slotCodes()) {
                    assignments.add(SchedulingAssignment.builder()
                            .taskId(placedTask.task().getTaskId())
                            .taskCode(placedTask.task().getTaskCode())
                            .classNo(placedTask.task().getClassNo())
                            .courseNo(placedTask.task().getCourseNo())
                            .teacherNo(placedTask.task().getTeacherNo())
                            .classroomId(item.classroom().getClassroomId())
                            .classroomCode(item.classroom().getClassroomCode())
                            .weekdayNo(constraintEvaluator.resolveWeekday(slotCode))
                            .periodNo(constraintEvaluator.resolvePeriod(slotCode))
                            .timeSlotCode(slotCode)
                            .build());
                }
            }
        }
        return assignments;
    }

    private List<List<String>> candidateSlotBlocks(SchedulingTask task,
                                                   List<String> slotCodes,
                                                   List<Integer> chunkSizes,
                                                   int chunkIndex) {
        if (slotCodes == null || slotCodes.isEmpty()) {
            return List.of();
        }
        if (chunkSizes == null || chunkIndex < 0 || chunkIndex >= chunkSizes.size()) {
            return List.of();
        }
        int chunkSize = chunkSizes.get(chunkIndex);
        if (task.isFixedTime()) {
            List<String> fixedSlots = task.getFixedTimeSlots() == null ? List.of() : task.getFixedTimeSlots();
            if (fixedSlots.isEmpty()) {
                return List.of();
            }
            if (!new java.util.LinkedHashSet<>(slotCodes).containsAll(fixedSlots)) {
                return List.of();
            }
            if (task.isNeedContinuous()) {
                int start = cumulativeChunkOffset(chunkSizes, chunkIndex);
                int end = start + chunkSize;
                if (start < 0 || end > fixedSlots.size()) {
                    return List.of();
                }
                List<String> fixedBlock = fixedSlots.subList(start, end);
                return isContinuousBlock(fixedBlock) ? List.of(fixedBlock) : List.of();
            }
            return fixedSlots.stream().map(List::of).toList();
        }
        List<List<String>> blocks = new ArrayList<>();
        for (int i = 0; i <= slotCodes.size() - chunkSize; i++) {
            List<String> block = new ArrayList<>(slotCodes.subList(i, i + chunkSize));
            if (task.isNeedContinuous() && chunkSize > 1 && !isContinuousBlock(block)) {
                continue;
            }
            blocks.add(block);
        }
        return blocks;
    }

    private int cumulativeChunkOffset(List<Integer> chunkSizes, int chunkIndex) {
        int offset = 0;
        for (int i = 0; i < chunkIndex; i++) {
            offset += chunkSizes.get(i);
        }
        return offset;
    }

    private boolean isContinuousBlock(List<String> block) {
        if (block.size() <= 1) {
            return true;
        }
        for (int i = 1; i < block.size(); i++) {
            int previous = Integer.parseInt(block.get(i - 1));
            int current = Integer.parseInt(block.get(i));
            int previousWeekday = constraintEvaluator.resolveWeekday(block.get(i - 1));
            int currentWeekday = constraintEvaluator.resolveWeekday(block.get(i));
            if (current != previous + 1 || currentWeekday != previousWeekday) {
                return false;
            }
        }
        return true;
    }

    private List<Integer> splitChunkSizes(SchedulingTask task) {
        // weekHours 现在直接表示“每周占用几个时间格”，不再按旧规则折半。
        int requiredUnits = Math.max(1, task.getWeekHours() == null ? 0 : task.getWeekHours());
        if (!task.isNeedContinuous()) {
            return java.util.stream.IntStream.range(0, requiredUnits).mapToObj(index -> 1).toList();
        }
        int chunkSize = Math.max(2, task.getContinuousSize() == null ? 2 : task.getContinuousSize());
        List<Integer> chunks = new ArrayList<>();
        int remaining = requiredUnits;
        while (remaining > 0) {
            int current = Math.min(chunkSize, remaining);
            chunks.add(current);
            remaining -= current;
        }
        return chunks;
    }

    private void incrementFailure(Map<SchedulingFailureCode, Integer> failureCounter, SchedulingFailureCode code) {
        failureCounter.put(code, failureCounter.getOrDefault(code, 0) + 1);
    }

    private record CandidatePlacement(List<String> slotCodes, SchedulingClassroom classroom, double score) {
    }

    private record PlacedTask(SchedulingTask task, List<PendingAssignment> pendingAssignments) {
    }

    private record RearrangementResult(boolean success,
                                       SchedulingConstraintEvaluator.SchedulingState state,
                                       List<PlacedTask> placedTasks) {

        static RearrangementResult success(SchedulingConstraintEvaluator.SchedulingState state,
                                           List<PlacedTask> placedTasks) {
            return new RearrangementResult(true, state, placedTasks);
        }

        static RearrangementResult failure() {
            return new RearrangementResult(false, null, List.of());
        }
    }

    private record PendingAssignment(List<String> slotCodes, SchedulingClassroom classroom) {
    }

    private record TaskPlacement(boolean success,
                                 List<PendingAssignment> pendingAssignments,
                                 UnscheduledTaskDetail failureDetail) {

        static TaskPlacement success(List<PendingAssignment> pendingAssignments) {
            return new TaskPlacement(true, List.copyOf(pendingAssignments), null);
        }

        static TaskPlacement failure(UnscheduledTaskDetail detail) {
            return new TaskPlacement(false, List.of(), detail);
        }
    }
}
