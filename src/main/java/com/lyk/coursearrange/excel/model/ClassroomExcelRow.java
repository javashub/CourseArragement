package com.lyk.coursearrange.excel.model;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 教室导出行模型。
 */
@Data
public class ClassroomExcelRow {

    @ExcelProperty("教室编号")
    private String classroomNo;

    @ExcelProperty("教室名称")
    private String classroomName;

    @ExcelProperty("教学楼编号")
    private String teachbuildNo;

    @ExcelProperty("容量")
    private Integer capacity;

    @ExcelProperty("教室属性")
    private String attr;

    @ExcelProperty("备注")
    private String remark;
}
