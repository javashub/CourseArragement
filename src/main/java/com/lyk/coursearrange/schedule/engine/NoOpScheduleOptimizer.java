package com.lyk.coursearrange.schedule.engine;

import com.lyk.coursearrange.schedule.engine.model.OptimizationContext;
import org.springframework.stereotype.Component;

/**
 * 本轮仅保留最小可插拔优化器实现。
 */
@Component
public class NoOpScheduleOptimizer implements ScheduleOptimizer {

    @Override
    public OptimizationContext optimize(OptimizationContext context) {
        return context;
    }
}
