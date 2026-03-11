package com.lyk.coursearrange.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author: 15760
 * @Date: 2020/3/4
 * @Descripe: 解决跨域问题
 */
@Configuration
public class CrosConfig {
    @Bean
    public WebMvcConfigurer crosConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            // 重写父类提供的跨域请求处理的接口，解决跨域问题
            public void addCorsMappings(CorsRegistry registry) {
                // 添加映射路径
                registry.addMapping("/**")
                        // Spring Boot 2.7 下，allowCredentials(true) 不能再配 allowedOrigins("*")
                        .allowedOriginPatterns(
                                "http://localhost:*",
                                "http://127.0.0.1:*",
                                "http://192.168.*:*",
                                "http://10.*:*",
                                "http://172.16.*:*",
                                "http://172.17.*:*",
                                "http://172.18.*:*",
                                "http://172.19.*:*",
                                "http://172.20.*:*",
                                "http://172.21.*:*",
                                "http://172.22.*:*",
                                "http://172.23.*:*",
                                "http://172.24.*:*",
                                "http://172.25.*:*",
                                "http://172.26.*:*",
                                "http://172.27.*:*",
                                "http://172.28.*:*",
                                "http://172.29.*:*",
                                "http://172.30.*:*",
                                "http://172.31.*:*"
                        )
                        // 是否发送Cookie信息
                        .allowCredentials(true)
                        // 放行哪些原始域(请求方式)
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        // 放行哪些原始域(头部信息)
                        .allowedHeaders("*")
                        // 暴露哪些头部信息（因为跨域访问默认不能获取全部头部信息）
                        .exposedHeaders("Header1", "Header2");
            }
        };
    }
}
