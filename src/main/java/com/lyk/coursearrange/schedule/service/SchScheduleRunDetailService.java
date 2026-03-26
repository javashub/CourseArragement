package com.lyk.coursearrange.schedule.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyk.coursearrange.schedule.engine.model.UnscheduledTaskDetail;
import com.lyk.coursearrange.schedule.entity.SchScheduleRunDetail;

import java.util.List;

/**
 * 排课执行失败任务明细服务。
 */
public interface SchScheduleRunDetailService extends IService<SchScheduleRunDetail> {

    void replaceRunDetails(Long runLogId, List<UnscheduledTaskDetail> details);

    List<UnscheduledTaskDetail> listByRunLogId(Long runLogId);
}
