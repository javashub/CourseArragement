package com.lyk.coursearrange.organization.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.organization.entity.OrgStage;
import com.lyk.coursearrange.organization.query.StageQuery;
import com.lyk.coursearrange.organization.request.OrgStageSaveRequest;
import com.lyk.coursearrange.organization.service.OrgStageService;
import com.lyk.coursearrange.organization.service.OrganizationWriteService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学段控制器。
 */
@RestController
@RequestMapping("/api/stage")
public class OrgStageController {

    private final OrgStageService stageService;
    private final OrganizationWriteService organizationWriteService;

    public OrgStageController(OrgStageService stageService,
                              OrganizationWriteService organizationWriteService) {
        this.stageService = stageService;
        this.organizationWriteService = organizationWriteService;
    }

    @GetMapping("/list")
    public ServerResponse<?> list(@ModelAttribute StageQuery query) {
        LambdaQueryWrapper<OrgStage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrgStage::getDeleted, 0)
                .eq(query.getStatus() != null, OrgStage::getStatus, query.getStatus())
                .orderByAsc(OrgStage::getStageLevel)
                .orderByAsc(OrgStage::getStageCode);
        return ServerResponse.ofSuccess(stageService.list(wrapper));
    }

    @GetMapping("/{id}")
    public ServerResponse<?> detail(@PathVariable("id") Long id) {
        LambdaQueryWrapper<OrgStage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrgStage::getId, id)
                .eq(OrgStage::getDeleted, 0);
        return ServerResponse.ofSuccess(stageService.getOne(wrapper, false));
    }

    @PostMapping
    public ServerResponse<?> save(@Validated @RequestBody OrgStageSaveRequest request) {
        return ServerResponse.ofSuccess(organizationWriteService.saveStage(request));
    }

    @DeleteMapping("/{id}")
    public ServerResponse<?> delete(@PathVariable("id") Long id) {
        organizationWriteService.deleteStage(id);
        return ServerResponse.ofSuccess("删除学段成功");
    }
}
