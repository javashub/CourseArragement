package com.lyk.coursearrange.excel.model;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 课程任务 Excel 行模型。
 */
@Data
public class ClassTaskExcelRow {

    @ExcelProperty("学期")
    private String semester;

    @ExcelProperty("年级编号")
    private String gradeNo;

    @ExcelProperty("班级编号")
    private String classNo;

    @ExcelProperty("课程编号")
    private String courseNo;

    @ExcelProperty("课程名称")
    private String courseName;

    @ExcelProperty("教师编号")
    private String teacherNo;

    @ExcelProperty("教师姓名")
    private String teacherName;

    @ExcelProperty("课程属性")
    private String courseAttr;

    @ExcelProperty("学生人数")
    private Integer studentNum;

    @ExcelProperty("周学时")
    private Integer weeksNumber;

    @ExcelProperty("周数")
    private Integer weeksSum;

    @ExcelProperty("是否固定排课")
    private String isFix;

    @ExcelProperty("固定时间编码")
    private String classTime;
}
