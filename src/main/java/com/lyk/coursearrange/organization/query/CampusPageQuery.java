package com.lyk.coursearrange.organization.query;

import com.lyk.coursearrange.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 校区分页查询对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CampusPageQuery extends PageQuery {

    private String keyword;

    private Integer status;
}
