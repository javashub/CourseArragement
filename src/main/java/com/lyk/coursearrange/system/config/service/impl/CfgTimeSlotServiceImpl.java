package com.lyk.coursearrange.system.config.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyk.coursearrange.system.config.entity.CfgTimeSlot;
import com.lyk.coursearrange.system.config.mapper.CfgTimeSlotMapper;
import com.lyk.coursearrange.system.config.service.CfgTimeSlotService;
import org.springframework.stereotype.Service;

/**
 * 时间片服务实现。
 */
@Service
public class CfgTimeSlotServiceImpl extends ServiceImpl<CfgTimeSlotMapper, CfgTimeSlot> implements CfgTimeSlotService {
}
