package com.lyk.coursearrange.system.rbac.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyk.coursearrange.system.rbac.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 角色 Mapper。
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    List<String> selectRoleCodesByUserId(Long userId);
}
