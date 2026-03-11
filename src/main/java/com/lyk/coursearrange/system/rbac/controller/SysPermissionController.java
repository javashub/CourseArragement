package com.lyk.coursearrange.system.rbac.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.system.rbac.entity.SysPermission;
import com.lyk.coursearrange.system.rbac.service.SysPermissionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 权限点控制器。
 * 步骤说明：
 * 1. 提供权限点列表，供角色分配权限页面加载。
 * 2. 当前阶段按启用状态和权限类型做简单筛选，后续再补分页与维护能力。
 */
@RestController
@RequestMapping("/api/rbac/permissions")
public class SysPermissionController {

    private final SysPermissionService permissionService;

    public SysPermissionController(SysPermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping("/list")
    public ServerResponse<?> list(@RequestParam(value = "permissionType", required = false) String permissionType,
                                  @RequestParam(value = "status", required = false) Integer status) {
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPermission::getDeleted, 0)
                .eq(status != null, SysPermission::getStatus, status)
                .eq(StringUtils.isNotBlank(permissionType), SysPermission::getPermissionType, permissionType)
                .orderByAsc(SysPermission::getPermissionType)
                .orderByAsc(SysPermission::getPermissionCode)
                .orderByAsc(SysPermission::getId);
        return ServerResponse.ofSuccess(permissionService.list(wrapper));
    }
}
