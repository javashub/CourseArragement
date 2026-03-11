package com.lyk.coursearrange.config.satoken;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 路由拦截基础配置。
 * 当前阶段先对公开接口做白名单放行，业务接口会在后续模块迁移时逐步切换到 @SaCheckLogin 等注解。
 */
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handler -> SaRouter.match("/**")
                        .notMatch(
                                "/error",
                                "/student/login",
                                "/student/register",
                                "/admin/login",
                                "/teacher/login",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/v3/api-docs/**",
                                "/doc.html",
                                "/favicon.ico"
                        )))
                .addPathPatterns("/**");
    }
}
