package com.lyk.coursearrange.resource.vo;

import lombok.Data;

/**
 * 教室分页视图对象。
 */
@Data
public class ResourceClassroomPageVO {

    private Long id;

    private String classroomCode;

    private String classroomName;

    private Long buildingId;

    private String buildingCode;

    private String buildingName;

    private Integer seatCount;

    private String roomType;

    private Integer status;

    private String remark;
}
