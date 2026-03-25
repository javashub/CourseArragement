package com.lyk.coursearrange.schedule.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 调课日志视图对象。
 */
@Data
public class ScheduleAdjustLogVO {

    private Long id;

    private String classNo;

    private String teacherNo;

    private String operatorName;

    private String beforeClassTime;

    private String afterClassTime;

    private String remark;

    private LocalDateTime createTime;
}
