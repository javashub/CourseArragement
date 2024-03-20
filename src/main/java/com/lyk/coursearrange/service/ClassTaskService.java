package com.lyk.coursearrange.service;

import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.ClassTask;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * @author lequal
 * @since 2020-04-06
 */
public interface ClassTaskService extends IService<ClassTask> {

//    Boolean classScheduling(ClassTask classTask);
    ServerResponse classScheduling(@Param("semester") String semester);

}
