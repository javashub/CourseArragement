package com.lyk.coursearrange.system.config.controller;

import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.system.config.request.CfgFeatureToggleBatchSaveRequest;
import com.lyk.coursearrange.system.config.request.CfgScheduleRuleSaveRequest;
import com.lyk.coursearrange.system.config.request.CfgTimeSlotBatchSaveRequest;
import com.lyk.coursearrange.system.config.service.ConfigWriteService;
import com.lyk.coursearrange.system.config.query.ConfigScopeQuery;
import com.lyk.coursearrange.system.config.query.TimeSlotQuery;
import com.lyk.coursearrange.system.config.service.ScheduleConfigFacadeService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统配置控制器。
 * 步骤说明：
 * 1. 统一对外提供功能开关、排课规则、时间片配置查询能力。
 * 2. 当前阶段先落查询接口，后续再补新增、编辑、保存能力。
 */
@RestController
@RequestMapping("/api/config")
public class SystemConfigController {

    private final ScheduleConfigFacadeService scheduleConfigFacadeService;
    private final ConfigWriteService configWriteService;

    public SystemConfigController(ScheduleConfigFacadeService scheduleConfigFacadeService,
                                  ConfigWriteService configWriteService) {
        this.scheduleConfigFacadeService = scheduleConfigFacadeService;
        this.configWriteService = configWriteService;
    }

    @GetMapping("/features")
    public ServerResponse<?> listFeatureToggles(@ModelAttribute ConfigScopeQuery query) {
        return ServerResponse.ofSuccess(scheduleConfigFacadeService.listFeatureToggles(query));
    }

    @GetMapping("/feature-toggles")
    public ServerResponse<?> getFeatureToggles(@ModelAttribute ConfigScopeQuery query) {
        return listFeatureToggles(query);
    }

    @GetMapping("/schedule")
    public ServerResponse<?> getScheduleConfig(@ModelAttribute ConfigScopeQuery query) {
        return ServerResponse.ofSuccess(scheduleConfigFacadeService.getScheduleConfig(query));
    }

    @GetMapping("/schedule-rules/active")
    public ServerResponse<?> getActiveScheduleRule(@ModelAttribute ConfigScopeQuery query) {
        return getScheduleConfig(query);
    }

    @GetMapping("/time-slots")
    public ServerResponse<?> getScheduleConfigByRuleId(@Validated @ModelAttribute TimeSlotQuery query) {
        return ServerResponse.ofSuccess(scheduleConfigFacadeService.getScheduleConfigByRuleId(query));
    }

    @PostMapping("/schedule-rules")
    public ServerResponse<?> createScheduleRule(@Validated @RequestBody CfgScheduleRuleSaveRequest request) {
        return ServerResponse.ofSuccess(configWriteService.saveScheduleRule(request));
    }

    @PutMapping("/schedule-rules/{id}")
    public ServerResponse<?> updateScheduleRule(@PathVariable("id") Long id,
                                                @Validated @RequestBody CfgScheduleRuleSaveRequest request) {
        request.setId(id);
        return ServerResponse.ofSuccess(configWriteService.saveScheduleRule(request));
    }

    @PutMapping("/feature-toggles")
    public ServerResponse<?> updateFeatureToggles(@Validated @RequestBody CfgFeatureToggleBatchSaveRequest request) {
        return ServerResponse.ofSuccess(configWriteService.saveFeatureToggles(request));
    }

    @PutMapping("/schedule-rules/{id}/time-slots")
    public ServerResponse<?> updateTimeSlots(@PathVariable("id") Long id,
                                             @Validated @RequestBody CfgTimeSlotBatchSaveRequest request) {
        request.setScheduleRuleId(id);
        return ServerResponse.ofSuccess(configWriteService.saveTimeSlots(request));
    }

    @PostMapping("/schedule")
    public ServerResponse<?> saveScheduleRule(@Validated @RequestBody CfgScheduleRuleSaveRequest request) {
        return request.getId() == null ? createScheduleRule(request) : updateScheduleRule(request.getId(), request);
    }

    @PostMapping("/features")
    public ServerResponse<?> saveFeatureToggles(@Validated @RequestBody CfgFeatureToggleBatchSaveRequest request) {
        return updateFeatureToggles(request);
    }

    @PostMapping("/time-slots")
    public ServerResponse<?> saveTimeSlots(@Validated @RequestBody CfgTimeSlotBatchSaveRequest request) {
        return updateTimeSlots(request.getScheduleRuleId(), request);
    }
}
