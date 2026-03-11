package com.lyk.coursearrange.system.rbac.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.system.rbac.entity.SysRole;
import com.lyk.coursearrange.system.rbac.query.SysRoleQuery;
import com.lyk.coursearrange.system.rbac.service.SysRoleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 角色控制器。
 */
@RestController
@RequestMapping("/api/rbac/roles")
public class SysRoleController {

    private final SysRoleService roleService;

    public SysRoleController(SysRoleService roleService) {
        this.roleService = roleService;
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
}
