package com.lyk.coursearrange.util;

import java.io.ByteArrayInputStream;

/**
 * @author: 15760
 * @Date: 2020/5/19
 * @Descripe:
 */
public class AliyunUploadUtil {

    // bucket域名：arrange.oss-cn-shenzhen.aliyuncs.com
    
    private String endPoint = "oss-cn-shenzhen.aliyuncs.com";

    private String accessKeyId = "LTAI4FgC4srdFY4KPjYNVx1u";

    private String accessKeySecret = "IKoCvPxq7aMX4Bh9revMk7z30fjqP1";

    private String bucketName = "arrange";

    public static void upload() {
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        // 上传内容到指定的存储空间（bucketName）并保存为指定的文件名称（objectName）。
        String content = "Hello OSS";
        ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(content.getBytes()));

        // 关闭OSSClient。
        ossClient.shutdown();
    }
}
