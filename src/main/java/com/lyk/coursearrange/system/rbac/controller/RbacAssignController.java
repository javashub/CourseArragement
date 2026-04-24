package com.lyk.coursearrange.system.rbac.controller;

import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.system.rbac.request.BatchUserRoleAssignRequest;
import com.lyk.coursearrange.system.rbac.request.RoleMenuAssignRequest;
import com.lyk.coursearrange.system.rbac.request.RolePermissionAssignRequest;
import com.lyk.coursearrange.system.rbac.request.UserRoleAssignRequest;
import com.lyk.coursearrange.system.rbac.service.RbacAssignService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * RBAC 授权控制器。
 */
@RestController
@RequestMapping("/api/rbac/assign")
public class RbacAssignController {

    private final RbacAssignService rbacAssignService;

    public RbacAssignController(RbacAssignService rbacAssignService) {
        this.rbacAssignService = rbacAssignService;
    }

    @PostMapping("/user-roles")
    public ServerResponse<?> assignUserRoles(@Validated @RequestBody UserRoleAssignRequest request) {
        rbacAssignService.assignRolesToUser(request);
        return ServerResponse.ofSuccess("分配用户角色成功");
    }

    @PostMapping("/user-roles/batch")
    public ServerResponse<?> batchAssignUserRoles(@Validated @RequestBody BatchUserRoleAssignRequest request) {
        rbacAssignService.batchAssignRolesToUsers(request);
        return ServerResponse.ofSuccess("批量分配角色成功");
    }

    @PostMapping("/role-menus")
    public ServerResponse<?> assignRoleMenus(@Validated @RequestBody RoleMenuAssignRequest request) {
        rbacAssignService.assignMenusToRole(request);
        return ServerResponse.ofSuccess("分配角色菜单成功");
    }

    @PostMapping("/role-permissions")
    public ServerResponse<?> assignRolePermissions(@Validated @RequestBody RolePermissionAssignRequest request) {
        rbacAssignService.assignPermissionsToRole(request);
        return ServerResponse.ofSuccess("分配角色权限成功");
    }

    @GetMapping("/user-roles/{userId}")
    public ServerResponse<?> assignedUserRoles(@PathVariable("userId") Long userId) {
        return ServerResponse.ofSuccess(rbacAssignService.getAssignedRoleIds(userId));
    }

    @GetMapping("/role-menus/{roleId}")
    public ServerResponse<?> assignedRoleMenus(@PathVariable("roleId") Long roleId) {
        return ServerResponse.ofSuccess(rbacAssignService.getAssignedMenuIds(roleId));
    }

    @GetMapping("/role-permissions/{roleId}")
    public ServerResponse<?> assignedRolePermissions(@PathVariable("roleId") Long roleId) {
        return ServerResponse.ofSuccess(rbacAssignService.getAssignedPermissionIds(roleId));
    }
}
