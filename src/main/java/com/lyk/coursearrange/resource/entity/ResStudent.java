package com.lyk.coursearrange.resource.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lyk.coursearrange.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学生资源实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("res_student")
public class ResStudent extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String studentCode;

    private String studentName;

    private String gender;

    private String mobile;

    private String email;

    private Long campusId;

    private Long collegeId;

    private Long stageId;

    private Long gradeId;

    private Long majorId;

    private Long adminClassId;

    private Integer entryYear;

    private Long userId;

    private Integer status;
}
