package com.lyk.coursearrange.service.impl;

import com.lyk.coursearrange.schedule.service.SchTaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ClassTaskServiceImplTest {

    @Mock
    private SchTaskService schTaskService;

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
}
