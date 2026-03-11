package com.lyk.coursearrange.system.config.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 功能开关视图对象。
 */
@Data
@Builder
public class FeatureToggleVO {

    private String toggleCode;

    private String toggleName;

    private String toggleValue;

    private String valueType;

    private Long campusId;

    private Long collegeId;

    private Long stageId;

    private Long termId;

    private Integer status;
}
