package com.lyk.coursearrange.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author: 15760
 * @Date: 2020/2/5
 * @Descripe: 学生实体类(包括学生的所有属性)
 * 字段正在完善中。。。。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_student")
public class Student {

    private Integer id;
    // 学号
    @TableField("student_no")
    private String studentNo;
    // 昵称（用户名）
    private String username;
    // 登录密码
    private String password;
    // 真实姓名
    private String realname;
    // 年龄
    private Integer age;
    // 就读年级(高一、高二、高三)
    private String grade;
    // 所在班级(班级编号、名称)
    @TableField("class_no")
    private Integer classNo;
    // 已选课程ID
    private String courses; // 这里设计需要注意类型为数组还是字符串切割课程id
    // 所在地
    private String address;
    // 联系方式
    private String telephone;
    // 逻辑删除
    private Integer deleted;
    // 创建时间
    @TableField("create_time")
    private Date createTime;
    // 更新时间
    @TableField("update_time")
    private Date updateTime;
    // 头像
    private String avatar;
    // 账号状态
    private Integer status;
}
