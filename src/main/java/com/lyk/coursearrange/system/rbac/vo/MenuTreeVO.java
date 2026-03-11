package com.lyk.coursearrange.system.rbac.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 菜单树视图对象。
 */
@Data
@Builder
public class MenuTreeVO {

    private Long id;

    private String menuCode;

    private Long parentId;

    private String menuName;

    private String menuType;

    private String routeName;

    private String routePath;

    private String componentPath;

    private String icon;

    private String permissionCode;

    private Integer isHidden;

    private Integer isKeepAlive;

    private Integer sortNo;

    private List<MenuTreeVO> children;
}
