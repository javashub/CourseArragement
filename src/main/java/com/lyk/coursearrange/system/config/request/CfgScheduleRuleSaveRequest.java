package com.lyk.coursearrange.system.config.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 排课规则保存请求。
 */
@Data
public class CfgScheduleRuleSaveRequest {

    private Long id;

    @NotBlank(message = "规则编码不能为空")
    private String ruleCode;

    @NotBlank(message = "规则名称不能为空")
    private String ruleName;

    private Long campusId;

    private Long collegeId;

    private Long stageId;

    private Long termId;

    @NotNull(message = "每周上课天数不能为空")
    private Integer weekDays;

    @NotNull(message = "每天总节数不能为空")
    private Integer dayPeriods;

    @NotNull(message = "上午节数不能为空")
    private Integer morningPeriods;

    @NotNull(message = "下午节数不能为空")
    private Integer afternoonPeriods;

    @NotNull(message = "晚上节数不能为空")
    private Integer nightPeriods;

    @NotNull(message = "是否允许周末排课不能为空")
    private Integer allowWeekend;

    @NotNull(message = "默认最大连堂数不能为空")
    private Integer defaultContinuousLimit;

    @NotNull(message = "状态不能为空")
    private Integer status;

    @NotNull(message = "是否默认不能为空")
    private Integer isDefault;

    private String remark;
}
