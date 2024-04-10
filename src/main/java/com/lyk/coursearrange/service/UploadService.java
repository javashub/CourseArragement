package com.lyk.coursearrange.service;

import com.lyk.coursearrange.common.ServerResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author: 15760
 * @Date: 2020/5/13
 * @Descripe:
 */
public interface UploadService {

    ServerResponse upload(MultipartFile file);

}
