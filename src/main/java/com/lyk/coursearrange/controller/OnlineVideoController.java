package com.lyk.coursearrange.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.OnlineVideo;
import com.lyk.coursearrange.entity.request.UserInfoVO;
import com.lyk.coursearrange.service.OnlineVideoService;
import com.lyk.coursearrange.util.AliyunUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lequal
 * @since 2020-06-04
 */
@RestController
@RequestMapping("/onlinevideo")
public class OnlineVideoController {

    @Autowired
    private OnlineVideoService ovs;

    /**
     * 根据课程id获取课程包含的视频
     * @param id
     * @return
     */
    @GetMapping("/get/{id}")
    public ServerResponse getAllVideo(@PathVariable("id") Integer id) {
        return ServerResponse.ofSuccess(ovs.list( new QueryWrapper<OnlineVideo>().eq("online_course_id", id)));
    }

    /**
     * 上传视频并返回url和文件名到前端
     * @return
     */
    @PostMapping("/upload")
    public ServerResponse upload(MultipartFile file) {
        Map<String, Object> map = AliyunUtil.upload(file, null);
        if (map != null) {
            return ServerResponse.ofSuccess(map);
        }
        return ServerResponse.ofError("视频上传失败");
    }

    /**
     * 上传新视频
     * @param
     * @return
     */
    @PostMapping("/add")
    public ServerResponse addVideo(MultipartFile file, @RequestBody UserInfoVO u) {
        Map<String, Object> map = new HashMap();
//        map = AliyunUtil.upload(file, null);
        OnlineVideo onlineVideo = new OnlineVideo();
        // 所属课程的id
        onlineVideo.setOnlineCourseId(u.getCourseId());
        onlineVideo.setVideoName(u.getVideoName());
        onlineVideo.setVideoUrl(u.getVideoUrl());
        onlineVideo.setCover(u.getCover());

//        onlineVideo.setVideoUrl(map.get("url").toString());
//        onlineVideo.setVideoName(map.get("name").toString());
        onlineVideo.setVideoNo(u.getVideoNo()); // 视频编号
        onlineVideo.setFromUserType(u.getUserType());
        onlineVideo.setFromUserId(u.getId());
        onlineVideo.setFromUserName(u.getRealname());

        boolean b = ovs.save(onlineVideo);
        if (b) {
            return ServerResponse.ofSuccess("添加视频成功");
        }
        return ServerResponse.ofError("添加视频失败");
    }

    /**
     * 根据id删除视频
     * @param id
     * @return
     */
    @DeleteMapping("/delete/{id}")
    public ServerResponse deleteVideo(@PathVariable("id") Integer id) {

        boolean b = ovs.removeById(id);

        if (b) {
            return ServerResponse.ofSuccess("删除视频成功");
        }
        return ServerResponse.ofError("删除视频失败");
    }

}

