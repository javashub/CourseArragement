package com.lyk.coursearrange.organization.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 行政区划数据源配置。
 * 说明：
 * 1. 绑定 application.yml 中 app.region.* 配置项，数据源地址可替换（镜像 / 内网缓存），
 *    替换时不需要修改业务代码。
 * 2. 默认地址指向 modood/Administrative-divisions-of-China 的省市区三级带编码数据集
 *    （pca-code.json，约 137 KB，体积小且自带 code）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.region")
public class RegionDivisionProperties {

    /**
     * 数据源地址，返回省市区三级嵌套 JSON。
     */
    private String sourceUrl =
            "https://raw.githubusercontent.com/modood/Administrative-divisions-of-China/master/dist/pca-code.json";

    /**
     * HTTP 连接超时时间（毫秒）。
     */
    private long connectTimeoutMs = 5000;

    /**
     * HTTP 读取超时时间（毫秒），数据集约 3000 条，预留 30 秒。
     */
    private long readTimeoutMs = 30000;
}
