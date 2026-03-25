package com.lyk.coursearrange.organization.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lyk.coursearrange.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 年级实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("org_grade")
public class OrgGrade extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String gradeCode;

    private String gradeName;

    private Long stageId;

    private Long schoolYearId;

    private Long campusId;

    private Long collegeId;

    private Long majorId;

    private Integer entryYear;

    private Integer graduateYear;

    private Integer status;
}
