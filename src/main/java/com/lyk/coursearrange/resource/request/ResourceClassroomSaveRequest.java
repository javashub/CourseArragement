package com.lyk.coursearrange.resource.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 教室资源保存请求。
 */
@Data
public class ResourceClassroomSaveRequest {

    private Long id;

    @NotBlank(message = "教室编码不能为空")
    private String classroomCode;

    @NotBlank(message = "教室名称不能为空")
    private String classroomName;

    @NotBlank(message = "教学楼编码不能为空")
    private String buildingCode;

    @NotNull(message = "座位数不能为空")
    private Integer seatCount;

    @NotBlank(message = "教室类型不能为空")
    private String roomType;

    @NotNull(message = "状态不能为空")
    private Integer status;

    private String remark;
}
