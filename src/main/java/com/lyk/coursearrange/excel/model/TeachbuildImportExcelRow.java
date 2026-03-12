package com.lyk.coursearrange.excel.model;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 教学楼导入行模型。
 */
@Data
public class TeachbuildImportExcelRow {

    @ExcelProperty("教学楼编号")
    private String teachBuildNo;

    @ExcelProperty("教学楼名称")
    private String teachBuildName;

    @ExcelProperty("教学楼位置")
    private String teachBuildLocation;
}
