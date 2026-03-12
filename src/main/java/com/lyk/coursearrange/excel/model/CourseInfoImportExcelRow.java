package com.lyk.coursearrange.excel.model;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 课程导入行模型。
 */
@Data
public class CourseInfoImportExcelRow {

    @ExcelProperty("课程编号")
    private String courseNo;

    @ExcelProperty("课程名称")
    private String courseName;

    @ExcelProperty("课程属性")
    private String courseAttr;

    @ExcelProperty("出版社")
    private String publisher;

    @ExcelProperty("优先级")
    private Integer piority;

    @ExcelProperty("状态")
    private String statusText;

    @ExcelProperty("备注")
    private String remark;
}
