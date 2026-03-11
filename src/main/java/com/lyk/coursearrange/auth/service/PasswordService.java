package com.lyk.coursearrange.auth.service;

/**
 * 密码服务。
 * 步骤说明：
 * 1. 统一处理密码加密。
 * 2. 统一处理密码匹配。
 * 3. 为旧密码平滑迁移到 BCrypt 提供入口。
 */
public interface PasswordService {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);

    boolean isEncoded(String password);
}
