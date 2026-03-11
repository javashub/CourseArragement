package com.lyk.coursearrange.system.config.service;

import com.lyk.coursearrange.system.config.query.ConfigScopeQuery;
import com.lyk.coursearrange.system.config.query.TimeSlotQuery;
import com.lyk.coursearrange.system.config.vo.FeatureToggleVO;
import com.lyk.coursearrange.system.config.vo.ScheduleConfigVO;

import java.util.List;

/**
 * 排课配置门面服务。
 * 场景说明：
 * 1. 配置中心页面需要同时读取功能开关、排课规则、时间片。
 * 2. 如果前端分别调用多个 service，职责会分散且组装逻辑会重复。
 * 3. 这里采用 Facade 模式，对上层统一暴露配置聚合入口。
 */
public interface ScheduleConfigFacadeService {

    List<FeatureToggleVO> listFeatureToggles(ConfigScopeQuery query);

    ScheduleConfigVO getScheduleConfig(ConfigScopeQuery query);

    ScheduleConfigVO getScheduleConfigByRuleId(TimeSlotQuery query);
}
