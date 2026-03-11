package com.lyk.coursearrange.system.config.service;

import com.lyk.coursearrange.system.config.entity.CfgFeatureToggle;
import com.lyk.coursearrange.system.config.entity.CfgScheduleRule;
import com.lyk.coursearrange.system.config.entity.CfgTimeSlot;
import com.lyk.coursearrange.system.config.request.CfgFeatureToggleBatchSaveRequest;
import com.lyk.coursearrange.system.config.request.CfgScheduleRuleSaveRequest;
import com.lyk.coursearrange.system.config.request.CfgTimeSlotBatchSaveRequest;

import java.util.List;

/**
 * 配置写服务。
 */
public interface ConfigWriteService {

    CfgScheduleRule saveScheduleRule(CfgScheduleRuleSaveRequest request);

    List<CfgFeatureToggle> saveFeatureToggles(CfgFeatureToggleBatchSaveRequest request);

    List<CfgTimeSlot> saveTimeSlots(CfgTimeSlotBatchSaveRequest request);
}
