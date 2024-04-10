package com.lyk.coursearrange.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author: 15760
 * @Date: 2020/3/4
 * @Descripe: MybatisPlus配置类
 */

@Configuration
@MapperScan("com.lyk.coursearrange.dao")
public class MybatisPlusConfig {

    // 分页插件
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }

    // 逻辑删除插件,MP3.1之后不需要配置

}
