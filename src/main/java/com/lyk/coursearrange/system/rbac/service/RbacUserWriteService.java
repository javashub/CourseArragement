package com.lyk.coursearrange.system.rbac.service;

import com.lyk.coursearrange.system.rbac.entity.SysUser;
import com.lyk.coursearrange.system.rbac.request.SysUserSaveRequest;

/**
 * RBAC 用户写服务。
 */
public interface RbacUserWriteService {

    SysUser saveUser(SysUserSaveRequest request);

    void changeUserStatus(Long userId, Integer status);
}
