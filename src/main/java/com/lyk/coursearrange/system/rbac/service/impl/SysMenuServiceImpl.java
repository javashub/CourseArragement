package com.lyk.coursearrange.system.rbac.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyk.coursearrange.common.constants.SystemConstants;
import com.lyk.coursearrange.system.rbac.entity.SysMenu;
import com.lyk.coursearrange.system.rbac.mapper.SysMenuMapper;
import com.lyk.coursearrange.system.rbac.service.SysMenuService;
import com.lyk.coursearrange.system.rbac.vo.MenuTreeVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 菜单服务实现。
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Override
    public List<SysMenu> listMenusByUserId(Long userId) {
        return baseMapper.selectMenusByUserId(userId);
    }

    @Override
    public List<MenuTreeVO> listMenuTreeByUserId(Long userId) {
        return buildMenuTree(listMenusByUserId(userId));
    }

    @Override
    public List<MenuTreeVO> listEnabledMenuTree() {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getDeleted, 0)
                .eq(SysMenu::getStatus, SystemConstants.Status.ENABLED)
                .orderByAsc(SysMenu::getParentId)
                .orderByAsc(SysMenu::getSortNo)
                .orderByAsc(SysMenu::getId);
        return buildMenuTree(list(wrapper));
    }

    private List<MenuTreeVO> buildMenuTree(List<SysMenu> menus) {
        Map<Long, MenuTreeVO> nodeMap = new LinkedHashMap<>();
        List<SysMenu> sortedMenus = menus.stream()
                .sorted(Comparator.comparing(SysMenu::getSortNo, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(SysMenu::getId))
                .toList();

        for (SysMenu menu : sortedMenus) {
            nodeMap.put(menu.getId(), MenuTreeVO.builder()
                    .id(menu.getId())
                    .menuCode(menu.getMenuCode())
                    .parentId(menu.getParentId())
                    .menuName(menu.getMenuName())
                    .menuType(menu.getMenuType())
                    .routeName(menu.getRouteName())
                    .routePath(menu.getRoutePath())
                    .componentPath(menu.getComponentPath())
                    .icon(menu.getIcon())
                    .permissionCode(menu.getPermissionCode())
                    .isHidden(menu.getIsHidden())
                    .isKeepAlive(menu.getIsKeepAlive())
                    .sortNo(menu.getSortNo())
                    .children(new ArrayList<>())
                    .build());
        }

        List<MenuTreeVO> roots = new ArrayList<>();
        for (MenuTreeVO node : nodeMap.values()) {
            if (node.getParentId() == null || node.getParentId() <= 0) {
                roots.add(node);
                continue;
            }
            MenuTreeVO parent = nodeMap.get(node.getParentId());
            if (parent == null) {
                roots.add(node);
                continue;
            }
            parent.getChildren().add(node);
        }
        return roots;
    }
}
