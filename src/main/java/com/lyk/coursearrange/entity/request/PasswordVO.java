package com.lyk.coursearrange.entity.request;

import lombok.Data;

/**
 * @author: 15760
 * @Date: 2020/5/30
 * @Descripe:
 */
@Data
public class PasswordVO {

    private Integer id;

    private String oldPass;

    private String newPass;

    private String rePass;
}
