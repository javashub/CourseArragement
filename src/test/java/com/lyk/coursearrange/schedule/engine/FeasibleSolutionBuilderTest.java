package com.lyk.coursearrange.schedule.engine;

import com.lyk.coursearrange.schedule.engine.model.SchedulingAssignment;
import com.lyk.coursearrange.schedule.engine.model.SchedulingClassroom;
import com.lyk.coursearrange.schedule.engine.model.SchedulingConstraintSummary;
import com.lyk.coursearrange.schedule.engine.model.SchedulingEngineRequest;
import com.lyk.coursearrange.schedule.engine.model.SchedulingExecutionResult;
import com.lyk.coursearrange.schedule.engine.model.SchedulingTask;
import com.lyk.coursearrange.schedule.engine.model.UnscheduledTaskDetail;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeasibleSolutionBuilderTest {

    private final FeasibleSolutionBuilder builder = new FeasibleSolutionBuilder(
            new SchedulingConstraintEvaluator(),
            new SchedulingFailureReporter()
    );

    @Test
    void build_shouldAssignFixedTimeTaskToExpectedSlot() {
        SchedulingTask task = SchedulingTask.builder()
                .taskId(1L)
                .taskCode("TK-1")
                .classNo("C1")
                .courseNo("K1")
                .teacherNo("T1")
                .weekHours(1)
                .priorityLevel(9)
                .fixedTime(true)
                .fixedTimeSlots(List.of("03"))
                .build();

        SchedulingEngineRequest request = SchedulingEngineRequest.builder()
                .timeSlotCodes(List.of("01", "02", "03", "04", "05"))
                .tasks(List.of(task))
                .classrooms(List.of(normalRoom("A101")))
                .build();

        SchedulingExecutionResult result = builder.build(request);

        assertEquals(1, result.getAssignments().size());
        assertEquals("03", result.getAssignments().get(0).getTimeSlotCode());
        assertTrue(result.getUnscheduledTasks().isEmpty());
    }

    @Test
    void build_shouldUseWeekHoursAsExactSlotCountForNonContinuousTask() {
        SchedulingTask task = SchedulingTask.builder()
                .taskId(11L)
                .taskCode("TK-11")
                .classNo("C11")
                .courseNo("K11")
                .teacherNo("T11")
                .weekHours(3)
                .build();

        SchedulingEngineRequest request = SchedulingEngineRequest.builder()
                .timeSlotCodes(List.of("0101", "0102", "0103"))
                .tasks(List.of(task))
                .classrooms(List.of(normalRoom("A111")))
                .build();

        SchedulingExecutionResult result = builder.build(request);

        assertEquals(3, result.getAssignments().size());
        assertEquals(List.of("0101", "0102", "0103"), result.getAssignments().stream()
                .map(SchedulingAssignment::getTimeSlotCode)
                .toList());
        assertTrue(result.getUnscheduledTasks().isEmpty());
    }

    @Test
    void build_shouldKeepContinuousTaskOnConsecutiveSlots() {
        SchedulingTask task = SchedulingTask.builder()
                .taskId(2L)
                .taskCode("TK-2")
                .classNo("C2")
                .courseNo("K2")
                .teacherNo("T2")
                .weekHours(2)
                .needContinuous(true)
                .continuousSize(2)
                .build();

        SchedulingEngineRequest request = SchedulingEngineRequest.builder()
                .timeSlotCodes(List.of("01", "02", "03", "04", "05"))
                .tasks(List.of(task))
                .classrooms(List.of(normalRoom("A102")))
                .build();

        SchedulingExecutionResult result = builder.build(request);

        assertEquals(2, result.getAssignments().size());
        assertEquals(List.of("01", "02"), result.getAssignments().stream()
                .map(SchedulingAssignment::getTimeSlotCode)
                .toList());
    }

    @Test
    void build_shouldTreatHigherPeriodSlotsOnSameWeekdayAsContinuous() {
        SchedulingTask task = SchedulingTask.builder()
                .taskId(21L)
                .taskCode("TK-21")
                .classNo("C21")
                .courseNo("K21")
                .teacherNo("T21")
                .weekHours(2)
                .needContinuous(true)
                .continuousSize(2)
                .build();

        SchedulingEngineRequest request = SchedulingEngineRequest.builder()
                .timeSlotCodes(List.of("0105", "0106", "0201"))
                .tasks(List.of(task))
                .classrooms(List.of(normalRoom("A121")))
                .build();

        SchedulingExecutionResult result = builder.build(request);

        assertEquals(2, result.getAssignments().size());
        assertEquals(List.of("0105", "0106"), result.getAssignments().stream()
                .map(SchedulingAssignment::getTimeSlotCode)
                .toList());
        assertTrue(result.getUnscheduledTasks().isEmpty());
    }

    @Test
    void build_shouldOnlyUseMatchingSpecialRoom() {
        SchedulingTask task = SchedulingTask.builder()
                .taskId(3L)
                .taskCode("TK-3")
                .classNo("C3")
                .courseNo("K3")
                .teacherNo("T3")
                .weekHours(1)
                .needSpecialRoom(true)
                .requiredRoomType("LAB")
                .build();

        SchedulingEngineRequest request = SchedulingEngineRequest.builder()
                .timeSlotCodes(List.of("01", "02", "03"))
                .tasks(List.of(task))
                .classrooms(List.of(
                        normalRoom("A103"),
                        SchedulingClassroom.builder().classroomId(22L).classroomCode("LAB201").roomType("LAB").seatCount(48).build()
                ))
                .build();

        SchedulingExecutionResult result = builder.build(request);

        assertEquals(1, result.getAssignments().size());
        assertEquals("LAB201", result.getAssignments().get(0).getClassroomCode());
    }

    @Test
    void build_shouldReportTeacherForbiddenTimeWhenAllSlotsBlocked() {
        SchedulingTask task = SchedulingTask.builder()
                .taskId(4L)
                .taskCode("TK-4")
                .classNo("C4")
                .courseNo("K4")
                .teacherNo("T4")
                .weekHours(1)
                .teacherForbiddenTimeSlots(List.of("01", "02"))
                .build();

        SchedulingEngineRequest request = SchedulingEngineRequest.builder()
                .timeSlotCodes(List.of("01", "02"))
                .tasks(List.of(task))
                .classrooms(List.of(normalRoom("A104")))
                .build();

        SchedulingExecutionResult result = builder.build(request);

        assertTrue(result.getAssignments().isEmpty());
        assertEquals(1, result.getUnscheduledTasks().size());
        UnscheduledTaskDetail detail = result.getUnscheduledTasks().get(0);
        assertEquals("TEACHER_FORBIDDEN_TIME", detail.getReasonCode());
        SchedulingConstraintSummary summary = result.getConstraintSummary().get(0);
        assertEquals("TEACHER_FORBIDDEN_TIME", summary.getReasonCode());
        assertEquals(1, summary.getCount());
    }

    @Test
    void build_shouldReportFixedTimeUnsatisfiedWhenConfiguredSlotsDoNotContainTargetSlot() {
        SchedulingTask task = SchedulingTask.builder()
                .taskId(41L)
                .taskCode("TK-41")
                .classNo("C41")
                .courseNo("K41")
                .teacherNo("T41")
                .weekHours(1)
                .fixedTime(true)
                .fixedTimeSlots(List.of("0507"))
                .build();

        SchedulingEngineRequest request = SchedulingEngineRequest.builder()
                .timeSlotCodes(List.of("0101", "0102", "0505"))
                .tasks(List.of(task))
                .classrooms(List.of(normalRoom("A141")))
                .build();

        SchedulingExecutionResult result = builder.build(request);

        assertTrue(result.getAssignments().isEmpty());
        assertEquals(1, result.getUnscheduledTasks().size());
        assertEquals("FIXED_TIME_UNSATISFIED", result.getUnscheduledTasks().get(0).getReasonCode());
    }

    @Test
    void build_shouldScheduleMultiUnitFixedTimeTaskAcrossConfiguredSlots() {
        SchedulingTask fixedMultiSlotTask = SchedulingTask.builder()
                .taskId(51L)
                .taskCode("TK-51")
                .classNo("AC-1")
                .courseNo("MATH")
                .teacherNo("T-01")
                .weekHours(2)
                .priorityLevel(10)
                .fixedTime(true)
                .fixedTimeSlots(List.of("0101", "0102"))
                .build();

        SchedulingExecutionResult result = builder.build(SchedulingEngineRequest.builder()
                .timeSlotCodes(List.of("0101", "0102", "0103"))
                .tasks(List.of(fixedMultiSlotTask))
                .classrooms(List.of(normalRoom("A151")))
                .build());

        assertEquals(1, result.getScheduledTaskCount());
        assertEquals(0, result.getUnscheduledTaskCount());
        assertEquals(Set.of("0101", "0102"), result.getAssignments().stream()
                .map(SchedulingAssignment::getTimeSlotCode)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    @Test
    void build_shouldScheduleFixedContinuousTaskAcrossMultipleConfiguredBlocks() {
        SchedulingTask fixedContinuousTask = SchedulingTask.builder()
                .taskId(55L)
                .taskCode("TK-55")
                .classNo("AC-55")
                .courseNo("CHEM")
                .teacherNo("T-55")
                .weekHours(4)
                .fixedTime(true)
                .needContinuous(true)
                .continuousSize(2)
                .fixedTimeSlots(List.of("0101", "0102", "0201", "0202"))
                .build();

        SchedulingExecutionResult result = builder.build(SchedulingEngineRequest.builder()
                .timeSlotCodes(List.of("0101", "0102", "0201", "0202", "0301"))
                .tasks(List.of(fixedContinuousTask))
                .classrooms(List.of(normalRoom("A155")))
                .build());

        assertEquals(1, result.getScheduledTaskCount());
        assertEquals(0, result.getUnscheduledTaskCount());
        assertEquals(List.of("0101", "0102", "0201", "0202"), result.getAssignments().stream()
                .map(SchedulingAssignment::getTimeSlotCode)
                .toList());
    }

    @Test
    void build_shouldNotOverscheduleWhenFixedContinuousTaskHasTrailingSingleUnitChunk() {
        SchedulingTask fixedContinuousTask = SchedulingTask.builder()
                .taskId(56L)
                .taskCode("TK-56")
                .classNo("AC-56")
                .courseNo("CHEM-56")
                .teacherNo("T-56")
                .weekHours(3)
                .fixedTime(true)
                .needContinuous(true)
                .continuousSize(2)
                .fixedTimeSlots(List.of("0101", "0102", "0201"))
                .build();

        SchedulingExecutionResult result = builder.build(SchedulingEngineRequest.builder()
                .timeSlotCodes(List.of("0101", "0102", "0201", "0202"))
                .tasks(List.of(fixedContinuousTask))
                .classrooms(List.of(normalRoom("A156")))
                .build());

        assertEquals(1, result.getScheduledTaskCount());
        assertEquals(0, result.getUnscheduledTaskCount());
        assertEquals(List.of("0101", "0102", "0201"), result.getAssignments().stream()
                .map(SchedulingAssignment::getTimeSlotCode)
                .toList());
    }

    @Test
    void build_shouldSliceFixedContinuousBlocksByCumulativeChunkOffset() {
        SchedulingTask fixedContinuousTask = SchedulingTask.builder()
                .taskId(57L)
                .taskCode("TK-57")
                .classNo("AC-57")
                .courseNo("CHEM-57")
                .teacherNo("T-57")
                .weekHours(5)
                .fixedTime(true)
                .needContinuous(true)
                .continuousSize(3)
                .fixedTimeSlots(List.of("0101", "0102", "0103", "0201", "0202"))
                .build();

        SchedulingExecutionResult result = builder.build(SchedulingEngineRequest.builder()
                .timeSlotCodes(List.of("0101", "0102", "0103", "0201", "0202"))
                .tasks(List.of(fixedContinuousTask))
                .classrooms(List.of(normalRoom("A157")))
                .build());

        assertEquals(1, result.getScheduledTaskCount());
        assertEquals(0, result.getUnscheduledTaskCount());
        assertEquals(List.of("0101", "0102", "0103", "0201", "0202"), result.getAssignments().stream()
                .map(SchedulingAssignment::getTimeSlotCode)
                .toList());
    }

    @Test
    void build_shouldRearrangePlacedTasksWhenLaterTeacherConstraintWouldOtherwiseFail() {
        SchedulingTask task1 = SchedulingTask.builder()
                .taskId(52L)
                .taskCode("TK-52")
                .classNo("AC-1")
                .courseNo("MATH")
                .teacherNo("T-01")
                .weekHours(1)
                .priorityLevel(10)
                .build();
        SchedulingTask task2 = SchedulingTask.builder()
                .taskId(53L)
                .taskCode("TK-53")
                .classNo("AC-2")
                .courseNo("ENGLISH")
                .teacherNo("T-02")
                .weekHours(1)
                .priorityLevel(9)
                .build();
        SchedulingTask task3 = SchedulingTask.builder()
                .taskId(54L)
                .taskCode("TK-54")
                .classNo("AC-3")
                .courseNo("PHYSICS")
                .teacherNo("T-01")
                .weekHours(1)
                .priorityLevel(1)
                .classForbiddenTimeSlots(List.of("0102"))
                .build();

        SchedulingExecutionResult result = builder.build(SchedulingEngineRequest.builder()
                .timeSlotCodes(List.of("0101", "0102"))
                .tasks(List.of(task1, task2, task3))
                .classrooms(List.of(
                        normalRoom("A152"),
                        SchedulingClassroom.builder().classroomId(12L).classroomCode("B152").roomType("NORMAL").seatCount(48).build()
                ))
                .build());

        assertEquals(3, result.getScheduledTaskCount());
        assertEquals(0, result.getUnscheduledTaskCount());
        assertEquals("0101", result.getAssignments().stream()
                .filter(item -> "TK-54".equals(item.getTaskCode()))
                .map(SchedulingAssignment::getTimeSlotCode)
                .findFirst()
                .orElseThrow());
        assertEquals("0102", result.getAssignments().stream()
                .filter(item -> "TK-52".equals(item.getTaskCode()))
                .map(SchedulingAssignment::getTimeSlotCode)
                .findFirst()
                .orElseThrow());
        assertDistinctBy(result.getAssignments(), item -> item.getClassNo() + "::" + item.getTimeSlotCode());
        assertDistinctBy(result.getAssignments(), item -> item.getTeacherNo() + "::" + item.getTimeSlotCode());
        assertDistinctBy(result.getAssignments(), item -> item.getClassroomCode() + "::" + item.getTimeSlotCode());
    }

    @Test
    void build_shouldSpreadAdministrativeClassTasksAcrossDaysWhenSelectingCandidates() {
        SchedulingTask task1 = SchedulingTask.builder()
                .taskId(61L)
                .taskCode("TK-61")
                .classNo("AC-10")
                .courseNo("COURSE-61")
                .teacherNo("T-61")
                .weekHours(1)
                .priorityLevel(10)
                .build();
        SchedulingTask task2 = SchedulingTask.builder()
                .taskId(62L)
                .taskCode("TK-62")
                .classNo("AC-10")
                .courseNo("COURSE-62")
                .teacherNo("T-62")
                .weekHours(1)
                .priorityLevel(9)
                .build();
        SchedulingTask task3 = SchedulingTask.builder()
                .taskId(63L)
                .taskCode("TK-63")
                .classNo("AC-10")
                .courseNo("COURSE-63")
                .teacherNo("T-63")
                .weekHours(1)
                .priorityLevel(8)
                .build();

        SchedulingExecutionResult result = builder.build(SchedulingEngineRequest.builder()
                .timeSlotCodes(List.of("0101", "0102", "0201"))
                .tasks(List.of(task1, task2, task3))
                .classrooms(List.of(normalRoom("A161")))
                .build());

        assertEquals(3, result.getScheduledTaskCount());
        assertEquals(0, result.getUnscheduledTaskCount());
        assertEquals(Set.of("TK-61", "TK-62", "TK-63"), result.getAssignments().stream()
                .map(SchedulingAssignment::getTaskCode)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        assertDistinctBy(result.getAssignments(), item -> item.getClassNo() + "::" + item.getTimeSlotCode());
        Set<Integer> weekdays = result.getAssignments().stream()
                .map(SchedulingAssignment::getWeekdayNo)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        assertTrue(weekdays.size() >= 2);
        assertTrue(weekdays.contains(2));
    }

    private SchedulingClassroom normalRoom(String classroomCode) {
        return SchedulingClassroom.builder()
                .classroomId(11L)
                .classroomCode(classroomCode)
                .roomType("NORMAL")
                .seatCount(48)
                .build();
    }

    private void assertDistinctBy(List<SchedulingAssignment> assignments,
                                  java.util.function.Function<SchedulingAssignment, String> keyExtractor) {
        Set<String> keys = assignments.stream()
                .map(keyExtractor)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        assertEquals(assignments.size(), keys.size());
    }
}
