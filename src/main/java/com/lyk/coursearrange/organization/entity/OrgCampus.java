package com.lyk.coursearrange.organization.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lyk.coursearrange.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 校区实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("org_campus")
public class OrgCampus extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String campusCode;

    private String campusName;

    private String campusType;

    private String provinceCode;

    private String cityCode;

    private String districtCode;

    private String address;

    private Integer sortNo;

    private Integer status;
}
