package com.lyk.coursearrange.system.config.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lyk.coursearrange.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 功能开关实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cfg_feature_toggle")
public class CfgFeatureToggle extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String toggleCode;

    private String toggleName;

    private Long campusId;

    private Long collegeId;

    private Long stageId;

    private Long termId;

    private String toggleValue;

    private String valueType;

    private Integer status;
}
