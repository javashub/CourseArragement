package com.lyk.coursearrange.schedule.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 班级维护请求。
 */
@Data
public class AdminClassSaveRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String gradeNo;
    private String classNo;
    private String className;
    private Integer num;
    private Integer teacherId;
    private String forbiddenTimeSlots;
}
