package com.lyk.coursearrange.schedule.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 适应度计算上下文。
 *
 * <p>把优化阶段需要频繁查询的任务、教室和时间片索引提前整理出来，
 * 可以减少交叉、变异和适应度评估过程中的重复构建成本。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchedulingFitnessContext {

    private SchedulingEngineRequest request;

    private Map<Long, SchedulingTask> taskMap;

    private Map<Long, SchedulingClassroom> classroomMap;

    private List<String> timeSlotCodes;
}
