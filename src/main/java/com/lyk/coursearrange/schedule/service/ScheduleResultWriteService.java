package com.lyk.coursearrange.schedule.service;

import com.lyk.coursearrange.entity.CoursePlan;
import com.lyk.coursearrange.schedule.vo.SchedulingTaskInput;

import java.util.List;

/**
 * 标准课表结果写服务。
 */
public interface ScheduleResultWriteService {

    void replaceScheduleResults(String semester, List<SchedulingTaskInput> schedulingTasks, List<CoursePlan> coursePlans);
}
