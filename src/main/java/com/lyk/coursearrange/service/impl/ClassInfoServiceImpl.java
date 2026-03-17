package com.lyk.coursearrange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.ClassInfo;
import com.lyk.coursearrange.dao.ClassInfoDao;
import com.lyk.coursearrange.entity.response.ClassInfoVO;
import com.lyk.coursearrange.service.ClassInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lequal
 * @since 2020-03-06
 */
@Service
public class ClassInfoServiceImpl extends ServiceImpl<ClassInfoDao, ClassInfo> implements ClassInfoService {

    @Resource
    private ClassInfoDao classInfoDao;

    @Override
    public ServerResponse queryClassInfos(Integer page, Integer limit, String gradeNo) {
        Map<String, Object> map = new HashMap<>();
        List<ClassInfoVO> classInfoVOS;
        if ("".equals(gradeNo)) {
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

    @Override
    public List<ClassInfo> listClassOptions(String gradeNo) {
        QueryWrapper<ClassInfo> wrapper = new QueryWrapper<ClassInfo>()
                .select("id", "class_no", "class_name", "remark")
                .orderByAsc("class_no");
        if (!"".equals(gradeNo)) {
            wrapper.eq("remark", gradeNo);
        }
        return classInfoDao.selectList(wrapper);
    }
}
