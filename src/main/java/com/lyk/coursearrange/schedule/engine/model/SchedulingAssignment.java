package com.lyk.coursearrange.schedule.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 排课结果中的单条分配记录。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchedulingAssignment {

    private Long taskId;

    private String taskCode;

    private String classNo;

    private String courseNo;

    private String teacherNo;

    private Long classroomId;

    private String classroomCode;

    private Integer weekdayNo;

    private Integer periodNo;

    private String timeSlotCode;
}
