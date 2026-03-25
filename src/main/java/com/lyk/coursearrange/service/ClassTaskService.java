package com.lyk.coursearrange.service;

import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.schedule.vo.ScheduleRunLogVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author lequal
 * @since 2020-04-06
 */
public interface ClassTaskService {

    ServerResponse classScheduling(@Param("semester") String semester);

    long countScheduleTasks();

    List<ScheduleRunLogVO> listRecentExecuteLogs(String semester, Integer limit);

}
