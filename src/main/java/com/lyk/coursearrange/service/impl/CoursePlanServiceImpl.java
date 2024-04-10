package com.lyk.coursearrange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.CourseInfo;
import com.lyk.coursearrange.entity.CoursePlan;
import com.lyk.coursearrange.dao.CoursePlanDao;
import com.lyk.coursearrange.entity.Teacher;
import com.lyk.coursearrange.entity.response.CoursePlanVo;
import com.lyk.coursearrange.service.CourseInfoService;
import com.lyk.coursearrange.service.CoursePlanService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyk.coursearrange.service.TeacherService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lequal
 * @since 2020-04-15
 */
@Service
public class CoursePlanServiceImpl extends ServiceImpl<CoursePlanDao, CoursePlan> implements CoursePlanService {

    @Resource
    private CourseInfoService courseInfoService;
    @Resource
    private TeacherService teacherService;
    @Resource
    private CoursePlanDao coursePlanDao;

    /**
     * 根据班级编号查询课表
     * @param classNo 班级编号
     */
    @Override
    public ServerResponse queryCoursePlanByClassNo(String classNo) {
        LambdaQueryWrapper<CoursePlan> wrapper = new LambdaQueryWrapper<CoursePlan>().eq(CoursePlan::getClassNo, classNo).orderByAsc(CoursePlan::getClassTime);
        List<CoursePlan> coursePlanList = coursePlanDao.selectList(wrapper);

        if (null == coursePlanList || coursePlanList.isEmpty()) {
            return ServerResponse.ofError("该班级没有课表");
        }

        // 过滤出教师编号
        List<String> teacherNos = new ArrayList<>(coursePlanList.stream().map(CoursePlan::getTeacherNo).collect(Collectors.toSet()));
        // 过滤出课程编号
        List<String> courseNos = new ArrayList<>(coursePlanList.stream().map(CoursePlan::getCourseNo).collect(Collectors.toSet()));

        // 查询教师信息
        List<Teacher> teachers = teacherService.list(new LambdaQueryWrapper<Teacher>().in(Teacher::getTeacherNo, teacherNos));
        // 查询课程信息
        List<CourseInfo> courseInfos = courseInfoService.list(new LambdaQueryWrapper<CourseInfo>().in(CourseInfo::getCourseNo, courseNos));

        List<CoursePlanVo> coursePlanVos = new LinkedList<>();
        coursePlanList.forEach(v -> {
            // v 转换成 CoursePlanVo 对象
            CoursePlanVo coursePlanVo = new CoursePlanVo();
            BeanUtils.copyProperties(v, coursePlanVo);

            // 根据教师编号找到教师名称
            // 根据教师编号找到教师名称
            Optional<Teacher> teacherName = teachers.stream().filter(t -> t.getTeacherNo().equals(v.getTeacherNo())).findFirst();
            if (teacherName.isPresent()) {
                coursePlanVo.setRealname(teacherName.get().getRealname());
            }

            Optional<CourseInfo> courseName = courseInfos.stream().filter(c -> c.getCourseNo().equals(v.getCourseNo())).findFirst();
            if (courseName.isPresent()) {
                coursePlanVo.setCourseName(courseName.get().getCourseName());
            }

            coursePlanVos.add(coursePlanVo);
        });

        return ServerResponse.ofSuccess(coursePlanVos);
    }
}
