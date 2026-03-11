package com.lyk.coursearrange.organization.query;

import com.lyk.coursearrange.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学院分页查询对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CollegePageQuery extends PageQuery {

    private Long campusId;

    private String keyword;

    private Integer status;
}
