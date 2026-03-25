package com.lyk.coursearrange.auth.service.impl;

import com.lyk.coursearrange.auth.service.AuthLoginService;
import com.lyk.coursearrange.auth.service.PasswordService;
import com.lyk.coursearrange.common.constants.SystemConstants;
import com.lyk.coursearrange.system.rbac.entity.SysUser;
import com.lyk.coursearrange.system.rbac.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 统一登录服务实现。
 */
@Slf4j
@Service
public class AuthLoginServiceImpl implements AuthLoginService {

    private final SysUserService sysUserService;
    private final PasswordService passwordService;

    public AuthLoginServiceImpl(SysUserService sysUserService,
                                PasswordService passwordService) {
        this.sysUserService = sysUserService;
        this.passwordService = passwordService;
    }

    @Override
    public SysUser login(String username, String password, String userType) {
        String normalizedUserType = normalizeUserType(userType);
        if (normalizedUserType == null) {
            return null;
        }
        return tryLoginSysUser(username, password, normalizedUserType);
    }

    private SysUser tryLoginSysUser(String username, String password, String userType) {
        SysUser sysUser = sysUserService.getByUsernameAndUserType(username, userType);
        if (sysUser == null) {
            return null;
        }
        String storedPassword = sysUser.getPasswordHash();
        if (storedPassword == null || storedPassword.isBlank()) {
            return null;
        }
        if (passwordService.isEncoded(storedPassword)) {
            if (!passwordService.matches(password, storedPassword)) {
                return null;
            }
        } else if (!password.equals(storedPassword)) {
            return null;
        } else {
            sysUser.setPasswordHash(passwordService.encode(password));
            sysUserService.updateById(sysUser);
        }
        touchLoginSuccess(sysUser);
        log.info("sys_user 登录成功，sysUserId={}, username={}, userType={}", sysUser.getId(), sysUser.getUsername(), sysUser.getUserType());
        return sysUser;
    }

    private void touchLoginSuccess(SysUser sysUser) {
        sysUser.setLastLoginAt(LocalDateTime.now());
        sysUserService.updateById(sysUser);
    }

    private String normalizeUserType(String userType) {
        if (userType == null || userType.isBlank()) {
            return null;
        }
        String normalized = userType.trim().toUpperCase();
        if (SystemConstants.UserType.ADMIN.equals(normalized)
                || SystemConstants.UserType.TEACHER.equals(normalized)
                || SystemConstants.UserType.STUDENT.equals(normalized)) {
            return normalized;
        }
        return null;
    }
}
