package com.lyk.coursearrange.schedule.engine;

import com.lyk.coursearrange.schedule.engine.model.OptimizationContext;

/**
 * 占位优化器。
 *
 * <p>当前默认注入已经切换到 {@link GeneticScheduleOptimizer}，
 * 这里保留一个最小实现，仅用于极少数需要显式禁用优化的测试或回退场景。</p>
 */
public class NoOpScheduleOptimizer implements ScheduleOptimizer {

    @Override
    public OptimizationContext optimize(OptimizationContext context) {
        return context;
    }
}
