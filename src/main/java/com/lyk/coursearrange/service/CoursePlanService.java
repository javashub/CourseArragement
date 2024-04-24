package com.lyk.coursearrange.service;

import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.CoursePlan;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author lequal
 * @since 2020-04-15
 */
public interface CoursePlanService extends IService<CoursePlan> {

    /**
     * 根据班级编号查询课程安排
     */
    ServerResponse queryCoursePlanByClassNo(@PathVariable("classno") String classNo);

}
