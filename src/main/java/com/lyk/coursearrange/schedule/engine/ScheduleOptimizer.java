package com.lyk.coursearrange.schedule.engine;

import com.lyk.coursearrange.schedule.engine.model.OptimizationContext;

/**
 * 排课优化阶段接口。
 */
public interface ScheduleOptimizer {

    OptimizationContext optimize(OptimizationContext context);
}
