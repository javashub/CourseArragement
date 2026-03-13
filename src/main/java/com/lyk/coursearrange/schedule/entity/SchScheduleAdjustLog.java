package com.lyk.coursearrange.schedule.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lyk.coursearrange.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 标准调课日志实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sch_schedule_adjust_log")
public class SchScheduleAdjustLog extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String adjustCode;

    private Long termId;

    private Long sourceResultId;

    private Long targetResultId;

    private String adjustType;

    private Integer beforeWeekdayNo;

    private Integer beforePeriodNo;

    private Long beforeClassroomId;

    private Integer afterWeekdayNo;

    private Integer afterPeriodNo;

    private Long afterClassroomId;

    private String adjustReason;

    private Long operatorUserId;

    private Integer status;
}
