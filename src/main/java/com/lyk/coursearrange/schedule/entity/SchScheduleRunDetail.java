package com.lyk.coursearrange.schedule.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lyk.coursearrange.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 排课执行失败任务明细。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sch_schedule_run_detail")
public class SchScheduleRunDetail extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long runLogId;

    private Long taskId;

    private String taskCode;

    private String classNo;

    private String courseNo;

    private String teacherNo;

    private String reasonCode;

    private String reasonMessage;
}
