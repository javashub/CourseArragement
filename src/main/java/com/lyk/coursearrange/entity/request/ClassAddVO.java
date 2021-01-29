package com.lyk.coursearrange.entity.request;

import lombok.Data;

/**
 * @author: 15760
 * @Date: 2020/5/26
 * @Descripe:
 */
@Data
public class ClassAddVO {

    // 讲师id
    private Integer id;

    private String gradeNo;

    private String classNo;

    private String className;

    private Integer num;

    private String realname;
}
