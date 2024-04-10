package com.lyk.coursearrange.entity.response;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.lyk.coursearrange.entity.CoursePlan;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: 15760
 * @Date: 2020/5/3
 * @Descripe:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoursePlanVo {

    /**
     * 上课时间
     */
    private String classTime;

    /**
     * 教师真名
     */
    private String realname;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * id
     */
    private Integer id;

    /**
     * 年级编号
     */
    private String gradeNo;

    /**
     * 班级编号
     */
    private String classNo;

    /**
     * 课程编号
     */
    private String courseNo;

    /**
     * 讲师编号
     */
    private String teacherNo;

    /**
     * 教室编号
     */
    private String classroomNo;



    /**
     * 学期
     */
    private String semester;


}
