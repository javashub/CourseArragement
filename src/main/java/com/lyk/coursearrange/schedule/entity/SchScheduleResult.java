package com.lyk.coursearrange.schedule.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lyk.coursearrange.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 标准课表结果实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sch_schedule_result")
public class SchScheduleResult extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long runLogId;

    private Long taskId;

    private Long schoolYearId;

    private Long termId;

    private Long campusId;

    private Long collegeId;

    private Long stageId;

    private Long gradeId;

    private Long adminClassId;

    private Long teachingClassId;

    private Long courseId;

    private Long teacherId;

    private Long classroomId;

    private Integer weekdayNo;

    private Integer periodNo;

    private String weekRangeType;

    private String weekRangeText;

    private Integer isLocked;

    private String sourceType;

    private Integer conflictFlag;

    private Integer status;
}
