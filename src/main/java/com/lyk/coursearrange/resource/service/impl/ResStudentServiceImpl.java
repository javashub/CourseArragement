package com.lyk.coursearrange.resource.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyk.coursearrange.resource.entity.ResStudent;
import com.lyk.coursearrange.resource.mapper.ResStudentMapper;
import com.lyk.coursearrange.resource.service.ResStudentService;
import org.springframework.stereotype.Service;

/**
 * 学生资源服务实现。
 */
@Service
public class ResStudentServiceImpl extends ServiceImpl<ResStudentMapper, ResStudent> implements ResStudentService {
}
