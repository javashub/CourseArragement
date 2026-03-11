package com.lyk.coursearrange.system.rbac.controller;

import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.system.rbac.request.RoleMenuAssignRequest;
import com.lyk.coursearrange.system.rbac.request.RolePermissionAssignRequest;
import com.lyk.coursearrange.system.rbac.request.UserRoleAssignRequest;
import com.lyk.coursearrange.system.rbac.service.RbacAssignService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
