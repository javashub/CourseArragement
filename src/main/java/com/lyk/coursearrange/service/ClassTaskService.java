package com.lyk.coursearrange.service;

import com.lyk.coursearrange.common.ServerResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lyk.coursearrange.entity.ClassTask;
import com.lyk.coursearrange.entity.ScheduleExecuteLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author lequal
 * @since 2020-04-06
 */
public interface ClassTaskService {

    ServerResponse classScheduling(@Param("semester") String semester);

    void ensureLegacyTasksForSemester(String semester);

    long countScheduleTasks();

    IPage<ClassTask> pageLegacyTasks(int pageNum, int pageSize, String semester);

    List<ClassTask> listLegacyTasks(String semester);

    boolean saveLegacyTask(ClassTask classTask);

    ClassTask getLegacyTaskById(Integer id);

    ClassTask getLegacyTask(LambdaQueryWrapper<ClassTask> wrapper, boolean throwEx);

    boolean removeLegacyTaskById(Integer id);

    boolean removeLegacyTasks(LambdaQueryWrapper<ClassTask> wrapper);

    boolean saveLegacyTasksBatch(List<ClassTask> classTasks);

    List<ScheduleExecuteLog> listRecentExecuteLogs(String semester, Integer limit);

}
