package com.lyk.coursearrange.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author: 15760
 * @Date: 2020/5/19
 * @Descripe: 阿里云OSS工具类
 */
public class AliyunUtil {

    // bucket域名：arrange.oss-cn-shenzhen.aliyuncs.com
    @Value("${aliyun.oss.file.endpoint}")
    private static String endpoint = "oss-cn-shenzhen.aliyuncs.com";

    @Value("${aliyun.oss.file.accessKeyId}")
    private static String accessKeyId = "LTAI4FgC4srdFY4KPjYNVx1u";

    @Value("${aliyun.oss.file.accessKeySecret}")
    private static String accessKeySecret = "IKoCvPxq7aMX4Bh9revMk7z30fjqP1";

    @Value("${aliyun.oss.file.bucketName}")
    private static String bucketName = "arrange";

    /**
     * 文件上传成功返回路径
     */
    public static Map<String, Object> upload(MultipartFile file, String directory) {

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        String url = "";
        Map map = null;

        try {
            // 获取输入流
            InputStream inputStream = file.getInputStream();
            String fileName = file.getOriginalFilename();
            //1 在文件名称里面添加随机唯一的值
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            // 随机id
            String newFileName = uuid + fileName;
//            String datePath = new DateTime().toString("yyyy/MM/dd");
            //拼接
            //  2019/11/12/ewtqr313401.jpg
//            fileName = datePath+"/"+fileName;
            // 上传
            ossClient.putObject(bucketName, newFileName, inputStream);
            // 关闭OSSClient。
            ossClient.shutdown();

            url = "https://" + bucketName + "." + endpoint + "/" + newFileName;
            map = new HashMap();
            map.put("url", url);
            map.put("name", fileName);
            return map;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 文件下载
     */
    public static void download(String fileName) {

    }

}
