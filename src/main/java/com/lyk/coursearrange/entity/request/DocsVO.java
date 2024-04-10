package com.lyk.coursearrange.entity.request;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author: 15760
 * @Date: 2020/5/27
 * @Descripe:
 */
@Data
public class DocsVO {

    /**
     * 接收班级
     */
    private String toClassNo;

    private String docName;

    private String fileName;

    // 文件所在的路径
    private String docUrl;

    /**
     * 文件描述
     */
    private String description;

    private Integer expire;

    private String fromUserName;

    private Integer fromUserType;

    private Integer fromUserId;

    // 上传时间需要显示在前端
}
