package com.lyk.coursearrange.entity.request;

import lombok.Data;

/**
 * @author: 15760
 * @Date: 2020/3/21
 * @Descripe: 需要排课的上课任务
 */
@Data
public class TeachCoursePlan {

    private Integer id;

    // 班级编号
    private String classNo;

    // 课程编号
    private String courseNo;

    // 课程名称
    private String courseName;

    // 上课的年级
    private String gradeNo;

    // 上课的学生人数，到时候需要判断教室容量够不够
    private Integer studentnumber;

    // 课程属性，用于设置上课的优先级
    private String courseAttr;

    // 课程的学时，为每周节数*持续周数,约定一节课为一个学时
    private Integer classHour;

    // 一周上课节数
    private Integer weeksTime;

    // 持续多少周
    private Integer weeksSum;

    // 上课的学期
    private String semester;
}
