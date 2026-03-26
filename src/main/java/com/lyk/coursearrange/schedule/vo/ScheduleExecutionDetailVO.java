package com.lyk.coursearrange.schedule.vo;

import com.lyk.coursearrange.schedule.engine.model.SchedulingConstraintSummary;
import com.lyk.coursearrange.schedule.engine.model.UnscheduledTaskDetail;
import lombok.Data;

import java.util.List;

/**
 * 排课执行详情视图对象。
 */
@Data
public class ScheduleExecutionDetailVO {

    private Long runLogId;

    private String semester;

    private Integer taskCount;

    private Integer scheduledTaskCount;

    private Integer unscheduledTaskCount;

    private Double successRate;

    private Integer generatedResultCount;

    private Long durationMs;

    private String runStatus;

    private String message;

    private List<UnscheduledTaskDetail> unscheduledTasks;

    private List<SchedulingConstraintSummary> constraintSummary;
}
