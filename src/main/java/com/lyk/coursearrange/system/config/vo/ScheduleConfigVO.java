package com.lyk.coursearrange.system.config.vo;

import com.lyk.coursearrange.system.config.entity.CfgScheduleRule;
import com.lyk.coursearrange.system.config.entity.CfgTimeSlot;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 排课配置聚合视图对象。
 */
@Data
@Builder
public class ScheduleConfigVO {

    private CfgScheduleRule scheduleRule;

    private List<CfgTimeSlot> timeSlots;

    private List<FeatureToggleVO> featureToggles;
}
