package com.lyk.coursearrange.schedule.service;

import com.lyk.coursearrange.schedule.engine.model.SchedulingAssignment;
import com.lyk.coursearrange.schedule.vo.SchedulingTaskInput;

import java.util.List;

/**
 * 标准课表结果写服务。
 */
public interface ScheduleResultWriteService {

    void replaceScheduleResults(String semester,
                                Long runLogId,
                                List<SchedulingTaskInput> schedulingTasks,
                                List<SchedulingAssignment> assignments);
}

