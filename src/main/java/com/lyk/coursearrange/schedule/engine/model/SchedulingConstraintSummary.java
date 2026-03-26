package com.lyk.coursearrange.schedule.engine.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 失败原因汇总。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchedulingConstraintSummary {

    private String reasonCode;

    private String reasonLabel;

    private Integer count;
}
