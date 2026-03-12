package com.lyk.coursearrange.entity.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 调课请求对象。
 * 步骤说明：
 * 1. 当前阶段先支持在同一学期内调整课程时间片。
 * 2. 教室调整字段一并预留，后续可继续扩展拖拽调教室能力。
 */
@Data
public class CoursePlanAdjustRequest implements Serializable {

    private static final long serialVersionUID = -1961944305152826008L;

    /**
     * 课表主键。
     */
    private Integer id;

    /**
     * 新时间片编码。
     */
    private String classTime;

    /**
     * 新教室编号，可选。
     */
    private String classroomNo;
}
