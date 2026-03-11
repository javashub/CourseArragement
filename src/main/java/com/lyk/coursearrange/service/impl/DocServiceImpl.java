package com.lyk.coursearrange.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyk.coursearrange.dao.DocDao;
import com.lyk.coursearrange.entity.Doc;
import com.lyk.coursearrange.service.DocService;
import org.springframework.stereotype.Service;

@Service
public class DocServiceImpl extends ServiceImpl<DocDao, Doc> implements DocService {
}
