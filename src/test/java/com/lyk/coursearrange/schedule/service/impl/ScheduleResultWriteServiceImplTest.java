package com.lyk.coursearrange.schedule.service.impl;

import com.lyk.coursearrange.resource.entity.ResClassroom;
import com.lyk.coursearrange.resource.service.ResClassroomService;
import com.lyk.coursearrange.schedule.engine.model.SchedulingAssignment;
import com.lyk.coursearrange.schedule.entity.SchScheduleResult;
import com.lyk.coursearrange.schedule.service.SchScheduleResultService;
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
    private SchScheduleResultService resultService;
    @Mock
    private ResClassroomService resClassroomService;

    @Test
    void replaceScheduleResults_shouldResolveClassroomIdFromStandardClassroomCode() {
        ScheduleResultWriteServiceImpl service = new ScheduleResultWriteServiceImpl(
                resultService,
                resClassroomService
        );

        SchedulingAssignment assignment = SchedulingAssignment.builder()
                .taskId(11L)
                .taskCode("TK-11")
                .classNo("25010001")
                .courseNo("100001")
                .teacherNo("T0001")
                .timeSlotCode("01")
                .weekdayNo(1)
                .periodNo(1)
                .classroomCode("A101")
                .build();

        ResClassroom classroom = new ResClassroom();
        classroom.setId(21L);
        classroom.setClassroomCode("A101");

        when(resClassroomService.list(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ResClassroom>>any()))
                .thenReturn(List.of(classroom));

        service.replaceScheduleResults("2025-2026-1", 9001L, List.of(), List.of(assignment));

        ArgumentCaptor<SchScheduleResult> captor = ArgumentCaptor.forClass(SchScheduleResult.class);
        verify(resultService).save(captor.capture());
        assertEquals(11L, captor.getValue().getTaskId());
        assertEquals(21L, captor.getValue().getClassroomId());
        assertEquals(9001L, captor.getValue().getRunLogId());
    }
}
