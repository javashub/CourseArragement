package com.lyk.coursearrange.dashboard.vo;

import lombok.Data;

/**
 * 工作台任务统计视图对象。
 */
@Data
public class DashboardTaskStatsVO {

    /**
     * 当前学期标准排课任务总数。
     */
    private int total;

    /**
     * 已生成标准课表的任务数（来自 sch_schedule_result）。
     */
    private int completed;

    /**
     * 尚未生成课表且未记录冲突的任务数。
     */
    private int pending;

    /**
     * 最近一次执行中因冲突原因未排入的任务数。
     */
    private int conflict;
}
