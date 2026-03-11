package com.lyk.coursearrange.auth.vo;

import com.lyk.coursearrange.system.rbac.vo.CurrentUserVO;
import com.lyk.coursearrange.system.rbac.vo.MenuTreeVO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 登录态上下文视图对象。
 * 步骤说明：
 * 1. 聚合当前用户、角色、权限、菜单。
 * 2. 供前端登录后一次性拉取认证上下文，减少接口往返次数。
 */
@Data
@Builder
public class AuthContextVO {

    private CurrentUserVO user;

    private List<String> permissions;

    private List<MenuTreeVO> menus;
}
