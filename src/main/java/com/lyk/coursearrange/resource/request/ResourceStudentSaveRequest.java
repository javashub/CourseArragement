package com.lyk.coursearrange.resource.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 学生资源保存请求。
 */
@Data
public class ResourceStudentSaveRequest {

    private Long id;

    @NotBlank(message = "学号不能为空")
    private String studentCode;

    @NotBlank(message = "学生姓名不能为空")
    private String studentName;

    private String gender;

    private String mobile;

    private String email;

    private Integer entryYear;

    @NotNull(message = "状态不能为空")
    private Integer status;

    private String remark;
}
