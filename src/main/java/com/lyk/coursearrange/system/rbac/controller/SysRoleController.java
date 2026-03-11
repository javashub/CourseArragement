package com.lyk.coursearrange.system.rbac.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.system.rbac.entity.SysRole;
import com.lyk.coursearrange.system.rbac.query.SysRoleQuery;
import com.lyk.coursearrange.system.rbac.request.SysRoleSaveRequest;
import com.lyk.coursearrange.system.rbac.service.RbacRoleWriteService;
import com.lyk.coursearrange.system.rbac.service.SysRoleService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 角色控制器。
 */
@RestController
@RequestMapping("/api/rbac/roles")
public class SysRoleController {

    private final SysRoleService roleService;
    private final RbacRoleWriteService roleWriteService;

    public SysRoleController(SysRoleService roleService, RbacRoleWriteService roleWriteService) {
        this.roleService = roleService;
        this.roleWriteService = roleWriteService;
    }

    @GetMapping("/list")
    public ServerResponse<?> list(@ModelAttribute SysRoleQuery query) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getDeleted, 0)
                .eq(query.getStatus() != null, SysRole::getStatus, query.getStatus())
                .orderByAsc(SysRole::getSortNo)
                .orderByAsc(SysRole::getRoleCode);
        return ServerResponse.ofSuccess(roleService.list(wrapper));
    }

    @PostMapping
    public ServerResponse<?> save(@Validated @RequestBody SysRoleSaveRequest request) {
        return ServerResponse.ofSuccess(roleWriteService.saveRole(request));
    }

    @PostMapping("/{roleId}/status/{status}")
    public ServerResponse<?> changeStatus(@PathVariable("roleId") Long roleId,
                                          @PathVariable("status") Integer status) {
        roleWriteService.changeRoleStatus(roleId, status);
        return ServerResponse.ofSuccess("修改角色状态成功");
    }

    @DeleteMapping("/{roleId}")
    public ServerResponse<?> delete(@PathVariable("roleId") Long roleId) {
        roleWriteService.deleteRole(roleId);
        return ServerResponse.ofSuccess("删除角色成功");
    }
}
