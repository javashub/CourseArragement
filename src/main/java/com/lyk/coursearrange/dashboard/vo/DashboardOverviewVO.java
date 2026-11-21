package com.lyk.coursearrange.dashboard.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 工作台总览视图对象。
 * 说明：
 * 1. 单次只读聚合接口返回六块数据：学期、任务统计、最近任务、今日课表、最近调课、资源计数。
 * 2. 数据源为 sch_task / sch_schedule_result / sch_schedule_adjust_log，不读写 legacy tb_* 表。
 */
@Data
public class DashboardOverviewVO {

    /**
     * 当前学期，取最近更新的标准任务所属学期；无数据时为空串。
     */
    private String semester = "";

    private DashboardTaskStatsVO taskStats = new DashboardTaskStatsVO();

    private List<DashboardRecentTaskVO> recentTasks = new ArrayList<>();

    private DashboardTodayTimetableVO todayTimetable = new DashboardTodayTimetableVO();

    private List<DashboardRecentAdjustmentVO> recentAdjustments = new ArrayList<>();

    private DashboardResourceCountsVO resourceCounts = new DashboardResourceCountsVO();
}
