package com.lyk.coursearrange.schedule.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 排课引擎使用的教室资源模型。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchedulingClassroom {

    private Long classroomId;

    private String classroomCode;

    private String roomType;

    private Integer seatCount;

    private Long campusId;

    private Long collegeId;
}
