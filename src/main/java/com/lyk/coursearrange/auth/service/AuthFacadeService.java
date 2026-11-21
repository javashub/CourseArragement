package com.lyk.coursearrange.auth.service;

import com.lyk.coursearrange.auth.model.LoginUser;
import com.lyk.coursearrange.auth.vo.AuthContextVO;
import com.lyk.coursearrange.system.rbac.vo.CurrentUserVO;
import com.lyk.coursearrange.system.rbac.vo.MenuTreeVO;

import java.util.List;

/**
 * 认证门面服务。
 * 场景说明：
 * 1. 控制器只需要感知当前用户、权限、菜单这些认证结果。
 * 2. 这里使用 Facade 模式，屏蔽底层 RBAC 查询细节。
 * 3. 对外统一暴露“当前用户、权限、菜单”查询入口。
 */
public interface AuthFacadeService {

    LoginUser getCurrentLoginUser();

    CurrentUserVO getCurrentUserView();

    AuthContextVO getCurrentAuthContext();

    List<String> getCurrentPermissionCodes();

    List<MenuTreeVO> getCurrentMenus();

    /**
     * 保存当前登录用户的界面主题偏好，并返回更新后的当前用户视图。
     *
     * @param uiTheme 主题标识，仅接受 daylight / deep-teal / top-nav
     * @return 更新后的当前用户视图
     */
    CurrentUserVO updateCurrentUiTheme(String uiTheme);
}
