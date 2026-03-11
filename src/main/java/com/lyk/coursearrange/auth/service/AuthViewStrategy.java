package com.lyk.coursearrange.auth.service;

import com.lyk.coursearrange.auth.model.LoginUser;
import com.lyk.coursearrange.system.rbac.vo.MenuTreeVO;

import java.util.List;

/**
 * 认证视图策略。
 * 场景说明：
 * 1. 当前旧登录链路还没有完全切到 RBAC 表。
 * 2. 不同角色对应的默认菜单和权限完全不同。
 * 3. 这里使用 Strategy 模式，将管理员、教师、学生的菜单权限组装逻辑拆开管理。
 */
public interface AuthViewStrategy {

    boolean supports(String roleCode);

    List<String> buildPermissions(LoginUser loginUser);

    List<MenuTreeVO> buildMenus(LoginUser loginUser);
}
