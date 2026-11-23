package com.lyk.coursearrange.organization.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.common.enums.ResultCode;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.common.util.PageUtils;
import com.lyk.coursearrange.organization.entity.OrgRegionDivision;
import com.lyk.coursearrange.organization.query.RegionDivisionPageQuery;
import com.lyk.coursearrange.organization.service.RegionDivisionSyncService;
import com.lyk.coursearrange.organization.vo.RegionSyncResultVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 行政区划控制器。
 * 说明：
 * 1. 浏览类接口（分页 / 子级列表）沿用菜单可见性控制，与校区等组织域接口一致。
 * 2. 更新接口单独要求按钮权限 btn:region:sync，仅管理员可触发。
 */
@RestController
@RequestMapping("/api/organization/regions")
public class OrgRegionController {

    /**
     * 行政区划服务：分页 / 子级查询复用 IService 能力，同步走 syncFromSource。
     */
    private final RegionDivisionSyncService regionDivisionSyncService;

    public OrgRegionController(RegionDivisionSyncService regionDivisionSyncService) {
        this.regionDivisionSyncService = regionDivisionSyncService;
    }

    /**
     * 分页浏览行政区划。
     * 过滤规则：关键字模糊匹配名称或代码，层级与父级代码精确匹配。
     *
     * @param query 分页查询参数
     * @return 统一分页响应
     */
    @GetMapping("/page")
    public ServerResponse<?> page(@Validated @ModelAttribute RegionDivisionPageQuery query) {
        Page<OrgRegionDivision> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<OrgRegionDivision> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrgRegionDivision::getDeleted, 0)
                // 关键字同时匹配名称与代码，方便按拼音或编码片段检索
                .and(StringUtils.isNotBlank(query.getKeyword()), w -> w
                        .like(OrgRegionDivision::getRegionName, query.getKeyword())
                        .or()
                        .like(OrgRegionDivision::getRegionCode, query.getKeyword()))
                .eq(query.getRegionLevel() != null, OrgRegionDivision::getRegionLevel, query.getRegionLevel())
                .eq(StringUtils.isNotBlank(query.getParentCode()), OrgRegionDivision::getParentCode, query.getParentCode())
                .orderByAsc(OrgRegionDivision::getRegionCode);
        return ServerResponse.ofSuccess(PageUtils.toPageResponse(regionDivisionSyncService.page(page, wrapper)));
    }

    /**
     * 查询指定父级的直属下级区划（供级联选择使用）。
     *
     * @param parentCode 父级行政区划代码，必填
     * @return 直属下级列表，按代码升序
     */
    @GetMapping("/children")
    public ServerResponse<?> children(@RequestParam("parentCode") String parentCode) {
        // 空白代码无法定位父级，直接按参数错误处理
        if (StringUtils.isBlank(parentCode)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "parentCode 不能为空");
        }
        LambdaQueryWrapper<OrgRegionDivision> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrgRegionDivision::getDeleted, 0)
                .eq(OrgRegionDivision::getParentCode, parentCode)
                .orderByAsc(OrgRegionDivision::getRegionCode);
        List<OrgRegionDivision> children = regionDivisionSyncService.list(wrapper);
        return ServerResponse.ofSuccess(children);
    }

    /**
     * 触发一次行政区划数据源同步。
     * 权限：btn:region:sync，仅管理员（ADMIN）被授予。
     *
     * @return 本次同步的新增 / 更新 / 停用条数与耗时
     */
    @SaCheckPermission("btn:region:sync")
    @PostMapping("/sync")
    public ServerResponse<?> sync() {
        RegionSyncResultVO result = regionDivisionSyncService.syncFromSource();
        return ServerResponse.ofSuccess(result);
    }
}
