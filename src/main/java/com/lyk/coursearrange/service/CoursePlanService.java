package com.lyk.coursearrange.service;

import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.CoursePlanAdjustLog;
import com.lyk.coursearrange.entity.CoursePlan;
import com.lyk.coursearrange.entity.request.CoursePlanAdjustRequest;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @author lequal
 * @since 2020-04-15
 */
public interface CoursePlanService extends IService<CoursePlan> {

    /**
     * 根据班级编号查询课程安排
     */
    ServerResponse queryCoursePlanByClassNo(@PathVariable("classno") String classNo, String semester);

    /**
     * 根据教师编号查询课程安排
     */
    ServerResponse queryCoursePlanByTeacherNo(@PathVariable("teacherno") String teacherNo, String semester);

    /**
     * 调整课表时间片。
     */
    ServerResponse adjustCoursePlan(CoursePlanAdjustRequest request);

    /**
     * 查询最近调课日志。
     */
    List<CoursePlanAdjustLog> listRecentAdjustLogs(String semester, String classNo, String teacherNo, Integer limit);

}
