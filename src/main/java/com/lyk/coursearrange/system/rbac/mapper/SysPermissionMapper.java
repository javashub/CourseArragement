package com.lyk.coursearrange.system.rbac.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyk.coursearrange.system.rbac.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 权限 Mapper。
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    List<String> selectPermissionCodesByUserId(Long userId);
}
