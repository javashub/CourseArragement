package com.lyk.coursearrange.system.config.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyk.coursearrange.system.config.entity.CfgFeatureToggle;
import com.lyk.coursearrange.system.config.mapper.CfgFeatureToggleMapper;
import com.lyk.coursearrange.system.config.service.CfgFeatureToggleService;
import org.springframework.stereotype.Service;

/**
 * 功能开关服务实现。
 */
@Service
public class CfgFeatureToggleServiceImpl extends ServiceImpl<CfgFeatureToggleMapper, CfgFeatureToggle> implements CfgFeatureToggleService {
}
