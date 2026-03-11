package com.lyk.coursearrange.common.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private Long pageNum;
    private Long pageSize;
    private Long total;
    private List<T> records;

    public static <T> PageResponse<T> empty(long pageNum, long pageSize) {
        return PageResponse.<T>builder()
                .pageNum(pageNum)
                .pageSize(pageSize)
                .total(0L)
                .records(Collections.emptyList())
                .build();
    }

    /**
     * 统一转换 MyBatis-Plus 分页结果。
     */
    public static <T> PageResponse<T> of(IPage<T> page) {
        if (page == null) {
            return PageResponse.empty(1L, 10L);
        }
        return PageResponse.<T>builder()
                .pageNum(page.getCurrent())
                .pageSize(page.getSize())
                .total(page.getTotal())
                .records(page.getRecords())
                .build();
    }
}
