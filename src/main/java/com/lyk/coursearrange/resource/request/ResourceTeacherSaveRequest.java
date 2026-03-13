package com.lyk.coursearrange.resource.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 教师资源保存请求。
 */
@Data
public class ResourceTeacherSaveRequest {

    private Long id;

    @NotBlank(message = "教师编码不能为空")
    private String teacherCode;

    @NotBlank(message = "教师姓名不能为空")
    private String teacherName;

    private String gender;

    private String mobile;

    private String email;

    private String titleName;

    private Integer allowCrossCampus;

    private Integer allowCrossCollege;

    private Integer maxWeekHours;

    private Integer maxDayHours;

    private String hireStatus;

    @NotNull(message = "状态不能为空")
    private Integer status;

    private String remark;
}
