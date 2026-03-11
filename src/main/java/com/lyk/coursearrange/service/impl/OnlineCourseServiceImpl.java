package com.lyk.coursearrange.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyk.coursearrange.dao.OnlineCourseDao;
import com.lyk.coursearrange.entity.OnlineCourse;
import com.lyk.coursearrange.service.OnlineCourseService;
import org.springframework.stereotype.Service;

@Service
public class OnlineCourseServiceImpl extends ServiceImpl<OnlineCourseDao, OnlineCourse> implements OnlineCourseService {
}
