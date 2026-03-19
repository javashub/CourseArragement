package com.lyk.coursearrange.schedule.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.request.ClassTaskDTO;
import com.lyk.coursearrange.schedule.entity.SchTask;
import com.lyk.coursearrange.schedule.service.SchTaskService;
import com.lyk.coursearrange.schedule.vo.ScheduleTaskPageVO;
import com.lyk.coursearrange.service.ClassTaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleTaskControllerTest {

    @Mock
    private ClassTaskService classTaskService;
    @Mock
    private SchTaskService schTaskService;

    @Test
    void page_shouldReturnStandardTasksWithoutReadingLegacyTaskTable() {
        ScheduleTaskController controller = new ScheduleTaskController(classTaskService, schTaskService);

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

        ServerResponse<?> response = controller.page("2025-2026-1", 1, 10);

        assertTrue(response.isSuccess());
        assertInstanceOf(IPage.class, response.getData());
        IPage<?> page = (IPage<?>) response.getData();
        assertEquals(1, page.getTotal());
        ScheduleTaskPageVO vo = (ScheduleTaskPageVO) page.getRecords().get(0);
        assertEquals(Long.valueOf(101L), vo.getStandardId());
        assertEquals("C1", vo.getClassNo());
        assertEquals("数学", vo.getCourseName());
    }

    @Test
    void page_shouldReturnEmptyPageWhenStandardTasksMissing() {
        ScheduleTaskController controller = new ScheduleTaskController(classTaskService, schTaskService);

        Page<SchTask> taskPage = new Page<>(1, 10, 0);
        taskPage.setRecords(List.of());
        when(schTaskService.page(org.mockito.ArgumentMatchers.any(Page.class), org.mockito.ArgumentMatchers.any(Wrapper.class))).thenReturn(taskPage);

        ServerResponse<?> response = controller.page("2025-2026-1", 1, 10);

        assertTrue(response.isSuccess());
        assertInstanceOf(IPage.class, response.getData());
        IPage<?> page = (IPage<?>) response.getData();
        assertEquals(0, page.getTotal());
    }

    @Test
    void save_shouldOnlyPersistStandardTask() {
        ScheduleTaskController controller = new ScheduleTaskController(classTaskService, schTaskService);

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

        ServerResponse<?> response = controller.save(request);

        assertTrue(response.isSuccess());
    }

    @Test
    void delete_shouldOnlyDeleteStandardTask() {
        ScheduleTaskController controller = new ScheduleTaskController(classTaskService, schTaskService);

        SchTask task = new SchTask();
        task.setId(101L);
        task.setDeleted(0);

        when(schTaskService.getById(101L)).thenReturn(task);
        when(schTaskService.removeById(101L)).thenReturn(true);

        ServerResponse<?> response = controller.delete(101);

        assertTrue(response.isSuccess());
    }

}
