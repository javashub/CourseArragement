package com.lyk.coursearrange.auth.service;

import com.lyk.coursearrange.system.rbac.entity.SysUser;

/**
 * 统一登录服务。
 */
public interface AuthLoginService {

    SysUser loginAdmin(String username, String password);

    SysUser loginTeacher(String username, String password);

    SysUser loginStudent(String username, String password);
}
