package com.lyk.coursearrange.schedule.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lyk.coursearrange.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 标准排课任务实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sch_task")
public class SchTask extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String taskCode;

    private Long schoolYearId;

    private Long termId;

    private Long campusId;

    private Long collegeId;

    private Long stageId;

    private Long gradeId;

    private Long majorId;

    private Long adminClassId;

    private Long teachingClassId;

    private Long courseId;

    private Long teacherId;

    private Integer studentCount;

    private Integer weekHours;

    private Integer totalHours;

    private Integer needContinuous;

    private Integer continuousSize;

    private Integer needFixedRoom;

    private Long fixedRoomId;

    private Integer needFixedTime;

    private Integer fixedWeekdayNo;

    private Integer fixedPeriodNo;

    private Integer priorityLevel;

    private Integer allowConflict;

    private String taskStatus;

    private String sourceType;

    private Integer status;
}
