package com.lyk.coursearrange.schedule.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lyk.coursearrange.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 标准排课执行日志实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sch_schedule_run_log")
public class SchScheduleRunLog extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String runCode;

    private Long termId;

    private Long campusId;

    private Long collegeId;

    private Long stageId;

    private String runType;

    private String algorithmType;

    private Integer taskTotal;

    private Integer taskSuccess;

    private Integer taskFailed;

    private String runStatus;

    private String failureReason;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    private Long operatorUserId;
}
