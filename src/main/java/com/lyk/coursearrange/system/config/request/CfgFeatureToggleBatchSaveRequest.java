package com.lyk.coursearrange.system.config.request;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 功能开关批量保存请求。
 */
@Data
public class CfgFeatureToggleBatchSaveRequest {

    @NotEmpty(message = "功能开关列表不能为空")
    @Valid
    private List<Item> items;

    @Data
    public static class Item {

        private Long id;

        @NotNull(message = "功能开关编码不能为空")
        private String toggleCode;

        @NotNull(message = "功能开关名称不能为空")
        private String toggleName;

        private Long campusId;

        private Long collegeId;

        private Long stageId;

        private Long termId;

        @NotNull(message = "功能开关值不能为空")
        private String toggleValue;

        @NotNull(message = "值类型不能为空")
        private String valueType;

        @NotNull(message = "状态不能为空")
        private Integer status;

        private String remark;
    }
}
