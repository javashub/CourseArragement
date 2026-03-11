package com.lyk.coursearrange.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 认证安全配置。
 * 步骤说明：
 * 1. 统一承载认证与密码安全相关配置项。
 * 2. 后续通过 yml 或环境变量注入，避免代码硬编码。
 * 3. 当前先为密码加密和 token 前缀预留能力。
 */
@Data
@ConfigurationProperties(prefix = "course-arrange.security")
public class AuthSecurityProperties {

    /**
     * BCrypt 强度，值越高安全性越高，但计算也越慢。
     */
    private Integer bcryptStrength = 10;

    /**
     * Token 前缀，后续前后端统一约定时使用。
     */
    private String tokenPrefix = "Bearer";

    /**
     * 是否允许多端同时登录，当前仅预留配置位。
     */
    private Boolean allowMultiLogin = true;
}
