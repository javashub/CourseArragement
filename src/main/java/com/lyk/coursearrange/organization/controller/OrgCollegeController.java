package com.lyk.coursearrange.organization.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.common.util.PageUtils;
import com.lyk.coursearrange.organization.entity.OrgCollege;
import com.lyk.coursearrange.organization.query.CollegePageQuery;
import com.lyk.coursearrange.organization.request.OrgCollegeSaveRequest;
import com.lyk.coursearrange.organization.service.OrgCollegeService;
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
 * 学院控制器。
 */
@RestController
@RequestMapping("/api/college")
public class OrgCollegeController {

    private final OrgCollegeService collegeService;
    private final OrganizationWriteService organizationWriteService;

    public OrgCollegeController(OrgCollegeService collegeService,
                                OrganizationWriteService organizationWriteService) {
        this.collegeService = collegeService;
        this.organizationWriteService = organizationWriteService;
    }

    @GetMapping("/page")
    public ServerResponse<?> page(@Validated @ModelAttribute CollegePageQuery query) {
        Page<OrgCollege> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<OrgCollege> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrgCollege::getDeleted, 0)
                .eq(query.getCampusId() != null, OrgCollege::getCampusId, query.getCampusId())
                .and(StringUtils.isNotBlank(query.getKeyword()), w -> w.like(OrgCollege::getCollegeName, query.getKeyword())
                        .or()
                        .like(OrgCollege::getCollegeCode, query.getKeyword()))
                .eq(query.getStatus() != null, OrgCollege::getStatus, query.getStatus())
                .orderByAsc(OrgCollege::getSortNo)
                .orderByDesc(OrgCollege::getUpdatedAt);
        return ServerResponse.ofSuccess(PageUtils.toPageResponse(collegeService.page(page, wrapper)));
    }

    @GetMapping("/list")
    public ServerResponse<?> list(@ModelAttribute CollegePageQuery query) {
        LambdaQueryWrapper<OrgCollege> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrgCollege::getDeleted, 0)
                .eq(query.getCampusId() != null, OrgCollege::getCampusId, query.getCampusId())
                .eq(query.getStatus() != null, OrgCollege::getStatus, query.getStatus())
                .orderByAsc(OrgCollege::getSortNo)
                .orderByAsc(OrgCollege::getCollegeCode);
        return ServerResponse.ofSuccess(collegeService.list(wrapper));
    }

    @GetMapping("/{id}")
    public ServerResponse<?> detail(@PathVariable("id") Long id) {
        LambdaQueryWrapper<OrgCollege> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrgCollege::getId, id)
                .eq(OrgCollege::getDeleted, 0);
        return ServerResponse.ofSuccess(collegeService.getOne(wrapper, false));
    }

    @PostMapping
    public ServerResponse<?> save(@Validated @RequestBody OrgCollegeSaveRequest request) {
        return ServerResponse.ofSuccess(organizationWriteService.saveCollege(request));
    }

    @DeleteMapping("/{id}")
    public ServerResponse<?> delete(@PathVariable("id") Long id) {
        organizationWriteService.deleteCollege(id);
        return ServerResponse.ofSuccess("删除学院成功");
    }
}
