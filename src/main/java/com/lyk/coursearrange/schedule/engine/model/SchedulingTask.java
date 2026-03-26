package com.lyk.coursearrange.schedule.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 排课引擎使用的任务模型。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchedulingTask {

    private Long taskId;

    private String taskCode;

    private String semester;

    private String classNo;

    private String courseNo;

    private String teacherNo;

    private String courseName;

    private String teacherName;

    private String courseAttr;

    private Integer studentCount;

    private Integer weekHours;

    private Integer priorityLevel;

    private boolean fixedTime;

    private List<String> fixedTimeSlots;

    private boolean needContinuous;

    private Integer continuousSize;

    private List<String> teacherForbiddenTimeSlots;

    private List<String> classForbiddenTimeSlots;

    private Integer teacherMaxDayHours;

    private boolean needSpecialRoom;

    private String requiredRoomType;

    private Long fixedRoomId;

    private String fixedRoomCode;

    private Long campusId;

    private Long collegeId;
}
