package com.lyk.coursearrange.common.model;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
public class PageQuery {

    @Min(value = 1, message = "页码不能小于 1")
    private long pageNum = 1;

    @Min(value = 1, message = "每页数量不能小于 1")
    @Max(value = 100, message = "每页数量不能超过 100")
    private long pageSize = 10;
}
