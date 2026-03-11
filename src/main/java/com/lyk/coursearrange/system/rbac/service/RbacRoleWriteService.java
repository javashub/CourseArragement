package com.lyk.coursearrange.system.rbac.service;

import com.lyk.coursearrange.system.rbac.entity.SysRole;
import com.lyk.coursearrange.system.rbac.request.SysRoleSaveRequest;

/**
 * RBAC 角色写服务。
 */
public interface RbacRoleWriteService {

    SysRole saveRole(SysRoleSaveRequest request);

    void changeRoleStatus(Long roleId, Integer status);

    void deleteRole(Long roleId);
}
