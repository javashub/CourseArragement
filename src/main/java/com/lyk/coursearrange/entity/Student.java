package com.lyk.coursearrange.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * @author lequal
 * @since 2020-03-13
 */
@TableName("tb_student")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Student extends Model<Student> {

    private static final long serialVersionUID=1L;

    /**
     * 学生id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 学号，可以用于登录
     */
    private String studentNo;

    /**
     * 昵称，可以用于登录
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 标识用户类型3
     */
    private Integer userType;

    /**
     * 真实姓名
     */
    private String realname;

    /**
     * 年级
     */
    private String grade;

    /**
     * 所在班级
     */
    private String classNo;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 当前住址
     */
    private String address;

    /**
     * 联系电话
     */
    private String telephone;

    /**
     * 邮件地址
     */
    private String email;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 签名
     */
    private String description;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer deleted;

    /**
     * 账号状态,0为正常，1为封禁
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }


}
