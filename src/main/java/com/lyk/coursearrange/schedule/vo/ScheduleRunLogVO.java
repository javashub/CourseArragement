package com.lyk.coursearrange.schedule.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 排课执行日志视图对象。
 */
@Data
public class ScheduleRunLogVO {

    private Long id;

    private String semester;

    private Integer taskCount;

    private Integer generatedPlanCount;

    private Integer status;

    private Long durationMs;

    private String message;

    private Long operatorUserId;

    private String operatorName;

    private LocalDateTime createTime;
}
