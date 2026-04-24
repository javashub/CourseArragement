package com.lyk.coursearrange.schedule.engine;

import com.lyk.coursearrange.schedule.engine.model.OptimizationContext;
import com.lyk.coursearrange.schedule.engine.model.SchedulingAssignment;
import com.lyk.coursearrange.schedule.engine.model.SchedulingChromosome;
import com.lyk.coursearrange.schedule.engine.model.SchedulingClassroom;
import com.lyk.coursearrange.schedule.engine.model.SchedulingEngineRequest;
import com.lyk.coursearrange.schedule.engine.model.SchedulingGene;
import com.lyk.coursearrange.schedule.engine.model.SchedulingTask;
import com.lyk.coursearrange.schedule.engine.model.UnscheduledTaskDetail;
import com.lyk.coursearrange.schedule.util.ScheduleTaskMetaUtils;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneticScheduleOptimizerTest {

    @Test
    void optimize_shouldBuildObjectGenesWithVariableLengthCodes() {
        SchedulingTask task = SchedulingTask.builder()
                .taskId(1L)
                .taskCode("TASK-001")
                .classNo("CLASS-2026-ALPHA")
                .courseNo("COURSE-CHEMISTRY-CORE-2026")
                .teacherNo("TEACHER-LI-0000001")
                .teacherMaxDayHours(2)
                .weekHours(1)
                .build();

        SchedulingEngineRequest request = SchedulingEngineRequest.builder()
                .semester("2025-2026-2")
                .timeSlotCodes(List.of("01", "02", "03", "06", "07", "08"))
                .tasks(List.of(task))
                .classrooms(List.of(normalRoom("ROOM-A-101")))
                .build();

        OptimizationContext optimized = optimizer(7).optimize(OptimizationContext.builder()
                .request(request)
                .assignments(List.of(assignment(task, "ROOM-A-101", "01")))
                .unscheduledTasks(List.of())
                .build());

        SchedulingChromosome chromosome = optimized.getOptimizedChromosome();
        assertNotNull(chromosome);
        assertEquals(1, chromosome.getGenes().size());
        SchedulingGene gene = chromosome.getGenes().get(0);
        assertEquals("CLASS-2026-ALPHA", gene.getClassNo());
        assertEquals("COURSE-CHEMISTRY-CORE-2026", gene.getCourseNo());
        assertEquals("TEACHER-LI-0000001", gene.getTeacherNo());
        assertEquals(List.of("01"), gene.getTimeSlotCodes());
    }

    @Test
    void optimize_shouldImproveTeacherDistributionForSecondarySchoolScenario() {
        SchedulingTask task1 = task(1L, "TK-1", "C1", "MATH", "T-01");
        SchedulingTask task2 = task(2L, "TK-2", "C2", "ENGLISH", "T-01");

        SchedulingEngineRequest request = SchedulingEngineRequest.builder()
                .semester("2025-2026-2")
                .timeSlotCodes(List.of("01", "02", "03", "06", "07", "08", "11", "12", "13"))
                .tasks(List.of(task1, task2))
                .classrooms(List.of(normalRoom("A101"), normalRoom("A102")))
                .build();

        OptimizationContext optimized = optimizer(3).optimize(OptimizationContext.builder()
                .request(request)
                .assignments(List.of(
                        assignment(task1, "A101", "01"),
                        assignment(task2, "A102", "02")
                ))
                .unscheduledTasks(List.of())
                .build());

        SchedulingChromosome seed = optimized.getSeedChromosome();
        SchedulingChromosome best = optimized.getOptimizedChromosome();

        assertNotNull(seed);
        assertNotNull(best);
        assertTrue(best.getFitnessScore() >= seed.getFitnessScore());
        assertTrue(best.getGenes().stream()
                .map(SchedulingGene::getTimeSlotCodes)
                .flatMap(List::stream)
                .anyMatch(slot -> slot.startsWith("06") || slot.startsWith("07") || slot.startsWith("08")
                        || slot.startsWith("11") || slot.startsWith("12") || slot.startsWith("13")));
    }

    @Test
    void optimize_shouldBypassFullOptimizationForDenseAdministrativeWorkload() {
        List<SchedulingTask> tasks = new java.util.ArrayList<>();
        List<SchedulingAssignment> assignments = new java.util.ArrayList<>();
        for (int i = 1; i <= 41; i++) {
            SchedulingTask task = SchedulingTask.builder()
                    .taskId((long) i)
                    .taskCode("TK-" + i)
                    .classNo("AC-" + ((i % 3) + 1))
                    .courseNo("COURSE-" + i)
                    .teacherNo("T-" + i)
                    .weekHours(1)
                    .teacherMaxDayHours(4)
                    .priorityLevel(5)
                    .build();
            tasks.add(task);
            assignments.add(assignment(task, "A10" + ((i % 3) + 1), String.format("%02d01", (i % 5) + 1)));
        }
        SchedulingEngineRequest request = SchedulingEngineRequest.builder()
                .semester("2026-2027-1")
                .timeSlotCodes(List.of("0101", "0201", "0301", "0401", "0501"))
                .tasks(tasks)
                .classrooms(List.of(normalRoom("A101"), normalRoom("A102"), normalRoom("A103")))
                .build();

        OptimizationContext optimized = optimizer(31).optimize(OptimizationContext.builder()
                .request(request)
                .assignments(assignments)
                .unscheduledTasks(List.of())
                .build());

        assertEquals(assignments.size(), optimized.getAssignments().size());
        assertEquals(0, optimized.getPopulation().getGeneration());
        assertEquals(1, optimized.getPopulation().getChromosomes().size());
        assertEquals(assignments.size(), optimized.getOptimizedChromosome().getGenes().size());
    }

    @Test
    void optimize_shouldInsertUnscheduledTaskByRearrangingConflictingAssignment() {
        SchedulingTask scheduledTask = task(1L, "TK-1", "C1", "MATH", "T-01");
        SchedulingTask unscheduledTask = task(2L, "TK-2", "C2", "ENGLISH", "T-01");

        SchedulingEngineRequest request = SchedulingEngineRequest.builder()
                .semester("2025-2026-2")
                .timeSlotCodes(List.of("01", "02"))
                .tasks(List.of(scheduledTask, unscheduledTask))
                .classrooms(List.of(normalRoom("A101")))
                .build();

        OptimizationContext optimized = optimizer(11).optimize(OptimizationContext.builder()
                .request(request)
                .assignments(List.of(assignment(scheduledTask, "A101", "01")))
                .unscheduledTasks(List.of(UnscheduledTaskDetail.builder()
                        .taskId(unscheduledTask.getTaskId())
                        .taskCode(unscheduledTask.getTaskCode())
                        .classNo(unscheduledTask.getClassNo())
                        .courseNo(unscheduledTask.getCourseNo())
                        .teacherNo(unscheduledTask.getTeacherNo())
                        .reasonCode("TEACHER_CONFLICT")
                        .reasonMessage("教师时间冲突")
                        .build()))
                .build());

        assertEquals(2, optimized.getAssignments().size());
        assertTrue(optimized.getUnscheduledTasks().isEmpty());
        assertEquals(2, optimized.getOptimizedChromosome().getGenes().size());
        assertTrue(optimized.getOptimizedChromosome().getGenes().stream()
                .anyMatch(gene -> "TK-1".equals(gene.getTaskCode()) && List.of("02").equals(gene.getTimeSlotCodes())));
        assertTrue(optimized.getOptimizedChromosome().getGenes().stream()
                .anyMatch(gene -> "TK-2".equals(gene.getTaskCode()) && List.of("01").equals(gene.getTimeSlotCodes())));
        assertNoResourceConflicts(optimized.getAssignments());
    }

    @Test
    void optimize_shouldRecoverUnscheduledTaskWhenSeedAssignmentsEmpty() {
        SchedulingTask pending = task(1L, "TK-1", "C1", "MATH", "T-01");
        SchedulingEngineRequest request = SchedulingEngineRequest.builder()
                .semester("2026-2027-1")
                .timeSlotCodes(List.of("01"))
                .tasks(List.of(pending))
                .classrooms(List.of(normalRoom("A101")))
                .build();

        OptimizationContext optimized = optimizer(23).optimize(OptimizationContext.builder()
                .request(request)
                .assignments(List.of())
                .unscheduledTasks(List.of(UnscheduledTaskDetail.builder()
                        .taskId(pending.getTaskId())
                        .taskCode(pending.getTaskCode())
                        .classNo(pending.getClassNo())
                        .courseNo(pending.getCourseNo())
                        .teacherNo(pending.getTeacherNo())
                        .reasonCode("NO_AVAILABLE_TIME_SLOT")
                        .reasonMessage("无可用时间片")
                        .build()))
                .build());

        assertTrue(optimized.getUnscheduledTasks().isEmpty());
        assertEquals(1, optimized.getAssignments().size());
        assertTrue(optimized.getAssignments().stream()
                .anyMatch(item -> Long.valueOf(1L).equals(item.getTaskId()) && "01".equals(item.getTimeSlotCode())));
        assertNoResourceConflicts(optimized.getAssignments());
    }

    @Test
    void optimize_shouldRecoverThreeWeeklyPeriodsAsThreeAssignments() {
        SchedulingTask pending = SchedulingTask.builder()
                .taskId(21L)
                .taskCode("TK-21")
                .classNo("C21")
                .courseNo("MATH-21")
                .teacherNo("T-21")
                .weekHours(3)
                .build();
        SchedulingEngineRequest request = SchedulingEngineRequest.builder()
                .semester("2026-2027-1")
                .timeSlotCodes(List.of("0101", "0102", "0103"))
                .tasks(List.of(pending))
                .classrooms(List.of(normalRoom("A121")))
                .build();

        OptimizationContext optimized = optimizer(29).optimize(OptimizationContext.builder()
                .request(request)
                .assignments(List.of())
                .unscheduledTasks(List.of(UnscheduledTaskDetail.builder()
                        .taskId(pending.getTaskId())
                        .taskCode(pending.getTaskCode())
                        .classNo(pending.getClassNo())
                        .courseNo(pending.getCourseNo())
                        .teacherNo(pending.getTeacherNo())
                        .reasonCode("NO_AVAILABLE_TIME_SLOT")
                        .reasonMessage("无可用时间片")
                        .build()))
                .build());

        assertTrue(optimized.getUnscheduledTasks().isEmpty());
        assertEquals(3, optimized.getAssignments().size());
        assertEquals(List.of("0101", "0102", "0103"), optimized.getAssignments().stream()
                .map(SchedulingAssignment::getTimeSlotCode)
                .sorted()
                .toList());
        assertNoResourceConflicts(optimized.getAssignments());
    }

    @Test
    void optimize_shouldRecoverAdministrativeClassTaskBySmallRearrangementWithoutDroppingExistingAssignments() {
        SchedulingTask blocker1 = SchedulingTask.builder()
                .taskId(1L)
                .taskCode("TK-1")
                .classNo("AC-2")
                .courseNo("MATH")
                .teacherNo("T-ADMIN")
                .weekHours(1)
                .teacherMaxDayHours(4)
                .priorityLevel(8)
                .build();
        SchedulingTask blocker2 = SchedulingTask.builder()
                .taskId(2L)
                .taskCode("TK-2")
                .classNo("AC-3")
                .courseNo("ENGLISH")
                .teacherNo("T-ADMIN")
                .weekHours(1)
                .teacherMaxDayHours(4)
                .priorityLevel(8)
                .build();
        SchedulingTask blocker3 = task(3L, "TK-3", "AC-1", "PHYSICS", "T-03");
        SchedulingTask blocker4 = task(4L, "TK-4", "AC-1", "CHEMISTRY", "T-04");
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
                .priorityLevel(9)
                .build();

        SchedulingEngineRequest request = SchedulingEngineRequest.builder()
                .semester("2026-2027-1")
                .timeSlotCodes(List.of("0101", "0102", "0103", "0104"))
                .tasks(List.of(blocker1, blocker2, blocker3, blocker4, pending))
                .classrooms(List.of(normalRoom("A101"), normalRoom("A102")))
                .build();

        OptimizationContext optimized = optimizer(17).optimize(OptimizationContext.builder()
                .request(request)
                .assignments(List.of(
                        assignment(blocker1, "A101", "0101"),
                        assignment(blocker2, "A102", "0102"),
                        assignment(blocker3, "A102", "0101"),
                        assignment(blocker4, "A101", "0102")
                ))
                .unscheduledTasks(List.of(UnscheduledTaskDetail.builder()
                        .taskId(pending.getTaskId())
                        .taskCode(pending.getTaskCode())
                        .classNo(pending.getClassNo())
                        .courseNo(pending.getCourseNo())
                        .teacherNo(pending.getTeacherNo())
                        .reasonCode("TEACHER_CONFLICT")
                        .reasonMessage("教师冲突")
                        .build()))
                .build());

        assertTrue(optimized.getUnscheduledTasks().isEmpty());
        assertEquals(6, optimized.getAssignments().size());

        Map<Long, Long> assignmentCountByTask = optimized.getAssignments().stream()
                .collect(Collectors.groupingBy(SchedulingAssignment::getTaskId, Collectors.counting()));
        assertEquals(1L, assignmentCountByTask.get(1L));
        assertEquals(1L, assignmentCountByTask.get(2L));
        assertEquals(1L, assignmentCountByTask.get(3L));
        assertEquals(1L, assignmentCountByTask.get(4L));
        assertEquals(2L, assignmentCountByTask.get(5L));

        Set<String> pendingSlots = optimized.getAssignments().stream()
                .filter(item -> Long.valueOf(5L).equals(item.getTaskId()))
                .map(SchedulingAssignment::getTimeSlotCode)
                .collect(Collectors.toSet());
        assertEquals(Set.of("0101", "0102"), pendingSlots);
        assertNoResourceConflicts(optimized.getAssignments());
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

    private GeneticScheduleOptimizer optimizer(long seed) {
        return new GeneticScheduleOptimizer(new SchedulingConstraintEvaluator(), new Random(seed));
    }

    private SchedulingTask task(Long taskId, String taskCode, String classNo, String courseNo, String teacherNo) {
        return SchedulingTask.builder()
                .taskId(taskId)
                .taskCode(taskCode)
                .classNo(classNo)
                .courseNo(courseNo)
                .teacherNo(teacherNo)
                .weekHours(1)
                .teacherMaxDayHours(3)
                .priorityLevel(8)
                .build();
    }

    private SchedulingAssignment assignment(SchedulingTask task, String classroomCode, String slotCode) {
        return SchedulingAssignment.builder()
                .taskId(task.getTaskId())
                .taskCode(task.getTaskCode())
                .classNo(task.getClassNo())
                .courseNo(task.getCourseNo())
                .teacherNo(task.getTeacherNo())
                .classroomId(1L)
                .classroomCode(classroomCode)
                .weekdayNo(ScheduleTaskMetaUtils.resolveWeekdayNo(slotCode))
                .periodNo(ScheduleTaskMetaUtils.resolvePeriodNo(slotCode))
                .timeSlotCode(slotCode)
                .build();
    }

    private SchedulingClassroom normalRoom(String classroomCode) {
        return SchedulingClassroom.builder()
                .classroomId((long) classroomCode.hashCode())
                .classroomCode(classroomCode)
                .roomType("NORMAL")
                .seatCount(50)
                .build();
    }
}
