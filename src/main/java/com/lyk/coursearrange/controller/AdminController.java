package com.lyk.coursearrange.controller;



import cn.dev33.satoken.stp.StpUtil;
import com.lyk.coursearrange.auth.service.AuthAccountSyncService;
import com.lyk.coursearrange.auth.service.AuthLoginService;
import com.lyk.coursearrange.auth.service.PasswordService;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.common.constants.SystemConstants;
import com.lyk.coursearrange.common.enums.ResultCode;
import com.lyk.coursearrange.common.exception.BusinessException;
import com.lyk.coursearrange.entity.Admin;
import com.lyk.coursearrange.entity.request.PasswordVO;
import com.lyk.coursearrange.entity.request.UserLoginRequest;
import com.lyk.coursearrange.service.AdminService;
import com.lyk.coursearrange.system.rbac.entity.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lequal
 * @since 2020-03-06
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;
    @Autowired
    private PasswordService passwordService;
    @Autowired
    private AuthLoginService authLoginService;
    @Autowired
    private AuthAccountSyncService authAccountSyncService;


    /**
     * 管理员登录
     * @param adminLoginRequest
     * @return
     */
    @PostMapping("/login")
    public ServerResponse adminLogin(@RequestBody UserLoginRequest adminLoginRequest) {
        Map<String, Object> map = new HashMap<>();
        SysUser sysUser = authLoginService.loginAdmin(adminLoginRequest.getUsername(), adminLoginRequest.getPassword());
        if (sysUser != null){
            StpUtil.login("sys_user:" + sysUser.getId());
            String token = StpUtil.getTokenValue();
            Admin admin = adminService.getById(sysUser.getSourceId().intValue());
            map.put("admin", admin);
            map.put("token", token);
            return ServerResponse.ofSuccess(map);
        }
        throw new BusinessException(ResultCode.BUSINESS_ERROR, "用户名或密码错误!");
    }

    /**
     * 管理员更新个人资料
     * @return
     */
    @PostMapping("/modify")
    public ServerResponse modifyAdmin(@RequestBody Admin admin) {
        requireAdminExists(admin.getId());
        return adminService.updateById(admin) ? ServerResponse.ofSuccess("更新成功！")
                : throwBusiness(ResultCode.SYSTEM_ERROR, "更新失败！");
    }

    /**
     * 根据ID查询管理员信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public ServerResponse queryAdmin(@PathVariable("id") Integer id) {
        Admin admin = adminService.getById(id);
        if (admin == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "管理员不存在");
        }
        return ServerResponse.ofSuccess(admin);
    }


    /**
     * 管理员修改密码
     * @param passwordVO
     * @return
     */
    @PostMapping("/password")
    public ServerResponse updatePass(@RequestBody PasswordVO passwordVO) {
        Admin admin = adminService.getById(passwordVO.getId());
        if (admin == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "管理员不存在");
        }
        // 步骤1：校验旧密码，同时兼容明文历史密码与 BCrypt 新密码。
        boolean passwordMatched = passwordService.isEncoded(admin.getPassword())
                ? passwordService.matches(passwordVO.getOldPass(), admin.getPassword())
                : passwordVO.getOldPass().equals(admin.getPassword());
        if (!passwordMatched) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "旧密码错误");
        }
        // 步骤2：新密码统一使用 BCrypt 加密保存。
        admin.setPassword(passwordService.encode(passwordVO.getNewPass()));
        boolean b = adminService.updateById(admin);
        if (b) {
            authAccountSyncService.updatePasswordBySource(SystemConstants.SourceType.ADMIN, admin.getId().longValue(), admin.getPassword());
            return ServerResponse.ofSuccess("密码修改成功");
        }
        throw new BusinessException(ResultCode.SYSTEM_ERROR, "密码更新失败");
    }

    private void requireAdminExists(Integer id) {
        if (id == null || adminService.getById(id) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "管理员不存在");
        }
    }

    private ServerResponse throwBusiness(ResultCode resultCode, String message) {
        throw new BusinessException(resultCode, message);
    }

}
