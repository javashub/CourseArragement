package com.lyk.coursearrange.auth.controller;

import com.lyk.coursearrange.auth.service.AuthFacadeService;
import com.lyk.coursearrange.common.ServerResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证信息控制器。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthFacadeService authFacadeService;

    public AuthController(AuthFacadeService authFacadeService) {
        this.authFacadeService = authFacadeService;
    }

    @GetMapping("/me")
    public ServerResponse<?> me() {
        return ServerResponse.ofSuccess(authFacadeService.getCurrentUserView());
    }

    @GetMapping("/context")
    public ServerResponse<?> context() {
        return ServerResponse.ofSuccess(authFacadeService.getCurrentAuthContext());
    }

    @GetMapping("/permissions")
    public ServerResponse<?> permissions() {
        return ServerResponse.ofSuccess(authFacadeService.getCurrentPermissionCodes());
    }

    @GetMapping("/menus")
    public ServerResponse<?> menus() {
        return ServerResponse.ofSuccess(authFacadeService.getCurrentMenus());
    }
}
