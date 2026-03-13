package com.lyk.coursearrange.resource.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lyk.coursearrange.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 教师资源实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("res_teacher")
public class ResTeacher extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String teacherCode;

    private String teacherName;

    private String gender;

    private String mobile;

    private String email;

    private String titleName;

    private Long campusId;

    private Long collegeId;

    private Long stageId;

    private Integer allowCrossCampus;

    private Integer allowCrossCollege;

    private Integer maxWeekHours;

    private Integer maxDayHours;

    private String hireStatus;

    private Long userId;

    private Integer status;
}
