package com.lyk.coursearrange.service.impl;

import com.lyk.coursearrange.dao.CoursePlanDao;
import com.lyk.coursearrange.entity.CoursePlan;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class CoursePlanLegacySupportTest {

    @Mock
    private CoursePlanDao coursePlanDao;

    @Test
    void replaceCoursePlans_shouldReturnFalseWhenLegacyTableMissing() {
        CoursePlanLegacySupport support = new CoursePlanLegacySupport();
        ReflectionTestUtils.setField(support, "coursePlanDao", coursePlanDao);
        doThrow(new RuntimeException("Table 'course_arrange_v2.tb_course_plan' doesn't exist"))
                .when(coursePlanDao)
                .deleteBySemester(anyString());

        boolean success = support.replaceCoursePlans("2025-2026-1", List.of(new CoursePlan()));

        assertFalse(success);
    }
}
