package com.lyk.coursearrange.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyk.coursearrange.dao.ExerciseDao;
import com.lyk.coursearrange.entity.Exercise;
import com.lyk.coursearrange.service.ExerciseService;
import org.springframework.stereotype.Service;

@Service
public class ExerciseServiceImpl extends ServiceImpl<ExerciseDao, Exercise> implements ExerciseService {
}
