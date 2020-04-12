package com.lyk.coursearrange.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * @author lequal
 * @since 2020-04-06
 */
@TableName("tb_class_task")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClassTask extends Model<ClassTask> {

    private static final long serialVersionUID=1L;

    /**
     * id，即将要上课的，需要进行排课的
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 学期
     */
    private String semester;

    /**
     * 年级编号
     */
    @TableField("gradeNo")
    private String gradeNo;

    /**
     * 班级编号
     */
    @TableField("classNo")
    private String classNo;

    /**
     * 课程编号
     */
    @TableField("courseNo")
    private String courseNo;

    /**
     * 教师编号
     */
    @TableField("teacherNo")
    private String teacherNo;

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
     * 一周几个学时，偶数
     */
    @TableField("weeksSum")
    private Integer weeksSum;

    /**
     * 要上多少周
     */
    @TableField("weeksNumber")
    private Integer weeksNumber;

    /**
     * 是否固定上课时间
     */
    @TableField("isFix")
    private String isFix;

    /**
     * 2位为一个时间编号
     */
    @TableField("classTime")
    private String classTime;

    @TableLogic
    private Boolean deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;




    @Override
    protected Serializable pkVal() {
        return this.id;
    }


}
