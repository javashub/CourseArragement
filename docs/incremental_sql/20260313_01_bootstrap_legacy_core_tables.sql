USE `course_arrange_v2`;

-- 说明：
-- 1. 当前前端仍有一部分页面走旧业务接口，依赖 tb_* 旧表。
-- 2. 如果这些旧表没有导入，页面查询会直接报 "Table ... doesn't exist"。
-- 3. 本脚本只补齐当前联调主链需要的旧核心表结构，不删除任何现有表。

CREATE TABLE IF NOT EXISTS `tb_course_info` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `course_no` varchar(32) NOT NULL COMMENT '课程编号',
  `course_name` varchar(64) NOT NULL COMMENT '课程名称',
  `course_attr` varchar(32) DEFAULT NULL COMMENT '课程属性',
  `publisher` varchar(128) DEFAULT NULL COMMENT '出版社',
  `status` int DEFAULT 0 COMMENT '状态',
  `piority` int DEFAULT 0 COMMENT '优先级',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tb_course_info_no` (`course_no`),
  KEY `idx_tb_course_info_name` (`course_name`),
  KEY `idx_tb_course_info_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='旧版课程表';

CREATE TABLE IF NOT EXISTS `tb_teach_build_info` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `teach_build_no` varchar(32) NOT NULL COMMENT '教学楼编号',
  `teach_build_name` varchar(64) NOT NULL COMMENT '教学楼名称',
  `teach_build_location` varchar(255) DEFAULT NULL COMMENT '教学楼位置',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tb_teach_build_info_no` (`teach_build_no`),
  KEY `idx_tb_teach_build_info_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='旧版教学楼表';

CREATE TABLE IF NOT EXISTS `tb_classroom` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `classroom_no` varchar(32) NOT NULL COMMENT '教室编号',
  `classroom_name` varchar(64) NOT NULL COMMENT '教室名称',
  `teachbuild_no` varchar(32) NOT NULL COMMENT '所属教学楼编号',
  `capacity` int DEFAULT 0 COMMENT '容量',
  `attr` varchar(64) DEFAULT NULL COMMENT '教室属性',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tb_classroom_no` (`classroom_no`),
  KEY `idx_tb_classroom_teachbuild` (`teachbuild_no`),
  KEY `idx_tb_classroom_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='旧版教室表';

CREATE TABLE IF NOT EXISTS `tb_class_info` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `class_no` varchar(32) NOT NULL COMMENT '班级编号',
  `class_name` varchar(64) DEFAULT NULL COMMENT '班级名称',
  `num` int DEFAULT 0 COMMENT '班级人数',
  `teacher` int DEFAULT NULL COMMENT '班主任ID',
  `remark` varchar(64) DEFAULT NULL COMMENT '备注/年级编号',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tb_class_info_no` (`class_no`),
  KEY `idx_tb_class_info_remark` (`remark`),
  KEY `idx_tb_class_info_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='旧版班级表';

CREATE TABLE IF NOT EXISTS `tb_class_task` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `semester` varchar(32) NOT NULL COMMENT '学期',
  `grade_no` varchar(32) DEFAULT NULL COMMENT '年级编号',
  `class_no` varchar(32) NOT NULL COMMENT '班级编号',
  `course_no` varchar(32) NOT NULL COMMENT '课程编号',
  `course_name` varchar(64) NOT NULL COMMENT '课程名称',
  `teacher_no` varchar(32) NOT NULL COMMENT '教师编号',
  `realname` varchar(64) DEFAULT NULL COMMENT '教师姓名',
  `courseAttr` varchar(32) DEFAULT NULL COMMENT '课程属性',
  `studentNum` int DEFAULT 0 COMMENT '学生人数',
  `weeks_sum` int DEFAULT 0 COMMENT '总周数',
  `weeks_number` int DEFAULT 0 COMMENT '周学时',
  `isFix` varchar(8) DEFAULT '0' COMMENT '是否固定',
  `class_time` varchar(32) DEFAULT NULL COMMENT '固定时间片',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_tb_class_task_semester` (`semester`),
  KEY `idx_tb_class_task_class` (`class_no`),
  KEY `idx_tb_class_task_teacher` (`teacher_no`),
  KEY `idx_tb_class_task_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='旧版开课任务表';

CREATE TABLE IF NOT EXISTS `tb_course_plan` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `grade_no` varchar(32) DEFAULT NULL COMMENT '年级编号',
  `class_no` varchar(32) NOT NULL COMMENT '班级编号',
  `course_no` varchar(32) NOT NULL COMMENT '课程编号',
  `teacher_no` varchar(32) NOT NULL COMMENT '教师编号',
  `classroom_no` varchar(32) DEFAULT NULL COMMENT '教室编号',
  `class_time` varchar(32) NOT NULL COMMENT '时间片',
  `weeks_sum` int DEFAULT 0 COMMENT '周数',
  `semester` varchar(32) DEFAULT NULL COMMENT '学期',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_tb_course_plan_class` (`class_no`),
  KEY `idx_tb_course_plan_teacher` (`teacher_no`),
  KEY `idx_tb_course_plan_semester` (`semester`),
  KEY `idx_tb_course_plan_time` (`class_time`),
  KEY `idx_tb_course_plan_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='旧版课表结果表';

CREATE TABLE IF NOT EXISTS `tb_course_plan_adjust_log` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `course_plan_id` int DEFAULT NULL COMMENT '课表ID',
  `semester` varchar(32) DEFAULT NULL COMMENT '学期',
  `grade_no` varchar(32) DEFAULT NULL COMMENT '年级编号',
  `class_no` varchar(32) DEFAULT NULL COMMENT '班级编号',
  `course_no` varchar(32) DEFAULT NULL COMMENT '课程编号',
  `teacher_no` varchar(32) DEFAULT NULL COMMENT '教师编号',
  `before_class_time` varchar(32) DEFAULT NULL COMMENT '调整前时间片',
  `after_class_time` varchar(32) DEFAULT NULL COMMENT '调整后时间片',
  `before_classroom_no` varchar(32) DEFAULT NULL COMMENT '调整前教室',
  `after_classroom_no` varchar(32) DEFAULT NULL COMMENT '调整后教室',
  `operator_user_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(64) DEFAULT NULL COMMENT '操作人名称',
  `operator_type` varchar(32) DEFAULT NULL COMMENT '操作人类型',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_tb_course_plan_adjust_log_plan` (`course_plan_id`),
  KEY `idx_tb_course_plan_adjust_log_semester` (`semester`),
  KEY `idx_tb_course_plan_adjust_log_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='旧版调课日志表';

CREATE TABLE IF NOT EXISTS `tb_schedule_execute_log` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `semester` varchar(32) DEFAULT NULL COMMENT '学期',
  `task_count` int DEFAULT 0 COMMENT '任务数',
  `generated_plan_count` int DEFAULT 0 COMMENT '生成课表数',
  `status` int DEFAULT 0 COMMENT '状态',
  `duration_ms` bigint DEFAULT 0 COMMENT '耗时',
  `message` varchar(500) DEFAULT NULL COMMENT '执行结果',
  `operator_user_id` bigint DEFAULT NULL COMMENT '执行人ID',
  `operator_name` varchar(64) DEFAULT NULL COMMENT '执行人名称',
  `operator_type` varchar(32) DEFAULT NULL COMMENT '执行人类型',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_tb_schedule_execute_log_semester` (`semester`),
  KEY `idx_tb_schedule_execute_log_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='旧版排课执行日志表';

CREATE TABLE IF NOT EXISTS `tb_online_category` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `parent_id` int DEFAULT 0 COMMENT '父级分类ID',
  `category_no` varchar(32) NOT NULL COMMENT '分类编号',
  `category_name` varchar(64) NOT NULL COMMENT '分类名称',
  `piority` int DEFAULT 0 COMMENT '优先级',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tb_online_category_no` (`category_no`),
  KEY `idx_tb_online_category_parent` (`parent_id`),
  KEY `idx_tb_online_category_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='旧版在线分类表';

CREATE TABLE IF NOT EXISTS `tb_online_course` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `online_no` varchar(32) NOT NULL COMMENT '网课编号',
  `online_name` varchar(128) NOT NULL COMMENT '网课名称',
  `description` varchar(1000) DEFAULT NULL COMMENT '简介',
  `cover` varchar(255) DEFAULT NULL COMMENT '封面',
  `online_category_id` int DEFAULT NULL COMMENT '分类ID',
  `online_category_name` varchar(64) DEFAULT NULL COMMENT '分类名称',
  `piority` int DEFAULT 0 COMMENT '优先级',
  `from_user_type` int DEFAULT NULL COMMENT '发布者类型',
  `from_user_id` int DEFAULT NULL COMMENT '发布者ID',
  `from_user_name` varchar(64) DEFAULT NULL COMMENT '发布者名称',
  `clicks` bigint DEFAULT 0 COMMENT '点击数',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tb_online_course_no` (`online_no`),
  KEY `idx_tb_online_course_category` (`online_category_id`),
  KEY `idx_tb_online_course_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='旧版在线课程表';
