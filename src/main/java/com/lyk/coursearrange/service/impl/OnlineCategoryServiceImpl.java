package com.lyk.coursearrange.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyk.coursearrange.dao.OnlineCategoryDao;
import com.lyk.coursearrange.entity.OnlineCategory;
import com.lyk.coursearrange.service.OnlineCategoryService;
import org.springframework.stereotype.Service;

@Service
public class OnlineCategoryServiceImpl extends ServiceImpl<OnlineCategoryDao, OnlineCategory> implements OnlineCategoryService {
}
