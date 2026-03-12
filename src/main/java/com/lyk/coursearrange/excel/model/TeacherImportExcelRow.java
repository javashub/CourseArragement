package com.lyk.coursearrange.excel.model;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 教师导入行模型。
 */
@Data
public class TeacherImportExcelRow {

    @ExcelProperty("教师编号")
    private String teacherNo;

    @ExcelProperty("登录账号")
    private String username;

    @ExcelProperty("姓名")
    private String realname;

    @ExcelProperty("职称")
    private String jobtitle;

    @ExcelProperty("教授科目")
    private String teach;

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
