package com.lyk.coursearrange.system.rbac.service;

import com.lyk.coursearrange.system.rbac.request.RoleMenuAssignRequest;
import com.lyk.coursearrange.system.rbac.request.RolePermissionAssignRequest;
import com.lyk.coursearrange.system.rbac.request.UserRoleAssignRequest;

/**
 * RBAC 授权写服务。
 * 场景说明：
 * 1. 用户分配角色、角色分配菜单、角色分配权限都属于授权写操作。
 * 2. 这里使用应用服务统一收口，便于事务控制和后续审计扩展。
 */
public interface RbacAssignService {

    void assignRolesToUser(UserRoleAssignRequest request);

    void assignMenusToRole(RoleMenuAssignRequest request);

    void assignPermissionsToRole(RolePermissionAssignRequest request);
}
