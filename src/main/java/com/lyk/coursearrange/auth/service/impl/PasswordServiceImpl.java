package com.lyk.coursearrange.auth.service.impl;

import com.lyk.coursearrange.auth.service.PasswordService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * BCrypt 密码服务实现。
 * 步骤说明：
 * 1. 新密码统一走 BCrypt。
 * 2. 登录校验统一通过编码器匹配。
 * 3. 通过哈希前缀判断是否已经完成密码升级。
 */
@Service
public class PasswordServiceImpl implements PasswordService {

    private final PasswordEncoder passwordEncoder;

    public PasswordServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        if (StringUtils.isAnyBlank(rawPassword, encodedPassword)) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    @Override
    public boolean isEncoded(String password) {
        if (StringUtils.isBlank(password)) {
            return false;
        }
        return password.startsWith("$2a$")
                || password.startsWith("$2b$")
                || password.startsWith("$2y$");
    }
}
