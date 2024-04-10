package com.lyk.coursearrange.entity;

import com.baomidou.mybatisplus.annotation.*;
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
 * @since 2020-03-13
 */
@TableName("tb_teacher")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Teacher extends Model<Teacher> {

    private static final long serialVersionUID=1L;

    /**
     * id，讲师表
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 教师编号
     */
    private String teacherNo;

    /**
     * 昵称（用户名）
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
     * 标识用户类型2
     */
    private Integer userType;

    /**
     * 职称
     */
    private String jobtitle;

    /**
     * 所属年级
     */
    private String gradeNo;

    /**
     * 证件照(地址)
     */
    private String license;

    /**
     * 教授科目
     */
    private String teach;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 联系电话
     */
    private String telephone;

    /**
     * 电子邮件
     */
    private String email;

    /**
     * 地址
     */
    private String address;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 描述（签名）
     */
    private String description;

    /**
     * 操作权限
     */
    private Integer power;

    /**
     * 优先级
     */
    private Integer piority;

    /**
     * 账号状态
     */
    private Integer status;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer deleted;

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
