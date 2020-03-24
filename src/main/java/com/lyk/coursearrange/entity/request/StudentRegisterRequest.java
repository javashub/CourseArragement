package com.lyk.coursearrange.entity.request;

import lombok.Data;

/**
 * @author: 15760
 * @Date: 2020/3/16
 * @Descripe: 封装学生注册的信息
 */
@Data
public class StudentRegisterRequest {

    // 学号由系统给学生生成，学生通过完善个人信息进行填写其它字段
    private String studentNo;

    private String username;

    private String password;

    private String realname;

    /**
     * 年级
     */
    private String grade;

    private Integer age;

    private String address;

    private String telephone;

}
