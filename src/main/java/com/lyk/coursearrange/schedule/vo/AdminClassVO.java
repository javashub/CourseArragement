package com.lyk.coursearrange.schedule.vo;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

/**
 * 标准班级视图对象。
 */
@Data
public class AdminClassVO {

    private Long id;

    private String gradeNo;

    private String gradeName;

    private String classNo;

    private String className;

    private Integer num;

    private Long campusId;

    private Long collegeId;

    private Long stageId;

    private Long teacherId;

    private String teacherNo;

    @JsonAlias("realname")
    private String teacherName;

    private String forbiddenTimeSlots;

    private String remark;

    private Integer status;
}
