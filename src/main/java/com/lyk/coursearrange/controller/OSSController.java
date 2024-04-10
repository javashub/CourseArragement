package com.lyk.coursearrange.controller;

import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.service.OSSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author: 15760
 * @Date: 2020/5/21
 * @Descripe:
 */
@RestController
@RequestMapping("/aliyun")
public class OSSController {

    @Autowired
    private OSSService ossService;

    /**
     * 上传头像
     * @param file
     * @param id 用户id
     * @param type 用户类型，1：管理员，2：讲师，3：学生
     * @return
     */
    @PostMapping("/avatar")
    public ServerResponse uploadAvatar(MultipartFile file, Integer id, Integer type) {
        return ossService.uploadAvatar(file, id, type);
    }

    /**
     * 上传课程
     * @param file
     * @param id 用户id
     * @param type 用户类型，1：管理员，2：讲师
     * @return
     */
    @PostMapping("/video")
    public ServerResponse uploadVideo(MultipartFile file, Integer id, Integer type) {
        return ossService.uploadVideo(file, id, type);
    }


}
