package com.lyk.coursearrange.entity.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.io.Serializable;

/**
 * @author: 15760
 * @Date: 2020/4/3
 * @Descripe:
 */
@Data
public class ClassTaskDTO implements Serializable {

    private static final long serialVersionUID = -4023200004755949854L;

    /**
     * 学期
     */
    private String semester;

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
     * 课程名称
     */
    private String courseName;

    /**
     * 讲师编号
     */
    private String teacherNo;

    /**
     * 讲师名字
     */
    @JsonAlias("realname")
    private String teacherName;

    /**
     * 课程属性
     */
    private String courseAttr;

    /**
     * 上课人数
     */
    private Integer studentNum;

    /**
     * 周学时
     */
    private Integer weeksNumber;

    /**
     * 周数
     */
    private Integer weeksSum;

    /**
     * 是否固定上课时间
     */
    private String isFix;

    /**
     * 是否需要连堂
     */
    private Integer needContinuous;

    /**
     * 连堂节数
     */
    private Integer continuousSize;

    /**
     * 任务优先级，数值越大优先级越高
     */
    private Integer priorityLevel;

    /**
     * 固定时间的话时间是什么时候
     */
    private String classTime;
}
