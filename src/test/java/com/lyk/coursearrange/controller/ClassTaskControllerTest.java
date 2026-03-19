package com.lyk.coursearrange.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.request.ClassTaskDTO;
import com.lyk.coursearrange.schedule.entity.SchTask;
import com.lyk.coursearrange.schedule.service.SchTaskService;
import com.lyk.coursearrange.schedule.service.ScheduleLogMirrorService;
import com.lyk.coursearrange.schedule.vo.ScheduleTaskPageVO;
import com.lyk.coursearrange.service.ClassTaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClassTaskControllerTest {

    @Mock
    private ClassTaskService classTaskService;
    @Mock
    private ScheduleLogMirrorService scheduleLogMirrorService;
    @Mock
    private SchTaskService schTaskService;

    @Test
    void queryClassTask_shouldReturnStandardTasksWithoutReadingLegacyTaskTable() {
        ClassTaskController controller = new ClassTaskController();
        ReflectionTestUtils.setField(controller, "classTaskService", classTaskService);
        ReflectionTestUtils.setField(controller, "scheduleLogMirrorService", scheduleLogMirrorService);
        ReflectionTestUtils.setField(controller, "schTaskService", schTaskService);

        SchTask task = new SchTask();
        task.setId(101L);
        task.setStudentCount(40);
        task.setWeekHours(4);
        task.setTotalHours(20);
        task.setNeedFixedTime(0);
        task.setRemark("semester=2025-2026-1,classNo=C1,courseNo=K1,teacherNo=T1,gradeNo=G1,courseName=数学,teacherName=张老师");

        Page<SchTask> taskPage = new Page<>(1, 10, 1);
        taskPage.setRecords(List.of(task));

        when(schTaskService.page(org.mockito.ArgumentMatchers.any(Page.class), org.mockito.ArgumentMatchers.any(Wrapper.class))).thenReturn(taskPage);

        ServerResponse response = controller.queryClassTask(1, "2025-2026-1", 10);

        assertTrue(response.isSuccess());
        assertInstanceOf(IPage.class, response.getData());
        IPage<?> page = (IPage<?>) response.getData();
        assertEquals(1, page.getTotal());
        ScheduleTaskPageVO vo = (ScheduleTaskPageVO) page.getRecords().get(0);
        assertEquals(Long.valueOf(101L), vo.getStandardId());
        assertEquals("C1", vo.getClassNo());
        verify(classTaskService, never()).listLegacyTasks("2025-2026-1");
    }

    @Test
    void queryClassTask_shouldReturnEmptyPageWhenStandardTasksMissing() {
        ClassTaskController controller = new ClassTaskController();
        ReflectionTestUtils.setField(controller, "classTaskService", classTaskService);
        ReflectionTestUtils.setField(controller, "scheduleLogMirrorService", scheduleLogMirrorService);
        ReflectionTestUtils.setField(controller, "schTaskService", schTaskService);

        Page<SchTask> taskPage = new Page<>(1, 10, 0);
        taskPage.setRecords(List.of());
        when(schTaskService.page(org.mockito.ArgumentMatchers.any(Page.class), org.mockito.ArgumentMatchers.any(Wrapper.class))).thenReturn(taskPage);

        ServerResponse response = controller.queryClassTask(1, "2025-2026-1", 10);

        assertTrue(response.isSuccess());
        assertInstanceOf(IPage.class, response.getData());
        IPage<?> page = (IPage<?>) response.getData();
        assertEquals(0, page.getTotal());
        verify(classTaskService, never()).pageLegacyTasks(1, 10, "2025-2026-1");
    }

    @Test
    void addClassTask_shouldOnlySaveStandardTask() {
        ClassTaskController controller = new ClassTaskController();
        ReflectionTestUtils.setField(controller, "classTaskService", classTaskService);
        ReflectionTestUtils.setField(controller, "scheduleLogMirrorService", scheduleLogMirrorService);
        ReflectionTestUtils.setField(controller, "schTaskService", schTaskService);

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

        when(schTaskService.getOne(org.mockito.ArgumentMatchers.any(Wrapper.class), org.mockito.ArgumentMatchers.eq(false))).thenReturn(null);
        when(schTaskService.save(org.mockito.ArgumentMatchers.any(SchTask.class))).thenReturn(true);

        ServerResponse response = controller.addClassTask(request);

        assertTrue(response.isSuccess());
        verify(classTaskService, never()).saveLegacyTask(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void deleteClassTask_shouldOnlyDeleteStandardTask() {
        ClassTaskController controller = new ClassTaskController();
        ReflectionTestUtils.setField(controller, "classTaskService", classTaskService);
        ReflectionTestUtils.setField(controller, "scheduleLogMirrorService", scheduleLogMirrorService);
        ReflectionTestUtils.setField(controller, "schTaskService", schTaskService);

        SchTask task = new SchTask();
        task.setId(101L);
        task.setDeleted(0);

        when(schTaskService.getById(101L)).thenReturn(task);
        when(schTaskService.removeById(101L)).thenReturn(true);

        ServerResponse response = controller.deleteClassTask(101);

        assertTrue(response.isSuccess());
        verify(classTaskService, never()).getLegacyTaskById(101);
        verify(classTaskService, never()).removeLegacyTaskById(101);
        verify(scheduleLogMirrorService, never()).removeTaskMirror(org.mockito.ArgumentMatchers.any());
    }

}
