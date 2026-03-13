package com.lyk.coursearrange.resource.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lyk.coursearrange.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课程资源实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("res_course")
public class ResCourse extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String courseCode;

    private String courseName;

    private String courseShortName;

    private String courseType;

    private Long stageId;

    private Long collegeId;

    private Integer totalHours;

    private Integer weekHours;

    private Integer needContinuous;

    private Integer continuousSize;

    private Integer needSpecialRoom;

    private String roomType;

    private Integer isCoreCourse;

    private Integer allowCrossDay;

    private Integer status;
}
