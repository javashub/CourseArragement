package com.lyk.coursearrange.schedule.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.entity.ClassInfo;
import com.lyk.coursearrange.entity.request.ClassTaskDTO;
import com.lyk.coursearrange.resource.entity.ResTeacher;
import com.lyk.coursearrange.resource.service.ResTeacherService;
import com.lyk.coursearrange.schedule.entity.SchTask;
import com.lyk.coursearrange.schedule.service.SchTaskService;
import com.lyk.coursearrange.schedule.vo.ScheduleTaskPageVO;
import com.lyk.coursearrange.service.ClassInfoService;
import com.lyk.coursearrange.service.ClassTaskService;
import com.lyk.coursearrange.system.config.entity.CfgScheduleRule;
import com.lyk.coursearrange.system.config.service.ScheduleConfigFacadeService;
import com.lyk.coursearrange.system.config.vo.ScheduleConfigVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleTaskControllerTest {

    @Mock
    private ClassTaskService classTaskService;
    @Mock
    private SchTaskService schTaskService;
    @Mock
    private ScheduleConfigFacadeService scheduleConfigFacadeService;
    @Mock
    private ResTeacherService resTeacherService;
    @Mock
    private ClassInfoService classInfoService;

    @Test
    void page_shouldReturnStandardTasksWithoutReadingLegacyTaskTable() {
        ScheduleTaskController controller = new ScheduleTaskController(classTaskService, schTaskService, scheduleConfigFacadeService, resTeacherService, classInfoService);

        SchTask task = new SchTask();
        task.setId(101L);
        task.setStudentCount(40);
        task.setWeekHours(4);
        task.setTotalHours(20);
        task.setNeedContinuous(1);
        task.setContinuousSize(2);
        task.setNeedFixedTime(0);
        task.setRemark("semester=2025-2026-1,classNo=C1,courseNo=K1,teacherNo=T1,gradeNo=G1,courseName=数学,teacherName=张老师");

        Page<SchTask> taskPage = new Page<>(1, 10, 1);
        taskPage.setRecords(List.of(task));

        when(schTaskService.page(org.mockito.ArgumentMatchers.any(Page.class), org.mockito.ArgumentMatchers.any(Wrapper.class))).thenReturn(taskPage);

        ServerResponse<?> response = controller.page("2025-2026-1", 1, 10, null, null, null);

        assertTrue(response.isSuccess());
        assertInstanceOf(IPage.class, response.getData());
        IPage<?> page = (IPage<?>) response.getData();
        assertEquals(1, page.getTotal());
        ScheduleTaskPageVO vo = (ScheduleTaskPageVO) page.getRecords().get(0);
        assertEquals(Long.valueOf(101L), vo.getStandardId());
        assertEquals("C1", vo.getClassNo());
        assertEquals("数学", vo.getCourseName());
        assertEquals(1, vo.getNeedContinuous());
        assertEquals(2, vo.getContinuousSize());
    }

    @Test
    void page_shouldReturnEmptyPageWhenStandardTasksMissing() {
        ScheduleTaskController controller = new ScheduleTaskController(classTaskService, schTaskService, scheduleConfigFacadeService, resTeacherService, classInfoService);

        Page<SchTask> taskPage = new Page<>(1, 10, 0);
        taskPage.setRecords(List.of());
        when(schTaskService.page(org.mockito.ArgumentMatchers.any(Page.class), org.mockito.ArgumentMatchers.any(Wrapper.class))).thenReturn(taskPage);

        ServerResponse<?> response = controller.page("2025-2026-1", 1, 10, null, null, null);

        assertTrue(response.isSuccess());
        assertInstanceOf(IPage.class, response.getData());
        IPage<?> page = (IPage<?>) response.getData();
        assertEquals(0, page.getTotal());
    }

    @Test
    void page_shouldFilterTasksByClassTeacherAndCourse() {
        ScheduleTaskController controller = new ScheduleTaskController(classTaskService, schTaskService, scheduleConfigFacadeService, resTeacherService, classInfoService);

        SchTask matchedTask = new SchTask();
        matchedTask.setId(101L);
        matchedTask.setStudentCount(40);
        matchedTask.setWeekHours(4);
        matchedTask.setTotalHours(20);
        matchedTask.setNeedFixedTime(0);
        matchedTask.setRemark("semester=2025-2026-1,classNo=C1,courseNo=K1,teacherNo=T1,gradeNo=G1,courseName=数学,teacherName=张老师");

        SchTask filteredTask = new SchTask();
        filteredTask.setId(102L);
        filteredTask.setStudentCount(42);
        filteredTask.setWeekHours(4);
        filteredTask.setTotalHours(20);
        filteredTask.setNeedFixedTime(0);
        filteredTask.setRemark("semester=2025-2026-1,classNo=C2,courseNo=K2,teacherNo=T2,gradeNo=G1,courseName=英语,teacherName=李老师");

        Page<SchTask> taskPage = new Page<>(1, 10, 2);
        taskPage.setRecords(List.of(matchedTask, filteredTask));

        when(schTaskService.page(org.mockito.ArgumentMatchers.any(Page.class), org.mockito.ArgumentMatchers.any(Wrapper.class))).thenReturn(taskPage);

        ServerResponse<?> response = controller.page("2025-2026-1", 1, 10, "C1", "T1", "K1");

        assertTrue(response.isSuccess());
        assertInstanceOf(IPage.class, response.getData());
        IPage<?> page = (IPage<?>) response.getData();
        assertEquals(1, page.getTotal());
        ScheduleTaskPageVO vo = (ScheduleTaskPageVO) page.getRecords().get(0);
        assertEquals("C1", vo.getClassNo());
        assertEquals("T1", vo.getTeacherNo());
        assertEquals("K1", vo.getCourseNo());
    }

    @Test
    void save_shouldPersistContinuousTaskFields() {
        ScheduleTaskController controller = new ScheduleTaskController(classTaskService, schTaskService, scheduleConfigFacadeService, resTeacherService, classInfoService);

        ClassTaskDTO request = new ClassTaskDTO();
        request.setSemester("2025-2026-1");
        request.setGradeNo("G1");
        request.setClassNo("C1");
        request.setCourseNo("K1");
        request.setCourseName("数学");
        request.setTeacherNo("T1");
        request.setRealname("张老师");
        request.setCourseAttr("必修");
        request.setStudentNum(40);
        request.setWeeksNumber(4);
        request.setWeeksSum(16);
        request.setIsFix("0");
        request.setClassTime("");
        request.setNeedContinuous(1);
        request.setContinuousSize(2);

        when(scheduleConfigFacadeService.getScheduleConfig(any())).thenReturn(buildScheduleConfig(2));
        when(schTaskService.getOne(org.mockito.ArgumentMatchers.any(Wrapper.class), org.mockito.ArgumentMatchers.eq(false))).thenReturn(null);
        when(schTaskService.save(org.mockito.ArgumentMatchers.any(SchTask.class))).thenReturn(true);

        ServerResponse<?> response = controller.save(request);

        assertTrue(response.isSuccess());
        ArgumentCaptor<SchTask> captor = ArgumentCaptor.forClass(SchTask.class);
        verify(schTaskService).save(captor.capture());
        assertEquals(1, captor.getValue().getNeedContinuous());
        assertEquals(2, captor.getValue().getContinuousSize());
    }

    @Test
    void save_shouldRejectWhenContinuousSizeExceedsRuleLimit() {
        ScheduleTaskController controller = new ScheduleTaskController(classTaskService, schTaskService, scheduleConfigFacadeService, resTeacherService, classInfoService);

        ClassTaskDTO request = new ClassTaskDTO();
        request.setSemester("2025-2026-1");
        request.setGradeNo("G1");
        request.setClassNo("C1");
        request.setCourseNo("K1");
        request.setCourseName("数学");
        request.setTeacherNo("T1");
        request.setRealname("张老师");
        request.setCourseAttr("必修");
        request.setStudentNum(40);
        request.setWeeksNumber(4);
        request.setWeeksSum(16);
        request.setIsFix("0");
        request.setClassTime("");
        request.setNeedContinuous(1);
        request.setContinuousSize(3);

        when(scheduleConfigFacadeService.getScheduleConfig(any())).thenReturn(buildScheduleConfig(2));

        assertThrows(BusinessException.class, () -> controller.save(request));
    }

    @Test
    void update_shouldModifyStandardTask() {
        ScheduleTaskController controller = new ScheduleTaskController(classTaskService, schTaskService, scheduleConfigFacadeService, resTeacherService, classInfoService);

        ClassTaskDTO request = new ClassTaskDTO();
        request.setSemester("2025-2026-1");
        request.setGradeNo("G2");
        request.setClassNo("C2");
        request.setCourseNo("K2");
        request.setCourseName("英语");
        request.setTeacherNo("T2");
        request.setRealname("李老师");
        request.setCourseAttr("选修");
        request.setStudentNum(45);
        request.setWeeksNumber(2);
        request.setWeeksSum(18);
        request.setIsFix("1");
        request.setClassTime("03");
        request.setNeedContinuous(1);
        request.setContinuousSize(2);

        SchTask task = new SchTask();
        task.setId(101L);
        task.setDeleted(0);
        task.setRemark("semester=2025-2026-1,classNo=C1,courseNo=K1,teacherNo=T1,gradeNo=G1,courseName=数学,teacherName=张老师");

        when(scheduleConfigFacadeService.getScheduleConfig(any())).thenReturn(buildScheduleConfig(2));
        when(resTeacherService.getOne(org.mockito.ArgumentMatchers.any(Wrapper.class), org.mockito.ArgumentMatchers.eq(false))).thenReturn(null);
        when(schTaskService.getById(101L)).thenReturn(task);
        when(schTaskService.getOne(org.mockito.ArgumentMatchers.any(Wrapper.class), org.mockito.ArgumentMatchers.eq(false))).thenReturn(null);
        when(schTaskService.updateById(org.mockito.ArgumentMatchers.any(SchTask.class))).thenReturn(true);

        ServerResponse<?> response = controller.update(101L, request);

        assertTrue(response.isSuccess());
    }

    @Test
    void delete_shouldOnlyDeleteStandardTask() {
        ScheduleTaskController controller = new ScheduleTaskController(classTaskService, schTaskService, scheduleConfigFacadeService, resTeacherService, classInfoService);

        SchTask task = new SchTask();
        task.setId(101L);
        task.setDeleted(0);

        when(schTaskService.getById(101L)).thenReturn(task);
        when(schTaskService.removeById(101L)).thenReturn(true);

        ServerResponse<?> response = controller.delete(101);

        assertTrue(response.isSuccess());
    }

    @Test
    void createExecution_shouldDelegateToSchedulingService() {
        ScheduleTaskController controller = new ScheduleTaskController(classTaskService, schTaskService, scheduleConfigFacadeService, resTeacherService, classInfoService);
        ServerResponse<?> expected = ServerResponse.ofSuccess("排课成功");
        when(classTaskService.classScheduling("2025-2026-1")).thenReturn(expected);

        ServerResponse<?> response = controller.createExecution("2025-2026-1");

        assertEquals(expected, response);
    }

    @Test
    void listExecutions_shouldReturnRecentArrangeLogs() {
        ScheduleTaskController controller = new ScheduleTaskController(classTaskService, schTaskService, scheduleConfigFacadeService, resTeacherService, classInfoService);
        when(classTaskService.listRecentExecuteLogs("2025-2026-1", 8)).thenReturn(List.of());

        ServerResponse<?> response = controller.listExecutions("2025-2026-1", 8);

        assertTrue(response.isSuccess());
        assertEquals(List.of(), response.getData());
    }

    @Test
    void save_shouldRejectFixedTaskWhenTeacherTimeSlotIsForbidden() {
        ScheduleTaskController controller = new ScheduleTaskController(classTaskService, schTaskService, scheduleConfigFacadeService, resTeacherService, classInfoService);

        ClassTaskDTO request = new ClassTaskDTO();
        request.setSemester("2025-2026-1");
        request.setGradeNo("G1");
        request.setClassNo("C1");
        request.setCourseNo("K1");
        request.setCourseName("数学");
        request.setTeacherNo("T1");
        request.setRealname("张老师");
        request.setCourseAttr("必修");
        request.setStudentNum(40);
        request.setWeeksNumber(2);
        request.setWeeksSum(16);
        request.setIsFix("1");
        request.setClassTime("01");

        ResTeacher teacher = new ResTeacher();
        teacher.setTeacherCode("T1");
        teacher.setForbiddenTimeSlots("01");

        when(schTaskService.getOne(org.mockito.ArgumentMatchers.any(Wrapper.class), org.mockito.ArgumentMatchers.eq(false))).thenReturn(null);
        when(resTeacherService.getOne(org.mockito.ArgumentMatchers.any(Wrapper.class), org.mockito.ArgumentMatchers.eq(false))).thenReturn(teacher);

        BusinessException exception = assertThrows(BusinessException.class, () -> controller.save(request));

        assertTrue(exception.getMessage().contains("禁排"));
    }

    @Test
    void save_shouldRejectFixedTaskWhenClassTimeSlotIsForbidden() {
        ScheduleTaskController controller = new ScheduleTaskController(classTaskService, schTaskService, scheduleConfigFacadeService, resTeacherService, classInfoService);

        ClassTaskDTO request = new ClassTaskDTO();
        request.setSemester("2025-2026-1");
        request.setGradeNo("G1");
        request.setClassNo("C1");
        request.setCourseNo("K1");
        request.setCourseName("数学");
        request.setTeacherNo("T1");
        request.setRealname("张老师");
        request.setCourseAttr("必修");
        request.setStudentNum(40);
        request.setWeeksNumber(2);
        request.setWeeksSum(16);
        request.setIsFix("1");
        request.setClassTime("01");

        ClassInfo classInfo = new ClassInfo();
        classInfo.setClassNo("C1");
        classInfo.setForbiddenTimeSlots("01");

        when(schTaskService.getOne(org.mockito.ArgumentMatchers.any(Wrapper.class), org.mockito.ArgumentMatchers.eq(false))).thenReturn(null);
        when(resTeacherService.getOne(org.mockito.ArgumentMatchers.any(Wrapper.class), org.mockito.ArgumentMatchers.eq(false))).thenReturn(null);
        when(classInfoService.getOne(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(false))).thenReturn(classInfo);

        BusinessException exception = assertThrows(BusinessException.class, () -> controller.save(request));

        assertTrue(exception.getMessage().contains("班级"));
        assertTrue(exception.getMessage().contains("禁排"));
    }

    private ScheduleConfigVO buildScheduleConfig(int defaultContinuousLimit) {
        CfgScheduleRule scheduleRule = new CfgScheduleRule();
        scheduleRule.setDefaultContinuousLimit(defaultContinuousLimit);
        return ScheduleConfigVO.builder()
                .scheduleRule(scheduleRule)
                .timeSlots(List.of())
                .featureToggles(List.of())
                .build();
    }
}
