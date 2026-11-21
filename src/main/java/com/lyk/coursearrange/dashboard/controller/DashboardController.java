package com.lyk.coursearrange.dashboard.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.dashboard.service.DashboardOverviewService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 工作台控制器。
 * 说明：只读聚合接口，权限点与菜单保持一致（page:dashboard:view）。
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardOverviewService dashboardOverviewService;

    public DashboardController(DashboardOverviewService dashboardOverviewService) {
        this.dashboardOverviewService = dashboardOverviewService;
    }

    @SaCheckPermission("page:dashboard:view")
    @GetMapping("/overview")
    public ServerResponse<?> overview(@RequestParam(required = false) String classNo) {
        return ServerResponse.ofSuccess(dashboardOverviewService.getOverview(classNo));
    }
}
