package com.lyk.coursearrange.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyk.coursearrange.dao.CoursePlanAdjustLogDao;
import com.lyk.coursearrange.entity.CoursePlanAdjustLog;
import com.lyk.coursearrange.service.CoursePlanAdjustLogService;
import org.springframework.stereotype.Service;

/**
 * 调课日志服务实现。
 */
@Service
public class CoursePlanAdjustLogServiceImpl extends ServiceImpl<CoursePlanAdjustLogDao, CoursePlanAdjustLog>
        implements CoursePlanAdjustLogService {
}
