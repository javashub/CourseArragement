package com.lyk.coursearrange.entity.response;

import com.lyk.coursearrange.entity.LocationInfo;
import lombok.Data;

import java.io.Serializable;

/**
 * @author: 15760
 * @Date: 2020/5/20
 * @Descripe:
 */
@Data
public class LocationVO extends LocationInfo implements Serializable {

    private static final long serialVersionUID = 5046782554274403171L;

    private String teachBuildName;

    private String gradeName;
}
