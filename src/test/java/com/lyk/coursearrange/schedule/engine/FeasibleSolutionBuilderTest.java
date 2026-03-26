package com.lyk.coursearrange.schedule.engine;

import com.lyk.coursearrange.schedule.engine.model.SchedulingAssignment;
import com.lyk.coursearrange.schedule.engine.model.SchedulingClassroom;
import com.lyk.coursearrange.schedule.engine.model.SchedulingConstraintSummary;
import com.lyk.coursearrange.schedule.engine.model.SchedulingEngineRequest;
import com.lyk.coursearrange.schedule.engine.model.SchedulingExecutionResult;
import com.lyk.coursearrange.schedule.engine.model.SchedulingTask;
import com.lyk.coursearrange.schedule.engine.model.UnscheduledTaskDetail;
import org.junit.jupiter.api.Test;

import java.util.List;

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
                .weekHours(2)
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
    void build_shouldKeepContinuousTaskOnConsecutiveSlots() {
        SchedulingTask task = SchedulingTask.builder()
                .taskId(2L)
                .taskCode("TK-2")
                .classNo("C2")
                .courseNo("K2")
                .teacherNo("T2")
                .weekHours(4)
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
    void build_shouldOnlyUseMatchingSpecialRoom() {
        SchedulingTask task = SchedulingTask.builder()
                .taskId(3L)
                .taskCode("TK-3")
                .classNo("C3")
                .courseNo("K3")
                .teacherNo("T3")
                .weekHours(2)
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
                .weekHours(2)
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

    private SchedulingClassroom normalRoom(String classroomCode) {
        return SchedulingClassroom.builder()
                .classroomId(11L)
                .classroomCode(classroomCode)
                .roomType("NORMAL")
                .seatCount(48)
                .build();
    }
}
