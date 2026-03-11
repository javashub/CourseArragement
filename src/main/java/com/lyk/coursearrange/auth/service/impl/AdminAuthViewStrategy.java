package com.lyk.coursearrange.auth.service.impl;

import com.lyk.coursearrange.auth.model.LoginUser;
import com.lyk.coursearrange.auth.service.AuthViewStrategy;
import com.lyk.coursearrange.common.constants.SystemConstants;
import com.lyk.coursearrange.system.rbac.vo.MenuTreeVO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 管理员菜单权限策略。
 */
@Component
public class AdminAuthViewStrategy implements AuthViewStrategy {

    @Override
    public boolean supports(String roleCode) {
        return SystemConstants.RoleCode.ADMIN.equals(roleCode);
    }

    @Override
    public List<String> buildPermissions(LoginUser loginUser) {
        return List.of(
                "page:dashboard:view",
                "page:campus:view",
                "page:college:view",
                "page:stage:view",
                "page:config:view",
                "page:rbac:view",
                "btn:config:edit",
                "btn:timetable:drag-adjust"
        );
    }

    @Override
    public List<MenuTreeVO> buildMenus(LoginUser loginUser) {
        return List.of(
                MenuTreeVO.builder()
                        .id(1L)
                        .menuCode("dashboard")
                        .parentId(0L)
                        .menuName("工作台")
                        .menuType("MENU")
                        .routeName("Dashboard")
                        .routePath("/dashboard")
                        .componentPath("views/dashboard/DashboardView.vue")
                        .icon("House")
                        .permissionCode("page:dashboard:view")
                        .isHidden(0)
                        .isKeepAlive(0)
                        .sortNo(1)
                        .children(List.of())
                        .build(),
                MenuTreeVO.builder()
                        .id(2L)
                        .menuCode("organization")
                        .parentId(0L)
                        .menuName("组织架构")
                        .menuType("CATALOG")
                        .routeName("Organization")
                        .routePath("/organization")
                        .componentPath("layouts/BasicLayout.vue")
                        .icon("OfficeBuilding")
                        .isHidden(0)
                        .isKeepAlive(0)
                        .sortNo(2)
                        .children(List.of(
                                MenuTreeVO.builder()
                                        .id(21L)
                                        .menuCode("campus")
                                        .parentId(2L)
                                        .menuName("校区管理")
                                        .menuType("MENU")
                                        .routeName("CampusPage")
                                        .routePath("/organization/campus")
                                        .componentPath("views/organization/campus/index.vue")
                                        .permissionCode("page:campus:view")
                                        .isHidden(0)
                                        .isKeepAlive(0)
                                        .sortNo(1)
                                        .children(List.of())
                                        .build(),
                                MenuTreeVO.builder()
                                        .id(22L)
                                        .menuCode("college")
                                        .parentId(2L)
                                        .menuName("学院管理")
                                        .menuType("MENU")
                                        .routeName("CollegePage")
                                        .routePath("/organization/college")
                                        .componentPath("views/organization/college/index.vue")
                                        .permissionCode("page:college:view")
                                        .isHidden(0)
                                        .isKeepAlive(0)
                                        .sortNo(2)
                                        .children(List.of())
                                        .build(),
                                MenuTreeVO.builder()
                                        .id(23L)
                                        .menuCode("stage")
                                        .parentId(2L)
                                        .menuName("学段管理")
                                        .menuType("MENU")
                                        .routeName("StagePage")
                                        .routePath("/organization/stage")
                                        .componentPath("views/organization/stage/index.vue")
                                        .permissionCode("page:stage:view")
                                        .isHidden(0)
                                        .isKeepAlive(0)
                                        .sortNo(3)
                                        .children(List.of())
                                        .build()
                        ))
                        .build(),
                MenuTreeVO.builder()
                        .id(3L)
                        .menuCode("system-config")
                        .parentId(0L)
                        .menuName("系统配置")
                        .menuType("MENU")
                        .routeName("SystemConfigPage")
                        .routePath("/system/config")
                        .componentPath("views/system/config/index.vue")
                        .icon("Setting")
                        .permissionCode("page:config:view")
                        .isHidden(0)
                        .isKeepAlive(0)
                        .sortNo(3)
                        .children(List.of())
                        .build()
        );
    }
}
