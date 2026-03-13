package com.lyk.coursearrange.resource.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lyk.coursearrange.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 教室资源实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("res_classroom")
public class ResClassroom extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String classroomCode;

    private String classroomName;

    private Long campusId;

    private Long collegeId;

    private Long buildingId;

    private Integer floorNo;

    private String roomType;

    private Integer seatCount;

    private Integer isShared;

    private Integer status;
}
