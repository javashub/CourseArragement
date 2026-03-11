package com.lyk.coursearrange.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyk.coursearrange.dao.ExerciseCategoryDao;
import com.lyk.coursearrange.entity.ExerciseCategory;
import com.lyk.coursearrange.service.ExerciseCategoryService;
import org.springframework.stereotype.Service;

@Service
public class ExerciseCategoryServiceImpl extends ServiceImpl<ExerciseCategoryDao, ExerciseCategory> implements ExerciseCategoryService {
}
