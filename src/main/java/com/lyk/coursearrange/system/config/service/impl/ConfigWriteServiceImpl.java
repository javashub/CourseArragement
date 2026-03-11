package com.lyk.coursearrange.system.config.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyk.coursearrange.common.enums.ResultCode;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.system.config.entity.CfgFeatureToggle;
import com.lyk.coursearrange.system.config.entity.CfgScheduleRule;
import com.lyk.coursearrange.system.config.entity.CfgTimeSlot;
import com.lyk.coursearrange.system.config.request.CfgFeatureToggleBatchSaveRequest;
import com.lyk.coursearrange.system.config.request.CfgScheduleRuleSaveRequest;
import com.lyk.coursearrange.system.config.request.CfgTimeSlotBatchSaveRequest;
import com.lyk.coursearrange.system.config.service.CfgFeatureToggleService;
import com.lyk.coursearrange.system.config.service.CfgScheduleRuleService;
import com.lyk.coursearrange.system.config.service.CfgTimeSlotService;
import com.lyk.coursearrange.system.config.service.ConfigWriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 配置写服务实现。
 * 步骤说明：
 * 1. 排课规则、功能开关、时间片都属于配置中心写操作。
 * 2. 这些操作存在批量保存场景，因此统一放到应用服务中收口。
 * 3. 时间片批量保存需要事务，保证同一次提交的数据一致性。
 */
@Slf4j
@Service
public class ConfigWriteServiceImpl implements ConfigWriteService {

    private final CfgScheduleRuleService scheduleRuleService;
    private final CfgFeatureToggleService featureToggleService;
    private final CfgTimeSlotService timeSlotService;

    public ConfigWriteServiceImpl(CfgScheduleRuleService scheduleRuleService,
                                  CfgFeatureToggleService featureToggleService,
                                  CfgTimeSlotService timeSlotService) {
        this.scheduleRuleService = scheduleRuleService;
        this.featureToggleService = featureToggleService;
        this.timeSlotService = timeSlotService;
    }

    @Override
    public CfgScheduleRule saveScheduleRule(CfgScheduleRuleSaveRequest request) {
        validateScheduleRuleCodeUnique(request.getId(), request.getRuleCode());
        CfgScheduleRule entity = request.getId() == null ? new CfgScheduleRule() : getScheduleRuleOrThrow(request.getId());
        BeanUtils.copyProperties(request, entity);
        scheduleRuleService.saveOrUpdate(entity);
        log.info("保存排课规则成功，ruleCode={}, id={}", entity.getRuleCode(), entity.getId());
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<CfgFeatureToggle> saveFeatureToggles(CfgFeatureToggleBatchSaveRequest request) {
        List<CfgFeatureToggle> result = new ArrayList<>();
        for (CfgFeatureToggleBatchSaveRequest.Item item : request.getItems()) {
            CfgFeatureToggle entity = item.getId() == null ? new CfgFeatureToggle() : getFeatureToggleOrThrow(item.getId());
            BeanUtils.copyProperties(item, entity);
            featureToggleService.saveOrUpdate(entity);
            result.add(entity);
        }
        log.info("批量保存功能开关成功，count={}", result.size());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<CfgTimeSlot> saveTimeSlots(CfgTimeSlotBatchSaveRequest request) {
        if (scheduleRuleService.getById(request.getScheduleRuleId()) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "排课规则不存在");
        }
        List<CfgTimeSlot> result = new ArrayList<>();
        for (CfgTimeSlotBatchSaveRequest.Item item : request.getItems()) {
            CfgTimeSlot entity = item.getId() == null ? new CfgTimeSlot() : getTimeSlotOrThrow(item.getId());
            BeanUtils.copyProperties(item, entity);
            entity.setScheduleRuleId(request.getScheduleRuleId());
            entity.setSortNo(item.getSortNo() == null ? item.getPeriodNo() : item.getSortNo());
            timeSlotService.saveOrUpdate(entity);
            result.add(entity);
        }
        log.info("批量保存时间片成功，scheduleRuleId={}, count={}", request.getScheduleRuleId(), result.size());
        return result;
    }

    private void validateScheduleRuleCodeUnique(Long id, String ruleCode) {
        LambdaQueryWrapper<CfgScheduleRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CfgScheduleRule::getRuleCode, ruleCode)
                .eq(CfgScheduleRule::getDeleted, 0)
                .ne(id != null, CfgScheduleRule::getId, id);
        if (scheduleRuleService.count(wrapper) > 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "排课规则编码已存在");
        }
    }

    private CfgScheduleRule getScheduleRuleOrThrow(Long id) {
        CfgScheduleRule entity = scheduleRuleService.getById(id);
        if (entity == null || entity.getDeleted() != null && entity.getDeleted() == 1) {
            throw new BusinessException(ResultCode.NOT_FOUND, "排课规则不存在");
        }
        return entity;
    }

    private CfgFeatureToggle getFeatureToggleOrThrow(Long id) {
        CfgFeatureToggle entity = featureToggleService.getById(id);
        if (entity == null || entity.getDeleted() != null && entity.getDeleted() == 1) {
            throw new BusinessException(ResultCode.NOT_FOUND, "功能开关不存在");
        }
        return entity;
    }

    private CfgTimeSlot getTimeSlotOrThrow(Long id) {
        CfgTimeSlot entity = timeSlotService.getById(id);
        if (entity == null || entity.getDeleted() != null && entity.getDeleted() == 1) {
            throw new BusinessException(ResultCode.NOT_FOUND, "时间片不存在");
        }
        return entity;
    }
}
