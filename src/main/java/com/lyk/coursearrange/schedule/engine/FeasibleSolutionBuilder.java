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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 第一阶段可行解生成器。
 */
public class FeasibleSolutionBuilder {

    private final SchedulingConstraintEvaluator constraintEvaluator;
    private final SchedulingFailureReporter failureReporter;

    public FeasibleSolutionBuilder(SchedulingConstraintEvaluator constraintEvaluator,
                                   SchedulingFailureReporter failureReporter) {
        this.constraintEvaluator = constraintEvaluator;
        this.failureReporter = failureReporter;
    }

    public SchedulingExecutionResult build(SchedulingEngineRequest request) {
        List<SchedulingTask> tasks = sortTasks(request.getTasks(), request);
        List<SchedulingAssignment> assignments = new ArrayList<>();
        List<UnscheduledTaskDetail> unscheduledTasks = new ArrayList<>();
        SchedulingConstraintEvaluator.SchedulingState state = new SchedulingConstraintEvaluator.SchedulingState();
        for (SchedulingTask task : tasks) {
            TaskPlacement placement = placeTask(task, request, state);
            if (placement.success()) {
                placement.assignments().forEach(assignments::add);
            } else {
                unscheduledTasks.add(placement.failureDetail());
            }
        }
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
        return chunkSizes.stream()
                .mapToInt(chunk -> candidateSlotBlocks(task, request.getTimeSlotCodes(), chunk).size())
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
        List<SchedulingAssignment> expandedAssignments = new ArrayList<>();
        for (PendingAssignment item : pendingAssignments) {
            for (String slotCode : item.slotCodes()) {
                expandedAssignments.add(SchedulingAssignment.builder()
                        .taskId(task.getTaskId())
                        .taskCode(task.getTaskCode())
                        .classNo(task.getClassNo())
                        .courseNo(task.getCourseNo())
                        .teacherNo(task.getTeacherNo())
                        .classroomId(item.classroom().getClassroomId())
                        .classroomCode(item.classroom().getClassroomCode())
                        .weekdayNo(constraintEvaluator.resolveWeekday(slotCode))
                        .periodNo(constraintEvaluator.resolvePeriod(slotCode))
                        .timeSlotCode(slotCode)
                        .build());
            }
        }
        return TaskPlacement.success(expandedAssignments);
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
        List<List<String>> candidateBlocks = candidateSlotBlocks(task, request.getTimeSlotCodes(), chunkSize);
        if (candidateBlocks.isEmpty()) {
            incrementFailure(failureCounter, task.isNeedContinuous()
                    ? SchedulingFailureCode.CONTINUOUS_SLOT_UNAVAILABLE
                    : SchedulingFailureCode.NO_AVAILABLE_TIME_SLOT);
            return false;
        }
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
                PendingAssignment pending = new PendingAssignment(slotCodes, classroom);
                pendingAssignments.add(pending);
                state.reserve(task, classroom, slotCodes, constraintEvaluator);
                boolean placed = placeChunk(task, chunkSizes, chunkIndex + 1, request, state, pendingAssignments, failureCounter);
                if (placed) {
                    return true;
                }
                state.release(task, classroom, slotCodes, constraintEvaluator);
                pendingAssignments.remove(pendingAssignments.size() - 1);
            }
        }
        return false;
    }

    private List<List<String>> candidateSlotBlocks(SchedulingTask task, List<String> slotCodes, int chunkSize) {
        if (slotCodes == null || slotCodes.isEmpty()) {
            return List.of();
        }
        if (task.isFixedTime()) {
            List<String> fixedSlots = task.getFixedTimeSlots() == null ? List.of() : task.getFixedTimeSlots();
            if (fixedSlots.isEmpty()) {
                return List.of();
            }
            if (task.isNeedContinuous()) {
                return fixedSlots.size() >= chunkSize ? List.of(fixedSlots.subList(0, chunkSize)) : List.of();
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

    private boolean isContinuousBlock(List<String> block) {
        if (block.size() <= 1) {
            return true;
        }
        for (int i = 1; i < block.size(); i++) {
            int previous = Integer.parseInt(block.get(i - 1));
            int current = Integer.parseInt(block.get(i));
            int previousWeekday = ((previous - 1) / 5) + 1;
            int currentWeekday = ((current - 1) / 5) + 1;
            if (current != previous + 1 || currentWeekday != previousWeekday) {
                return false;
            }
        }
        return true;
    }

    private List<Integer> splitChunkSizes(SchedulingTask task) {
        int requiredUnits = Math.max(1, (task.getWeekHours() == null ? 0 : task.getWeekHours()) / 2);
        if (!task.isNeedContinuous()) {
            return java.util.stream.IntStream.range(0, requiredUnits).mapToObj(index -> 1).toList();
        }
        int chunkSize = Math.max(2, task.getContinuousSize() == null ? 2 : task.getContinuousSize());
        List<Integer> chunks = new ArrayList<>();
        int remaining = requiredUnits;
        while (remaining > 0) {
            int current = Math.min(chunkSize, remaining);
            if (current == 1 && !chunks.isEmpty()) {
                current = chunkSize;
            }
            chunks.add(current);
            remaining -= current;
        }
        return chunks;
    }

    private void incrementFailure(Map<SchedulingFailureCode, Integer> failureCounter, SchedulingFailureCode code) {
        failureCounter.put(code, failureCounter.getOrDefault(code, 0) + 1);
    }

    private record PendingAssignment(List<String> slotCodes, SchedulingClassroom classroom) {
    }

    private record TaskPlacement(boolean success,
                                 List<SchedulingAssignment> assignments,
                                 UnscheduledTaskDetail failureDetail) {

        static TaskPlacement success(List<SchedulingAssignment> assignments) {
            return new TaskPlacement(true, assignments, null);
        }

        static TaskPlacement failure(UnscheduledTaskDetail detail) {
            return new TaskPlacement(false, List.of(), detail);
        }
    }
}
