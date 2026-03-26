package com.lyk.coursearrange.service.impl;

import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.common.ConstantInfo;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.entity.CoursePlan;
import com.lyk.coursearrange.resource.entity.ResCourse;
import com.lyk.coursearrange.resource.entity.ResClassroom;
import com.lyk.coursearrange.resource.entity.ResTeacher;
import com.lyk.coursearrange.resource.service.ResCourseService;
import com.lyk.coursearrange.resource.service.ResClassroomService;
import com.lyk.coursearrange.resource.service.ResTeacherService;
import com.lyk.coursearrange.schedule.engine.SchedulingEngine;
import com.lyk.coursearrange.schedule.engine.model.SchedulingAssignment;
import com.lyk.coursearrange.schedule.engine.model.SchedulingConstraintSummary;
import com.lyk.coursearrange.schedule.engine.model.SchedulingExecutionResult;
import com.lyk.coursearrange.schedule.engine.model.UnscheduledTaskDetail;
import com.lyk.coursearrange.schedule.service.AdminClassService;
import com.lyk.coursearrange.schedule.entity.SchScheduleResult;
import com.lyk.coursearrange.schedule.entity.SchScheduleRunLog;
import com.lyk.coursearrange.schedule.entity.SchTask;
import com.lyk.coursearrange.schedule.service.SchScheduleResultService;
import com.lyk.coursearrange.schedule.service.SchScheduleRunLogService;
import com.lyk.coursearrange.schedule.service.SchScheduleRunDetailService;
import com.lyk.coursearrange.schedule.vo.AdminClassVO;
import com.lyk.coursearrange.schedule.vo.ScheduleExecutionDetailVO;
import com.lyk.coursearrange.schedule.vo.ScheduleRunLogVO;
import com.lyk.coursearrange.schedule.vo.SchedulingTaskInput;
import com.lyk.coursearrange.schedule.service.SchTaskService;
import com.lyk.coursearrange.system.config.entity.CfgScheduleRule;
import com.lyk.coursearrange.system.config.entity.CfgTimeSlot;
import com.lyk.coursearrange.system.config.service.ScheduleConfigFacadeService;
import com.lyk.coursearrange.system.config.vo.ScheduleConfigVO;
import com.lyk.coursearrange.system.rbac.entity.SysUser;
import com.lyk.coursearrange.system.rbac.service.SysUserService;
import com.lyk.coursearrange.util.ClassUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    @Mock
    private ResTeacherService resTeacherService;
    @Mock
    private ResClassroomService resClassroomService;
    @Mock
    private AdminClassService adminClassService;
    @Mock
    private SchScheduleRunLogService schScheduleRunLogService;
    @Mock
    private SchScheduleRunDetailService schScheduleRunDetailService;
    @Mock
    private SchScheduleResultService schScheduleResultService;
    @Mock
    private SysUserService sysUserService;
    @Mock
    private ResCourseService resCourseService;
    @Mock
    private SchedulingEngine schedulingEngine;
    @Mock
    private com.lyk.coursearrange.schedule.service.ScheduleResultWriteService scheduleResultWriteService;

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
    void classScheduling_shouldReturnExecutionSummaryAndPersistRunDetails() {
        ClassTaskServiceImpl service = new ClassTaskServiceImpl();
        ReflectionTestUtils.setField(service, "schTaskService", schTaskService);
        ReflectionTestUtils.setField(service, "resTeacherService", resTeacherService);
        ReflectionTestUtils.setField(service, "resCourseService", resCourseService);
        ReflectionTestUtils.setField(service, "resClassroomService", resClassroomService);
        ReflectionTestUtils.setField(service, "scheduleConfigFacadeService", scheduleConfigFacadeService);
        ReflectionTestUtils.setField(service, "scheduleResultWriteService", scheduleResultWriteService);
        ReflectionTestUtils.setField(service, "schScheduleRunLogService", schScheduleRunLogService);
        ReflectionTestUtils.setField(service, "schScheduleRunDetailService", schScheduleRunDetailService);
        ReflectionTestUtils.setField(service, "schedulingEngine", schedulingEngine);

        SchTask task1 = buildSchTask(101L, "TK-101", "C1", "K1", "T1", "数学", "张老师");
        SchTask task2 = buildSchTask(102L, "TK-102", "C2", "K2", "T2", "英语", "李老师");

        ResCourse course = new ResCourse();
        course.setCourseCode("K1");
        course.setNeedSpecialRoom(0);
        course.setRoomType("NORMAL");

        SchedulingAssignment assignment = SchedulingAssignment.builder()
                .taskId(101L)
                .taskCode("TK-101")
                .classNo("C1")
                .courseNo("K1")
                .teacherNo("T1")
                .classroomCode("A101")
                .weekdayNo(1)
                .periodNo(1)
                .timeSlotCode("01")
                .build();

        UnscheduledTaskDetail unscheduledTask = UnscheduledTaskDetail.builder()
                .taskId(102L)
                .taskCode("TK-102")
                .classNo("C2")
                .courseNo("K2")
                .teacherNo("T2")
                .reasonCode("TEACHER_FORBIDDEN_TIME")
                .reasonMessage("教师禁排时间已覆盖全部可用时间片")
                .build();

        SchedulingExecutionResult executionResult = SchedulingExecutionResult.builder()
                .taskCount(2)
                .scheduledTaskCount(1)
                .unscheduledTaskCount(1)
                .successRate(50.0d)
                .assignments(List.of(assignment))
                .unscheduledTasks(List.of(unscheduledTask))
                .constraintSummary(List.of(new SchedulingConstraintSummary("TEACHER_FORBIDDEN_TIME", "教师禁排时间", 1)))
                .build();

        when(schTaskService.list(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SchTask>>any()))
                .thenReturn(List.of(task1, task2));
        when(resTeacherService.list(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ResTeacher>>any()))
                .thenReturn(List.of());
        when(resCourseService.list(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ResCourse>>any()))
                .thenReturn(List.of(course));
        when(scheduleConfigFacadeService.getScheduleConfig(any())).thenReturn(ScheduleConfigVO.builder().timeSlots(List.of()).build());
        when(schedulingEngine.execute(any())).thenReturn(executionResult);
        when(schScheduleRunLogService.save(any())).thenAnswer(invocation -> {
            SchScheduleRunLog runLog = invocation.getArgument(0);
            runLog.setId(9001L);
            if (runLog.getCreatedAt() == null) {
                runLog.setCreatedAt(LocalDateTime.of(2026, 3, 25, 10, 0, 0));
            }
            return true;
        });

        ServerResponse<?> response = service.classScheduling("2025-2026-1");

        assertTrue(response.isSuccess());
        assertTrue(response.getMessage().contains("排课完成，但仍有 1 个任务未生成标准课表"));
        assertEquals(9001L, ((Map<?, ?>) response.getData()).get("runLogId"));
        assertEquals(2, ((Map<?, ?>) response.getData()).get("taskCount"));
        assertEquals(1, ((Map<?, ?>) response.getData()).get("scheduledTaskCount"));
        assertEquals(1, ((Map<?, ?>) response.getData()).get("unscheduledTaskCount"));
        assertEquals(50.0d, ((Map<?, ?>) response.getData()).get("successRate"));
        verify(scheduleResultWriteService).replaceScheduleResults(eq("2025-2026-1"), eq(9001L), any(), eq(List.of(assignment)));
        verify(schScheduleRunDetailService).replaceRunDetails(9001L, List.of(unscheduledTask));
    }

    @Test
    void listSchedulingTasks_shouldReturnEmptyWhenStandardTasksMissing() {
        ClassTaskServiceImpl service = spy(new ClassTaskServiceImpl());
        ReflectionTestUtils.setField(service, "schTaskService", schTaskService);
        ReflectionTestUtils.setField(service, "resTeacherService", resTeacherService);
        when(schTaskService.list(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SchTask>>any()))
                .thenReturn(List.of());

        List<SchedulingTaskInput> tasks = service.listSchedulingTasks("2025-2026-1");

        assertTrue(tasks.isEmpty());
    }

    @Test
    void listSchedulingTasks_shouldMapStandardTaskToAlgorithmInput() {
        ClassTaskServiceImpl service = new ClassTaskServiceImpl();
        ReflectionTestUtils.setField(service, "schTaskService", schTaskService);
        ReflectionTestUtils.setField(service, "resTeacherService", resTeacherService);

        SchTask standardTask = new SchTask();
        standardTask.setId(101L);
        standardTask.setStudentCount(45);
        standardTask.setWeekHours(4);
        standardTask.setTotalHours(64);
        standardTask.setNeedFixedTime(1);
        standardTask.setFixedWeekdayNo(2);
        standardTask.setFixedPeriodNo(3);
        standardTask.setPriorityLevel(8);
        standardTask.setRemark("semester=2025-2026-1,classNo=2501,courseNo=10001,teacherNo=T2026001,gradeNo=2025,courseName=高等数学,courseAttr=必修,teacherName=张老师");

        when(schTaskService.list(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SchTask>>any()))
                .thenReturn(List.of(standardTask));
        when(resTeacherService.list(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ResTeacher>>any()))
                .thenReturn(List.of());

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
        assertEquals(8, task.getPriorityLevel());
    }

    @Test
    void listSchedulingTasks_shouldSortByPriorityLevelDescThenIdAsc() {
        ClassTaskServiceImpl service = new ClassTaskServiceImpl();
        ReflectionTestUtils.setField(service, "schTaskService", schTaskService);
        ReflectionTestUtils.setField(service, "resTeacherService", resTeacherService);

        SchTask lowPriorityTask = new SchTask();
        lowPriorityTask.setId(102L);
        lowPriorityTask.setPriorityLevel(2);
        lowPriorityTask.setRemark("semester=2025-2026-1,classNo=2502,courseNo=10002,teacherNo=T2,gradeNo=2025,courseName=英语,courseAttr=必修,teacherName=李老师");

        SchTask highPriorityTask = new SchTask();
        highPriorityTask.setId(101L);
        highPriorityTask.setPriorityLevel(9);
        highPriorityTask.setRemark("semester=2025-2026-1,classNo=2501,courseNo=10001,teacherNo=T1,gradeNo=2025,courseName=数学,courseAttr=必修,teacherName=张老师");

        when(schTaskService.list(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SchTask>>any()))
                .thenReturn(List.of(lowPriorityTask, highPriorityTask));
        when(resTeacherService.list(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ResTeacher>>any()))
                .thenReturn(List.of());

        List<SchedulingTaskInput> tasks = service.listSchedulingTasks("2025-2026-1");

        assertEquals(2, tasks.size());
        assertEquals("2501", tasks.get(0).getClassNo());
        assertEquals(9, tasks.get(0).getPriorityLevel());
        assertEquals("2502", tasks.get(1).getClassNo());
        assertEquals(2, tasks.get(1).getPriorityLevel());
    }

    @Test
    void listSchedulingTasks_shouldAttachTeacherHourLimitsFromTeacherResource() {
        ClassTaskServiceImpl service = new ClassTaskServiceImpl();
        ReflectionTestUtils.setField(service, "schTaskService", schTaskService);
        ReflectionTestUtils.setField(service, "resTeacherService", resTeacherService);

        SchTask standardTask = new SchTask();
        standardTask.setStudentCount(40);
        standardTask.setWeekHours(4);
        standardTask.setTotalHours(64);
        standardTask.setRemark("semester=2025-2026-1,classNo=2501,courseNo=10001,teacherNo=T0001,gradeNo=2025,courseName=高等数学,courseAttr=必修,teacherName=张老师");

        ResTeacher teacher = new ResTeacher();
        teacher.setTeacherCode("T0001");
        teacher.setMaxWeekHours(18);
        teacher.setMaxDayHours(2);

        when(schTaskService.list(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SchTask>>any()))
                .thenReturn(List.of(standardTask));
        when(resTeacherService.list(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ResTeacher>>any()))
                .thenReturn(List.of(teacher));

        List<SchedulingTaskInput> tasks = service.listSchedulingTasks("2025-2026-1");

        assertEquals(1, tasks.size());
        assertEquals(18, tasks.get(0).getMaxWeekHours());
        assertEquals(2, tasks.get(0).getMaxDayHours());
    }

    @Test
    void listSchedulingTasks_shouldAttachTeacherForbiddenTimeSlotsFromTeacherRemark() {
        ClassTaskServiceImpl service = new ClassTaskServiceImpl();
        ReflectionTestUtils.setField(service, "schTaskService", schTaskService);
        ReflectionTestUtils.setField(service, "resTeacherService", resTeacherService);

        SchTask standardTask = new SchTask();
        standardTask.setStudentCount(40);
        standardTask.setWeekHours(4);
        standardTask.setTotalHours(64);
        standardTask.setRemark("semester=2025-2026-1,classNo=2501,courseNo=10001,teacherNo=T0001,gradeNo=2025,courseName=高等数学,courseAttr=必修,teacherName=张老师");

        ResTeacher teacher = new ResTeacher();
        teacher.setTeacherCode("T0001");
        teacher.setForbiddenTimeSlots("01,06,11");

        when(schTaskService.list(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SchTask>>any()))
                .thenReturn(List.of(standardTask));
        when(resTeacherService.list(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ResTeacher>>any()))
                .thenReturn(List.of(teacher));

        List<SchedulingTaskInput> tasks = service.listSchedulingTasks("2025-2026-1");

        assertEquals(1, tasks.size());
        assertEquals(List.of("01", "06", "11"), tasks.get(0).getTeacherForbiddenTimeSlots());
    }

    @Test
    void listSchedulingTasks_shouldAttachClassForbiddenTimeSlotsFromAdminClass() {
        ClassTaskServiceImpl service = new ClassTaskServiceImpl();
        ReflectionTestUtils.setField(service, "schTaskService", schTaskService);
        ReflectionTestUtils.setField(service, "resTeacherService", resTeacherService);
        ReflectionTestUtils.setField(service, "adminClassService", adminClassService);

        SchTask standardTask = new SchTask();
        standardTask.setStudentCount(40);
        standardTask.setWeekHours(4);
        standardTask.setTotalHours(64);
        standardTask.setRemark("semester=2025-2026-1,classNo=2501,courseNo=10001,teacherNo=T0001,gradeNo=2025,courseName=高等数学,courseAttr=必修,teacherName=张老师");

        AdminClassVO classInfo = new AdminClassVO();
        classInfo.setClassNo("2501");
        classInfo.setForbiddenTimeSlots("02,07");

        when(schTaskService.list(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SchTask>>any()))
                .thenReturn(List.of(standardTask));
        when(resTeacherService.list(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ResTeacher>>any()))
                .thenReturn(List.of());
        when(adminClassService.listClassesByCodes(List.of("2501")))
                .thenReturn(List.of(classInfo));

        List<SchedulingTaskInput> tasks = service.listSchedulingTasks("2025-2026-1");

        assertEquals(1, tasks.size());
        assertEquals(List.of("02", "07"), tasks.get(0).getClassForbiddenTimeSlots());
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
    void listRecentExecuteLogs_shouldMapStandardRunLogsToFrontendShape() {
        ClassTaskServiceImpl service = new ClassTaskServiceImpl();
        ReflectionTestUtils.setField(service, "schScheduleRunLogService", schScheduleRunLogService);
        ReflectionTestUtils.setField(service, "sysUserService", sysUserService);

        SchScheduleRunLog log = new SchScheduleRunLog();
        log.setId(1L);
        log.setRemark("2025-2026-1");
        log.setTaskTotal(12);
        log.setTaskSuccess(48);
        log.setRunStatus("SUCCESS");
        log.setFailureReason("排课成功");
        log.setOperatorUserId(99L);
        log.setStartedAt(java.time.LocalDateTime.of(2026, 3, 25, 9, 0, 0));
        log.setFinishedAt(java.time.LocalDateTime.of(2026, 3, 25, 9, 0, 3));
        log.setCreatedAt(java.time.LocalDateTime.of(2026, 3, 25, 9, 0, 3));

        SysUser user = new SysUser();
        user.setId(99L);
        user.setDisplayName("管理员");

        when(schScheduleRunLogService.list(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.Wrapper<SchScheduleRunLog>>any()))
                .thenReturn(List.of(log));
        when(sysUserService.listByIds(List.of(99L))).thenReturn(List.of(user));

        List<ScheduleRunLogVO> logs = service.listRecentExecuteLogs("2025-2026-1", 10);

        assertEquals(1, logs.size());
        assertEquals("2025-2026-1", logs.get(0).getSemester());
        assertEquals(12, logs.get(0).getTaskCount());
        assertEquals(48, logs.get(0).getGeneratedPlanCount());
        assertEquals(1, logs.get(0).getStatus());
        assertEquals("管理员", logs.get(0).getOperatorName());
        assertEquals(3000L, logs.get(0).getDurationMs());
    }

    @Test
    void getExecutionDetail_shouldAggregateRunDetailAndGeneratedResultCount() {
        ClassTaskServiceImpl service = new ClassTaskServiceImpl();
        ReflectionTestUtils.setField(service, "schScheduleRunLogService", schScheduleRunLogService);
        ReflectionTestUtils.setField(service, "schScheduleRunDetailService", schScheduleRunDetailService);
        ReflectionTestUtils.setField(service, "schScheduleResultService", schScheduleResultService);

        SchScheduleRunLog runLog = new SchScheduleRunLog();
        runLog.setId(9001L);
        runLog.setRemark("2025-2026-1");
        runLog.setTaskTotal(10);
        runLog.setTaskSuccess(8);
        runLog.setTaskFailed(2);
        runLog.setRunStatus("PARTIAL");
        runLog.setFailureReason("排课完成，2 个任务未排成");
        runLog.setStartedAt(LocalDateTime.of(2026, 3, 25, 10, 0, 0));
        runLog.setFinishedAt(LocalDateTime.of(2026, 3, 25, 10, 0, 5));

        UnscheduledTaskDetail detail1 = UnscheduledTaskDetail.builder()
                .taskId(11L)
                .taskCode("TK-11")
                .classNo("C1")
                .courseNo("K1")
                .teacherNo("T1")
                .reasonCode("TEACHER_CONFLICT")
                .reasonMessage("教师时间冲突")
                .build();
        UnscheduledTaskDetail detail2 = UnscheduledTaskDetail.builder()
                .taskId(12L)
                .taskCode("TK-12")
                .classNo("C2")
                .courseNo("K2")
                .teacherNo("T2")
                .reasonCode("TEACHER_CONFLICT")
                .reasonMessage("教师时间冲突")
                .build();

        SchScheduleResult result1 = new SchScheduleResult();
        result1.setId(1L);
        SchScheduleResult result2 = new SchScheduleResult();
        result2.setId(2L);

        when(schScheduleRunLogService.getById(9001L)).thenReturn(runLog);
        when(schScheduleRunDetailService.listByRunLogId(9001L)).thenReturn(List.of(detail1, detail2));
        when(schScheduleResultService.list(org.mockito.ArgumentMatchers.<Wrapper<SchScheduleResult>>any()))
                .thenReturn(List.of(result1, result2));

        ScheduleExecutionDetailVO detail = service.getExecutionDetail(9001L);

        assertEquals(9001L, detail.getRunLogId());
        assertEquals("2025-2026-1", detail.getSemester());
        assertEquals(10, detail.getTaskCount());
        assertEquals(8, detail.getScheduledTaskCount());
        assertEquals(2, detail.getUnscheduledTaskCount());
        assertEquals(2, detail.getGeneratedResultCount());
        assertEquals(1, detail.getConstraintSummary().size());
        assertEquals("TEACHER_CONFLICT", detail.getConstraintSummary().get(0).getReasonCode());
        assertEquals(2, detail.getConstraintSummary().get(0).getCount());
        assertEquals(2, detail.getUnscheduledTasks().size());
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
        scheduledTask.setTeacherName("张老师");

        SchedulingTaskInput unscheduledTask = new SchedulingTaskInput();
        unscheduledTask.setSemester("2025-2026-1");
        unscheduledTask.setClassNo("2502");
        unscheduledTask.setCourseNo("10002");
        unscheduledTask.setCourseName("大学英语");
        unscheduledTask.setTeacherNo("T2026002");
        unscheduledTask.setTeacherName("李老师");

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

    @Test
    void validateTeacherDayHourLimit_shouldRejectFixedTasksExceedingDailyLimit() {
        ClassTaskServiceImpl service = new ClassTaskServiceImpl();

        SchedulingTaskInput firstTask = new SchedulingTaskInput();
        firstTask.setTeacherNo("T0001");
        firstTask.setTeacherName("张老师");
        firstTask.setCourseName("高等数学");
        firstTask.setClassNo("25010001");
        firstTask.setIsFix("1");
        firstTask.setClassTime("01");
        firstTask.setMaxDayHours(1);

        SchedulingTaskInput secondTask = new SchedulingTaskInput();
        secondTask.setTeacherNo("T0001");
        secondTask.setTeacherName("张老师");
        secondTask.setCourseName("大学英语");
        secondTask.setClassNo("25010002");
        secondTask.setIsFix("1");
        secondTask.setClassTime("02");
        secondTask.setMaxDayHours(1);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validateTeacherDayHourLimit(List.of(firstTask, secondTask)));

        assertTrue(exception.getMessage().contains("教师"));
        assertTrue(exception.getMessage().contains("日上限课时"));
    }

    @Test
    void enforceTeacherDayHourLimit_shouldMoveOverflowTaskToDifferentDay() {
        ClassTaskServiceImpl service = new ClassTaskServiceImpl();

        List<String> resultGeneList = new java.util.ArrayList<>(List.of(
                "12525010001T00011000010101",
                "12525010002T00011000020102"
        ));

        List<String> adjustedGenes = service.enforceTeacherDayHourLimit(resultGeneList, Map.of("T0001", 1), List.of("01", "02", "06"));

        long dayOneCount = adjustedGenes.stream()
                .map(gene -> gene.substring(24, 26))
                .filter(classTime -> classTime.equals("01") || classTime.equals("02"))
                .count();
        assertEquals(1, dayOneCount);
        assertTrue(adjustedGenes.stream().anyMatch(gene -> gene.endsWith("06")));
    }

    @Test
    void validateTeacherForbiddenTimeSlots_shouldRejectFixedTasksInForbiddenSlots() {
        ClassTaskServiceImpl service = new ClassTaskServiceImpl();

        SchedulingTaskInput fixedTask = new SchedulingTaskInput();
        fixedTask.setTeacherNo("T0001");
        fixedTask.setTeacherName("张老师");
        fixedTask.setCourseName("高等数学");
        fixedTask.setClassNo("25010001");
        fixedTask.setIsFix("1");
        fixedTask.setClassTime("01");
        fixedTask.setTeacherForbiddenTimeSlots(List.of("01", "06"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validateTeacherForbiddenTimeSlots(List.of(fixedTask)));

        assertTrue(exception.getMessage().contains("教师"));
        assertTrue(exception.getMessage().contains("禁排"));
    }

    @Test
    void enforceTeacherForbiddenTimeSlots_shouldMoveTaskAwayFromForbiddenSlots() {
        ClassTaskServiceImpl service = new ClassTaskServiceImpl();

        List<String> resultGeneList = new java.util.ArrayList<>(List.of(
                "12525010001T00011000010101",
                "12525010002T00021000020102"
        ));

        List<String> adjustedGenes = service.enforceTeacherForbiddenTimeSlots(
                resultGeneList,
                Map.of("T0001", List.of("01")),
                List.of("01", "02", "03", "04", "05")
        );

        assertTrue(adjustedGenes.stream()
                .filter(gene -> gene.contains("T0001"))
                .noneMatch(gene -> gene.endsWith("01")));
    }

    @Test
    void validateClassForbiddenTimeSlots_shouldRejectFixedTasksInForbiddenSlots() {
        ClassTaskServiceImpl service = new ClassTaskServiceImpl();

        SchedulingTaskInput fixedTask = new SchedulingTaskInput();
        fixedTask.setClassNo("25010001");
        fixedTask.setCourseName("高等数学");
        fixedTask.setIsFix("1");
        fixedTask.setClassTime("01");
        fixedTask.setClassForbiddenTimeSlots(List.of("01", "06"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.validateClassForbiddenTimeSlots(List.of(fixedTask)));

        assertTrue(exception.getMessage().contains("班级"));
        assertTrue(exception.getMessage().contains("禁排"));
    }

    @Test
    void enforceClassForbiddenTimeSlots_shouldMoveTaskAwayFromForbiddenSlots() {
        ClassTaskServiceImpl service = new ClassTaskServiceImpl();

        List<String> resultGeneList = new java.util.ArrayList<>(List.of(
                "12525010001T00011000010101",
                "12525010002T00021000020102"
        ));

        List<String> adjustedGenes = service.enforceClassForbiddenTimeSlots(
                resultGeneList,
                Map.of("25010001", List.of("01")),
                List.of("01", "02", "03", "04", "05")
        );

        assertTrue(adjustedGenes.stream()
                .filter(gene -> gene.contains("25010001"))
                .noneMatch(gene -> gene.endsWith("01")));
    }

    @Test
    void finalResult_shouldAssignStandardClassroomWithinAdminClassScope() {
        ClassTaskServiceImpl service = new ClassTaskServiceImpl();
        ReflectionTestUtils.setField(service, "adminClassService", adminClassService);
        ReflectionTestUtils.setField(service, "resClassroomService", resClassroomService);

        AdminClassVO adminClass = new AdminClassVO();
        adminClass.setClassNo("25010001");
        adminClass.setNum(42);
        adminClass.setCampusId(1L);
        adminClass.setCollegeId(2L);

        ResClassroom scopedRoom = new ResClassroom();
        scopedRoom.setId(1L);
        scopedRoom.setClassroomCode("A101");
        scopedRoom.setCampusId(1L);
        scopedRoom.setCollegeId(2L);
        scopedRoom.setRoomType("NORMAL");
        scopedRoom.setSeatCount(50);
        scopedRoom.setStatus(1);
        scopedRoom.setDeleted(0);

        ResClassroom otherScopeRoom = new ResClassroom();
        otherScopeRoom.setId(2L);
        otherScopeRoom.setClassroomCode("A201");
        otherScopeRoom.setCampusId(9L);
        otherScopeRoom.setCollegeId(9L);
        otherScopeRoom.setRoomType("NORMAL");
        otherScopeRoom.setSeatCount(80);
        otherScopeRoom.setStatus(1);
        otherScopeRoom.setDeleted(0);

        when(adminClassService.listClassesByCodes(List.of("25010001"))).thenReturn(List.of(adminClass));
        when(resClassroomService.list(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ResClassroom>>any()))
                .thenReturn(List.of(scopedRoom, otherScopeRoom));

        List<String> result = service.finalResult(Map.of(
                "25010001", List.of("12525010001T00011000010101")
        ));

        assertEquals(1, result.size());
        assertEquals("A101", ClassUtil.cutGene(ConstantInfo.CLASSROOM_NO, result.get(0)));
    }

    private CfgTimeSlot buildTimeSlot(int weekdayNo, int periodNo, int isTeaching, int isFixedBreak) {
        CfgTimeSlot timeSlot = new CfgTimeSlot();
        timeSlot.setWeekdayNo(weekdayNo);
        timeSlot.setPeriodNo(periodNo);
        timeSlot.setIsTeaching(isTeaching);
        timeSlot.setIsFixedBreak(isFixedBreak);
        return timeSlot;
    }

    private SchTask buildSchTask(Long id,
                                 String taskCode,
                                 String classNo,
                                 String courseNo,
                                 String teacherNo,
                                 String courseName,
                                 String teacherName) {
        SchTask task = new SchTask();
        task.setId(id);
        task.setTaskCode(taskCode);
        task.setStudentCount(45);
        task.setWeekHours(2);
        task.setTotalHours(16);
        task.setRemark(String.format(
                "semester=2025-2026-1,classNo=%s,courseNo=%s,teacherNo=%s,gradeNo=G1,courseName=%s,courseAttr=必修,teacherName=%s",
                classNo, courseNo, teacherNo, courseName, teacherName
        ));
        return task;
    }
}
