package com.lyk.coursearrange.service;

import com.lyk.coursearrange.common.ServerResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author: 15760
 * @Date: 2020/5/21
 * @Descripe:
 */
public interface OSSService {

    // 上传头像
    ServerResponse uploadAvatar(MultipartFile file, Integer id, Integer type);

    // 上传视频
    ServerResponse uploadVideo(MultipartFile file, Integer id, Integer type);
}
