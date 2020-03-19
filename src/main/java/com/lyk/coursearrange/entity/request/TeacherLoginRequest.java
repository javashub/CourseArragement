package com.lyk.coursearrange.entity.request;

import lombok.Data;

/**
 * @author: 15760
 * @Date: 2020/3/14
 * @Descripe: 封装教师登录的请求体
 */

@Data
public class TeacherLoginRequest {

    private Integer id;

    private String username;

    private String password;

}
