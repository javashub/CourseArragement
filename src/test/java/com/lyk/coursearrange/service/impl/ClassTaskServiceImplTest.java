package com.lyk.coursearrange.service.impl;

import com.lyk.coursearrange.dao.CoursePlanDao;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.CoursePlan;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ClassTaskServiceImplTest {

    @Mock
    private SchTaskService schTaskService;
    @Mock
    private CoursePlanDao coursePlanDao;

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
    void replaceLegacyCoursePlans_shouldReturnFalseWhenLegacyTableMissing() {
        ClassTaskServiceImpl service = new ClassTaskServiceImpl();
        ReflectionTestUtils.setField(service, "coursePlanDao", coursePlanDao);
        doThrow(new RuntimeException("Table 'course_arrange_v2.tb_course_plan' doesn't exist"))
                .when(coursePlanDao)
                .deleteBySemester(anyString());

        boolean success = service.replaceLegacyCoursePlans("2025-2026-1", List.of(new CoursePlan()));

        assertFalse(success);
    }

    @Test
    void buildSchedulingSuccessResponse_shouldExposeDegradedStatus() {
        ClassTaskServiceImpl service = new ClassTaskServiceImpl();

        ServerResponse<Map<String, Object>> response = service.buildSchedulingSuccessResponse(123L, 8, false);

        assertTrue(response.isSuccess());
        assertEquals("排课成功，标准课表已生成，legacy 课表副本未写入，耗时：123ms", response.getMessage());
        assertEquals(Boolean.FALSE, response.getData().get("legacyCoursePlanSaved"));
        assertEquals(8, response.getData().get("generatedPlanCount"));
        assertEquals(123L, response.getData().get("durationMs"));
    }
}
