package com.lyk.coursearrange.organization.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.common.util.PageUtils;
import com.lyk.coursearrange.organization.entity.OrgCampus;
import com.lyk.coursearrange.organization.query.CampusPageQuery;
import com.lyk.coursearrange.organization.request.OrgCampusSaveRequest;
import com.lyk.coursearrange.organization.service.OrgCampusService;
import com.lyk.coursearrange.organization.service.OrganizationWriteService;
import org.apache.commons.lang3.StringUtils;
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
 * 校区控制器。
 */
@RestController
@RequestMapping("/api/campus")
public class OrgCampusController {

    private final OrgCampusService campusService;
    private final OrganizationWriteService organizationWriteService;

    public OrgCampusController(OrgCampusService campusService,
                               OrganizationWriteService organizationWriteService) {
        this.campusService = campusService;
        this.organizationWriteService = organizationWriteService;
    }

    @GetMapping("/page")
    public ServerResponse<?> page(@Validated @ModelAttribute CampusPageQuery query) {
        Page<OrgCampus> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<OrgCampus> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrgCampus::getDeleted, 0)
                .and(StringUtils.isNotBlank(query.getKeyword()), w -> w.like(OrgCampus::getCampusName, query.getKeyword())
                        .or()
                        .like(OrgCampus::getCampusCode, query.getKeyword()))
                .eq(query.getStatus() != null, OrgCampus::getStatus, query.getStatus())
                .orderByAsc(OrgCampus::getSortNo)
                .orderByDesc(OrgCampus::getUpdatedAt);
        return ServerResponse.ofSuccess(PageUtils.toPageResponse(campusService.page(page, wrapper)));
    }

    @GetMapping("/list")
    public ServerResponse<?> list() {
        LambdaQueryWrapper<OrgCampus> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrgCampus::getDeleted, 0)
                .orderByAsc(OrgCampus::getSortNo)
                .orderByAsc(OrgCampus::getCampusCode);
        return ServerResponse.ofSuccess(campusService.list(wrapper));
    }

    @GetMapping("/{id}")
    public ServerResponse<?> detail(@PathVariable("id") Long id) {
        LambdaQueryWrapper<OrgCampus> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrgCampus::getId, id)
                .eq(OrgCampus::getDeleted, 0);
        return ServerResponse.ofSuccess(campusService.getOne(wrapper, false));
    }

    @PostMapping
    public ServerResponse<?> save(@Validated @RequestBody OrgCampusSaveRequest request) {
        return ServerResponse.ofSuccess(organizationWriteService.saveCampus(request));
    }

    @DeleteMapping("/{id}")
    public ServerResponse<?> delete(@PathVariable("id") Long id) {
        organizationWriteService.deleteCampus(id);
        return ServerResponse.ofSuccess("删除校区成功");
    }
}
