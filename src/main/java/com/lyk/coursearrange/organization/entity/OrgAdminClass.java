package com.lyk.coursearrange.organization.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lyk.coursearrange.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 行政班实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("org_admin_class")
public class OrgAdminClass extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String classCode;

    private String className;

    private Long gradeId;

    private Long campusId;

    private Long collegeId;

    private Long majorId;

    private Long stageId;

    private Long headTeacherId;

    private Integer studentCount;

    private String classMode;

    private Integer status;

    private String forbiddenTimeSlots;
}
