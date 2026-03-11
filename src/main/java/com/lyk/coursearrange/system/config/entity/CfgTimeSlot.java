package com.lyk.coursearrange.system.config.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lyk.coursearrange.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 时间片实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cfg_time_slot")
public class CfgTimeSlot extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long scheduleRuleId;

    private Integer weekdayNo;

    private Integer periodNo;

    private String periodName;

    private String timeGroup;

    private String startTimeText;

    private String endTimeText;

    private Integer isTeaching;

    private Integer isFixedBreak;

    private Integer sortNo;
}
