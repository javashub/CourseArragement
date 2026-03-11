package com.lyk.coursearrange.system.rbac.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.common.util.PageUtils;
import com.lyk.coursearrange.system.rbac.entity.SysUser;
import com.lyk.coursearrange.system.rbac.query.SysUserPageQuery;
import com.lyk.coursearrange.system.rbac.request.SysUserSaveRequest;
import com.lyk.coursearrange.system.rbac.service.RbacUserWriteService;
import com.lyk.coursearrange.system.rbac.service.SysUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统用户控制器。
 */
@RestController
@RequestMapping("/api/rbac/users")
public class SysUserController {

    private final SysUserService userService;
    private final RbacUserWriteService userWriteService;

    public SysUserController(SysUserService userService, RbacUserWriteService userWriteService) {
        this.userService = userService;
        this.userWriteService = userWriteService;
    }

    @GetMapping("/page")
    public ServerResponse<?> page(@Validated @ModelAttribute SysUserPageQuery query) {
        Page<SysUser> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getDeleted, 0)
                .and(StringUtils.isNotBlank(query.getKeyword()), w -> w.like(SysUser::getUsername, query.getKeyword())
                        .or()
                        .like(SysUser::getRealName, query.getKeyword())
                        .or()
                        .like(SysUser::getUserCode, query.getKeyword()))
                .eq(StringUtils.isNotBlank(query.getUserType()), SysUser::getUserType, query.getUserType())
                .eq(query.getStatus() != null, SysUser::getStatus, query.getStatus())
                .orderByDesc(SysUser::getUpdatedAt);
        return ServerResponse.ofSuccess(PageUtils.toPageResponse(userService.page(page, wrapper)));
    }

    @PostMapping
    public ServerResponse<?> save(@Validated @RequestBody SysUserSaveRequest request) {
        return ServerResponse.ofSuccess(userWriteService.saveUser(request));
    }

    @PostMapping("/{userId}/status/{status}")
    public ServerResponse<?> changeStatus(@PathVariable("userId") Long userId,
                                          @PathVariable("status") Integer status) {
        userWriteService.changeUserStatus(userId, status);
        return ServerResponse.ofSuccess("修改用户状态成功");
    }
}
