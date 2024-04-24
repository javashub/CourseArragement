package com.lyk.coursearrange.entity.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: 15760
 * @Date: 2020/5/30
 * @Descripe:
 */
@Data
public class PasswordVO implements Serializable {

    private static final long serialVersionUID = -3552372075611472951L;
    private Integer id;

    private String oldPass;

    private String newPass;

    private String rePass;
}
