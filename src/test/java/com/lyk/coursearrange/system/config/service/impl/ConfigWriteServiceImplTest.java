package com.lyk.coursearrange.system.config.service.impl;

import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.system.config.entity.CfgScheduleRule;
import com.lyk.coursearrange.system.config.entity.CfgTimeSlot;
import com.lyk.coursearrange.system.config.request.CfgScheduleRuleSaveRequest;
import com.lyk.coursearrange.system.config.request.CfgTimeSlotBatchSaveRequest;
import com.lyk.coursearrange.system.config.service.CfgFeatureToggleService;
import com.lyk.coursearrange.system.config.service.CfgScheduleRuleService;
import com.lyk.coursearrange.system.config.service.CfgTimeSlotService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
class ConfigWriteServiceImplTest {

    @Mock
    private CfgScheduleRuleService scheduleRuleService;
    @Mock
    private CfgFeatureToggleService featureToggleService;
    @Mock
    private CfgTimeSlotService timeSlotService;

    @Test
    void saveScheduleRule_shouldRejectWhenWeekendDisabledButWeekdaysExceedFive() {
        ConfigWriteServiceImpl service = new ConfigWriteServiceImpl(scheduleRuleService, featureToggleService, timeSlotService);
        CfgScheduleRuleSaveRequest request = new CfgScheduleRuleSaveRequest();
        request.setRuleCode("RULE_GLOBAL_DEFAULT");
        request.setRuleName("全局默认规则");
        request.setWeekDays(6);
        request.setDayPeriods(8);
        request.setMorningPeriods(4);
        request.setAfternoonPeriods(4);
        request.setNightPeriods(0);
        request.setAllowWeekend(0);
        request.setDefaultContinuousLimit(2);
        request.setStatus(1);
        request.setIsDefault(1);

        assertThrows(BusinessException.class, () -> service.saveScheduleRule(request));
        verify(scheduleRuleService, never()).saveOrUpdate(any());
    }

    @Test
    void saveTimeSlots_shouldRejectDuplicateWeekdayAndPeriod() {
        ConfigWriteServiceImpl service = new ConfigWriteServiceImpl(scheduleRuleService, featureToggleService, timeSlotService);
        CfgScheduleRule scheduleRule = new CfgScheduleRule();
        scheduleRule.setId(1L);
        scheduleRule.setWeekDays(5);
        scheduleRule.setDayPeriods(8);
        scheduleRule.setAllowWeekend(0);
        when(scheduleRuleService.getById(1L)).thenReturn(scheduleRule);

        CfgTimeSlotBatchSaveRequest request = new CfgTimeSlotBatchSaveRequest();
        request.setScheduleRuleId(1L);
        request.setItems(List.of(buildItem(1, 1, 1, 0), buildItem(1, 1, 1, 0)));

        assertThrows(BusinessException.class, () -> service.saveTimeSlots(request));
        verify(timeSlotService, never()).saveOrUpdate(any());
    }

    @Test
    void saveTimeSlots_shouldRejectTeachingBreakSlot() {
        ConfigWriteServiceImpl service = new ConfigWriteServiceImpl(scheduleRuleService, featureToggleService, timeSlotService);
        CfgScheduleRule scheduleRule = new CfgScheduleRule();
        scheduleRule.setId(1L);
        scheduleRule.setWeekDays(5);
        scheduleRule.setDayPeriods(8);
        scheduleRule.setAllowWeekend(0);
        when(scheduleRuleService.getById(1L)).thenReturn(scheduleRule);

        CfgTimeSlotBatchSaveRequest request = new CfgTimeSlotBatchSaveRequest();
        request.setScheduleRuleId(1L);
        request.setItems(List.of(buildItem(1, 2, 1, 1)));

        assertThrows(BusinessException.class, () -> service.saveTimeSlots(request));
        verify(timeSlotService, never()).saveOrUpdate(any());
    }

    @Test
    void saveTimeSlots_shouldReuseExistingSlotIdsWhenTemplateIsRegenerated() {
        ConfigWriteServiceImpl service = new ConfigWriteServiceImpl(scheduleRuleService, featureToggleService, timeSlotService);
        CfgScheduleRule scheduleRule = new CfgScheduleRule();
        scheduleRule.setId(1L);
        scheduleRule.setWeekDays(5);
        scheduleRule.setDayPeriods(8);
        scheduleRule.setAllowWeekend(0);
        when(scheduleRuleService.getById(1L)).thenReturn(scheduleRule);

        CfgTimeSlot existingSlot1 = new CfgTimeSlot();
        existingSlot1.setId(101L);
        existingSlot1.setScheduleRuleId(1L);
        existingSlot1.setWeekdayNo(1);
        existingSlot1.setPeriodNo(1);
        CfgTimeSlot existingSlot2 = new CfgTimeSlot();
        existingSlot2.setId(102L);
        existingSlot2.setScheduleRuleId(1L);
        existingSlot2.setWeekdayNo(1);
        existingSlot2.setPeriodNo(2);
        when(timeSlotService.list(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class))).thenReturn(List.of(existingSlot1, existingSlot2));
        when(timeSlotService.getById(101L)).thenReturn(existingSlot1);
        when(timeSlotService.getById(102L)).thenReturn(existingSlot2);

        CfgTimeSlotBatchSaveRequest request = new CfgTimeSlotBatchSaveRequest();
        request.setScheduleRuleId(1L);
        request.setItems(List.of(buildItem(1, 1, 1, 0), buildItem(1, 2, 1, 0)));

        service.saveTimeSlots(request);

        ArgumentCaptor<CfgTimeSlot> captor = ArgumentCaptor.forClass(CfgTimeSlot.class);
        verify(timeSlotService, times(2)).saveOrUpdate(captor.capture());
        List<CfgTimeSlot> captured = captor.getAllValues();
        assertEquals(101L, captured.get(0).getId());
        assertEquals(102L, captured.get(1).getId());
        verify(timeSlotService, never()).removePhysicallyByIds(anyList());
    }

    private CfgTimeSlotBatchSaveRequest.Item buildItem(int weekdayNo, int periodNo, int isTeaching, int isFixedBreak) {
        CfgTimeSlotBatchSaveRequest.Item item = new CfgTimeSlotBatchSaveRequest.Item();
        item.setWeekdayNo(weekdayNo);
        item.setPeriodNo(periodNo);
        item.setPeriodName("第" + periodNo + "节");
        item.setTimeGroup("MORNING");
        item.setStartTimeText("08:00");
        item.setEndTimeText("08:45");
        item.setIsTeaching(isTeaching);
        item.setIsFixedBreak(isFixedBreak);
        item.setSortNo(periodNo);
        return item;
    }
}
