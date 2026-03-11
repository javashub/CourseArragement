package com.lyk.coursearrange.organization.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 学段保存请求对象。
 */
@Data
public class OrgStageSaveRequest {

    private Long id;

    @NotBlank(message = "学段编码不能为空")
    private String stageCode;

    @NotBlank(message = "学段名称不能为空")
    private String stageName;

    @NotNull(message = "学段层级不能为空")
    private Integer stageLevel;

    @NotNull(message = "状态不能为空")
    private Integer status;

    private String remark;
}
