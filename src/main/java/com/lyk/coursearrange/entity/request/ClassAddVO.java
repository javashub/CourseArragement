package com.lyk.coursearrange.entity.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: 15760
 * @Date: 2020/5/26
 * @Descripe:
 */
@Data
public class ClassAddVO implements Serializable {

    private static final long serialVersionUID = 2204170304448219769L;

    private Integer id;

    private String gradeNo;

    private String classNo;

    private String className;

    private Integer num;

    private String realname;
}
