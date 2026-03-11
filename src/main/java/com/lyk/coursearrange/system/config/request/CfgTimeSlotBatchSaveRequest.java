package com.lyk.coursearrange.system.config.request;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 时间片批量保存请求。
 */
@Data
public class CfgTimeSlotBatchSaveRequest {

    @NotNull(message = "排课规则ID不能为空")
    private Long scheduleRuleId;

    @NotEmpty(message = "时间片列表不能为空")
    @Valid
    private List<Item> items;

    @Data
    public static class Item {

        private Long id;

        @NotNull(message = "星期不能为空")
        private Integer weekdayNo;

        @NotNull(message = "节次不能为空")
        private Integer periodNo;

        @NotNull(message = "节次名称不能为空")
        private String periodName;

        @NotNull(message = "时间分组不能为空")
        private String timeGroup;

        @NotNull(message = "开始时间不能为空")
        private String startTimeText;

        @NotNull(message = "结束时间不能为空")
        private String endTimeText;

        @NotNull(message = "是否可上课不能为空")
        private Integer isTeaching;

        @NotNull(message = "是否固定休息时间不能为空")
        private Integer isFixedBreak;

        private Integer sortNo;

        private String remark;
    }
}
