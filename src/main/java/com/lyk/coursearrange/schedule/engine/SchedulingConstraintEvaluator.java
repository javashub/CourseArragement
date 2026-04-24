package com.lyk.coursearrange.schedule.engine;

import com.lyk.coursearrange.schedule.engine.model.SchedulingAssignment;
import com.lyk.coursearrange.schedule.engine.model.SchedulingClassroom;
import com.lyk.coursearrange.schedule.engine.model.SchedulingFailureCode;
import com.lyk.coursearrange.schedule.engine.model.SchedulingTask;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 统一约束校验。
 */
public class SchedulingConstraintEvaluator {

    EvaluationResult evaluate(SchedulingTask task,
                              List<String> slotCodes,
                              SchedulingClassroom classroom,
                              SchedulingState state) {
        List<SchedulingFailureCode> failures = new ArrayList<>();
        if (slotCodes == null || slotCodes.isEmpty()) {
            failures.add(SchedulingFailureCode.NO_AVAILABLE_TIME_SLOT);
            return new EvaluationResult(false, failures);
        }
        if (task.isFixedTime() && !matchesFixedSlots(task, slotCodes)) {
            failures.add(SchedulingFailureCode.FIXED_TIME_UNSATISFIED);
        }
        if (task.isNeedContinuous() && !isContinuous(slotCodes)) {
            failures.add(SchedulingFailureCode.CONTINUOUS_SLOT_UNAVAILABLE);
        }
        if (containsAny(slotCodes, task.getTeacherForbiddenTimeSlots())) {
            failures.add(SchedulingFailureCode.TEACHER_FORBIDDEN_TIME);
        }
        if (containsAny(slotCodes, task.getClassForbiddenTimeSlots())) {
            failures.add(SchedulingFailureCode.CLASS_FORBIDDEN_TIME);
        }
        if (hasClassConflict(task, slotCodes, state)) {
            failures.add(SchedulingFailureCode.CLASS_CONFLICT);
        }
        if (hasTeacherConflict(task, slotCodes, state)) {
            failures.add(SchedulingFailureCode.TEACHER_CONFLICT);
        }
        if (exceedsTeacherDayLimit(task, slotCodes, state)) {
            failures.add(SchedulingFailureCode.TEACHER_DAY_HOUR_LIMIT);
        }
        if (classroom == null) {
            failures.add(resolveRoomFailure(task));
        } else {
            if (!matchesClassroom(task, classroom)) {
                failures.add(resolveRoomFailure(task));
            }
            if (hasClassroomConflict(classroom, slotCodes, state)) {
                failures.add(SchedulingFailureCode.CLASSROOM_CONFLICT);
            }
        }
        return new EvaluationResult(failures.isEmpty(), failures);
    }

    List<SchedulingClassroom> selectCandidateClassrooms(SchedulingTask task,
                                                        List<SchedulingClassroom> classrooms,
                                                        SchedulingState state,
                                                        List<String> slotCodes) {
        if (classrooms == null || classrooms.isEmpty()) {
            return List.of();
        }
        return classrooms.stream()
                .filter(Objects::nonNull)
                .filter(room -> matchesClassroom(task, room))
                .sorted(Comparator
                        .comparing((SchedulingClassroom room) -> scopeDistance(task, room))
                        .thenComparing(room -> Math.abs((room.getSeatCount() == null ? 0 : room.getSeatCount()) - safeStudentCount(task)))
                        .thenComparing(room -> classroomConflictCount(room, slotCodes, state))
                        .thenComparing(SchedulingClassroom::getClassroomCode, Comparator.nullsLast(String::compareTo)))
                .toList();
    }

    private boolean matchesFixedSlots(SchedulingTask task, List<String> slotCodes) {
        List<String> fixedSlots = normalizeSlotCodes(task.getFixedTimeSlots());
        List<String> candidateSlots = normalizeSlotCodes(slotCodes);
        if (fixedSlots.isEmpty() || candidateSlots.isEmpty()) {
            return false;
        }
        Set<String> fixedSet = new LinkedHashSet<>(fixedSlots);
        return candidateSlots.stream().allMatch(fixedSet::contains);
    }

    private List<String> normalizeSlotCodes(List<String> slotCodes) {
        if (slotCodes == null) {
            return List.of();
        }
        return slotCodes.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .toList();
    }

    private boolean isContinuous(List<String> slotCodes) {
        if (slotCodes.size() <= 1) {
            return true;
        }
        for (int i = 1; i < slotCodes.size(); i++) {
            int previous = parseSlotCode(slotCodes.get(i - 1));
            int current = parseSlotCode(slotCodes.get(i));
            if (current != previous + 1 || resolveWeekday(slotCodes.get(i - 1)) != resolveWeekday(slotCodes.get(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean containsAny(List<String> source, List<String> targets) {
        if (source == null || source.isEmpty() || targets == null || targets.isEmpty()) {
            return false;
        }
        Set<String> targetSet = new LinkedHashSet<>(targets);
        return source.stream().anyMatch(targetSet::contains);
    }

    private boolean hasClassConflict(SchedulingTask task, List<String> slotCodes, SchedulingState state) {
        return slotCodes.stream().anyMatch(slotCode -> state.classBusy(task.getClassNo(), slotCode));
    }

    private boolean hasTeacherConflict(SchedulingTask task, List<String> slotCodes, SchedulingState state) {
        return slotCodes.stream().anyMatch(slotCode -> state.teacherBusy(task.getTeacherNo(), slotCode));
    }

    private boolean exceedsTeacherDayLimit(SchedulingTask task, List<String> slotCodes, SchedulingState state) {
        if (task.getTeacherMaxDayHours() == null || task.getTeacherMaxDayHours() <= 0) {
            return false;
        }
        Map<Integer, Long> grouped = slotCodes.stream()
                .collect(Collectors.groupingBy(this::resolveWeekday, Collectors.counting()));
        for (Map.Entry<Integer, Long> entry : grouped.entrySet()) {
            int existing = state.teacherDayLoad(task.getTeacherNo(), entry.getKey());
            if (existing + entry.getValue() > task.getTeacherMaxDayHours()) {
                return true;
            }
        }
        return false;
    }

    private boolean hasClassroomConflict(SchedulingClassroom classroom, List<String> slotCodes, SchedulingState state) {
        if (classroom.getClassroomCode() == null || classroom.getClassroomCode().isBlank()) {
            return false;
        }
        return slotCodes.stream().anyMatch(slotCode -> state.classroomBusy(classroom.getClassroomCode(), slotCode));
    }

    private boolean matchesClassroom(SchedulingTask task, SchedulingClassroom classroom) {
        if (classroom == null) {
            return false;
        }
        if (task.getFixedRoomId() != null && task.getFixedRoomId() > 0) {
            return Objects.equals(task.getFixedRoomId(), classroom.getClassroomId());
        }
        if (task.getFixedRoomCode() != null && !task.getFixedRoomCode().isBlank()) {
            return task.getFixedRoomCode().equalsIgnoreCase(classroom.getClassroomCode());
        }
        if (classroom.getSeatCount() != null && classroom.getSeatCount() < safeStudentCount(task)) {
            return false;
        }
        if (task.isNeedSpecialRoom()) {
            return normalizeRoomType(task.getRequiredRoomType()).equals(normalizeRoomType(classroom.getRoomType()));
        }
        return true;
    }

    private int scopeDistance(SchedulingTask task, SchedulingClassroom classroom) {
        int score = 2;
        if (task.getCampusId() != null && Objects.equals(task.getCampusId(), classroom.getCampusId())) {
            score -= 1;
        }
        if (task.getCollegeId() != null && Objects.equals(task.getCollegeId(), classroom.getCollegeId())) {
            score -= 1;
        }
        return Math.max(score, 0);
    }

    private int classroomConflictCount(SchedulingClassroom classroom, List<String> slotCodes, SchedulingState state) {
        if (slotCodes == null || slotCodes.isEmpty()) {
            return 0;
        }
        return (int) slotCodes.stream()
                .filter(slotCode -> state.classroomBusy(classroom.getClassroomCode(), slotCode))
                .count();
    }

    private SchedulingFailureCode resolveRoomFailure(SchedulingTask task) {
        return task.isNeedSpecialRoom() || (task.getRequiredRoomType() != null && !task.getRequiredRoomType().isBlank())
                ? SchedulingFailureCode.SPECIAL_ROOM_UNAVAILABLE
                : SchedulingFailureCode.NO_AVAILABLE_TIME_SLOT;
    }

    private int safeStudentCount(SchedulingTask task) {
        return task.getStudentCount() == null ? 0 : task.getStudentCount();
    }

    int resolveWeekday(String slotCode) {
        int code = parseSlotCode(slotCode);
        if (code >= 100) {
            return code / 100;
        }
        return ((code - 1) / 5) + 1;
    }

    int resolvePeriod(String slotCode) {
        int code = parseSlotCode(slotCode);
        if (code >= 100) {
            return code % 100;
        }
        return ((code - 1) % 5) + 1;
    }

    private int parseSlotCode(String slotCode) {
        return Integer.parseInt(slotCode);
    }

    private String normalizeRoomType(String roomType) {
        return roomType == null || roomType.isBlank()
                ? "NORMAL"
                : roomType.trim().toUpperCase(Locale.ROOT);
    }

    record EvaluationResult(boolean success, List<SchedulingFailureCode> failures) {
    }

    static class SchedulingState {

        private final Set<String> classSlots = new LinkedHashSet<>();
        private final Set<String> teacherSlots = new LinkedHashSet<>();
        private final Set<String> classroomSlots = new LinkedHashSet<>();
        private final Map<String, Integer> teacherDayLoads = new java.util.HashMap<>();
        private final Map<String, Integer> classDayLoads = new java.util.HashMap<>();

        void reserve(SchedulingTask task, SchedulingClassroom classroom, List<String> slotCodes, SchedulingConstraintEvaluator evaluator) {
            for (String slotCode : slotCodes) {
                classSlots.add(task.getClassNo() + "::" + slotCode);
                teacherSlots.add(task.getTeacherNo() + "::" + slotCode);
                if (classroom != null && classroom.getClassroomCode() != null) {
                    classroomSlots.add(classroom.getClassroomCode() + "::" + slotCode);
                }
                int weekday = evaluator.resolveWeekday(slotCode);
                String teacherDayKey = task.getTeacherNo() + "::" + weekday;
                teacherDayLoads.put(teacherDayKey, teacherDayLoads.getOrDefault(teacherDayKey, 0) + 1);
                String classDayKey = task.getClassNo() + "::" + weekday;
                classDayLoads.put(classDayKey, classDayLoads.getOrDefault(classDayKey, 0) + 1);
            }
        }

        void release(SchedulingTask task, SchedulingClassroom classroom, List<String> slotCodes, SchedulingConstraintEvaluator evaluator) {
            for (String slotCode : slotCodes) {
                classSlots.remove(task.getClassNo() + "::" + slotCode);
                teacherSlots.remove(task.getTeacherNo() + "::" + slotCode);
                if (classroom != null && classroom.getClassroomCode() != null) {
                    classroomSlots.remove(classroom.getClassroomCode() + "::" + slotCode);
                }
                int weekday = evaluator.resolveWeekday(slotCode);
                String teacherDayKey = task.getTeacherNo() + "::" + weekday;
                int current = teacherDayLoads.getOrDefault(teacherDayKey, 0);
                if (current <= 1) {
                    teacherDayLoads.remove(teacherDayKey);
                } else {
                    teacherDayLoads.put(teacherDayKey, current - 1);
                }
                String classDayKey = task.getClassNo() + "::" + weekday;
                int classCurrent = classDayLoads.getOrDefault(classDayKey, 0);
                if (classCurrent <= 1) {
                    classDayLoads.remove(classDayKey);
                } else {
                    classDayLoads.put(classDayKey, classCurrent - 1);
                }
            }
        }

        boolean classBusy(String classNo, String slotCode) {
            return classSlots.contains(classNo + "::" + slotCode);
        }

        boolean teacherBusy(String teacherNo, String slotCode) {
            return teacherSlots.contains(teacherNo + "::" + slotCode);
        }

        boolean classroomBusy(String classroomCode, String slotCode) {
            return classroomSlots.contains(classroomCode + "::" + slotCode);
        }

        int teacherDayLoad(String teacherNo, int weekday) {
            return teacherDayLoads.getOrDefault(teacherNo + "::" + weekday, 0);
        }

        int classDayLoad(String classNo, int weekday) {
            return classDayLoads.getOrDefault(classNo + "::" + weekday, 0);
        }

        /**
         * 供第一阶段候选评分使用：判断班级是否已在其它天占用课时。
         */
        boolean classHasLoadOnOtherDay(String classNo, int weekday, Set<Integer> weekdays) {
            if (classNo == null || classNo.isBlank() || weekdays == null || weekdays.isEmpty()) {
                return false;
            }
            for (Integer day : weekdays) {
                if (day == null || day == weekday) {
                    continue;
                }
                if (classDayLoad(classNo, day) > 0) {
                    return true;
                }
            }
            return false;
        }
    }
}
