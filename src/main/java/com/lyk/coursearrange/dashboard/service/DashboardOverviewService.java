package com.lyk.coursearrange.dashboard.service;

import com.lyk.coursearrange.dashboard.vo.DashboardOverviewVO;

/**
 * 工作台总览服务。
 */
public interface DashboardOverviewService {

    /**
     * 查询工作台总览数据，只读。
     *
     * @param classNo 可选班级编号，用于覆盖今日课表默认班级
     * @return 总览视图
     */
    DashboardOverviewVO getOverview(String classNo);
}
