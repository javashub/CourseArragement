package com.lyk.coursearrange.service;

import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.Doc;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lyk.coursearrange.entity.request.DocsVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author lequal
 * @since 2020-05-27
 */
public interface DocService extends IService<Doc> {

    // 上传文档
    ServerResponse uploadDocs(MultipartFile file);

    // 根据id下载文档
    ServerResponse downloadDocs(Integer id);

    ServerResponse addDcos(DocsVO docsVO);
}
