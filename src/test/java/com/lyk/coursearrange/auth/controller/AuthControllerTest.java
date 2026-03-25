package com.lyk.coursearrange.auth.controller;

import com.lyk.coursearrange.auth.request.AuthLoginRequest;
import com.lyk.coursearrange.auth.service.AuthFacadeService;
import com.lyk.coursearrange.auth.service.AuthLoginService;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.system.rbac.entity.SysUser;
import com.lyk.coursearrange.system.rbac.vo.CurrentUserVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
