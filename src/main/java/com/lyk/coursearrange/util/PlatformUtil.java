package com.lyk.coursearrange.util;

import cn.hutool.system.SystemUtil;
import lombok.experimental.UtilityClass;

/**
 * @author lyk
 * @version 1.0
 * @date 2024/4/19 21:00
 * @description
 */
@UtilityClass
public class PlatformUtil {

    private static final String LINUX_PATH = "/opt/excel-template/";
    private static final String WINDOWS_PATH = "/excel-template/";
    private static final String MAC_PATH = "/excel-template/";


    public static String getDefaultDownloadPath() {
        String homeDir = SystemUtil.getUserInfo().getHomeDir();
        if (SystemUtil.getOsInfo().isLinux()) {
            return LINUX_PATH;
        } else if (SystemUtil.getOsInfo().isWindows()) {
            return homeDir + WINDOWS_PATH;
        } else if (SystemUtil.getOsInfo().isMac() || SystemUtil.getOsInfo().isMacOsX()) {
            return homeDir + MAC_PATH;
        }
        return "";
    }
}
