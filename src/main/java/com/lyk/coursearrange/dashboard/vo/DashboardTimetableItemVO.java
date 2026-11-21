package com.lyk.coursearrange.dashboard.vo;

import lombok.Data;

/**
 * 工作台今日课表节次条目。
 */
@Data
public class DashboardTimetableItemVO {

    /**
     * 节次序号，来自 sch_schedule_result.period_no。
     */
    private Integer periodIndex;

    private String startTime;

    private String endTime;

    private String courseName;

    private String teacherName;

    private String classroomNo;
}
