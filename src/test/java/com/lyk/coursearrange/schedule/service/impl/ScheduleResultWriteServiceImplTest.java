package com.lyk.coursearrange.schedule.service.impl;

import com.lyk.coursearrange.entity.CoursePlan;
import com.lyk.coursearrange.resource.entity.ResClassroom;
import com.lyk.coursearrange.resource.service.ResClassroomService;
import com.lyk.coursearrange.schedule.entity.SchScheduleResult;
import com.lyk.coursearrange.schedule.entity.SchTask;
import com.lyk.coursearrange.schedule.service.SchScheduleResultService;
import com.lyk.coursearrange.schedule.service.SchTaskService;
import com.lyk.coursearrange.schedule.vo.SchedulingTaskInput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleResultWriteServiceImplTest {

    @Mock
    private SchTaskService schTaskService;
    @Mock
    private SchScheduleResultService resultService;
    @Mock
    private ResClassroomService resClassroomService;

    @Test
    void replaceScheduleResults_shouldResolveClassroomIdFromStandardClassroomCode() {
        ScheduleResultWriteServiceImpl service = new ScheduleResultWriteServiceImpl(
                schTaskService,
                resultService,
                resClassroomService
        );

        SchedulingTaskInput taskInput = new SchedulingTaskInput();
        taskInput.setSemester("2025-2026-1");
        taskInput.setClassNo("25010001");
        taskInput.setCourseNo("100001");
        taskInput.setTeacherNo("T0001");

        CoursePlan coursePlan = new CoursePlan();
        coursePlan.setClassNo("25010001");
        coursePlan.setCourseNo("100001");
        coursePlan.setTeacherNo("T0001");
        coursePlan.setClassTime("01");
        coursePlan.setClassroomNo("A101");

        SchTask schTask = new SchTask();
        schTask.setId(11L);

        ResClassroom classroom = new ResClassroom();
        classroom.setId(21L);
        classroom.setClassroomCode("A101");

        when(schTaskService.getOne(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SchTask>>any(), eq(false)))
                .thenReturn(schTask);
        when(resClassroomService.list(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ResClassroom>>any()))
                .thenReturn(List.of(classroom));

        service.replaceScheduleResults("2025-2026-1", List.of(taskInput), List.of(coursePlan));

        ArgumentCaptor<SchScheduleResult> captor = ArgumentCaptor.forClass(SchScheduleResult.class);
        verify(resultService).save(captor.capture());
        assertEquals(11L, captor.getValue().getTaskId());
        assertEquals(21L, captor.getValue().getClassroomId());
    }
}
