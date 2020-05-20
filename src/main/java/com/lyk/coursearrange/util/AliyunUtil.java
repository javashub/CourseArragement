package com.lyk.coursearrange.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author: 15760
 * @Date: 2020/5/19
 * @Descripe:
 */
public class AliyunUtil {

    // bucket域名：arrange.oss-cn-shenzhen.aliyuncs.com

    private static String endpoint = "oss-cn-shenzhen.aliyuncs.com";

    private static String accessKeyId = "LTAI4FgC4srdFY4KPjYNVx1u";

    private static String accessKeySecret = "IKoCvPxq7aMX4Bh9revMk7z30fjqP1";

    private static String bucketName = "arrange";

    /**
     * 文件上传
     */
    public static void upload(MultipartFile file) {
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        // 上传内容到指定的存储空间（bucketName）并保存为指定的文件名称（objectName）。

//        try {
//            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(file.getBytes()));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        // 关闭OSSClient。
        ossClient.shutdown();
    }

    /**
     * 文件下载
     */
    public static void download() {

    }
}
