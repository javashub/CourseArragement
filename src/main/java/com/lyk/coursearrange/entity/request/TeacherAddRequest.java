package com.lyk.coursearrange.entity.request;

import lombok.Data;

/**
 * @author: 15760
 * @Date: 2020/3/16
 * @Descripe: 管理员添加讲师功能的封装体
 */
@Data
public class TeacherAddRequest {

    private String teacherNo;

    private String username;

    private String password;

    private String realname;

    private String jobtitle;

    private String teach;

    private String telephone;

    private String email;

    private String address;

    private Integer age;
}
