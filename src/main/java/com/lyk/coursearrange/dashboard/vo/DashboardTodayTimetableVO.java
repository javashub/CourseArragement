package com.lyk.coursearrange.dashboard.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 工作台今日课表视图对象。
 */
@Data
public class DashboardTodayTimetableVO {

    /**
     * 当前展示的班级编号；当天无课时为空。
     */
    private String classNo;

    private List<DashboardTimetableItemVO> items = new ArrayList<>();
}
