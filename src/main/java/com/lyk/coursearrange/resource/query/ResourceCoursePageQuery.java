package com.lyk.coursearrange.resource.query;

import com.lyk.coursearrange.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课程资源分页查询对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ResourceCoursePageQuery extends PageQuery {

    private String keyword;

    private Integer status;
}
