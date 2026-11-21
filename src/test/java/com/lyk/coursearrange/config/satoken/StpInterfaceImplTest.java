package com.lyk.coursearrange.config.satoken;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.context.mock.SaTokenContextMockUtil;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.lyk.coursearrange.auth.model.LoginUser;
import com.lyk.coursearrange.auth.service.AuthFacadeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Sa-Token 权限扩展点测试。
 * 说明：使用 Sa-Token 官方 MockContext，不启动 Spring 容器，只验证权限/角色解析与拒绝行为。
 */
@ExtendWith(MockitoExtension.class)
class StpInterfaceImplTest {

    @Mock
    private AuthFacadeService authFacadeService;

    @BeforeEach
    void setUp() {
        SaTokenContextMockUtil.setMockContext();
        StpUtil.login("sys_user:1");
    }

    @AfterEach
    void tearDown() {
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
        SaTokenContextMockUtil.clearContext();
    }

    @Test
    void getPermissionList_shouldReturnCurrentUserPermissions() {
        when(authFacadeService.getCurrentPermissionCodes()).thenReturn(List.of("page:dashboard:view"));

        StpInterfaceImpl impl = new StpInterfaceImpl(authFacadeService);

        assertEquals(List.of("page:dashboard:view"), impl.getPermissionList("sys_user:1", "login"));
    }

    @Test
    void getPermissionList_shouldIgnoreMismatchedLoginId() {
        StpInterfaceImpl impl = new StpInterfaceImpl(authFacadeService);

        assertTrue(impl.getPermissionList("sys_user:2", "login").isEmpty());
    }

    @Test
    void getRoleList_shouldReturnCurrentUserRoles() {
        when(authFacadeService.getCurrentLoginUser())
                .thenReturn(LoginUser.builder().roles(List.of("ADMIN")).build());

        StpInterfaceImpl impl = new StpInterfaceImpl(authFacadeService);

        assertEquals(List.of("ADMIN"), impl.getRoleList("sys_user:1", "login"));
    }

    @Test
    void checkPermission_shouldDenyWhenCurrentUserLacksPermission() {
        StpInterface previous = SaManager.getStpInterface();
        try {
            SaManager.setStpInterface(new StpInterface() {
                @Override
                public List<String> getPermissionList(Object loginId, String loginType) {
                    return Collections.emptyList();
                }

                @Override
                public List<String> getRoleList(Object loginId, String loginType) {
                    return Collections.emptyList();
                }
            });

            assertThrows(NotPermissionException.class,
                    () -> StpUtil.checkPermission("page:dashboard:view"));
        } finally {
            SaManager.setStpInterface(previous);
        }
    }
}
