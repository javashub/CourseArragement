package com.lyk.coursearrange.auth.service;

import com.lyk.coursearrange.auth.model.LoginUser;
import com.lyk.coursearrange.auth.vo.AuthContextVO;
import com.lyk.coursearrange.system.rbac.vo.CurrentUserVO;
import com.lyk.coursearrange.system.rbac.vo.MenuTreeVO;

import java.util.List;

/**
 * 认证门面服务。
 * 场景说明：
 * 1. 当前系统同时存在旧登录模型和新 RBAC 模型。
 * 2. 控制器如果直接感知这两套模型会很乱。
 * 3. 这里使用 Facade 模式，对外统一暴露“当前用户、权限、菜单”查询入口。
 */
public interface AuthFacadeService {

    LoginUser getCurrentLoginUser();

    CurrentUserVO getCurrentUserView();

    AuthContextVO getCurrentAuthContext();

    List<String> getCurrentPermissionCodes();

    List<MenuTreeVO> getCurrentMenus();
}
