package com.lyk.coursearrange.entity.request;

import lombok.Data;

/**
 * @author: 15760
 * @Date: 2020/3/23
 * @Descripe:
 */
@Data
public class ClassroomAddRequest {

    /**
     * 教室编号
     */
    private String classroomNo;

    /**
     * 教室名称
     */
    private String classroomName;

    /**
     * 所属教学楼
     */
    private String teachbuildingNo;

    /**
     * 教室容量
     */
    private Integer capacity;
}
