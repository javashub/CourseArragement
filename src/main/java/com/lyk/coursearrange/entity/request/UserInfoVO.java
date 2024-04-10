package com.lyk.coursearrange.entity.request;

import lombok.Data;

/**
 * @author: 15760
 * @Date: 2020/6/4
 * @Descripe: 添加网课，上传视频时操作用户的信息，类型，id，用户名
 */
@Data
public class UserInfoVO {

    // 课程的id
    private Integer courseId;

    private Integer UserType;

    // 操作者的id
    private Integer id;

    private String realname;

    // 设置视频的编号，比如3-1
    private String videoNo;

    private String videoName;

    private String videoUrl;

    /**
     * 视频封面
     */
    private String cover;

}
