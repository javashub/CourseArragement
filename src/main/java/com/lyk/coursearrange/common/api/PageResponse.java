package com.lyk.coursearrange.common.api;

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
}
