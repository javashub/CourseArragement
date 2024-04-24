package com.lyk.coursearrange.entity.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: 15760
 * @Date: 2020/3/23
 * @Descripe:
 */
@Data
public class ClassroomAddRequest implements Serializable {

    private static final long serialVersionUID = -4307053023235633239L;

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
    private String teachbuildNo;

    /**
     * 教室容量
     */
    private Integer capacity;


    private String remark;
}
