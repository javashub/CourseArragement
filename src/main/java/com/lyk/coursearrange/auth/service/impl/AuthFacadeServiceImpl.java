package com.lyk.coursearrange.auth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.lyk.coursearrange.auth.model.LoginUser;
import com.lyk.coursearrange.auth.service.AuthFacadeService;
import com.lyk.coursearrange.auth.service.AuthViewStrategy;
import com.lyk.coursearrange.auth.vo.AuthContextVO;
import com.lyk.coursearrange.common.constants.SystemConstants;
import com.lyk.coursearrange.common.enums.ResultCode;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.system.rbac.entity.SysUser;
import com.lyk.coursearrange.system.rbac.service.SysMenuService;
import com.lyk.coursearrange.system.rbac.service.SysPermissionService;
import com.lyk.coursearrange.system.rbac.service.SysRoleService;
import com.lyk.coursearrange.system.rbac.service.SysUserService;
import com.lyk.coursearrange.system.rbac.vo.CurrentUserVO;
import com.lyk.coursearrange.system.rbac.vo.MenuTreeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 认证门面服务实现。
 */
@Slf4j
@Service
public class AuthFacadeServiceImpl implements AuthFacadeService {

    private final SysUserService sysUserService;
    private final SysRoleService sysRoleService;
    private final SysPermissionService sysPermissionService;
    private final SysMenuService sysMenuService;
    private final List<AuthViewStrategy> authViewStrategies;

    public AuthFacadeServiceImpl(SysUserService sysUserService,
                                 SysRoleService sysRoleService,
                                 SysPermissionService sysPermissionService,
                                 SysMenuService sysMenuService,
                                 List<AuthViewStrategy> authViewStrategies) {
        this.sysUserService = sysUserService;
        this.sysRoleService = sysRoleService;
        this.sysPermissionService = sysPermissionService;
        this.sysMenuService = sysMenuService;
        this.authViewStrategies = authViewStrategies;
    }

    @Override
    public LoginUser getCurrentLoginUser() {
        if (!StpUtil.isLogin()) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        String loginId = StpUtil.getLoginIdAsString();
        if (!loginId.startsWith("sys_user:")) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "暂不支持的登录类型");
        }
        Long userId = Long.parseLong(loginId.substring("sys_user:".length()));
        SysUser sysUser = sysUserService.getById(userId);
        if (sysUser == null || sysUser.getDeleted() != null && sysUser.getDeleted() == 1) {
            throw new BusinessException(ResultCode.NOT_FOUND, "认证账号不存在");
        }
        List<String> roleCodes = safeListRoleCodes(sysUser);
        if (roleCodes.isEmpty()) {
            roleCodes = fallbackRolesByUserType(sysUser.getUserType());
        }
        return LoginUser.builder()
                .userId(sysUser.getId())
                .username(sysUser.getUsername())
                .realName(sysUser.getRealName())
                .displayName(resolveSysUserDisplayName(sysUser))
                .userType(sysUser.getUserType())
                .roles(roleCodes)
                .build();
    }

    @Override
    public CurrentUserVO getCurrentUserView() {
        LoginUser loginUser = getCurrentLoginUser();
        SysUser sysUser = requireCurrentSysUser(loginUser.getUserId());
        List<String> roleCodes = getRoleCodes(loginUser, sysUser);
        return CurrentUserVO.builder()
                .userId(sysUser.getId())
                .username(sysUser.getUsername())
                .realName(sysUser.getRealName())
                .displayName(resolveDisplayName(loginUser, sysUser))
                .userType(sysUser.getUserType())
                .roles(roleCodes)
                .build();
    }

    @Override
    public AuthContextVO getCurrentAuthContext() {
        return AuthContextVO.builder()
                .user(getCurrentUserView())
                .permissions(getCurrentPermissionCodes())
                .menus(getCurrentMenus())
                .build();
    }

    @Override
    public List<String> getCurrentPermissionCodes() {
        LoginUser loginUser = getCurrentLoginUser();
        SysUser sysUser = requireCurrentSysUser(loginUser.getUserId());
        List<String> permissionCodes = safeListPermissionCodes(sysUser, loginUser);
        if (!permissionCodes.isEmpty()) {
            return permissionCodes;
        }
        AuthViewStrategy strategy = getStrategy(loginUser);
        return strategy.buildPermissions(loginUser);
    }

    @Override
    public List<MenuTreeVO> getCurrentMenus() {
        LoginUser loginUser = getCurrentLoginUser();
        SysUser sysUser = requireCurrentSysUser(loginUser.getUserId());
        List<MenuTreeVO> menus = safeListMenus(sysUser, loginUser);
        if (!menus.isEmpty()) {
            return menus;
        }
        AuthViewStrategy strategy = getStrategy(loginUser);
        return strategy.buildMenus(loginUser);
    }

    private SysUser requireCurrentSysUser(Long userId) {
        SysUser sysUser = sysUserService.getById(userId);
        if (sysUser == null || sysUser.getDeleted() != null && sysUser.getDeleted() == 1) {
            throw new BusinessException(ResultCode.NOT_FOUND, "认证账号不存在");
        }
        return sysUser;
    }

    private List<String> getRoleCodes(LoginUser loginUser, SysUser sysUser) {
        List<String> roleCodes = safeListRoleCodes(sysUser);
        if (!roleCodes.isEmpty()) {
            return roleCodes;
        }
        return loginUser.getRoles();
    }

    private List<String> safeListRoleCodes(SysUser sysUser) {
        try {
            return sysRoleService.listRoleCodesByUserId(sysUser.getId());
        } catch (Exception exception) {
            log.error("查询用户角色失败，回退默认角色策略，sysUserId={}", sysUser.getId(), exception);
            return new ArrayList<>();
        }
    }

    private List<String> safeListPermissionCodes(SysUser sysUser, LoginUser loginUser) {
        try {
            return sysPermissionService.listPermissionCodesByUserId(sysUser.getId());
        } catch (Exception exception) {
            log.error("查询用户权限失败，回退默认权限策略，sysUserId={}", sysUser.getId(), exception);
            AuthViewStrategy strategy = getStrategy(loginUser);
            return strategy.buildPermissions(loginUser);
        }
    }

    private List<MenuTreeVO> safeListMenus(SysUser sysUser, LoginUser loginUser) {
        try {
            return sysMenuService.listMenuTreeByUserId(sysUser.getId());
        } catch (Exception exception) {
            log.error("查询用户菜单失败，回退默认菜单策略，sysUserId={}", sysUser.getId(), exception);
            AuthViewStrategy strategy = getStrategy(loginUser);
            return strategy.buildMenus(loginUser);
        }
    }

    private String resolveDisplayName(LoginUser loginUser, SysUser sysUser) {
        return resolveSysUserDisplayName(sysUser);
    }

    private AuthViewStrategy getStrategy(LoginUser loginUser) {
        List<String> roleCodes = loginUser.getRoles() == null ? new ArrayList<>() : loginUser.getRoles();
        String roleCode = roleCodes.isEmpty() ? null : roleCodes.get(0);
        return authViewStrategies.stream()
                .filter(strategy -> strategy.supports(roleCode))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ResultCode.FORBIDDEN, "当前角色未配置菜单策略"));
    }

    private String resolveSysUserDisplayName(SysUser sysUser) {
        if (sysUser.getDisplayName() != null && !sysUser.getDisplayName().isBlank()) {
            return sysUser.getDisplayName();
        }
        if (sysUser.getRealName() != null && !sysUser.getRealName().isBlank()) {
            return sysUser.getRealName();
        }
        return sysUser.getUsername();
    }

    private List<String> fallbackRolesByUserType(String userType) {
        if (SystemConstants.UserType.ADMIN.equals(userType)) {
            return List.of(SystemConstants.RoleCode.ADMIN);
        }
        if (SystemConstants.UserType.TEACHER.equals(userType)) {
            return List.of(SystemConstants.RoleCode.TEACHER);
        }
        if (SystemConstants.UserType.STUDENT.equals(userType)) {
            return List.of(SystemConstants.RoleCode.STUDENT);
        }
        return new ArrayList<>();
    }
}
