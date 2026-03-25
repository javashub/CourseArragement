package com.lyk.coursearrange.auth.service;

import com.lyk.coursearrange.system.rbac.entity.SysUser;

/**
 * 统一登录服务。
 */
public interface AuthLoginService {

    SysUser login(String username, String password, String userType);
}
