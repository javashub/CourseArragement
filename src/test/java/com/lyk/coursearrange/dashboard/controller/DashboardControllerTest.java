package com.lyk.coursearrange.dashboard.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.dashboard.service.DashboardOverviewService;
import com.lyk.coursearrange.dashboard.vo.DashboardOverviewVO;
import com.lyk.coursearrange.dashboard.vo.DashboardResourceCountsVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private DashboardOverviewService dashboardOverviewService;

    @Test
    void overview_shouldReturnAggregateFromService() {
        DashboardController controller = new DashboardController(dashboardOverviewService);
        DashboardOverviewVO overview = new DashboardOverviewVO();
        overview.setSemester("2025-2026-1");
        when(dashboardOverviewService.getOverview(null)).thenReturn(overview);

        ServerResponse<?> response = controller.overview(null);

        assertTrue(response.isSuccess());
        assertSame(overview, response.getData());
    }

    @Test
    void overview_shouldPassClassNoOverrideToService() {
        DashboardController controller = new DashboardController(dashboardOverviewService);
        DashboardOverviewVO overview = new DashboardOverviewVO();
        when(dashboardOverviewService.getOverview("C2")).thenReturn(overview);

        ServerResponse<?> response = controller.overview("C2");

        assertTrue(response.isSuccess());
        verify(dashboardOverviewService).getOverview("C2");
    }

    @Test
    void resourceCounts_shouldSerializeClassCountAsClassKey() throws Exception {
        DashboardResourceCountsVO counts = new DashboardResourceCountsVO();
        counts.setTeacher(3L);
        counts.setClassCount(2L);
        counts.setClassroom(4L);

        String json = new ObjectMapper().writeValueAsString(counts);

        assertTrue(json.contains("\"class\":2"), json);
        assertFalse(json.contains("classCount"), json);
        assertEquals(3L, counts.getTeacher());
    }

    @Test
    void overview_shouldDeclareDashboardPermission() throws Exception {
        Method method = DashboardController.class.getMethod("overview", String.class);
        SaCheckPermission permission = method.getAnnotation(SaCheckPermission.class);

        assertNotNull(permission);
        assertArrayEquals(new String[]{"page:dashboard:view"}, permission.value());
    }
}
