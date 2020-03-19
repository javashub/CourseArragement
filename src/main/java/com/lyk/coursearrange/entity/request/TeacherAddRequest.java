package com.lyk.coursearrange.entity.request;

import lombok.Data;

/**
 * @author: 15760
 * @Date: 2020/3/16
 * @Descripe: 管理员添加讲师功能的封装体
 */
@Data
public class TeacherAddRequest {

    private String username;

    private String password;
}
