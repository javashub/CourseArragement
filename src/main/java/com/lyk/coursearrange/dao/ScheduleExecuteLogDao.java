package com.lyk.coursearrange.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyk.coursearrange.entity.ScheduleExecuteLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 排课执行日志 Mapper。
 */
@Mapper
public interface ScheduleExecuteLogDao extends BaseMapper<ScheduleExecuteLog> {
}
