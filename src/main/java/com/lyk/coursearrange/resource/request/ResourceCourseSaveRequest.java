package com.lyk.coursearrange.resource.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 课程资源保存请求。
 */
@Data
public class ResourceCourseSaveRequest {

    private Long id;

    @NotBlank(message = "课程编码不能为空")
    private String courseCode;

    @NotBlank(message = "课程名称不能为空")
    private String courseName;

    private String courseShortName;

    @NotBlank(message = "课程类型不能为空")
    private String courseType;

    @NotNull(message = "总课时不能为空")
    private Integer totalHours;

    @NotNull(message = "周课时不能为空")
    private Integer weekHours;

    @NotNull(message = "是否需要专用教室不能为空")
    private Integer needSpecialRoom;

    private String roomType;

    @NotNull(message = "状态不能为空")
    private Integer status;

    private String remark;
}
