package com.lyk.coursearrange.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.dao.ClassInfoDao;
import com.lyk.coursearrange.entity.ClassInfo;
import com.lyk.coursearrange.entity.Student;
import com.lyk.coursearrange.entity.request.ClassAddVO;
import com.lyk.coursearrange.entity.response.ClassInfoVO;
import com.lyk.coursearrange.service.ClassInfoService;
import com.lyk.coursearrange.service.StudentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 班级信息
 * @author lequal
 * @since 2020-03-06
 */
@RestController
public class ClassInfoController {

    @Resource
    private ClassInfoService classInfoService;
    @Resource
    private StudentService studentService;


    /**
     * 根据年级查询所有班级
     */
    @GetMapping("/class-grade/{grade}")
    public ServerResponse queryClass(@PathVariable("grade") String grade) {
        QueryWrapper<ClassInfo> wrapper = new QueryWrapper<ClassInfo>().eq("remark", grade);
        List<ClassInfo> classInfoList = classInfoService.list(wrapper);

        return ServerResponse.ofSuccess(classInfoList);
    }

    /**
     * 根据班级查询学生
     */
    @GetMapping("/student-class/{page}/{classNo}")
    public ServerResponse queryStudentByClass(@PathVariable("page") Integer page,
                                              @PathVariable("classNo") String classNo,
                                              @RequestParam(defaultValue = "10") Integer limit) {
        QueryWrapper<Student> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("student_no");
        wrapper.like(!StringUtils.isEmpty(classNo), "class_no", classNo);
        Page<Student> pages = new Page<>(page, limit);
        IPage<Student> iPage = studentService.page(pages, wrapper);
        return ServerResponse.ofSuccess(iPage);
    }

    /**
     * 查询班级信息带详细信息
     */
    @GetMapping("/queryclassinfo/{page}")
    public ServerResponse queryClassInfos(@PathVariable("page") Integer page,
                                          @RequestParam(defaultValue = "10") Integer limit,
                                          @RequestParam(defaultValue = "") String gradeNo) {
        return classInfoService.queryClassInfos(page, limit, gradeNo);
    }


    /**
     * 添加班级
     */
    @PostMapping("/addclassinfo")
    public ServerResponse addClass(@RequestBody ClassAddVO classAddVO) {
        ClassInfo c = new ClassInfo();
        BeanUtils.copyProperties(classAddVO, c);
        c.setRemark(classAddVO.getGradeNo());

        return classInfoService.save(c) ? ServerResponse.ofSuccess("添加班级成功") : ServerResponse.ofError("添加班级失败");
    }

}

