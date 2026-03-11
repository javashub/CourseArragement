package com.lyk.coursearrange.system.config.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyk.coursearrange.common.constants.SystemConstants;
import com.lyk.coursearrange.system.config.entity.CfgFeatureToggle;
import com.lyk.coursearrange.system.config.entity.CfgScheduleRule;
import com.lyk.coursearrange.system.config.entity.CfgTimeSlot;
import com.lyk.coursearrange.system.config.query.ConfigScopeQuery;
import com.lyk.coursearrange.system.config.query.TimeSlotQuery;
import com.lyk.coursearrange.system.config.service.CfgFeatureToggleService;
import com.lyk.coursearrange.system.config.service.CfgScheduleRuleService;
import com.lyk.coursearrange.system.config.service.CfgTimeSlotService;
import com.lyk.coursearrange.system.config.service.ScheduleConfigFacadeService;
import com.lyk.coursearrange.system.config.vo.FeatureToggleVO;
import com.lyk.coursearrange.system.config.vo.ScheduleConfigVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 排课配置门面服务实现。
 */
@Slf4j
@Service
public class ScheduleConfigFacadeServiceImpl implements ScheduleConfigFacadeService {

    private final CfgFeatureToggleService featureToggleService;
    private final CfgScheduleRuleService scheduleRuleService;
    private final CfgTimeSlotService timeSlotService;

    public ScheduleConfigFacadeServiceImpl(CfgFeatureToggleService featureToggleService,
                                           CfgScheduleRuleService scheduleRuleService,
                                           CfgTimeSlotService timeSlotService) {
        this.featureToggleService = featureToggleService;
        this.scheduleRuleService = scheduleRuleService;
        this.timeSlotService = timeSlotService;
    }

    @Override
    public List<FeatureToggleVO> listFeatureToggles(ConfigScopeQuery query) {
        List<CfgFeatureToggle> toggles = featureToggleService.list(buildFeatureToggleQuery(query));
        log.info("查询功能开关成功，campusId={}, collegeId={}, stageId={}, termId={}, dbCount={}",
                query.getCampusId(), query.getCollegeId(), query.getStageId(), query.getTermId(), toggles.size());
        return mergeBuiltInFeatureToggles(toggles, query);
    }

    @Override
    public ScheduleConfigVO getScheduleConfig(ConfigScopeQuery query) {
        CfgScheduleRule scheduleRule = scheduleRuleService.getOne(buildScheduleRuleQuery(query), false);
        List<CfgTimeSlot> timeSlots = listTimeSlots(scheduleRule == null ? null : scheduleRule.getId());
        return ScheduleConfigVO.builder()
                .scheduleRule(scheduleRule)
                .timeSlots(timeSlots)
                .featureToggles(listFeatureToggles(query))
                .build();
    }

    @Override
    public ScheduleConfigVO getScheduleConfigByRuleId(TimeSlotQuery query) {
        LambdaQueryWrapper<CfgScheduleRule> scheduleRuleWrapper = new LambdaQueryWrapper<>();
        scheduleRuleWrapper.eq(CfgScheduleRule::getId, query.getScheduleRuleId())
                .eq(CfgScheduleRule::getDeleted, 0);
        CfgScheduleRule scheduleRule = scheduleRuleService.getOne(scheduleRuleWrapper, false);
        ConfigScopeQuery scopeQuery = new ConfigScopeQuery();
        if (scheduleRule != null) {
            scopeQuery.setCampusId(scheduleRule.getCampusId());
            scopeQuery.setCollegeId(scheduleRule.getCollegeId());
            scopeQuery.setStageId(scheduleRule.getStageId());
            scopeQuery.setTermId(scheduleRule.getTermId());
        }
        return ScheduleConfigVO.builder()
                .scheduleRule(scheduleRule)
                .timeSlots(listTimeSlots(query.getScheduleRuleId()))
                .featureToggles(listFeatureToggles(scopeQuery))
                .build();
    }

    private LambdaQueryWrapper<CfgFeatureToggle> buildFeatureToggleQuery(ConfigScopeQuery query) {
        LambdaQueryWrapper<CfgFeatureToggle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CfgFeatureToggle::getDeleted, 0)
                .eq(CfgFeatureToggle::getStatus, SystemConstants.Status.ENABLED);
        wrapper.eq(query.getCampusId() != null, CfgFeatureToggle::getCampusId, query.getCampusId());
        wrapper.eq(query.getCollegeId() != null, CfgFeatureToggle::getCollegeId, query.getCollegeId());
        wrapper.eq(query.getStageId() != null, CfgFeatureToggle::getStageId, query.getStageId());
        wrapper.eq(query.getTermId() != null, CfgFeatureToggle::getTermId, query.getTermId());
        wrapper.orderByAsc(CfgFeatureToggle::getToggleCode);
        return wrapper;
    }

    private LambdaQueryWrapper<CfgScheduleRule> buildScheduleRuleQuery(ConfigScopeQuery query) {
        LambdaQueryWrapper<CfgScheduleRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CfgScheduleRule::getDeleted, 0)
                .eq(CfgScheduleRule::getStatus, SystemConstants.Status.ENABLED);
        wrapper.eq(query.getCampusId() != null, CfgScheduleRule::getCampusId, query.getCampusId());
        wrapper.eq(query.getCollegeId() != null, CfgScheduleRule::getCollegeId, query.getCollegeId());
        wrapper.eq(query.getStageId() != null, CfgScheduleRule::getStageId, query.getStageId());
        wrapper.eq(query.getTermId() != null, CfgScheduleRule::getTermId, query.getTermId());
        wrapper.orderByDesc(CfgScheduleRule::getIsDefault)
                .orderByDesc(CfgScheduleRule::getUpdatedAt);
        return wrapper;
    }

    private List<CfgTimeSlot> listTimeSlots(Long ruleId) {
        if (ruleId == null) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<CfgTimeSlot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CfgTimeSlot::getScheduleRuleId, ruleId)
                .eq(CfgTimeSlot::getDeleted, 0)
                .orderByAsc(CfgTimeSlot::getWeekdayNo)
                .orderByAsc(CfgTimeSlot::getPeriodNo);
        return timeSlotService.list(wrapper);
    }

    private List<FeatureToggleVO> mergeBuiltInFeatureToggles(List<CfgFeatureToggle> databaseToggles, ConfigScopeQuery query) {
        Map<String, FeatureToggleVO> featureMap = new LinkedHashMap<>();
        addBuiltInToggle(featureMap, SystemConstants.FeatureToggleCode.ENABLE_MULTI_CAMPUS,
                SystemConstants.FeatureToggleName.ENABLE_MULTI_CAMPUS,
                SystemConstants.FeatureToggleDefaultValue.TRUE, query);
        addBuiltInToggle(featureMap, SystemConstants.FeatureToggleCode.ENABLE_MULTI_COLLEGE,
                SystemConstants.FeatureToggleName.ENABLE_MULTI_COLLEGE,
                SystemConstants.FeatureToggleDefaultValue.TRUE, query);
        addBuiltInToggle(featureMap, SystemConstants.FeatureToggleCode.ENABLE_MULTI_STAGE,
                SystemConstants.FeatureToggleName.ENABLE_MULTI_STAGE,
                SystemConstants.FeatureToggleDefaultValue.TRUE, query);
        addBuiltInToggle(featureMap, SystemConstants.FeatureToggleCode.ENABLE_SHIFT_CLASS,
                SystemConstants.FeatureToggleName.ENABLE_SHIFT_CLASS,
                SystemConstants.FeatureToggleDefaultValue.FALSE, query);
        addBuiltInToggle(featureMap, SystemConstants.FeatureToggleCode.ALLOW_TEACHER_CROSS_CAMPUS,
                SystemConstants.FeatureToggleName.ALLOW_TEACHER_CROSS_CAMPUS,
                SystemConstants.FeatureToggleDefaultValue.FALSE, query);
        addBuiltInToggle(featureMap, SystemConstants.FeatureToggleCode.ALLOW_TEACHER_CROSS_COLLEGE,
                SystemConstants.FeatureToggleName.ALLOW_TEACHER_CROSS_COLLEGE,
                SystemConstants.FeatureToggleDefaultValue.FALSE, query);
        addBuiltInToggle(featureMap, SystemConstants.FeatureToggleCode.ENABLE_TIMETABLE_DRAG,
                SystemConstants.FeatureToggleName.ENABLE_TIMETABLE_DRAG,
                SystemConstants.FeatureToggleDefaultValue.TRUE, query);

        for (CfgFeatureToggle item : databaseToggles) {
            featureMap.put(item.getToggleCode(), FeatureToggleVO.builder()
                    .toggleCode(item.getToggleCode())
                    .toggleName(item.getToggleName())
                    .toggleValue(item.getToggleValue())
                    .valueType(item.getValueType())
                    .campusId(item.getCampusId())
                    .collegeId(item.getCollegeId())
                    .stageId(item.getStageId())
                    .termId(item.getTermId())
                    .status(item.getStatus())
                    .build());
        }
        return featureMap.values().stream()
                .sorted(Comparator.comparing(FeatureToggleVO::getToggleCode))
                .toList();
    }

    private void addBuiltInToggle(Map<String, FeatureToggleVO> featureMap,
                                  String code,
                                  String name,
                                  String value,
                                  ConfigScopeQuery query) {
        featureMap.put(code, FeatureToggleVO.builder()
                .toggleCode(code)
                .toggleName(name)
                .toggleValue(value)
                .valueType("BOOLEAN")
                .campusId(query.getCampusId())
                .collegeId(query.getCollegeId())
                .stageId(query.getStageId())
                .termId(query.getTermId())
                .status(SystemConstants.Status.ENABLED)
                .build());
    }
}
