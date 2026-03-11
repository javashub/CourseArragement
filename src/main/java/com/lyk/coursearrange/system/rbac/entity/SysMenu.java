package com.lyk.coursearrange.system.rbac.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lyk.coursearrange.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {

    @TableId(type = IdType.AUTO)
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

    private Integer status;
}
