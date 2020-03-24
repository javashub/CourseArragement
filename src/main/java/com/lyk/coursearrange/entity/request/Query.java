package com.lyk.coursearrange.entity.request;

import lombok.Data;

import java.util.Map;

/**
 * @author: 15760
 * @Date: 2020/3/21
 * @Descripe:
 */
@Data
public class Query {

    private Map<String, String> param;

    private Map<String, Integer> pageParam;

    private String condition;

    private String gradeNo;

    private String semester;
}
