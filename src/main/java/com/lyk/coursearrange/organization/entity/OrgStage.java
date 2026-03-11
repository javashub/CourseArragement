package com.lyk.coursearrange.organization.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lyk.coursearrange.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学段实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("org_stage")
public class OrgStage extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String stageCode;

    private String stageName;

    private Integer stageLevel;

    private Integer status;
}
