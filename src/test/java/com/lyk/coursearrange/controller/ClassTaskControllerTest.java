package com.lyk.coursearrange.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.ClassTask;
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
import static org.mockito.ArgumentMatchers.any;
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
    void queryClassTask_shouldReturnStandardTasksWhenLegacyTaskTableMissing() {
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

        when(schTaskService.page(any(Page.class), any(Wrapper.class))).thenReturn(taskPage);
        when(classTaskService.list(any(Wrapper.class))).thenThrow(new RuntimeException("Table 'course_arrange_v2.tb_class_task' doesn't exist"));

        ServerResponse response = controller.queryClassTask(1, "2025-2026-1", 10);

        assertTrue(response.isSuccess());
        assertInstanceOf(IPage.class, response.getData());
        IPage<?> page = (IPage<?>) response.getData();
        assertEquals(1, page.getTotal());
        ScheduleTaskPageVO vo = (ScheduleTaskPageVO) page.getRecords().get(0);
        assertEquals(Long.valueOf(101L), vo.getStandardId());
        assertEquals("C1", vo.getClassNo());
    }

}
