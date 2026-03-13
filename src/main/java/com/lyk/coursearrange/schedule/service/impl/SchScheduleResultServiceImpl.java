package com.lyk.coursearrange.schedule.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyk.coursearrange.schedule.entity.SchScheduleResult;
import com.lyk.coursearrange.schedule.mapper.SchScheduleResultMapper;
import com.lyk.coursearrange.schedule.service.SchScheduleResultService;
import org.springframework.stereotype.Service;

/**
 * 标准课表结果服务实现。
 */
@Service
public class SchScheduleResultServiceImpl extends ServiceImpl<SchScheduleResultMapper, SchScheduleResult>
        implements SchScheduleResultService {
}
