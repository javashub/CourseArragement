package com.lyk.coursearrange.dashboard.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工作台最近调课记录视图对象。
 */
@Data
public class DashboardRecentAdjustmentVO {

    private String className;

    private String courseName;

    /**
     * 调整前时间片编码，与既有调课日志一致（四位：星期+节次）。
     */
    private String beforeClassTime;

    /**
     * 调整后时间片编码，与既有调课日志一致（四位：星期+节次）。
     */
    private String afterClassTime;

    private String operatorName;

    private LocalDateTime createTime;
}
