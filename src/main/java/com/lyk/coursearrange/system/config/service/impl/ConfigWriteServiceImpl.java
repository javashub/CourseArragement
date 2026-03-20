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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        validateScheduleRuleStructure(request);
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
        CfgScheduleRule scheduleRule = scheduleRuleService.getById(request.getScheduleRuleId());
        if (scheduleRule == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "排课规则不存在");
        }
        validateTimeSlotItems(scheduleRule, request);
        LambdaQueryWrapper<CfgTimeSlot> existingWrapper = new LambdaQueryWrapper<>();
        existingWrapper.eq(CfgTimeSlot::getScheduleRuleId, request.getScheduleRuleId())
                .eq(CfgTimeSlot::getDeleted, 0);
        List<CfgTimeSlot> existingSlots = timeSlotService.list(existingWrapper);
        Set<Long> keepIds = request.getItems().stream()
                .map(CfgTimeSlotBatchSaveRequest.Item::getId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        List<CfgTimeSlot> deleteSlots = existingSlots.stream()
                .filter(item -> !keepIds.contains(item.getId()))
                .toList();
        if (!deleteSlots.isEmpty()) {
            List<Long> deleteIds = deleteSlots.stream()
                    .map(CfgTimeSlot::getId)
                    .toList();
            timeSlotService.removeByIds(deleteIds);
            log.info("移除旧时间片成功，scheduleRuleId={}, deleteCount={}", request.getScheduleRuleId(), deleteIds.size());
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

    private void validateScheduleRuleStructure(CfgScheduleRuleSaveRequest request) {
        int segmentSum = safeInt(request.getMorningPeriods()) + safeInt(request.getAfternoonPeriods()) + safeInt(request.getNightPeriods());
        if (segmentSum != safeInt(request.getDayPeriods())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "上午、下午、晚间节次数之和必须等于每天总节数");
        }
        if (safeInt(request.getAllowWeekend()) == 0 && safeInt(request.getWeekDays()) > 5) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "未开启周末排课时，每周上课天数不能超过 5 天");
        }
        if (safeInt(request.getDefaultContinuousLimit()) > safeInt(request.getDayPeriods())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "默认连堂上限不能大于每天总节数");
        }
    }

    private void validateTimeSlotItems(CfgScheduleRule scheduleRule, CfgTimeSlotBatchSaveRequest request) {
        Set<String> slotKeys = new HashSet<>();
        for (CfgTimeSlotBatchSaveRequest.Item item : request.getItems()) {
            if (item.getWeekdayNo() < 1 || item.getWeekdayNo() > 7) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "时间片星期范围必须在 1 到 7 之间");
            }
            if (item.getWeekdayNo() > safeInt(scheduleRule.getWeekDays())) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "时间片星期超出了当前排课规则的每周上课天数");
            }
            if (safeInt(scheduleRule.getAllowWeekend()) == 0 && item.getWeekdayNo() > 5) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "当前排课规则未开启周末排课，不能保存周末时间片");
            }
            if (item.getPeriodNo() < 1 || item.getPeriodNo() > safeInt(scheduleRule.getDayPeriods())) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "时间片节次超出了当前排课规则的每天总节数");
            }
            if (item.getIsTeaching() != null && item.getIsTeaching() == 1
                    && item.getIsFixedBreak() != null && item.getIsFixedBreak() == 1) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "固定休息时间片不能同时标记为可上课");
            }
            String slotKey = item.getWeekdayNo() + "-" + item.getPeriodNo();
            if (!slotKeys.add(slotKey)) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "同一星期和节次不能重复保存时间片");
            }
        }
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
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
