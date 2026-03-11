package com.lyk.coursearrange.system.rbac.controller;

import com.lyk.coursearrange.auth.service.AuthFacadeService;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.system.rbac.service.SysMenuService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 菜单控制器。
 * 步骤说明：
 * 1. 提供全量启用菜单树，便于管理员端做菜单配置。
 * 2. 提供当前登录用户菜单树，便于前端动态路由直接消费。
 */
@RestController
@RequestMapping("/api/rbac/menus")
public class SysMenuController {

    private final SysMenuService menuService;
    private final AuthFacadeService authFacadeService;

    public SysMenuController(SysMenuService menuService, AuthFacadeService authFacadeService) {
        this.menuService = menuService;
        this.authFacadeService = authFacadeService;
    }

    @GetMapping("/tree")
    public ServerResponse<?> tree() {
        return ServerResponse.ofSuccess(menuService.listEnabledMenuTree());
    }

    @GetMapping("/current-tree")
    public ServerResponse<?> currentTree() {
        return ServerResponse.ofSuccess(authFacadeService.getCurrentMenus());
    }
}
