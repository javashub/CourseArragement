package com.lyk.coursearrange.dashboard.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 工作台教学资源计数视图对象。
 * 说明：JSON 字段按契约输出 teacher / class / classroom，Java 字段名避免使用关键字。
 */
@Data
public class DashboardResourceCountsVO {

    private Long teacher;

    private Long classroom;

    @JsonProperty("class")
    private Long classCount;

    @JsonProperty("class")
    public Long getClassCount() {
        return classCount;
    }

    @JsonProperty("class")
    public void setClassCount(Long classCount) {
        this.classCount = classCount;
    }
}
