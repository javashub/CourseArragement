package com.lyk.coursearrange.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 调课日志实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_course_plan_adjust_log")
public class CoursePlanAdjustLog extends Model<CoursePlanAdjustLog> implements Serializable {

    private static final long serialVersionUID = 1165851906332952806L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer coursePlanId;

    private String semester;

    private String gradeNo;

    private String classNo;

    private String courseNo;

    private String teacherNo;

    private String beforeClassTime;

    private String afterClassTime;

    private String beforeClassroomNo;

    private String afterClassroomNo;

    private Long operatorUserId;

    private String operatorName;

    private String operatorType;

    private String remark;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Override
    public Serializable pkVal() {
        return this.id;
    }
}
