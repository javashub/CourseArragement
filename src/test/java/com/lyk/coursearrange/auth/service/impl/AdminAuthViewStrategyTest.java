package com.lyk.coursearrange.auth.service.impl;

import com.lyk.coursearrange.system.rbac.vo.MenuTreeVO;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminAuthViewStrategyTest {

    private final AdminAuthViewStrategy strategy = new AdminAuthViewStrategy();

    @Test
    void shouldExposeCoreAdminPermissions() {
        List<String> permissions = strategy.buildPermissions(null);

        assertTrue(permissions.contains("page:course-plan:view"));
        assertTrue(permissions.contains("page:schedule:view"));
        assertTrue(permissions.contains("page:base-data:view"));
    }

    @Test
    void shouldExposeCoreAdminMenus() {
        List<MenuTreeVO> menus = strategy.buildMenus(null);
        List<String> routePaths = new ArrayList<>();
        collectRoutePaths(menus, routePaths);

        assertTrue(routePaths.contains("/course-plan"));
        assertTrue(routePaths.contains("/schedule"));
        assertTrue(routePaths.contains("/base-data"));
        assertTrue(routePaths.contains("/guide"));
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
