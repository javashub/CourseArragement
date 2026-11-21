package com.lyk.coursearrange.config.satoken;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.lyk.coursearrange.auth.service.AuthFacadeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Sa-Token 权限扩展点。
 *
 * 说明：
 * 1. 权限与角色码统一由认证门面按 RBAC 数据解析，未命中时沿用既有角色默认策略。
 * 2. @SaCheckPermission / @SaCheckRole 依赖本扩展点，因此这里必须返回真实权限而不是空集合。
 * 3. 解析异常时按空权限处理，交由 Sa-Token 输出既有鉴权错误结构，不阻断请求链路。
 */
@Slf4j
@Component
public class StpInterfaceImpl implements StpInterface {

    private final AuthFacadeService authFacadeService;

    public StpInterfaceImpl(AuthFacadeService authFacadeService) {
        this.authFacadeService = authFacadeService;
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return resolveCurrentLoginValues(() -> authFacadeService.getCurrentPermissionCodes(), loginId);
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return resolveCurrentLoginValues(() -> authFacadeService.getCurrentLoginUser().getRoles(), loginId);
    }

    private List<String> resolveCurrentLoginValues(Supplier<List<String>> supplier, Object loginId) {
        try {
            if (!StpUtil.isLogin()) {
                return Collections.emptyList();
            }
            if (loginId == null || !String.valueOf(loginId).equals(StpUtil.getLoginIdAsString())) {
                return Collections.emptyList();
            }
            List<String> values = supplier.get();
            return values == null ? Collections.emptyList() : values;
        } catch (Exception exception) {
            log.warn("解析 Sa-Token 权限失败，按空权限处理，loginId={}", loginId, exception);
            return Collections.emptyList();
        }
    }
}
