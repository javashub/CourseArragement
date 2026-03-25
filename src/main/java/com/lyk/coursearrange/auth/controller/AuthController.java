package com.lyk.coursearrange.auth.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.lyk.coursearrange.auth.request.AuthLoginRequest;
import com.lyk.coursearrange.auth.service.AuthFacadeService;
import com.lyk.coursearrange.auth.service.AuthLoginService;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.common.enums.ResultCode;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.system.rbac.entity.SysUser;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 认证信息控制器。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthFacadeService authFacadeService;
    private final AuthLoginService authLoginService;

    public AuthController(AuthFacadeService authFacadeService,
                          AuthLoginService authLoginService) {
        this.authFacadeService = authFacadeService;
        this.authLoginService = authLoginService;
    }

    @PostMapping("/login")
    public ServerResponse<?> login(@RequestBody AuthLoginRequest request) {
        SysUser sysUser = authLoginService.login(request.getUsername(), request.getPassword(), request.getUserType());
        if (sysUser == null) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "用户名或密码错误");
        }
        String token = issueLoginToken(sysUser);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("token", token);
        data.put("user", authFacadeService.getCurrentUserView());
        return ServerResponse.ofSuccess(data);
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

    String issueLoginToken(SysUser sysUser) {
        StpUtil.login("sys_user:" + sysUser.getId());
        return StpUtil.getTokenValue();
    }
}
