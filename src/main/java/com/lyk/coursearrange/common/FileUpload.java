package com.lyk.coursearrange.common;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author: 15760
 * @Date: 2020/3/13
 * @Descripe: 七牛云文件上传工具类
 */
@Component
public class FileUpload {

    // 配置密钥
    static String accessKey = "6sLzFMI1OuMEabWiej9QZ7p1NRK0OYR5tv808xVn";
    static String secretKey = "lmr9I8cQVlPEL8YVFNFkPVqCnkOZ--CHWBNxvj9X";
    // 需要上传的空间名
    static String bucket = "lequal";

    static String key = null;

    // 密钥配置
    //static Auth auth = Auth.create(accessKey, secretKey);
    // 创建上传对象
    static UploadManager uploadManager = new UploadManager(new Configuration(Region.autoRegion()));

    /**
     * @param file
     * @return
     */
    public static String upLoad(MultipartFile file) throws IOException {
        DefaultPutRet putRet = null;
        try {
            byte[] uploadBytes = file.getBytes();
            ByteArrayInputStream byteInputStream=new ByteArrayInputStream(uploadBytes);
            Auth auth = Auth.create(accessKey, secretKey);
            String upToken = auth.uploadToken(bucket);
            try {
                Response response = uploadManager.put(byteInputStream,key,upToken,null, null);
                // 解析上传成功的结果
                putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
                System.out.println(putRet.key);
                System.out.println(putRet.hash);
            } catch (QiniuException ex) {
                Response r = ex.response;
                System.err.println(r.toString());
                try {
                    System.err.println(r.bodyString());
                } catch (QiniuException ex2) {
                    // ignore
                }
            }
        } catch (UnsupportedEncodingException ex) {
            // ignore
        }
        return String.format("http://q2caan54b.bkt.clouddn.com/%s",putRet==null?"":putRet.hash);
    }

    public static String[] upLoads(MultipartFile[] files) throws IOException {
        if (files.length < 1){
            return null;
        }
        List<String> list = new LinkedList<>();
        for (MultipartFile file : files) {
            String url = upLoad(file);
            list.add(url);
        }
        return list.toArray(new String[files.length]);
    }
}
