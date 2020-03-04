package com.lyk.coursearrange.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

/**
 * @author: 15760
 * @Date: 2020/2/5
 * @Descripe: 讲师实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Teacher {

    private Integer id;
    // 用户名
    private String username;
    // 登录密码
    private String password;
    // 真实姓名
    private String realname;
    // 职称(初级讲师、高级讲师等)
    private String title;
    // 年龄
    private Integer age;
    // 手机号码
    private String phone;
    // 目前所在地
    private String address;
    // 逻辑删除
    private Integer deleted;
    // 创建时间
    private Date createTime;
    // 更新时间
    private Date updateTime;
    // 头像
    private String avatar;
}
