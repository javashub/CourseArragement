package com.lyk.coursearrange.entity.request;

import lombok.Data;

/**
 * @author: 15760
 * @Date: 2020/6/4
 * @Descripe:
 */
@Data
public class OnlineCourseAddVO {

    // 网课编号本地自己生成
//    private String onlineNo;

    /**
     * 网课名
     */
    private String onlineName;

    /**
     * 课程简介
     */
    private String description;

    /**
     * 课程封面
     */
    private String cover;

    /**
     * 网课类别id
     */
    private Integer onlineCategoryId;

    /**
     * 网课类别名称
     */
    private String onlineCategoryName;

    /**
     * 操作的用户类型，1为管理员，2为讲师
     */
    private Integer fromUserType;

    /**
     * 操作的用户id
     */
    private Integer fromUserId;

    /**
     * 操作用户名
     */
    private String fromUserName;

}
