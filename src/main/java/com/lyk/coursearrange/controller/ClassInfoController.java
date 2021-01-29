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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
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

    @Autowired
    private ClassInfoService classInfoService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private ClassInfoDao classInfoDao;


    /**
     * 分页查询所有班级
     * @param page
     * @param limit
     * @return
     */
//    @GetMapping("/queryclassinfobypage/{page}")
//    public ServerResponse queryClassInfo(@PathVariable(value = "page") Integer page,
//                                         @RequestParam(defaultValue = "10") Integer limit) {
//        System.out.println("*******************************************");
//        Page<ClassInfo> pages = new Page<>(page, limit);
//        QueryWrapper<ClassInfo> wrapper = new QueryWrapper<ClassInfo>().orderByDesc("remark");
//        IPage<ClassInfo> iPage = classInfoService.page(pages, wrapper);
//        return ServerResponse.ofSuccess(iPage);
//    }

    /**
     * 根据年级查询所有班级
     * @param grade
     * @return
     */
    @GetMapping("/class-grade/{grade}")
    public ServerResponse queryClass(@PathVariable("grade") String grade) {
        QueryWrapper<ClassInfo> wrapper = new QueryWrapper<ClassInfo>().eq("remark", grade);
        List<ClassInfo> classInfoList = classInfoService.list(wrapper);

        return ServerResponse.ofSuccess(classInfoList);
    }

    /**
     * 根据班级查询学生
     * @param page
     * @param classNo
     * @param limit
     * @return
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
     * @return
     */
    @GetMapping("/queryclassinfo/{page}")
    public ServerResponse queryClassInfos(@PathVariable("page") Integer page, @RequestParam(defaultValue = "10") Integer limit, @RequestParam(defaultValue = "") String gradeNo) {
        System.out.println("gradeNo = " + gradeNo);
        Map<String, Object> map = new HashMap();
        List<ClassInfoVO> classInfoVOS = null;
        if (gradeNo.equals("")) {
            classInfoVOS = classInfoDao.queryClassInfos((page - 1) * limit, limit);
            int total = classInfoDao.count2();
            map.put("records", classInfoVOS);
            map.put("total", total);
        } else {
            classInfoVOS = classInfoDao.queryClassInfo((page - 1) * limit, limit, gradeNo);
            int total = classInfoDao.count1(gradeNo);
            map.put("records", classInfoVOS);
            map.put("total", total);
        }
        return ServerResponse.ofSuccess(map);
    }

    // TODO 编辑班级

    // TODO 删除班级

    /**
     * 添加班级
     * @param classAddVO
     * @return
     */
    @PostMapping("/addclassinfo")
    public ServerResponse addClass(@RequestBody ClassAddVO classAddVO) {
        System.out.println(classAddVO);
        ClassInfo c = new ClassInfo();
        c.setRemark(classAddVO.getGradeNo());
        c.setClassNo(classAddVO.getClassNo());
        c.setClassName(classAddVO.getClassName());
        c.setTeacher(classAddVO.getId());
        c.setNum(classAddVO.getNum());
        boolean b = classInfoService.save(c);
        if (b) {
            return ServerResponse.ofSuccess("添加班级成功");
        }
        return ServerResponse.ofError("添加班级失败");
    }

    /**
     * 查询每个班级的学生人数000000000000000000000000000000000000000000000000000000000000000000000000000000出大问题
     * @return
     */
//    @GetMapping("/studentnums")
//    public ServerResponse queryStudentNumByClass() {
//        // 先获得所有的班级编号
//        QueryWrapper<ClassInfo> wrapper = new QueryWrapper();
//        QueryWrapper wrapper2 = new QueryWrapper();
//
//        int nums = 0;
//        List<ClassInfo> classList = classInfoService.list();
//        // 循环查每个班级的人数
//        for (ClassInfo classInfo : classList) {
//            wrapper2.eq("class_no", classInfo.getClassNo());
//            // 得到该班学生人数
//            nums = studentService.count(wrapper2);
//            classInfo.setNum(nums);
//            // 更新回去
//            classInfoService.update(classInfo, null);
//        }
//        return ServerResponse.ofSuccess();
//    }

}

