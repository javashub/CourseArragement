package com.lyk.coursearrange.schedule.engine;

import com.lyk.coursearrange.schedule.engine.model.OptimizationContext;
import com.lyk.coursearrange.schedule.engine.model.SchedulingEngineRequest;
import com.lyk.coursearrange.schedule.engine.model.SchedulingExecutionResult;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 标准排课引擎实现。
 */
@Component
public class StandardSchedulingEngine implements SchedulingEngine {

    /**
     * 第一阶段可行解构建器。
     * 运行期直接使用默认实现，测试场景允许通过专用构造器替换。
     */
    private FeasibleSolutionBuilder feasibleSolutionBuilder;

    /**
     * 第二阶段优化器统一走 {@link Resource} 注入，保持项目注入风格一致。
     */
    @Resource
    private ScheduleOptimizer scheduleOptimizer;

    /**
     * 未排成原因汇总器。
     */
    private SchedulingFailureReporter failureReporter;

    /**
     * 运行期给 Spring 使用的默认构造器。
     * 这里显式初始化非注入型协作者，避免容器在存在测试构造器时回退为无参反射失败。
     */
    public StandardSchedulingEngine() {
        this.feasibleSolutionBuilder = new FeasibleSolutionBuilder(
                new SchedulingConstraintEvaluator(),
                new SchedulingFailureReporter()
        );
        this.failureReporter = new SchedulingFailureReporter();
    }

    StandardSchedulingEngine(ScheduleOptimizer scheduleOptimizer) {
        this();
        this.scheduleOptimizer = scheduleOptimizer;
    }

    StandardSchedulingEngine(FeasibleSolutionBuilder feasibleSolutionBuilder,
                             ScheduleOptimizer scheduleOptimizer,
                             SchedulingFailureReporter failureReporter) {
        this.feasibleSolutionBuilder = feasibleSolutionBuilder;
        this.scheduleOptimizer = scheduleOptimizer;
        this.failureReporter = failureReporter;
    }

    @Override
    public SchedulingExecutionResult execute(SchedulingEngineRequest request) {
        SchedulingExecutionResult feasibleResult = feasibleSolutionBuilder.build(request);
        OptimizationContext optimized = scheduleOptimizer.optimize(OptimizationContext.builder()
                .request(request)
                .assignments(feasibleResult.getAssignments())
                .unscheduledTasks(feasibleResult.getUnscheduledTasks())
                .build());
        List<?> optimizedUnscheduledTasks = optimized.getUnscheduledTasks() == null ? List.of() : optimized.getUnscheduledTasks();
        int taskCount = feasibleResult.getTaskCount() == null ? 0 : feasibleResult.getTaskCount();
        int unscheduledTaskCount = optimizedUnscheduledTasks.size();
        int scheduledTaskCount = Math.max(taskCount - unscheduledTaskCount, 0);
        double successRate = taskCount == 0
                ? 0D
                : BigDecimal.valueOf((double) scheduledTaskCount * 100 / taskCount)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
        return SchedulingExecutionResult.builder()
                .taskCount(taskCount)
                .scheduledTaskCount(scheduledTaskCount)
                .unscheduledTaskCount(unscheduledTaskCount)
                .successRate(successRate)
                .assignments(optimized.getAssignments())
                .unscheduledTasks(optimized.getUnscheduledTasks())
                .constraintSummary(failureReporter.summarize(optimized.getUnscheduledTasks()))
                .build();
    }
}
