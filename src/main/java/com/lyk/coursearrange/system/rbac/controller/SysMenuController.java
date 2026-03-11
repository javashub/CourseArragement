package com.lyk.coursearrange.system.rbac.controller;

import com.lyk.coursearrange.auth.service.AuthFacadeService;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.system.rbac.request.SysMenuSaveRequest;
import com.lyk.coursearrange.system.rbac.service.RbacResourceWriteService;
import com.lyk.coursearrange.system.rbac.service.SysMenuService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    private final RbacResourceWriteService resourceWriteService;

    public SysMenuController(SysMenuService menuService,
                             AuthFacadeService authFacadeService,
                             RbacResourceWriteService resourceWriteService) {
        this.menuService = menuService;
        this.authFacadeService = authFacadeService;
        this.resourceWriteService = resourceWriteService;
    }

    @GetMapping("/tree")
    public ServerResponse<?> tree() {
        return ServerResponse.ofSuccess(menuService.listEnabledMenuTree());
    }

    @GetMapping("/current-tree")
    public ServerResponse<?> currentTree() {
        return ServerResponse.ofSuccess(authFacadeService.getCurrentMenus());
    }

    @PostMapping
    public ServerResponse<?> save(@Validated @RequestBody SysMenuSaveRequest request) {
        return ServerResponse.ofSuccess(resourceWriteService.saveMenu(request));
    }

    @PostMapping("/{menuId}/status/{status}")
    public ServerResponse<?> changeStatus(@PathVariable("menuId") Long menuId,
                                          @PathVariable("status") Integer status) {
        resourceWriteService.changeMenuStatus(menuId, status);
        return ServerResponse.ofSuccess("修改菜单状态成功");
    }

    @DeleteMapping("/{menuId}")
    public ServerResponse<?> delete(@PathVariable("menuId") Long menuId) {
        resourceWriteService.deleteMenu(menuId);
        return ServerResponse.ofSuccess("删除菜单成功");
    }
}
