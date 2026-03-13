package com.lyk.coursearrange.schedule.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyk.coursearrange.schedule.entity.SchTask;
import com.lyk.coursearrange.schedule.mapper.SchTaskMapper;
import com.lyk.coursearrange.schedule.service.SchTaskService;
import org.springframework.stereotype.Service;

/**
 * 标准排课任务服务实现。
 */
@Service
public class SchTaskServiceImpl extends ServiceImpl<SchTaskMapper, SchTask> implements SchTaskService {
}
