package com.lyk.coursearrange.schedule.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 排课引擎输入。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchedulingEngineRequest {

    private String semester;

    private List<String> timeSlotCodes;

    private List<SchedulingTask> tasks;

    private List<SchedulingClassroom> classrooms;
}
