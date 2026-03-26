package com.lyk.coursearrange.schedule.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 优化阶段上下文。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimizationContext {

    private SchedulingEngineRequest request;

    private List<SchedulingAssignment> assignments;

    private List<UnscheduledTaskDetail> unscheduledTasks;
}
