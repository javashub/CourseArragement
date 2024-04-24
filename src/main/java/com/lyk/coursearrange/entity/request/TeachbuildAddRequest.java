package com.lyk.coursearrange.entity.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: 15760
 * @Date: 2020/3/22
 * @Descripe:
 */
@Data
public class TeachbuildAddRequest implements Serializable {

    private static final long serialVersionUID = 4997406474824186887L;
    private String teachBuildNo;

    private String teachBuildName;

    private String teachBuildLocation;
}
