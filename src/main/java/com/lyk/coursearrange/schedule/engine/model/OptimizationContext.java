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
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class OptimizationContext {

    private SchedulingEngineRequest request;

    private List<SchedulingAssignment> assignments;

    private List<UnscheduledTaskDetail> unscheduledTasks;

    /**
     * 第一阶段产出的种子染色体，便于调试和测试优化前后的差异。
     */
    private SchedulingChromosome seedChromosome;

    /**
     * 第二阶段优化后的最佳染色体。
     */
    private SchedulingChromosome optimizedChromosome;

    /**
     * 本轮优化过程中最终保留的种群快照。
     */
    private SchedulingPopulation population;
}
