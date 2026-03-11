package com.lyk.coursearrange.common.util;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lyk.coursearrange.common.api.PageResponse;

/**
 * 分页工具类。
 * 步骤说明：
 * 1. 接收 MyBatis-Plus 的 IPage 分页结果。
 * 2. 转换为统一分页响应对象。
 * 3. 供控制器和服务层复用，减少重复代码。
 */
public final class PageUtils {

    private PageUtils() {
    }

    public static <T> PageResponse<T> toPageResponse(IPage<T> page) {
        return PageResponse.of(page);
    }
}
