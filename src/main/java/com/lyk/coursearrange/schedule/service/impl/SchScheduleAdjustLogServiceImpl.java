package com.lyk.coursearrange.schedule.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyk.coursearrange.schedule.entity.SchScheduleAdjustLog;
import com.lyk.coursearrange.schedule.mapper.SchScheduleAdjustLogMapper;
import com.lyk.coursearrange.schedule.service.SchScheduleAdjustLogService;
import org.springframework.stereotype.Service;

/**
 * 标准调课日志服务实现。
 */
@Service
public class SchScheduleAdjustLogServiceImpl extends ServiceImpl<SchScheduleAdjustLogMapper, SchScheduleAdjustLog>
        implements SchScheduleAdjustLogService {
}
