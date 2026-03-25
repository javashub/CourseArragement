package com.lyk.coursearrange.excel.model;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 学生导出行模型。
 */
@Data
public class StudentExcelRow {

    @ExcelProperty("学号")
    private String studentNo;

    @ExcelProperty("登录账号")
    private String username;

    @ExcelProperty("姓名")
    private String studentName;

    @ExcelProperty("年级")
    private String grade;

    @ExcelProperty("班级")
    private String classNo;

    @ExcelProperty("年龄")
    private Integer age;

    @ExcelProperty("联系电话")
    private String telephone;

    @ExcelProperty("邮箱")
    private String email;

    @ExcelProperty("地址")
    private String address;

    @ExcelProperty("状态")
    private String statusText;
}
