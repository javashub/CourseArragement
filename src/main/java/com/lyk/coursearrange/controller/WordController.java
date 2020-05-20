package com.lyk.coursearrange.controller;

import com.lyk.coursearrange.common.ServerResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletResponse;

/**
 * @author: 15760
 * @Date: 2020/5/18
 * @Descripe: 文档在线查看
 */
@RestController
public class WordController {

    @Value("${key}")
    private String key;

    @Autowired
    private HttpServletResponse response;

    @GetMapping("/online")
    public ServerResponse test() {
        System.out.println(key);
        return null;
    }

}
