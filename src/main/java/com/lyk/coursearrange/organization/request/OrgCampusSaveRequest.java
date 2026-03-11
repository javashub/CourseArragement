package com.lyk.coursearrange.organization.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 校区保存请求对象。
 */
@Data
public class OrgCampusSaveRequest {

    private Long id;

    @NotBlank(message = "校区编码不能为空")
    private String campusCode;

    @NotBlank(message = "校区名称不能为空")
    private String campusName;

    @NotBlank(message = "校区类型不能为空")
    private String campusType;

    private String provinceCode;

    private String cityCode;

    private String districtCode;

    private String address;

    @NotNull(message = "状态不能为空")
    private Integer status;

    private Integer sortNo;

    private String remark;
}
