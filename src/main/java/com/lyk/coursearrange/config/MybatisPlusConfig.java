package com.lyk.coursearrange.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 基础配置。
 */
@Configuration
@MapperScan({
        "com.lyk.coursearrange.dao",
        "com.lyk.coursearrange.resource.mapper",
        "com.lyk.coursearrange.schedule.mapper",
        "com.lyk.coursearrange.organization.mapper",
        "com.lyk.coursearrange.system.config.mapper",
        "com.lyk.coursearrange.system.dict.mapper",
        "com.lyk.coursearrange.system.rbac.mapper"
})
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
