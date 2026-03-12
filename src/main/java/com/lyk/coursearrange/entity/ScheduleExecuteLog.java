package com.lyk.coursearrange.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 排课执行日志。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_schedule_execute_log")
public class ScheduleExecuteLog extends Model<ScheduleExecuteLog> implements Serializable {

    private static final long serialVersionUID = 3581585204175234916L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String semester;

    private Integer taskCount;

    private Integer generatedPlanCount;

    private Integer status;

    private Long durationMs;

    private String message;

    private Long operatorUserId;

    private String operatorName;

    private String operatorType;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Override
    public Serializable pkVal() {
        return this.id;
    }
}
