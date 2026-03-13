package com.lyk.coursearrange.service;

import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.ClassTask;
import com.lyk.coursearrange.entity.ScheduleExecuteLog;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author lequal
 * @since 2020-04-06
 */
public interface ClassTaskService extends IService<ClassTask> {

    ServerResponse classScheduling(@Param("semester") String semester);

    void ensureLegacyTasksForSemester(String semester);

    long countScheduleTasks();

    List<ScheduleExecuteLog> listRecentExecuteLogs(String semester, Integer limit);

}
