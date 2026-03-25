package com.lyk.coursearrange.auth.service.impl;

import com.lyk.coursearrange.system.rbac.vo.MenuTreeVO;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminAuthViewStrategyTest {

    private final AdminAuthViewStrategy strategy = new AdminAuthViewStrategy();

    @Test
    void shouldExposeCoreAdminPermissions() {
        List<String> permissions = strategy.buildPermissions(null);

        assertTrue(permissions.contains("page:course-plan:view"));
        assertTrue(permissions.contains("page:schedule:view"));
        assertTrue(permissions.contains("page:teacher:view"));
        assertTrue(permissions.contains("page:student:view"));
        assertTrue(permissions.contains("page:course:view"));
        assertTrue(permissions.contains("page:classroom:view"));
    }

    @Test
    void shouldExposeGroupedAdminMenus() {
        List<MenuTreeVO> menus = strategy.buildMenus(null);
        List<String> routePaths = new ArrayList<>();
        collectRoutePaths(menus, routePaths);
        MenuTreeVO baseDataMenu = menus.stream()
                .filter(menu -> "base-data".equals(menu.getMenuCode()))
                .findFirst()
                .orElse(null);

        assertTrue(routePaths.contains("/course-plan"));
        assertTrue(routePaths.contains("/schedule"));
        assertTrue(routePaths.contains("/base-data/teachers"));
        assertTrue(routePaths.contains("/base-data/students"));
        assertTrue(routePaths.contains("/base-data/courses"));
        assertTrue(routePaths.contains("/base-data/classrooms"));
        assertTrue(routePaths.contains("/guide"));
        assertNotNull(baseDataMenu);
        assertEquals("CATALOG", baseDataMenu.getMenuType());
        assertEquals(4, baseDataMenu.getChildren().size());
    }

    private void collectRoutePaths(List<MenuTreeVO> menus, List<String> routePaths) {
        for (MenuTreeVO menu : menus) {
            if (menu.getRoutePath() != null) {
                routePaths.add(menu.getRoutePath());
            }
            collectRoutePaths(menu.getChildren(), routePaths);
        }
    }
}
