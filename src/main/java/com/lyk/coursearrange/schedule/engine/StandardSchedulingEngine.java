package com.lyk.coursearrange.schedule.engine;

import com.lyk.coursearrange.schedule.engine.model.OptimizationContext;
import com.lyk.coursearrange.schedule.engine.model.SchedulingEngineRequest;
import com.lyk.coursearrange.schedule.engine.model.SchedulingExecutionResult;
import org.springframework.stereotype.Component;

/**
 * 标准排课引擎实现。
 */
@Component
public class StandardSchedulingEngine implements SchedulingEngine {

    private final FeasibleSolutionBuilder feasibleSolutionBuilder;
    private final ScheduleOptimizer scheduleOptimizer;
    private final SchedulingFailureReporter failureReporter;

    public StandardSchedulingEngine(ScheduleOptimizer scheduleOptimizer) {
        this.feasibleSolutionBuilder = new FeasibleSolutionBuilder(
                new SchedulingConstraintEvaluator(),
                new SchedulingFailureReporter()
        );
        this.failureReporter = new SchedulingFailureReporter();
        this.scheduleOptimizer = scheduleOptimizer;
    }

    @Override
    public SchedulingExecutionResult execute(SchedulingEngineRequest request) {
        SchedulingExecutionResult feasibleResult = feasibleSolutionBuilder.build(request);
        OptimizationContext optimized = scheduleOptimizer.optimize(OptimizationContext.builder()
                .request(request)
                .assignments(feasibleResult.getAssignments())
                .unscheduledTasks(feasibleResult.getUnscheduledTasks())
                .build());
        return SchedulingExecutionResult.builder()
                .taskCount(feasibleResult.getTaskCount())
                .scheduledTaskCount(feasibleResult.getScheduledTaskCount())
                .unscheduledTaskCount(feasibleResult.getUnscheduledTaskCount())
                .successRate(feasibleResult.getSuccessRate())
                .assignments(optimized.getAssignments())
                .unscheduledTasks(optimized.getUnscheduledTasks())
                .constraintSummary(failureReporter.summarize(optimized.getUnscheduledTasks()))
                .build();
    }
}
