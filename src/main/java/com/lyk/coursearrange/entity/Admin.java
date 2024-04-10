package com.lyk.coursearrange.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.io.Serializable;

/**
 *
 * @author lequal
 * @since 2020-03-06
 */

@TableName("tb_admin")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Admin extends Model<Admin> {

    private static final long serialVersionUID=1L;

    /**
     * id
     */
    private Integer id;

    /**
     * 管理员编号
     */
    private String adminNo;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 真实姓名
     */
    private String realname;

    /**
     * 用户类型1
     */
    private Integer userType;

    /**
     * 职称
     */
    private String jobtitle;

    /**
     * 教授科目
     */
    private String teach;

    /**
     * 联系电话
     */
    private String telephone;

    /**
     * 电子邮件
     */
    private String email;

    /**
     * 证件
     */
    private String license;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 地址
     */
    private String address;

    /**
     * 签名
     */
    private String description;

    /**
     * 备注
     */
    private String remark;

    /**
     * 优先级
     */
    private Integer piority;

    /**
     * 权限,默认为1(普通管理员) 0为超级管理员，登录的时候需要进行验证
     */
    private Integer power;

    /**
     * 类型
     */
    private Integer type;

    /**
     * 账号状态
     */
    private Integer status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }


}
