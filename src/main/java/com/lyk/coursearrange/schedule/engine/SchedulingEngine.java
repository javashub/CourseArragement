package com.lyk.coursearrange.schedule.engine;

import com.lyk.coursearrange.schedule.engine.model.SchedulingEngineRequest;
import com.lyk.coursearrange.schedule.engine.model.SchedulingExecutionResult;

/**
 * 排课引擎统一入口。
 */
public interface SchedulingEngine {

    SchedulingExecutionResult execute(SchedulingEngineRequest request);
}
