package com.lyk.coursearrange.schedule.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 排课任务元数据工具类。
 */
public interface ScheduleTaskMetaUtils {

    /**
     * 解析 remark 中的 key=value 元数据。
     *
     * @param remark 任务备注
     * @return 元数据映射
     */
    static Map<String, String> parseTaskRemark(String remark) {
        Map<String, String> result = new HashMap<>();
        if (remark == null || remark.isBlank()) {
            return result;
        }
        String[] parts = remark.split(",");
        for (String part : parts) {
            String[] keyValue = part.split("=", 2);
            if (keyValue.length == 2) {
                result.put(keyValue[0], keyValue[1]);
            }
        }
        return result;
    }
}
