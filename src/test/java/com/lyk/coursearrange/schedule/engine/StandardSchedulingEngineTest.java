package com.lyk.coursearrange.schedule.engine;

import com.lyk.coursearrange.schedule.engine.model.SchedulingAssignment;
import com.lyk.coursearrange.schedule.engine.model.SchedulingConstraintSummary;
import com.lyk.coursearrange.schedule.engine.model.SchedulingClassroom;
import com.lyk.coursearrange.schedule.engine.model.SchedulingEngineRequest;
import com.lyk.coursearrange.schedule.engine.model.SchedulingExecutionResult;
import com.lyk.coursearrange.schedule.engine.model.SchedulingTask;
import com.lyk.coursearrange.schedule.engine.model.UnscheduledTaskDetail;
import com.lyk.coursearrange.schedule.util.ScheduleTaskMetaUtils;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StandardSchedulingEngineTest {

    @Test
    void springContext_shouldInstantiateStandardSchedulingEngineWithOptimizerBean() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(GeneticScheduleOptimizer.class, StandardSchedulingEngine.class);

        try {
            context.refresh();
            assertNotNull(context.getBean(StandardSchedulingEngine.class));
        } finally {
            context.close();
        }
    }

    @Test
    void execute_shouldKeepFailureReasonsWhileUsingRealOptimizer() {
        StandardSchedulingEngine engine = new StandardSchedulingEngine(
                new GeneticScheduleOptimizer(new SchedulingConstraintEvaluator(), new Random(5))
        );

        SchedulingTask fixedSuccess = SchedulingTask.builder()
                .taskId(1L)
                .taskCode("TK-1")
                .classNo("C1")
                .courseNo("MATH")
                .teacherNo("T1")
                .weekHours(1)
                .fixedTime(true)
                .fixedTimeSlots(List.of("01"))
                .teacherMaxDayHours(2)
                .build();

        SchedulingTask blocked = SchedulingTask.builder()
                .taskId(2L)
                .taskCode("TK-2")
                .classNo("C2")
                .courseNo("ENGLISH")
                .teacherNo("T2")
                .weekHours(1)
                .teacherForbiddenTimeSlots(List.of("01", "02"))
                .build();

        SchedulingEngineRequest request = SchedulingEngineRequest.builder()
                .semester("2025-2026-2")
                .timeSlotCodes(List.of("01", "02"))
                .tasks(List.of(fixedSuccess, blocked))
                .classrooms(List.of(normalRoom("A101")))
                .build();

        SchedulingExecutionResult result = engine.execute(request);

        assertEquals(2, result.getTaskCount());
        assertEquals(1, result.getScheduledTaskCount());
        assertEquals(1, result.getUnscheduledTaskCount());
        assertEquals(1, result.getAssignments().size());
        SchedulingAssignment assignment = result.getAssignments().get(0);
        assertEquals("TK-1", assignment.getTaskCode());
        assertFalse(result.getUnscheduledTasks().isEmpty());
        assertEquals("TEACHER_FORBIDDEN_TIME", result.getUnscheduledTasks().get(0).getReasonCode());
        assertEquals(1, result.getConstraintSummary().size());
        assertTrue(result.getConstraintSummary().stream()
                .anyMatch(item -> "TEACHER_FORBIDDEN_TIME".equals(item.getReasonCode())));
    }

    @Test
    void execute_shouldRecalculateSummaryAfterOptimizerSchedulesPreviouslyUnscheduledTasks() {
        SchedulingTask task1 = SchedulingTask.builder()
                .taskId(1L)
                .taskCode("TK-1")
                .classNo("C1")
                .courseNo("MATH")
                .teacherNo("T1")
                .build();
        SchedulingTask task2 = SchedulingTask.builder()
                .taskId(2L)
                .taskCode("TK-2")
                .classNo("C2")
                .courseNo("ENGLISH")
                .teacherNo("T2")
                .build();
        SchedulingEngineRequest request = SchedulingEngineRequest.builder()
                .semester("2025-2026-2")
                .tasks(List.of(task1, task2))
                .classrooms(List.of(normalRoom("A101")))
                .timeSlotCodes(List.of("01", "02"))
                .build();

        SchedulingExecutionResult feasibleResult = SchedulingExecutionResult.builder()
                .taskCount(2)
                .scheduledTaskCount(1)
                .unscheduledTaskCount(1)
                .successRate(50D)
                .assignments(List.of(assignment(task1, "A101", "01")))
                .unscheduledTasks(List.of(UnscheduledTaskDetail.builder()
                        .taskId(2L)
                        .taskCode("TK-2")
                        .classNo("C2")
                        .courseNo("ENGLISH")
                        .teacherNo("T2")
                        .reasonCode("TEACHER_CONFLICT")
                        .reasonMessage("教师冲突")
                        .build()))
                .constraintSummary(List.of(new SchedulingConstraintSummary("TEACHER_CONFLICT", "教师冲突", 1)))
                .build();

        StandardSchedulingEngine engine = new StandardSchedulingEngine(
                new FixedFeasibleSolutionBuilder(feasibleResult),
                context -> context.toBuilder()
                        .assignments(List.of(
                                assignment(task1, "A101", "01"),
                                assignment(task2, "A101", "02")
                        ))
                        .unscheduledTasks(List.of())
                        .build(),
                new SchedulingFailureReporter()
        );

        SchedulingExecutionResult result = engine.execute(request);

        assertEquals(2, result.getTaskCount());
        assertEquals(2, result.getScheduledTaskCount());
        assertEquals(0, result.getUnscheduledTaskCount());
        assertEquals(100D, result.getSuccessRate());
        assertEquals(2, result.getAssignments().size());
        assertTrue(result.getUnscheduledTasks().isEmpty());
        assertTrue(result.getConstraintSummary().isEmpty());
    }

    @Test
    void execute_shouldClearSummaryWhenRealOptimizerRecoversAdministrativeClassTask() {
        SchedulingTask blocker1 = SchedulingTask.builder()
                .taskId(1L)
                .taskCode("TK-1")
                .classNo("AC-2")
                .courseNo("MATH")
                .teacherNo("T-ADMIN")
                .weekHours(1)
                .teacherMaxDayHours(4)
                .build();
        SchedulingTask blocker2 = SchedulingTask.builder()
                .taskId(2L)
                .taskCode("TK-2")
                .classNo("AC-3")
                .courseNo("ENGLISH")
                .teacherNo("T-ADMIN")
                .weekHours(1)
                .teacherMaxDayHours(4)
                .build();
        SchedulingTask blocker3 = SchedulingTask.builder()
                .taskId(3L)
                .taskCode("TK-3")
                .classNo("AC-1")
                .courseNo("PHYSICS")
                .teacherNo("T-03")
                .weekHours(1)
                .teacherMaxDayHours(4)
                .build();
        SchedulingTask blocker4 = SchedulingTask.builder()
                .taskId(4L)
                .taskCode("TK-4")
                .classNo("AC-1")
                .courseNo("CHEMISTRY")
                .teacherNo("T-04")
                .weekHours(1)
                .teacherMaxDayHours(4)
                .build();
        SchedulingTask pending = SchedulingTask.builder()
                .taskId(5L)
                .taskCode("TK-5")
                .classNo("AC-1")
                .courseNo("BIOLOGY")
                .teacherNo("T-ADMIN")
                .weekHours(2)
                .needContinuous(true)
                .continuousSize(2)
                .teacherMaxDayHours(4)
                .teacherForbiddenTimeSlots(List.of("0103", "0104"))
                .build();

        SchedulingEngineRequest request = SchedulingEngineRequest.builder()
                .semester("2026-2027-1")
                .tasks(List.of(blocker1, blocker2, blocker3, blocker4, pending))
                .classrooms(List.of(normalRoom("A101"), normalRoom("A102")))
                .timeSlotCodes(List.of("0101", "0102", "0103", "0104"))
                .build();

        SchedulingExecutionResult feasibleResult = SchedulingExecutionResult.builder()
                .taskCount(5)
                .scheduledTaskCount(4)
                .unscheduledTaskCount(1)
                .successRate(80D)
                .assignments(List.of(
                        assignment(blocker1, "A101", "0101"),
                        assignment(blocker2, "A102", "0102"),
                        assignment(blocker3, "A102", "0101"),
                        assignment(blocker4, "A101", "0102")
                ))
                .unscheduledTasks(List.of(UnscheduledTaskDetail.builder()
                        .taskId(5L)
                        .taskCode("TK-5")
                        .classNo("AC-1")
                        .courseNo("BIOLOGY")
                        .teacherNo("T-ADMIN")
                        .reasonCode("TEACHER_CONFLICT")
                        .reasonMessage("教师冲突")
                        .build()))
                .constraintSummary(List.of(new SchedulingConstraintSummary("TEACHER_CONFLICT", "教师冲突", 1)))
                .build();

        StandardSchedulingEngine engine = new StandardSchedulingEngine(
                new FixedFeasibleSolutionBuilder(feasibleResult),
                new GeneticScheduleOptimizer(new SchedulingConstraintEvaluator(), new Random(17)),
                new SchedulingFailureReporter()
        );

        SchedulingExecutionResult result = engine.execute(request);

        assertEquals(5, result.getTaskCount());
        assertEquals(5, result.getScheduledTaskCount());
        assertEquals(0, result.getUnscheduledTaskCount());
        assertEquals(100D, result.getSuccessRate());
        assertTrue(result.getUnscheduledTasks().isEmpty());
        assertTrue(result.getConstraintSummary().isEmpty());
        assertEquals(6, result.getAssignments().size());

        Map<Long, Long> assignmentCountByTask = result.getAssignments().stream()
                .collect(Collectors.groupingBy(SchedulingAssignment::getTaskId, Collectors.counting()));
        assertEquals(1L, assignmentCountByTask.get(1L));
        assertEquals(1L, assignmentCountByTask.get(2L));
        assertEquals(1L, assignmentCountByTask.get(3L));
        assertEquals(1L, assignmentCountByTask.get(4L));
        assertEquals(2L, assignmentCountByTask.get(5L));
        assertNoResourceConflicts(result.getAssignments());
    }

    @Test
    void execute_shouldRecalculateReasonAndSummaryByFinalStateAfterPartialRecovery() {
        SchedulingTask blocker = SchedulingTask.builder()
                .taskId(1L)
                .taskCode("TK-1")
                .classNo("C1")
                .courseNo("MATH")
                .teacherNo("T1")
                .weekHours(1)
                .teacherMaxDayHours(2)
                .build();
        SchedulingTask recoverable = SchedulingTask.builder()
                .taskId(2L)
                .taskCode("TK-2")
                .classNo("C2")
                .courseNo("ENGLISH")
                .teacherNo("T1")
                .weekHours(1)
                .teacherMaxDayHours(2)
                .build();
        SchedulingTask stillBlocked = SchedulingTask.builder()
                .taskId(3L)
                .taskCode("TK-3")
                .classNo("C3")
                .courseNo("BIOLOGY")
                .teacherNo("T3")
                .weekHours(1)
                .teacherForbiddenTimeSlots(List.of("01", "02"))
                .build();

        SchedulingEngineRequest request = SchedulingEngineRequest.builder()
                .semester("2026-2027-1")
                .tasks(List.of(blocker, recoverable, stillBlocked))
                .classrooms(List.of(normalRoom("A101")))
                .timeSlotCodes(List.of("01", "02"))
                .build();

        SchedulingExecutionResult feasibleResult = SchedulingExecutionResult.builder()
                .taskCount(3)
                .scheduledTaskCount(1)
                .unscheduledTaskCount(2)
                .successRate(33.33D)
                .assignments(List.of(assignment(blocker, "A101", "01")))
                .unscheduledTasks(List.of(
                        UnscheduledTaskDetail.builder()
                                .taskId(2L)
                                .taskCode("TK-2")
                                .classNo("C2")
                                .courseNo("ENGLISH")
                                .teacherNo("T1")
                                .reasonCode("TEACHER_CONFLICT")
                                .reasonMessage("教师冲突")
                                .build(),
                        UnscheduledTaskDetail.builder()
                                .taskId(3L)
                                .taskCode("TK-3")
                                .classNo("C3")
                                .courseNo("BIOLOGY")
                                .teacherNo("T3")
                                .reasonCode("TEACHER_CONFLICT")
                                .reasonMessage("教师冲突")
                                .build()
                ))
                .constraintSummary(List.of(new SchedulingConstraintSummary("TEACHER_CONFLICT", "教师冲突", 2)))
                .build();

        StandardSchedulingEngine engine = new StandardSchedulingEngine(
                new FixedFeasibleSolutionBuilder(feasibleResult),
                new GeneticScheduleOptimizer(new SchedulingConstraintEvaluator(), new Random(11)),
                new SchedulingFailureReporter()
        );

        SchedulingExecutionResult result = engine.execute(request);

        assertEquals(3, result.getTaskCount());
        assertEquals(2, result.getScheduledTaskCount());
        assertEquals(1, result.getUnscheduledTaskCount());
        assertEquals(2, result.getAssignments().size());
        assertEquals(1, result.getUnscheduledTasks().size());
        assertEquals("TEACHER_FORBIDDEN_TIME", result.getUnscheduledTasks().get(0).getReasonCode());
        assertEquals(1, result.getConstraintSummary().size());
        assertEquals("TEACHER_FORBIDDEN_TIME", result.getConstraintSummary().get(0).getReasonCode());
        assertEquals(1, result.getConstraintSummary().get(0).getCount());
        assertNoResourceConflicts(result.getAssignments());
    }

    private SchedulingClassroom normalRoom(String classroomCode) {
        return SchedulingClassroom.builder()
                // 测试里可能会同时出现多个教室，ID 需要可区分，避免映射时互相覆盖。
                .classroomId((long) classroomCode.hashCode())
                .classroomCode(classroomCode)
                .roomType("NORMAL")
                .seatCount(50)
                .build();
    }

    private SchedulingAssignment assignment(SchedulingTask task, String classroomCode, String slotCode) {
        return SchedulingAssignment.builder()
                .taskId(task.getTaskId())
                .taskCode(task.getTaskCode())
                .classNo(task.getClassNo())
                .courseNo(task.getCourseNo())
                .teacherNo(task.getTeacherNo())
                .classroomId((long) classroomCode.hashCode())
                .classroomCode(classroomCode)
                .weekdayNo(ScheduleTaskMetaUtils.resolveWeekdayNo(slotCode))
                .periodNo(ScheduleTaskMetaUtils.resolvePeriodNo(slotCode))
                .timeSlotCode(slotCode)
                .build();
    }

    private void assertNoResourceConflicts(List<SchedulingAssignment> assignments) {
        assertTrue(assignments.stream()
                .collect(Collectors.groupingBy(item -> item.getTeacherNo() + "::" + item.getTimeSlotCode(), Collectors.counting()))
                .values()
                .stream()
                .allMatch(count -> count <= 1), "存在教师同时间冲突");
        assertTrue(assignments.stream()
                .collect(Collectors.groupingBy(item -> item.getClassNo() + "::" + item.getTimeSlotCode(), Collectors.counting()))
                .values()
                .stream()
                .allMatch(count -> count <= 1), "存在班级同时间冲突");
        assertTrue(assignments.stream()
                .collect(Collectors.groupingBy(item -> item.getClassroomCode() + "::" + item.getTimeSlotCode(), Collectors.counting()))
                .values()
                .stream()
                .allMatch(count -> count <= 1), "存在教室同时间冲突");
    }

    private static class FixedFeasibleSolutionBuilder extends FeasibleSolutionBuilder {

        private final SchedulingExecutionResult fixedResult;

        FixedFeasibleSolutionBuilder(SchedulingExecutionResult fixedResult) {
            super(new SchedulingConstraintEvaluator(), new SchedulingFailureReporter());
            this.fixedResult = fixedResult;
        }

        @Override
        public SchedulingExecutionResult build(SchedulingEngineRequest request) {
            return fixedResult;
        }
    }
}
