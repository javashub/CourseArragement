package com.lyk.coursearrange.auth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.lyk.coursearrange.auth.model.LoginUser;
import com.lyk.coursearrange.auth.service.AuthFacadeService;
import com.lyk.coursearrange.auth.service.AuthViewStrategy;
import com.lyk.coursearrange.auth.vo.AuthContextVO;
import com.lyk.coursearrange.common.constants.SystemConstants;
import com.lyk.coursearrange.common.enums.ResultCode;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.entity.Admin;
import com.lyk.coursearrange.entity.Student;
import com.lyk.coursearrange.entity.Teacher;
import com.lyk.coursearrange.service.AdminService;
import com.lyk.coursearrange.service.StudentService;
import com.lyk.coursearrange.service.TeacherService;
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

    private final AdminService adminService;
    private final TeacherService teacherService;
    private final StudentService studentService;
    private final SysUserService sysUserService;
    private final SysRoleService sysRoleService;
    private final SysPermissionService sysPermissionService;
    private final SysMenuService sysMenuService;
    private final List<AuthViewStrategy> authViewStrategies;

    public AuthFacadeServiceImpl(AdminService adminService,
                                 TeacherService teacherService,
                                 StudentService studentService,
                                 SysUserService sysUserService,
                                 SysRoleService sysRoleService,
                                 SysPermissionService sysPermissionService,
                                 SysMenuService sysMenuService,
                                 List<AuthViewStrategy> authViewStrategies) {
        this.adminService = adminService;
        this.teacherService = teacherService;
        this.studentService = studentService;
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
        if (loginId.startsWith("sys_user:")) {
            Long userId = Long.parseLong(loginId.substring("sys_user:".length()));
            SysUser sysUser = sysUserService.getById(userId);
            if (sysUser == null || sysUser.getDeleted() != null && sysUser.getDeleted() == 1) {
                throw new BusinessException(ResultCode.NOT_FOUND, "认证账号不存在");
            }
            List<String> roleCodes = sysRoleService.listRoleCodesByUserId(sysUser.getId());
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
        if (loginId.startsWith("admin:")) {
            Long userId = Long.parseLong(loginId.substring("admin:".length()));
            Admin admin = adminService.getById(userId);
            if (admin == null) {
                throw new BusinessException(ResultCode.NOT_FOUND, "管理员不存在");
            }
            return LoginUser.builder()
                    .userId(userId)
                    .username(admin.getAdminNo())
                    .realName(admin.getRealname())
                    .displayName(admin.getRealname())
                    .userType("ADMIN")
                    .roles(List.of(SystemConstants.RoleCode.ADMIN))
                    .build();
        }
        if (loginId.startsWith("teacher:")) {
            Long userId = Long.parseLong(loginId.substring("teacher:".length()));
            Teacher teacher = teacherService.getById(userId);
            if (teacher == null) {
                throw new BusinessException(ResultCode.NOT_FOUND, "教师不存在");
            }
            return LoginUser.builder()
                    .userId(userId)
                    .username(teacher.getTeacherNo())
                    .realName(teacher.getRealname())
                    .displayName(teacher.getRealname())
                    .userType("TEACHER")
                    .roles(List.of(SystemConstants.RoleCode.TEACHER))
                    .build();
        }
        if (loginId.startsWith("student:")) {
            Long userId = Long.parseLong(loginId.substring("student:".length()));
            Student student = studentService.getById(userId);
            if (student == null) {
                throw new BusinessException(ResultCode.NOT_FOUND, "学生不存在");
            }
            return LoginUser.builder()
                    .userId(userId)
                    .username(student.getStudentNo())
                    .realName(student.getRealname())
                    .displayName(student.getRealname())
                    .userType("STUDENT")
                    .roles(List.of(SystemConstants.RoleCode.STUDENT))
                    .build();
        }
        throw new BusinessException(ResultCode.UNAUTHORIZED, "暂不支持的登录类型");
    }

    @Override
    public CurrentUserVO getCurrentUserView() {
        LoginUser loginUser = getCurrentLoginUser();
        SysUser sysUser = getBoundSysUser(loginUser);
        List<String> roleCodes = getRoleCodes(loginUser, sysUser);
        return CurrentUserVO.builder()
                .userId(sysUser == null ? loginUser.getUserId() : sysUser.getId())
                .username(sysUser == null ? loginUser.getUsername() : sysUser.getUsername())
                .realName(sysUser == null ? loginUser.getRealName() : sysUser.getRealName())
                .displayName(resolveDisplayName(loginUser, sysUser))
                .userType(sysUser == null ? loginUser.getUserType() : sysUser.getUserType())
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
        SysUser sysUser = getBoundSysUser(loginUser);
        if (sysUser != null) {
            List<String> permissionCodes = sysPermissionService.listPermissionCodesByUserId(sysUser.getId());
            if (!permissionCodes.isEmpty()) {
                return permissionCodes;
            }
        }
        AuthViewStrategy strategy = getStrategy(loginUser);
        return strategy.buildPermissions(loginUser);
    }

    @Override
    public List<MenuTreeVO> getCurrentMenus() {
        LoginUser loginUser = getCurrentLoginUser();
        SysUser sysUser = getBoundSysUser(loginUser);
        if (sysUser != null) {
            List<MenuTreeVO> menus = sysMenuService.listMenuTreeByUserId(sysUser.getId());
            if (!menus.isEmpty()) {
                return menus;
            }
        }
        AuthViewStrategy strategy = getStrategy(loginUser);
        return strategy.buildMenus(loginUser);
    }

    private SysUser getBoundSysUser(LoginUser loginUser) {
        SysUser currentSysUser = sysUserService.getById(loginUser.getUserId());
        if (currentSysUser != null
                && currentSysUser.getDeleted() != null
                && currentSysUser.getDeleted() == 0
                && loginUser.getUsername().equals(currentSysUser.getUsername())) {
            return currentSysUser;
        }
        String sourceType = switch (loginUser.getUserType()) {
            case SystemConstants.UserType.ADMIN -> SystemConstants.SourceType.ADMIN;
            case SystemConstants.UserType.TEACHER -> SystemConstants.SourceType.TEACHER;
            case SystemConstants.UserType.STUDENT -> SystemConstants.SourceType.STUDENT;
            default -> null;
        };
        if (sourceType == null) {
            return null;
        }
        SysUser sysUser = sysUserService.getBySource(sourceType, loginUser.getUserId());
        if (sysUser != null) {
            log.info("当前登录账号已绑定新RBAC用户，sourceType={}, sourceId={}, sysUserId={}",
                    sourceType, loginUser.getUserId(), sysUser.getId());
        }
        return sysUser;
    }

    private List<String> getRoleCodes(LoginUser loginUser, SysUser sysUser) {
        if (sysUser == null) {
            return loginUser.getRoles();
        }
        List<String> roleCodes = sysRoleService.listRoleCodesByUserId(sysUser.getId());
        if (!roleCodes.isEmpty()) {
            return roleCodes;
        }
        return loginUser.getRoles();
    }

    private String resolveDisplayName(LoginUser loginUser, SysUser sysUser) {
        if (sysUser == null) {
            return loginUser.getDisplayName();
        }
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
