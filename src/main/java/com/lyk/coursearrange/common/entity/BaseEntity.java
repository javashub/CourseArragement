package com.lyk.coursearrange.common.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通用基础实体。
 * 步骤说明：
 * 1. 统一收敛新模型中的审计字段。
 * 2. 让 RBAC、组织架构、配置等新模块保持一致的字段风格。
 * 3. 兼容 MyBatis-Plus 自动填充与逻辑删除。
 */
@Data
public abstract class BaseEntity implements Serializable {

    /**
     * 创建人。
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    /**
     * 更新人。
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;

    /**
     * 创建时间。
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标记。
     */
    @TableLogic
    private Integer deleted;

    /**
     * 备注。
     */
    private String remark;
}
