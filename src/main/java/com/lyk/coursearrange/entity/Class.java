package com.lyk.coursearrange.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

/**
 * @author: 15760
 * @Date: 2020/3/4
 * @Descripe: 班级实体类
 */
public class Class {

    //id
    @TableId("id")
    private Integer id;

    // 班级编号
    @TableField("class_no")
    private String classNO;

    // 班级名称
    @TableField("class_name")
    private String className;

    // 学生数量
    @TableField("num")
    private Integer num;

    //
}
