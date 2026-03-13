package com.lyk.coursearrange.resource.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lyk.coursearrange.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 教学楼资源实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("res_building")
public class ResBuilding extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String buildingCode;

    private String buildingName;

    private Long campusId;

    private Long collegeId;

    private String buildingType;

    private Integer floorCount;

    private Integer status;
}
