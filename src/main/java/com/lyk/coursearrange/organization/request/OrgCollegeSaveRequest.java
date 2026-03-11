package com.lyk.coursearrange.organization.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 学院保存请求对象。
 */
@Data
public class OrgCollegeSaveRequest {

    private Long id;

    @NotBlank(message = "学院编码不能为空")
    private String collegeCode;

    @NotBlank(message = "学院名称不能为空")
    private String collegeName;

    private Long campusId;

    private Long deanUserId;

    @NotNull(message = "状态不能为空")
    private Integer status;

    private Integer sortNo;

    private String remark;
}
