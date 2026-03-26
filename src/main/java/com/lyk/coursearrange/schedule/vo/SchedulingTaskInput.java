package com.lyk.coursearrange.schedule.vo;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 排课算法使用的标准任务输入对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchedulingTaskInput implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String semester;
    private String gradeNo;
    private String classNo;
    private String courseNo;
    private String courseName;
    private String teacherNo;
    @JsonAlias("realname")
    private String teacherName;
    private String courseAttr;
    private Integer studentNum;
    private Integer weeksSum;
    private Integer weeksNumber;
    private Integer priorityLevel;
    private String taskCode;
    private Long standardTaskId;
    private Long campusId;
    private Long collegeId;
    private Integer needContinuous;
    private Integer continuousSize;
    private Integer needFixedRoom;
    private Long fixedRoomId;
    private Integer needSpecialRoom;
    private String roomType;
    private Integer maxWeekHours;
    private Integer maxDayHours;
    private List<String> teacherForbiddenTimeSlots;
    private List<String> classForbiddenTimeSlots;
    private String isFix;
    private String classTime;
}
