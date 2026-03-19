package com.lyk.coursearrange.service.impl;

import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.dao.ClassTaskDao;
import com.lyk.coursearrange.entity.ClassTask;
import com.lyk.coursearrange.schedule.entity.SchTask;
import com.lyk.coursearrange.schedule.service.SchTaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClassTaskServiceImplTest {

    @Mock
    private SchTaskService schTaskService;
    @Mock
    private ClassTaskDao classTaskDao;

    @Test
    void ensureLegacyTasksForSemester_shouldIgnoreMissingLegacyTaskTable() {
        ClassTaskServiceImpl service = spy(new ClassTaskServiceImpl());
        ReflectionTestUtils.setField(service, "schTaskService", schTaskService);
        doThrow(new RuntimeException("Table 'course_arrange_v2.tb_class_task' doesn't exist"))
                .when(service)
                .count(any());

        assertDoesNotThrow(() -> service.ensureLegacyTasksForSemester("2025-2026-1"));

        verifyNoInteractions(schTaskService);
    }

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
        ReflectionTestUtils.setField(service, "classTaskDao", classTaskDao);
        when(schTaskService.list(org.mockito.ArgumentMatchers.<com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SchTask>>any()))
                .thenReturn(List.of());

        List<ClassTask> tasks = service.listSchedulingTasks("2025-2026-1");

        assertTrue(tasks.isEmpty());
        verify(service, never()).ensureLegacyTasksForSemester(anyString());
        verify(classTaskDao, never()).selectList(any());
    }
}
