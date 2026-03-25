package com.lyk.coursearrange.excel.model;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 课程导出行模型。
 */
@Data
public class CourseInfoExcelRow {

    @ExcelProperty("课程编号")
    private String courseNo;

    @ExcelProperty("课程名称")
    private String courseName;

    @ExcelProperty("课程属性")
    private String courseAttr;

    @ExcelProperty("出版社")
    private String publisher;

    @ExcelProperty("周课时")
    private Integer weekHours;

    @ExcelProperty("状态")
    private String statusText;

    @ExcelProperty("备注")
    private String remark;
}
