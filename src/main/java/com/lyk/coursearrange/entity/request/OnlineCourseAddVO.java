package com.lyk.coursearrange.entity.request;

import lombok.Data;

@Data
public class OnlineCourseAddVO {

    private String onlineName;

    private String description;

    private String cover;

    private Integer onlineCategoryId;

    private String onlineCategoryName;

    private Integer fromUserType;

    private Integer fromUserId;

    private String fromUserName;
}
