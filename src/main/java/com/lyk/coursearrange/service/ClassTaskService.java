package com.lyk.coursearrange.service;

import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.ScheduleExecuteLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author lequal
 * @since 2020-04-06
 */
public interface ClassTaskService {

    ServerResponse classScheduling(@Param("semester") String semester);

    long countScheduleTasks();

    List<ScheduleExecuteLog> listRecentExecuteLogs(String semester, Integer limit);

}
