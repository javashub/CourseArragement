package com.lyk.coursearrange.schedule.vo;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.io.Serializable;

/**
 * 排课任务分页展示对象。
 * 说明：
 * 1. 对前端继续复用旧任务表格字段，降低迁移改动范围。
 * 2. 标准 sch_task 查询结果会在这里转换成页面当前可识别的结构。
 */
@Data
public class ScheduleTaskPageVO implements Serializable {

    private Integer id;

    private Long standardId;

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

    private Integer weeksNumber;

    private Integer weeksSum;

    private String isFix;

    private Integer needContinuous;

    private Integer continuousSize;

    private Integer priorityLevel;

    private String classTime;
}
