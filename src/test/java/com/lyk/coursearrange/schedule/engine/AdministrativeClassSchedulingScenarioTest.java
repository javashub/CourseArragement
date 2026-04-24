package com.lyk.coursearrange.schedule.engine;

import com.lyk.coursearrange.schedule.engine.model.SchedulingAssignment;
import com.lyk.coursearrange.schedule.engine.model.SchedulingClassroom;
import com.lyk.coursearrange.schedule.engine.model.SchedulingEngineRequest;
import com.lyk.coursearrange.schedule.engine.model.SchedulingExecutionResult;
import com.lyk.coursearrange.schedule.engine.model.SchedulingTask;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdministrativeClassSchedulingScenarioTest {

    @Test
    void execute_shouldScheduleAllTasksForAdministrativeClassTemplate() {
        StandardSchedulingEngine engine = new StandardSchedulingEngine(new GeneticScheduleOptimizer());
        SchedulingEngineRequest request = SchedulingEngineRequest.builder()
                .semester("2026-2027-1")
                .timeSlotCodes(List.of("0101", "0102", "0103", "0201", "0202", "0203"))
                .classrooms(List.of(
                        room(1L, "A101", 50),
                        room(2L, "A102", 50)
                ))
                .tasks(List.of(
                        task(1L, "TK-1", "AC-1", "MATH", "T-M1", 1),
                        task(2L, "TK-2", "AC-1", "ENGLISH", "T-E1", 1),
                        task(3L, "TK-3", "AC-1", "PHYSICS", "T-P1", 1),
                        task(4L, "TK-4", "AC-2", "MATH", "T-M2", 1),
                        task(5L, "TK-5", "AC-2", "ENGLISH", "T-E2", 1),
                        task(6L, "TK-6", "AC-2", "PHYSICS", "T-P2", 1)
                ))
                .build();

        SchedulingExecutionResult result = engine.execute(request);

        assertEquals(6, result.getTaskCount());
        assertEquals(6, result.getScheduledTaskCount());
        assertEquals(0, result.getUnscheduledTaskCount());
        assertTrue(result.getUnscheduledTasks().isEmpty());
        assertEquals(6, result.getAssignments().size());
        assertEquals(Set.of("TK-1", "TK-2", "TK-3", "TK-4", "TK-5", "TK-6"), result.getAssignments().stream()
                .map(SchedulingAssignment::getTaskCode)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        assertDistinctBy(result.getAssignments(), item -> item.getTaskCode() + "::" + item.getTimeSlotCode());
        assertDistinctBy(result.getAssignments(), item -> item.getClassNo() + "::" + item.getTimeSlotCode());
        assertDistinctBy(result.getAssignments(), item -> item.getTeacherNo() + "::" + item.getTimeSlotCode());
        assertDistinctBy(result.getAssignments(), item -> item.getClassroomCode() + "::" + item.getTimeSlotCode());
    }

    @Test
    void execute_shouldReportFixedConflictBeforeTryingToArrange() {
        StandardSchedulingEngine engine = new StandardSchedulingEngine(new GeneticScheduleOptimizer());
        SchedulingEngineRequest request = SchedulingEngineRequest.builder()
                .semester("2026-2027-1")
                .timeSlotCodes(List.of("0101", "0102", "0201", "0202"))
                .classrooms(List.of(room(1L, "A101", 50)))
                .tasks(List.of(
                        task(11L, "TK-11", "AC-1", "MATH", "T-01", 1, true, List.of("0101")),
                        task(12L, "TK-12", "AC-2", "ENGLISH", "T-01", 1, true, List.of("0101"))
                ))
                .build();

        SchedulingExecutionResult result = engine.execute(request);

        assertEquals(2, result.getTaskCount());
        assertEquals(1, result.getScheduledTaskCount());
        assertEquals(1, result.getUnscheduledTaskCount());
        assertTrue(result.getUnscheduledTasks().stream()
                .allMatch(item -> "TEACHER_CONFLICT".equals(item.getReasonCode())));
    }

    @Test
    void execute_shouldAvoidFalseFailureWhenAdministrativeClassNeedsCrossDayDistribution() {
        StandardSchedulingEngine engine = new StandardSchedulingEngine(context -> context);
        SchedulingEngineRequest request = SchedulingEngineRequest.builder()
                .semester("2026-2027-1")
                .timeSlotCodes(List.of("0101", "0102", "0201"))
                .classrooms(List.of(room(31L, "A301", 50)))
                .tasks(List.of(
                        task(21L, "TK-21", "AC-1", "MATH", "T-A1", 1),
                        task(22L, "TK-22", "AC-1", "ENGLISH", "T-B1", 1),
                        SchedulingTask.builder()
                                .taskId(23L)
                                .taskCode("TK-23")
                                .classNo("AC-2")
                                .courseNo("HISTORY")
                                .teacherNo("T-A1")
                                .weekHours(1)
                                .teacherMaxDayHours(3)
                                .priorityLevel(7)
                                .studentCount(45)
                                .classForbiddenTimeSlots(List.of("0102", "0201"))
                                .build()
                ))
                .build();

        SchedulingExecutionResult result = engine.execute(request);

        assertEquals(3, result.getTaskCount());
        assertEquals(3, result.getScheduledTaskCount());
        assertEquals(0, result.getUnscheduledTaskCount());
        Map<String, String> taskSlotMap = result.getAssignments().stream()
                .collect(Collectors.toMap(SchedulingAssignment::getTaskCode, SchedulingAssignment::getTimeSlotCode));
        assertEquals("0101", taskSlotMap.get("TK-23"));
        assertTrue(Set.of("0102", "0201").contains(taskSlotMap.get("TK-21")));
        assertTrue(Set.of("0102", "0201").contains(taskSlotMap.get("TK-22")));
        assertTrue(!taskSlotMap.get("TK-21").equals(taskSlotMap.get("TK-22")));
        Set<Integer> ac1Weekdays = result.getAssignments().stream()
                .filter(item -> "AC-1".equals(item.getClassNo()))
                .map(SchedulingAssignment::getWeekdayNo)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        assertEquals(Set.of(1, 2), ac1Weekdays);
    }

    private SchedulingTask task(Long id,
                                String code,
                                String classNo,
                                String courseNo,
                                String teacherNo,
                                Integer weekHours) {
        return SchedulingTask.builder()
                .taskId(id)
                .taskCode(code)
                .classNo(classNo)
                .courseNo(courseNo)
                .teacherNo(teacherNo)
                .weekHours(weekHours)
                .teacherMaxDayHours(3)
                .priorityLevel(8)
                .studentCount(45)
                .build();
    }

    private SchedulingTask task(Long id,
                                String code,
                                String classNo,
                                String courseNo,
                                String teacherNo,
                                Integer weekHours,
                                boolean fixedTime,
                                List<String> fixedSlots) {
        return SchedulingTask.builder()
                .taskId(id)
                .taskCode(code)
                .classNo(classNo)
                .courseNo(courseNo)
                .teacherNo(teacherNo)
                .weekHours(weekHours)
                .fixedTime(fixedTime)
                .fixedTimeSlots(fixedSlots)
                .teacherMaxDayHours(4)
                .priorityLevel(8)
                .studentCount(45)
                .build();
    }

    private SchedulingClassroom room(Long id, String code, Integer seats) {
        return SchedulingClassroom.builder()
                .classroomId(id)
                .classroomCode(code)
                .seatCount(seats)
                .roomType("NORMAL")
                .build();
    }

    private void assertDistinctBy(List<SchedulingAssignment> assignments,
                                  Function<SchedulingAssignment, String> keyExtractor) {
        Set<String> keys = assignments.stream()
                .map(keyExtractor)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        assertEquals(assignments.size(), keys.size());
    }
}
