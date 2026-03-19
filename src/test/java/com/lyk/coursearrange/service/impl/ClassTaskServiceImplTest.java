package com.lyk.coursearrange.service.impl;

import com.lyk.coursearrange.common.ServerResponse;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.entity.CoursePlan;
import com.lyk.coursearrange.schedule.entity.SchTask;
import com.lyk.coursearrange.schedule.vo.SchedulingTaskInput;
import com.lyk.coursearrange.schedule.service.SchTaskService;
import com.lyk.coursearrange.system.config.entity.CfgScheduleRule;
import com.lyk.coursearrange.system.config.entity.CfgTimeSlot;
import com.lyk.coursearrange.system.config.service.ScheduleConfigFacadeService;
import com.lyk.coursearrange.system.config.vo.ScheduleConfigVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClassTaskServiceImplTest {

    @Mock
    private SchTaskService schTaskService;
    @Mock
    private ScheduleConfigFacadeService scheduleConfigFacadeService;

    @Test
    void buildSchedulingSuccessResponse_shouldExposeStandardOnlyStatus() {
        ClassTaskServiceImpl service = new ClassTaskServiceImpl();

        ServerResponse<Map<String, Object>> response = service.buildSchedulingSuccessResponse(123L, 8);

        assertTrue(response.isSuccess());
        assertEquals("排课成功，标准课表已生成，耗时：123ms", response.getMessage());
        assertEquals(8, response.getData().get("generatedPlanCount"));
        assertEquals(123L, response.getData().get("durationMs"));
    }

    @Test
    void listSchedulingTasks_shouldReturnEmptyWhenStandardTasksMissing() {
        ClassTaskServiceImpl service = spy(new ClassTaskServiceImpl());
        ReflectionTestUtils.setField(service, "schTaskService", schTaskService);
        when(schTaskService.list(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SchTask>>any()))
                .thenReturn(List.of());

        List<SchedulingTaskInput> tasks = service.listSchedulingTasks("2025-2026-1");

        assertTrue(tasks.isEmpty());
    }

    @Test
    void listSchedulingTasks_shouldMapStandardTaskToAlgorithmInput() {
        ClassTaskServiceImpl service = new ClassTaskServiceImpl();
        ReflectionTestUtils.setField(service, "schTaskService", schTaskService);

        SchTask standardTask = new SchTask();
        standardTask.setStudentCount(45);
        standardTask.setWeekHours(4);
        standardTask.setTotalHours(64);
        standardTask.setNeedFixedTime(1);
        standardTask.setFixedWeekdayNo(2);
        standardTask.setFixedPeriodNo(3);
        standardTask.setRemark("semester=2025-2026-1,classNo=2501,courseNo=10001,teacherNo=T2026001,gradeNo=2025,courseName=高等数学,courseAttr=必修,teacherName=张老师");

        when(schTaskService.list(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SchTask>>any()))
                .thenReturn(List.of(standardTask));

        List<SchedulingTaskInput> tasks = service.listSchedulingTasks("2025-2026-1");

        assertEquals(1, tasks.size());
        SchedulingTaskInput task = tasks.get(0);
        assertEquals("2025-2026-1", task.getSemester());
        assertEquals("2501", task.getClassNo());
        assertEquals("10001", task.getCourseNo());
        assertEquals("T2026001", task.getTeacherNo());
        assertEquals("高等数学", task.getCourseName());
        assertEquals("必修", task.getCourseAttr());
        assertEquals(45, task.getStudentNum());
        assertEquals(4, task.getWeeksNumber());
        assertEquals(16, task.getWeeksSum());
        assertEquals("1", task.getIsFix());
        assertEquals("08", task.getClassTime());
    }

    @Test
    void countScheduleTasks_shouldReturnStandardCountOnly() {
        ClassTaskServiceImpl service = new ClassTaskServiceImpl();
        ReflectionTestUtils.setField(service, "schTaskService", schTaskService);
        when(schTaskService.count(any())).thenReturn(0L);

        long count = service.countScheduleTasks();

        assertEquals(0L, count);
    }

    @Test
    void buildSchedulingSummary_shouldAggregateScheduledAndUnscheduledTasks() {
        ClassTaskServiceImpl service = new ClassTaskServiceImpl();

        SchedulingTaskInput scheduledTask = new SchedulingTaskInput();
        scheduledTask.setSemester("2025-2026-1");
        scheduledTask.setClassNo("2501");
        scheduledTask.setCourseNo("10001");
        scheduledTask.setCourseName("高等数学");
        scheduledTask.setTeacherNo("T2026001");
        scheduledTask.setRealname("张老师");

        SchedulingTaskInput unscheduledTask = new SchedulingTaskInput();
        unscheduledTask.setSemester("2025-2026-1");
        unscheduledTask.setClassNo("2502");
        unscheduledTask.setCourseNo("10002");
        unscheduledTask.setCourseName("大学英语");
        unscheduledTask.setTeacherNo("T2026002");
        unscheduledTask.setRealname("李老师");

        CoursePlan plan = new CoursePlan();
        plan.setClassNo("2501");
        plan.setCourseNo("10001");
        plan.setTeacherNo("T2026001");

        Map<String, Object> summary = service.buildSchedulingSummary(List.of(scheduledTask, unscheduledTask), List.of(plan));

        assertEquals(2, summary.get("taskCount"));
        assertEquals(1, summary.get("scheduledTaskCount"));
        assertEquals(1, summary.get("unscheduledTaskCount"));
        assertEquals(1, summary.get("conflictTaskCount"));
        assertEquals(50.0d, summary.get("successRate"));
        assertInstanceOf(List.class, summary.get("unscheduledTasks"));
        List<?> unscheduledTasks = (List<?>) summary.get("unscheduledTasks");
        assertEquals(1, unscheduledTasks.size());
        String firstReason = String.valueOf(unscheduledTasks.get(0));
        assertTrue(firstReason.contains("2502"));
        assertTrue(firstReason.contains("大学英语"));
    }

    @Test
    void resolveSchedulingRuntimeContext_shouldUseTeachingTimeSlotsFromActiveRule() {
        ClassTaskServiceImpl service = new ClassTaskServiceImpl();
        ReflectionTestUtils.setField(service, "scheduleConfigFacadeService", scheduleConfigFacadeService);

        CfgScheduleRule rule = new CfgScheduleRule();
        rule.setRuleName("全局默认规则");

        CfgTimeSlot slot1 = buildTimeSlot(1, 1, 1, 0);
        CfgTimeSlot slot2 = buildTimeSlot(1, 2, 0, 0);
        CfgTimeSlot slot3 = buildTimeSlot(2, 1, 1, 1);
        CfgTimeSlot slot4 = buildTimeSlot(2, 2, 1, 0);
        CfgTimeSlot slot5 = buildTimeSlot(6, 1, 1, 0);

        when(scheduleConfigFacadeService.getScheduleConfig(any())).thenReturn(ScheduleConfigVO.builder()
                .scheduleRule(rule)
                .timeSlots(List.of(slot1, slot2, slot3, slot4, slot5))
                .featureToggles(List.of())
                .build());

        ClassTaskServiceImpl.SchedulingRuntimeContext context = service.resolveSchedulingRuntimeContext();

        assertEquals("全局默认规则", context.scheduleRuleName());
        assertTrue(context.configApplied());
        assertEquals(List.of("01", "07"), context.availableClassTimes());
    }

    @Test
    void coding_shouldRejectFixedClassTimeOutsideConfiguredSlots() {
        ClassTaskServiceImpl service = new ClassTaskServiceImpl();
        SchedulingTaskInput fixedTask = new SchedulingTaskInput();
        fixedTask.setIsFix("1");
        fixedTask.setGradeNo("25");
        fixedTask.setClassNo("25010001");
        fixedTask.setTeacherNo("T0001");
        fixedTask.setCourseNo("100001");
        fixedTask.setCourseAttr("01");
        fixedTask.setWeeksNumber(2);
        fixedTask.setClassTime("06");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.coding(List.of(fixedTask), List.of("01", "02", "03")));

        assertTrue(exception.getMessage().contains("固定时间编码"));
    }

    @Test
    void coding_shouldNormalizeStandardTaskFixFlags() {
        ClassTaskServiceImpl service = new ClassTaskServiceImpl();

        SchedulingTaskInput unfixedTask = new SchedulingTaskInput();
        unfixedTask.setIsFix("0");
        unfixedTask.setGradeNo("25");
        unfixedTask.setClassNo("25010001");
        unfixedTask.setTeacherNo("T0001");
        unfixedTask.setCourseNo("100001");
        unfixedTask.setCourseAttr("01");
        unfixedTask.setWeeksNumber(2);

        SchedulingTaskInput fixedTask = new SchedulingTaskInput();
        fixedTask.setIsFix("1");
        fixedTask.setGradeNo("25");
        fixedTask.setClassNo("25010002");
        fixedTask.setTeacherNo("T0002");
        fixedTask.setCourseNo("100002");
        fixedTask.setCourseAttr("01");
        fixedTask.setWeeksNumber(2);
        fixedTask.setClassTime("01");

        Map<String, List<String>> geneMap = service.coding(List.of(unfixedTask, fixedTask), List.of("01", "02", "03"));

        assertEquals(1, geneMap.get("unFixedTime").size());
        assertEquals(1, geneMap.get("isFixedTime").size());
        assertTrue(geneMap.get("unFixedTime").get(0).startsWith("1"));
        assertTrue(geneMap.get("isFixedTime").get(0).startsWith("2"));
    }

    private CfgTimeSlot buildTimeSlot(int weekdayNo, int periodNo, int isTeaching, int isFixedBreak) {
        CfgTimeSlot timeSlot = new CfgTimeSlot();
        timeSlot.setWeekdayNo(weekdayNo);
        timeSlot.setPeriodNo(periodNo);
        timeSlot.setIsTeaching(isTeaching);
        timeSlot.setIsFixedBreak(isFixedBreak);
        return timeSlot;
    }
}
