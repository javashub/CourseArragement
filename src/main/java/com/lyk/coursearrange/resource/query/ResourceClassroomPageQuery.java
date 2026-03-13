package com.lyk.coursearrange.resource.query;

import com.lyk.coursearrange.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 教室资源分页查询对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ResourceClassroomPageQuery extends PageQuery {

    private String keyword;

    private String buildingCode;

    private Integer status;
}
