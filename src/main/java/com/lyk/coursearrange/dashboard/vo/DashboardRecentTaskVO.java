package com.lyk.coursearrange.dashboard.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工作台最近排课任务视图对象。
 */
@Data
public class DashboardRecentTaskVO {

    private String taskCode;

    private String classNo;

    private String semester;

    private String status;

    /**
     * 最近一次执行中该任务记录的冲突次数。
     */
    private int conflictCount;

    private LocalDateTime updatedAt;
}
