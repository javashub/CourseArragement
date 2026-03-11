package com.lyk.coursearrange.auth.service.impl;

import com.lyk.coursearrange.auth.model.LoginUser;
import com.lyk.coursearrange.auth.service.AuthViewStrategy;
import com.lyk.coursearrange.common.constants.SystemConstants;
import com.lyk.coursearrange.system.rbac.vo.MenuTreeVO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 学生菜单权限策略。
 */
@Component
public class StudentAuthViewStrategy implements AuthViewStrategy {

    @Override
    public boolean supports(String roleCode) {
        return SystemConstants.RoleCode.STUDENT.equals(roleCode);
    }

    @Override
    public List<String> buildPermissions(LoginUser loginUser) {
        return List.of(
                "page:student-home:view",
                "page:student-timetable:view"
        );
    }

    @Override
    public List<MenuTreeVO> buildMenus(LoginUser loginUser) {
        return List.of(
                MenuTreeVO.builder()
                        .id(201L)
                        .menuCode("student-home")
                        .parentId(0L)
                        .menuName("工作台")
                        .menuType("MENU")
                        .routeName("StudentDashboard")
                        .routePath("/student/dashboard")
                        .componentPath("views/student/dashboard/index.vue")
                        .icon("House")
                        .permissionCode("page:student-home:view")
                        .isHidden(0)
                        .isKeepAlive(0)
                        .sortNo(1)
                        .children(List.of())
                        .build(),
                MenuTreeVO.builder()
                        .id(202L)
                        .menuCode("student-timetable")
                        .parentId(0L)
                        .menuName("我的课表")
                        .menuType("MENU")
                        .routeName("StudentTimetable")
                        .routePath("/student/timetable")
                        .componentPath("views/student/timetable/index.vue")
                        .icon("Calendar")
                        .permissionCode("page:student-timetable:view")
                        .isHidden(0)
                        .isKeepAlive(0)
                        .sortNo(2)
                        .children(List.of())
                        .build()
        );
    }
}
