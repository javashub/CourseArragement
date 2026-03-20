package com.lyk.coursearrange.system.config.controller;

import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.system.config.entity.CfgScheduleRule;
import com.lyk.coursearrange.system.config.request.CfgFeatureToggleBatchSaveRequest;
import com.lyk.coursearrange.system.config.request.CfgScheduleRuleSaveRequest;
import com.lyk.coursearrange.system.config.request.CfgTimeSlotBatchSaveRequest;
import com.lyk.coursearrange.system.config.service.ConfigWriteService;
import com.lyk.coursearrange.system.config.service.ScheduleConfigFacadeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemConfigControllerTest {

    @Mock
    private ScheduleConfigFacadeService scheduleConfigFacadeService;
    @Mock
    private ConfigWriteService configWriteService;

    @Test
    void createScheduleRule_shouldDelegateToWriteService() {
        SystemConfigController controller = new SystemConfigController(scheduleConfigFacadeService, configWriteService);
        CfgScheduleRuleSaveRequest request = new CfgScheduleRuleSaveRequest();
        request.setRuleCode("RULE_GLOBAL_DEFAULT");
        CfgScheduleRule entity = new CfgScheduleRule();
        entity.setId(1L);
        when(configWriteService.saveScheduleRule(request)).thenReturn(entity);

        ServerResponse<?> response = controller.createScheduleRule(request);

        assertTrue(response.isSuccess());
        assertEquals(entity, response.getData());
    }

    @Test
    void updateTimeSlots_shouldDelegateToWriteService() {
        SystemConfigController controller = new SystemConfigController(scheduleConfigFacadeService, configWriteService);
        CfgTimeSlotBatchSaveRequest request = new CfgTimeSlotBatchSaveRequest();
        request.setScheduleRuleId(1L);
        when(configWriteService.saveTimeSlots(request)).thenReturn(List.of());

        ServerResponse<?> response = controller.updateTimeSlots(1L, request);

        assertTrue(response.isSuccess());
        assertEquals(List.of(), response.getData());
    }

    @Test
    void updateFeatureToggles_shouldDelegateToWriteService() {
        SystemConfigController controller = new SystemConfigController(scheduleConfigFacadeService, configWriteService);
        CfgFeatureToggleBatchSaveRequest request = new CfgFeatureToggleBatchSaveRequest();
        request.setItems(List.of());
        when(configWriteService.saveFeatureToggles(request)).thenReturn(List.of());

        ServerResponse<?> response = controller.updateFeatureToggles(request);

        assertTrue(response.isSuccess());
        assertEquals(List.of(), response.getData());
    }
}
