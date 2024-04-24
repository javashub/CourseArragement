package com.lyk.coursearrange.entity.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: 15760
 * @Date: 2020/5/20
 * @Descripe:
 */
@Data
public class LocationSetVO implements Serializable {

    private static final long serialVersionUID = 8712383833724085301L;

    private String teachBuildNo;

    private String gradeNo;
}
