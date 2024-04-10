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
 * @since 2020-04-15
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_course_plan")
public class CoursePlan extends Model<CoursePlan> {

    private static final long serialVersionUID=1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

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
     * 讲师编号
     */
    @TableField("teacher_no")
    private String teacherNo;

    /**
     * 教室编号
     */
    @TableField("classroom_no")
    private String classroomNo;

    /**
     * 上课时间
     */
    @TableField("class_time")
    private String classTime;

    /**
     * 周数
     */
    @TableField("weeks_sum")
    private Integer weeksSum;

    /**
     * 学期
     */
    @TableField("semester")
    private String semester;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;




    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
