package com.lyk.coursearrange.controller;

import com.lyk.coursearrange.common.ServerResponse;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: 15760
 * @Date: 2020/7/3
 * @Descripe: 第三方登录
 * 1、QQ登录
 * 2、微信登录
 * 3、微博登录
 */
@RestController
public class LoginController {

    /**
     * QQ登录
     * @return
     */
    public ServerResponse QQLogin() {
        return null;
    }

    /**
     * 微信登录
     * @return
     */
    public ServerResponse WeChatLogin() {
        return null;
    }

    /**
     * 微博登录
     * @return
     */
    public ServerResponse WeiboLogin() {
        return null;
    }
}
