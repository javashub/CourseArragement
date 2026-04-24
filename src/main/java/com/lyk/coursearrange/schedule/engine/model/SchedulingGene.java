package com.lyk.coursearrange.schedule.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 对象化染色体中的单个基因单元。
 *
 * <p>这里不再使用旧式字符串拼接编码，而是把任务标识、教师、班级、
 * 教室和时间片等字段显式存放，方便后续做交叉、变异、调试和日志输出。</p>
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SchedulingGene {

    /**
     * 基因唯一键，通常由任务 + 块序号组成。
     */
    private String geneCode;

    private Long taskId;

    private String taskCode;

    private String classNo;

    private String courseNo;

    private String teacherNo;

    private Long classroomId;

    private String classroomCode;

    /**
     * 一个基因承载一个“课时块”。
     * 非连堂时一般只有一个时间片，连堂时会有多个连续时间片。
     */
    private List<String> timeSlotCodes;

    private boolean fixedTime;

    private boolean needContinuous;

    private Integer continuousSize;

    private Integer studentCount;

    private Integer priorityLevel;

    private Integer teacherMaxDayHours;

    private String requiredRoomType;

    private Long campusId;

    private Long collegeId;
}
