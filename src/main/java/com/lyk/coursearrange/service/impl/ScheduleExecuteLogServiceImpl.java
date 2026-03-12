package com.lyk.coursearrange.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyk.coursearrange.dao.ScheduleExecuteLogDao;
import com.lyk.coursearrange.entity.ScheduleExecuteLog;
import com.lyk.coursearrange.service.ScheduleExecuteLogService;
import org.springframework.stereotype.Service;

/**
 * 排课执行日志服务实现。
 */
@Service
public class ScheduleExecuteLogServiceImpl extends ServiceImpl<ScheduleExecuteLogDao, ScheduleExecuteLog>
        implements ScheduleExecuteLogService {
}
