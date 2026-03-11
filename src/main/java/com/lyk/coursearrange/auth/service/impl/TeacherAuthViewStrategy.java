package com.lyk.coursearrange.auth.service.impl;

import com.lyk.coursearrange.auth.model.LoginUser;
import com.lyk.coursearrange.auth.service.AuthViewStrategy;
import com.lyk.coursearrange.common.constants.SystemConstants;
import com.lyk.coursearrange.system.rbac.vo.MenuTreeVO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 教师菜单权限策略。
 */
@Component
public class TeacherAuthViewStrategy implements AuthViewStrategy {

    @Override
    public boolean supports(String roleCode) {
        return SystemConstants.RoleCode.TEACHER.equals(roleCode);
    }

    @Override
    public List<String> buildPermissions(LoginUser loginUser) {
        return List.of(
                "page:teacher-home:view",
                "page:teacher-timetable:view"
        );
    }

    @Override
    public List<MenuTreeVO> buildMenus(LoginUser loginUser) {
        return List.of(
                MenuTreeVO.builder()
                        .id(101L)
                        .menuCode("teacher-home")
                        .parentId(0L)
                        .menuName("工作台")
                        .menuType("MENU")
                        .routeName("TeacherDashboard")
                        .routePath("/teacher/dashboard")
                        .componentPath("views/teacher/dashboard/index.vue")
                        .icon("House")
                        .permissionCode("page:teacher-home:view")
                        .isHidden(0)
                        .isKeepAlive(0)
                        .sortNo(1)
                        .children(List.of())
                        .build(),
                MenuTreeVO.builder()
                        .id(102L)
                        .menuCode("teacher-timetable")
                        .parentId(0L)
                        .menuName("我的课表")
                        .menuType("MENU")
                        .routeName("TeacherTimetable")
                        .routePath("/teacher/timetable")
                        .componentPath("views/teacher/timetable/index.vue")
                        .icon("Calendar")
                        .permissionCode("page:teacher-timetable:view")
                        .isHidden(0)
                        .isKeepAlive(0)
                        .sortNo(2)
                        .children(List.of())
                        .build()
        );
    }
}
