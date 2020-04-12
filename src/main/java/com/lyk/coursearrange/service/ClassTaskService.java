package com.lyk.coursearrange.service;

import com.lyk.coursearrange.entity.ClassTask;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lequal
 * @since 2020-04-06
 */
public interface ClassTaskService extends IService<ClassTask> {

    Boolean classScheduling(ClassTask classTask);


}
