package com.lyk.coursearrange.auth.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 认证基础配置。
 * 步骤说明：
 * 1. 开启认证配置属性绑定。
 * 2. 注册密码编码器。
 * 3. 为后续统一登录模块改造提供基础设施。
 */
@Configuration
@EnableConfigurationProperties(AuthSecurityProperties.class)
public class AuthConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder(AuthSecurityProperties properties) {
        return new BCryptPasswordEncoder(properties.getBcryptStrength());
    }
}
