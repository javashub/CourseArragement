package com.lyk.coursearrange.organization.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lyk.coursearrange.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学院实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("org_college")
public class OrgCollege extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String collegeCode;

    private String collegeName;

    private Long campusId;

    private Long deanUserId;

    private Integer sortNo;

    private Integer status;
}
