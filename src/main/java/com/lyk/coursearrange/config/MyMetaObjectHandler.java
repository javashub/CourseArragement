package com.lyk.coursearrange.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充配置。
 */
@Configuration
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        if (metaObject.hasSetter("createdAt") && getFieldValByName("createdAt", metaObject) == null) {
            strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
        }
        if (metaObject.hasSetter("updatedAt") && getFieldValByName("updatedAt", metaObject) == null) {
            strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
        }
        if (metaObject.hasSetter("createdBy") && getFieldValByName("createdBy", metaObject) == null) {
            strictInsertFill(metaObject, "createdBy", Long.class, 0L);
        }
        if (metaObject.hasSetter("updatedBy") && getFieldValByName("updatedBy", metaObject) == null) {
            strictInsertFill(metaObject, "updatedBy", Long.class, 0L);
        }
        if (metaObject.hasSetter("createTime") && getFieldValByName("createTime", metaObject) == null) {
            strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        }
        if (metaObject.hasSetter("updateTime") && getFieldValByName("updateTime", metaObject) == null) {
            strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        if (metaObject.hasSetter("updatedAt")) {
            strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
        }
        if (metaObject.hasSetter("updatedBy")) {
            strictUpdateFill(metaObject, "updatedBy", Long.class, 0L);
        }
        if (metaObject.hasSetter("updateTime")) {
            strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        }
    }
}
