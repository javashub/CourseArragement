package com.lyk.coursearrange.organization.query;

import com.lyk.coursearrange.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 行政区划分页查询对象。
 * 说明：keyword 同时匹配名称与代码；层级与父级代码为可选的精确过滤条件。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RegionDivisionPageQuery extends PageQuery {

    /**
     * 关键字，模糊匹配 region_name 或 region_code。
     */
    private String keyword;

    /**
     * 层级精确过滤：1 省 / 2 市 / 3 区县；为空则不过滤。
     */
    private Integer regionLevel;

    /**
     * 父级行政区划代码精确过滤；为空则不过滤。
     */
    private String parentCode;

    /**
     * 每页数量：行政区划字典默认 20（覆盖 PageQuery 的默认 10），上限仍为 100。
     */
    @Min(value = 1, message = "每页数量不能小于 1")
    @Max(value = 100, message = "每页数量不能超过 100")
    private long pageSize = 20;
}
