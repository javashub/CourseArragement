package com.lyk.coursearrange.entity.request;

import lombok.Data;

/**
 * @author: 15760
 * @Date: 2020/4/3
 * @Descripe:
 */
@Data
public class ClassTaskRequest {
    /**
     * 年级
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
     * 课程属性
     */
    private String courseAttr;

    /**
     * 上课人数
     */
    private Integer studentNum;

    /**
     * 上课周数
     */
    private Integer weeksNumber;

    /**
     * 每周几节课
     */
    private Integer weeksSum;

    /**
     * 是否固定上课时间
     */
    private String isFix;

    /**
     * 固定时间的话时间是什么时候
     */
    private String classTime;
}
