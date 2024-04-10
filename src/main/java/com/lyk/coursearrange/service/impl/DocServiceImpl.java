package com.lyk.coursearrange.service.impl;

import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.Doc;
import com.lyk.coursearrange.dao.DocDao;
import com.lyk.coursearrange.entity.request.DocsVO;
import com.lyk.coursearrange.service.DocService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyk.coursearrange.util.AliyunUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * @author lequal
 * @since 2020-05-27
 */
@Service
public class DocServiceImpl extends ServiceImpl<DocDao, Doc> implements DocService {

    @Autowired
    private DocService docService;

    @Override
    public ServerResponse uploadDocs(MultipartFile file) {
        Map map = AliyunUtil.upload(file, null);
        System.out.println("docUrl文件路径为" + map.get("url"));
        return ServerResponse.ofSuccess(map);
    }

    @Override
    public ServerResponse downloadDocs(Integer id) {
        return null;
    }

    @Override
    public ServerResponse addDcos(DocsVO d) {
        Doc doc = new Doc();
        doc.setDescription(d.getDescription());
        doc.setDocName(d.getDocName());
        doc.setFileName(d.getFileName());
        doc.setDocUrl(d.getDocUrl());
        doc.setExpire(d.getExpire());
        doc.setFromUserId(d.getFromUserId());
        doc.setFromUserName(d.getFromUserName());
        doc.setFromUserType(d.getFromUserType());
        doc.setToClassNo(d.getToClassNo());

        boolean b = docService.save(doc);

        if (b) {
            return ServerResponse.ofSuccess("添加文档成功");
        }
        return ServerResponse.ofError("添加文档失败");
    }
}
