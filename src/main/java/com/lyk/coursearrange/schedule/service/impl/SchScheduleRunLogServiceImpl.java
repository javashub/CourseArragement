package com.lyk.coursearrange.schedule.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyk.coursearrange.schedule.entity.SchScheduleRunLog;
import com.lyk.coursearrange.schedule.mapper.SchScheduleRunLogMapper;
import com.lyk.coursearrange.schedule.service.SchScheduleRunLogService;
import org.springframework.stereotype.Service;

/**
 * 标准排课执行日志服务实现。
 */
@Service
public class SchScheduleRunLogServiceImpl extends ServiceImpl<SchScheduleRunLogMapper, SchScheduleRunLog>
        implements SchScheduleRunLogService {
}
