package com.lyk.coursearrange.schedule.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 排课引擎执行结果。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchedulingExecutionResult {

    private Integer taskCount;

    private Integer scheduledTaskCount;

    private Integer unscheduledTaskCount;

    private Double successRate;

    private List<SchedulingAssignment> assignments;

    private List<UnscheduledTaskDetail> unscheduledTasks;

    private List<SchedulingConstraintSummary> constraintSummary;
}
