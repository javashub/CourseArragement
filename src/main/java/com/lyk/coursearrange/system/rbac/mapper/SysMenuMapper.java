package com.lyk.coursearrange.system.rbac.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyk.coursearrange.system.rbac.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 菜单 Mapper。
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    List<SysMenu> selectMenusByUserId(Long userId);
}
