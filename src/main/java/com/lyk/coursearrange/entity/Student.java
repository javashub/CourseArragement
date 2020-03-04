package com.lyk.coursearrange.entity;

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
public class Student {

    private Integer id;
    // 学号
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
    private Integer classNo;
    // 已选课程ID
    private Integer[] courses; // 这里设计需要注意
    // 所在地
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
