package com.lyk.coursearrange.auth.service.impl;

import cn.dev33.satoken.context.mock.SaTokenContextMockUtil;
import cn.dev33.satoken.stp.StpUtil;
import com.lyk.coursearrange.auth.service.AuthViewStrategy;
import com.lyk.coursearrange.common.enums.ResultCode;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.system.rbac.entity.SysUser;
import com.lyk.coursearrange.system.rbac.service.SysMenuService;
import com.lyk.coursearrange.system.rbac.service.SysPermissionService;
import com.lyk.coursearrange.system.rbac.service.SysRoleService;
import com.lyk.coursearrange.system.rbac.service.SysUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 认证门面主题相关逻辑测试。
 * 说明：主题为空时的 daylight 回退与非法主题拒绝都在纯逻辑方法上验证，
 * 未登录分支由控制器层按既有 BusinessException(UNAUTHORIZED) 契约覆盖。
 */
@ExtendWith(MockitoExtension.class)
class AuthFacadeServiceImplTest {

    @Mock
    private SysUserService sysUserService;
    @Mock
    private SysRoleService sysRoleService;
    @Mock
    private SysPermissionService sysPermissionService;
    @Mock
    private SysMenuService sysMenuService;

    @Test
    void resolveUiTheme_shouldFallbackToDaylightWhenThemeMissing() {
        assertEquals("daylight", AuthFacadeServiceImpl.resolveUiTheme(new SysUser()));
    }

    @Test
    void resolveUiTheme_shouldKeepStoredTheme() {
        SysUser sysUser = new SysUser();
        sysUser.setUiTheme(" deep-teal ");

        assertEquals("deep-teal", AuthFacadeServiceImpl.resolveUiTheme(sysUser));
    }

    @Test
    void normalizeUiTheme_shouldAcceptSupportedThemes() {
        assertEquals("daylight", AuthFacadeServiceImpl.normalizeUiTheme("daylight"));
        assertEquals("deep-teal", AuthFacadeServiceImpl.normalizeUiTheme("DEEP-TEAL"));
        assertEquals("top-nav", AuthFacadeServiceImpl.normalizeUiTheme(" Top-Nav "));
        assertEquals("graphite", AuthFacadeServiceImpl.normalizeUiTheme("GRAPHITE"));
        assertEquals("emerald", AuthFacadeServiceImpl.normalizeUiTheme("EMERALD"));
    }

    @Test
    void normalizeUiTheme_shouldRejectUnknownTheme() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> AuthFacadeServiceImpl.normalizeUiTheme("dark"));

        assertEquals(ResultCode.BAD_REQUEST.getCode(), exception.getCode().intValue());
        assertTrue(exception.getMessage().contains("不支持的界面主题"));
    }

    @Test
    void normalizeUiTheme_shouldRejectBlankTheme() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> AuthFacadeServiceImpl.normalizeUiTheme("  "));

        assertEquals(ResultCode.BAD_REQUEST.getCode(), exception.getCode().intValue());
    }

    @Test
    void updateCurrentUiTheme_shouldRejectInvalidThemeBeforeTouchingLoginState() {
        AuthFacadeServiceImpl service = new AuthFacadeServiceImpl(sysUserService,
                sysRoleService,
                sysPermissionService,
                sysMenuService,
                List.<AuthViewStrategy>of());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.updateCurrentUiTheme("dark"));

        assertEquals(ResultCode.BAD_REQUEST.getCode(), exception.getCode().intValue());
    }

    @Test
    void updateCurrentUiTheme_shouldThrowWhenPersistenceFails() {
        SaTokenContextMockUtil.setMockContext();
        try {
            StpUtil.login("sys_user:910001");
            SysUser sysUser = new SysUser();
            sysUser.setId(910001L);
            sysUser.setUsername("900001");
            sysUser.setRealName("系统管理员");
            sysUser.setUserType("ADMIN");
            when(sysUserService.getById(910001L)).thenReturn(sysUser);
            when(sysRoleService.listRoleCodesByUserId(910001L)).thenReturn(List.of("ADMIN"));
            when(sysUserService.updateById(any(SysUser.class))).thenReturn(false);

            AuthFacadeServiceImpl service = new AuthFacadeServiceImpl(sysUserService,
                    sysRoleService,
                    sysPermissionService,
                    sysMenuService,
                    List.<AuthViewStrategy>of());

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> service.updateCurrentUiTheme("deep-teal"));

            assertEquals(ResultCode.SYSTEM_ERROR.getCode(), exception.getCode().intValue());
            assertTrue(exception.getMessage().contains("保存界面主题失败"));
        } finally {
            if (StpUtil.isLogin()) {
                StpUtil.logout();
            }
            SaTokenContextMockUtil.clearContext();
        }
    }
}
