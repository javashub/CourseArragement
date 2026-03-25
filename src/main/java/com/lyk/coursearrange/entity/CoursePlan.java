package com.lyk.coursearrange.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author lequal
 * @since 2020-04-15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoursePlan implements Serializable {

    private static final long serialVersionUID=1L;

    private Integer id;

    private String gradeNo;

    private String classNo;

    private String courseNo;

    private String teacherNo;

    private String classroomNo;

    private String classTime;

    private Integer weeksSum;

    private String semester;

    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
