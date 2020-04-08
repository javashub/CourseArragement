package com.lyk.coursearrange.service;

import com.lyk.coursearrange.entity.ClassTask;
import com.baomidou.mybatisplus.extension.service.IService;

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
