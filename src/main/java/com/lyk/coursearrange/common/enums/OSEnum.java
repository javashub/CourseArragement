package com.lyk.coursearrange.common.enums;

/**
 * @author lyk
 * @version 1.0
 * @date 2024/4/19 21:21
 * @description
 */
public enum OSEnum {

    LINUX("Linux"),
    WINDOWS("Windows"),
    MAC("Mac");

    String osType;

    OSEnum(String osType) {
        this.osType = osType;
    }

    public String getOsType() {
        return osType;
    }
}
