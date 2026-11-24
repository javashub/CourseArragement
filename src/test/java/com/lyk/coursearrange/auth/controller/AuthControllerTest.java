package com.lyk.coursearrange.auth.controller;

import com.lyk.coursearrange.auth.request.AuthLoginRequest;
import com.lyk.coursearrange.auth.request.AuthUiThemeUpdateRequest;
import com.lyk.coursearrange.auth.service.AuthFacadeService;
import com.lyk.coursearrange.auth.service.AuthLoginService;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.common.enums.ResultCode;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.system.rbac.entity.SysUser;
import com.lyk.coursearrange.system.rbac.vo.CurrentUserVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthFacadeService authFacadeService;
    @Mock
    private AuthLoginService authLoginService;

    @Test
    void login_shouldReturnTokenAndCurrentUserFromSysUser() {
        AuthController controller = spy(new AuthController(authFacadeService, authLoginService));

        AuthLoginRequest request = new AuthLoginRequest();
        request.setUsername("900001");
        request.setPassword("123456");
        request.setUserType("ADMIN");

        SysUser sysUser = new SysUser();
        sysUser.setId(910001L);
        sysUser.setUsername("900001");
        sysUser.setUserType("ADMIN");

        CurrentUserVO currentUser = CurrentUserVO.builder()
                .userId(910001L)
                .username("900001")
                .displayName("系统管理员")
                .userType("ADMIN")
                .roles(List.of("ADMIN"))
                .build();

        when(authLoginService.login("900001", "123456", "ADMIN")).thenReturn(sysUser);
        when(authFacadeService.getCurrentUserView()).thenReturn(currentUser);
        doReturn("mock-token-910001").when(controller).issueLoginToken(sysUser);

        ServerResponse<?> response = controller.login(request);

        assertTrue(response.isSuccess());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getData();
        assertEquals(currentUser, data.get("user"));
        assertEquals("mock-token-910001", data.get("token"));
    }

    @Test
    void updateUiTheme_shouldReturnUpdatedCurrentUser() {
        AuthController controller = new AuthController(authFacadeService, authLoginService);
        CurrentUserVO updated = CurrentUserVO.builder()
                .userId(910001L)
                .username("900001")
                .displayName("系统管理员")
                .userType("ADMIN")
                .uiTheme("deep-teal")
                .roles(List.of("ADMIN"))
                .build();
        when(authFacadeService.updateCurrentUiTheme("deep-teal")).thenReturn(updated);

        ServerResponse<?> response = controller.updateUiTheme(uiThemeRequest("deep-teal"));

        assertTrue(response.isSuccess());
        assertEquals(updated, response.getData());
        assertEquals("deep-teal", ((CurrentUserVO) response.getData()).getUiTheme());
    }

    @Test
    void updateUiTheme_shouldReturnBusinessErrorWhenThemeInvalid() {
        AuthController controller = new AuthController(authFacadeService, authLoginService);
        when(authFacadeService.updateCurrentUiTheme("dark"))
                .thenThrow(new BusinessException(ResultCode.BAD_REQUEST,
                        "不支持的界面主题：dark，仅支持 daylight/deep-teal/top-nav/graphite/emerald"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> controller.updateUiTheme(uiThemeRequest("dark")));

        assertEquals(ResultCode.BAD_REQUEST.getCode(), exception.getCode().intValue());
        assertTrue(exception.getMessage().contains("不支持的界面主题"));
    }

    @Test
    void updateUiTheme_shouldReturnUnauthorizedWhenNotLoggedIn() {
        AuthController controller = new AuthController(authFacadeService, authLoginService);
        when(authFacadeService.updateCurrentUiTheme("daylight"))
                .thenThrow(new BusinessException(ResultCode.UNAUTHORIZED));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> controller.updateUiTheme(uiThemeRequest("daylight")));

        assertEquals(ResultCode.UNAUTHORIZED.getCode(), exception.getCode().intValue());
    }

    private AuthUiThemeUpdateRequest uiThemeRequest(String uiTheme) {
        AuthUiThemeUpdateRequest request = new AuthUiThemeUpdateRequest();
        request.setUiTheme(uiTheme);
        return request;
    }
}
