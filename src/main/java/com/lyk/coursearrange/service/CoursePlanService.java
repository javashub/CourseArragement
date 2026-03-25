package com.lyk.coursearrange.service;

import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.request.CoursePlanAdjustRequest;
import com.lyk.coursearrange.schedule.vo.ScheduleAdjustLogVO;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @author lequal
 * @since 2020-04-15
 */
public interface CoursePlanService {

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
    List<ScheduleAdjustLogVO> listRecentAdjustLogs(String semester, String classNo, String teacherNo, Integer limit);

    /**
     * 查询指定教学楼下已占用的教室编号，标准课表优先。
     */
    List<String> listOccupiedClassroomNos(String buildingCode);

}
