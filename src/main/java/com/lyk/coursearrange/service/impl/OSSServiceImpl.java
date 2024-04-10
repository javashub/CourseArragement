package com.lyk.coursearrange.service.impl;

import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.service.OSSService;
import com.lyk.coursearrange.util.AliyunUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

/**
 * @author: 15760
 * @Date: 2020/5/21
 * @Descripe: 上传实现类
 */
@Service
public class OSSServiceImpl implements OSSService {

    @Override
    public ServerResponse uploadAvatar(MultipartFile file, Integer id, Integer type) {
        String directory = "avatar/";
        // 调用上传
        Map map = AliyunUtil.upload(file, "");
        return ServerResponse.ofSuccess(map);
    }

    @Override
    public ServerResponse uploadVideo(MultipartFile file, Integer id, Integer type) {
        return null;
    }
}
