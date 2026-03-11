package com.lyk.coursearrange.system.rbac.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyk.coursearrange.system.rbac.entity.SysMenu;
import com.lyk.coursearrange.system.rbac.vo.MenuTreeVO;

import java.util.List;

/**
 * 菜单服务。
 */
public interface SysMenuService extends IService<SysMenu> {

    List<SysMenu> listMenusByUserId(Long userId);

    List<MenuTreeVO> listMenuTreeByUserId(Long userId);

    List<MenuTreeVO> listEnabledMenuTree();
}
