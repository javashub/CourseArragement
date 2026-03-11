package com.lyk.coursearrange.system.config.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyk.coursearrange.system.config.entity.CfgScheduleRule;
import com.lyk.coursearrange.system.config.mapper.CfgScheduleRuleMapper;
import com.lyk.coursearrange.system.config.service.CfgScheduleRuleService;
import org.springframework.stereotype.Service;

/**
 * 排课规则服务实现。
 */
@Service
public class CfgScheduleRuleServiceImpl extends ServiceImpl<CfgScheduleRuleMapper, CfgScheduleRule> implements CfgScheduleRuleService {
}
