package com.lyk.coursearrange.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
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
     * 职称
     */
    private String jobtitle;

    /**
     * 证件
     */
    private String license;

    /**
     * 头像
     */
    private String avatar;

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
     * 账号状态
     */
    private Integer status;

    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;



    @Override
    protected Serializable pkVal() {
        return this.id;
    }


}
