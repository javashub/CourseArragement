package com.lyk.coursearrange.entity.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author: 15760
 * @Date: 2020/5/3
 * @Descripe:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoursePlanVo implements Serializable {

    private static final long serialVersionUID = -4879059791317395064L;
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
