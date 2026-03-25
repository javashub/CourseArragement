package com.lyk.coursearrange.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 排课算法使用的内存任务对象。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClassTask implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String semester;
    private String gradeNo;
    private String classNo;
    private String courseNo;
    private String courseName;
    private String teacherNo;
    private String teacherName;
    private String courseAttr;
    private Integer studentNum;
    private Integer weeksSum;
    private Integer weeksNumber;
    private String isFix;
    private String classTime;
}
