package com.lyk.coursearrange.common.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Excel 导入结果视图。
 */
@Data
@Builder
public class ImportResultVO {

    private Integer totalCount;

    private Integer successCount;

    private Integer failedCount;

    private List<String> errors;
}
