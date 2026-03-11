package com.lyk.coursearrange.system.config.query;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 时间片查询对象。
 */
@Data
public class TimeSlotQuery {

    @NotNull(message = "排课规则ID不能为空")
    private Long scheduleRuleId;
}
