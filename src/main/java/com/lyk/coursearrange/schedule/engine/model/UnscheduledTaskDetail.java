package com.lyk.coursearrange.schedule.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 未排成任务明细。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnscheduledTaskDetail {

    private Long taskId;

    private String taskCode;

    private String classNo;

    private String courseNo;

    private String teacherNo;

    private String reasonCode;

    private String reasonMessage;
}
