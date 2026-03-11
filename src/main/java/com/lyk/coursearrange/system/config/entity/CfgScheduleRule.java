package com.lyk.coursearrange.system.config.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lyk.coursearrange.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 排课规则实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cfg_schedule_rule")
public class CfgScheduleRule extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String ruleCode;

    private String ruleName;

    private Long campusId;

    private Long collegeId;

    private Long stageId;

    private Long termId;

    private Integer weekDays;

    private Integer dayPeriods;

    private Integer morningPeriods;

    private Integer afternoonPeriods;

    private Integer nightPeriods;

    private Integer allowWeekend;

    private Integer defaultContinuousLimit;

    private Integer status;

    private Integer isDefault;
}
