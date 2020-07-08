package com.lyk.coursearrange.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.GetObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.midi.Soundbank;
import java.io.File;
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
//    @Value("${aliyun.oss.file.endpoint}")
    private static String endpoint = "oss-cn-shenzhen.aliyuncs.com";

//    @Value("${aliyun.oss.file.accessKeyId}")
    private static String accessKeyId = "这里改成你们自己的key";

//    @Value("${aliyun.oss.file.accessKeySecret}")
    private static String accessKeySecret = "这里改成你们自己的密钥";

//    @Value("${aliyun.oss.file.bucketName}")
    private static String bucketName = "arrange";

    /**
     * 文件上传成功返回路径
     * @param file
     * @param directory 选择需要上传到的目录下面，暂时不用，原本想将用户头像、其他文件上传到不同的目录下面的
     * @return
     */
    public static Map<String, Object> upload(MultipartFile file, String directory) {

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        String url = "";
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
            ossClient.putObject(bucketName, fileName, inputStream);
            // 关闭OSSClient。
            ossClient.shutdown();

            url = "https://" + bucketName + "." + endpoint + "/" + fileName;
            System.out.println("url========" + url);
            Map<String, Object> map = new HashMap<>();
            map.put("url", url);
            map.put("name", fileName);
            return map;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 文件下载到本地
     */
    public static String download(String fileName) {
        System.out.println("1" + fileName);
        System.out.println("阿里云开始下载文件到本地");
        String path = "D:\\arrange\\tempfile\\" + fileName;
        File file = new File(path);
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        ossClient.getObject(new GetObjectRequest(bucketName, fileName), file);

        ossClient.shutdown();
        return path;
    }

}
