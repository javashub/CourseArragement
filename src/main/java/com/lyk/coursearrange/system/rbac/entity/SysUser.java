package com.lyk.coursearrange.system.rbac.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lyk.coursearrange.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 系统用户实体。
 * 步骤说明：
 * 1. 统一承载登录账号。
 * 2. 用 sourceType/sourceId 关联教师、学生等业务实体。
 * 3. 为后续 RBAC3 授权、登录态构建提供基础用户模型。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String userCode;

    private String username;

    private String passwordHash;

    private String passwordSalt;

    private String realName;

    private String displayName;

    private String mobile;

    private String email;

    private String userType;

    private String sourceType;

    private Long sourceId;

    private Integer status;

    private LocalDateTime lastLoginAt;

    private String lastLoginIp;
}
