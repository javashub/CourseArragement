package com.lyk.coursearrange.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * @author lequal
 * @since 2020-04-06
 */
@EqualsAndHashCode(callSuper = true)
@TableName("tb_class_task")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClassTask extends Model<ClassTask> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id，即将要上课的，需要进行排课的
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 学期
     */
    @TableField("semester")
    private String semester;

    /**
     * 年级编号
     */
    @TableField("grade_no")
    private String gradeNo;

    /**
     * 班级编号
     */
    @TableField("class_no")
    private String classNo;

    /**
     * 课程编号
     */
    @TableField("course_no")
    private String courseNo;

    /**
     * 课程名称
     */
    @TableField("course_name")
    private String courseName;

    /**
     * 讲师编号
     */
    @TableField("teacher_no")
    private String teacherNo;

    /**
     * 讲师名字
     */
    @TableField("realname")
    private String realname;

    /**
     * 课程属性
     */
    @TableField("courseAttr")
    private String courseAttr;

    /**
     * 学生人数
     */
    @TableField("studentNum")
    private Integer studentNum;

    /**
     * 周数
     */
    @TableField("weeks_sum")
    private Integer weeksSum;

    /**
     * 周学时，偶数
     */
    @TableField("weeks_number")
    private Integer weeksNumber;

    /**
     * 是否固定上课时间
     */
    @TableField("isFix")
    private String isFix;

    /**
     * 2位为一个时间编号
     */
    @TableField("class_time")
    private String classTime;

    @TableLogic
    private Boolean deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


    @Override
    public Serializable pkVal() {
        return this.id;
    }


}
