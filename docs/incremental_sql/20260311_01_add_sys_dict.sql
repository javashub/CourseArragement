-- =====================================================================
-- 增量 SQL：新增系统字典表
-- 执行时间：2026-03-11
-- 说明：
-- 1. 本脚本基于已存在的 course_arrange_v2 数据库执行
-- 2. 仅做增量变更，不修改既有表
-- 3. 用于支撑前端统一字典下拉、筛选项、展示标签等能力
-- =====================================================================

USE `course_arrange_v2`;

CREATE TABLE IF NOT EXISTS `sys_dict_type` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dict_type_code` varchar(64) NOT NULL COMMENT '字典类型编码',
  `dict_type_name` varchar(64) NOT NULL COMMENT '字典类型名称',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
  `sort_no` int NOT NULL DEFAULT 0 COMMENT '排序',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_dict_type_code` (`dict_type_code`),
  KEY `idx_sys_dict_type_status_sort` (`status`, `sort_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典类型表';

CREATE TABLE IF NOT EXISTS `sys_dict_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dict_type_code` varchar(64) NOT NULL COMMENT '字典类型编码',
  `item_code` varchar(64) NOT NULL COMMENT '字典项编码',
  `item_name` varchar(64) NOT NULL COMMENT '字典项名称',
  `item_value` varchar(128) DEFAULT NULL COMMENT '字典项值',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
  `sort_no` int NOT NULL DEFAULT 0 COMMENT '排序',
  `is_default` tinyint NOT NULL DEFAULT 0 COMMENT '是否默认项',
  `ext_json` varchar(1000) DEFAULT NULL COMMENT '扩展信息JSON',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_dict_item_unique` (`dict_type_code`, `item_code`),
  KEY `idx_sys_dict_item_type_status_sort` (`dict_type_code`, `status`, `sort_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典项表';

INSERT INTO `sys_dict_type` (`dict_type_code`, `dict_type_name`, `status`, `sort_no`, `remark`)
VALUES
  ('gender', '性别', 1, 1, '基础字典'),
  ('course_type', '课程类型', 1, 2, '教学相关字典'),
  ('classroom_type', '教室类型', 1, 3, '教学资源字典'),
  ('teacher_title', '教师职称', 1, 4, '教师基础字典'),
  ('stage_type', '学段类型', 1, 5, '组织架构字典')
ON DUPLICATE KEY UPDATE
  `dict_type_name` = VALUES(`dict_type_name`),
  `status` = VALUES(`status`),
  `sort_no` = VALUES(`sort_no`),
  `remark` = VALUES(`remark`);

INSERT INTO `sys_dict_item` (`dict_type_code`, `item_code`, `item_name`, `item_value`, `status`, `sort_no`, `is_default`, `remark`)
VALUES
  ('gender', 'MALE', '男', '男', 1, 1, 0, '性别'),
  ('gender', 'FEMALE', '女', '女', 1, 2, 0, '性别'),
  ('gender', 'UNKNOWN', '未知', '未知', 1, 99, 1, '默认值'),

  ('course_type', 'REQUIRED', '必修课', 'REQUIRED', 1, 1, 0, '课程类型'),
  ('course_type', 'ELECTIVE', '选修课', 'ELECTIVE', 1, 2, 0, '课程类型'),
  ('course_type', 'PUBLIC', '公共课', 'PUBLIC', 1, 3, 0, '课程类型'),
  ('course_type', 'LAB', '实验课', 'LAB', 1, 4, 0, '课程类型'),
  ('course_type', 'SPORT', '体育课', 'SPORT', 1, 5, 0, '课程类型'),

  ('classroom_type', 'NORMAL', '普通教室', 'NORMAL', 1, 1, 0, '教室类型'),
  ('classroom_type', 'MULTIMEDIA', '多媒体教室', 'MULTIMEDIA', 1, 2, 0, '教室类型'),
  ('classroom_type', 'COMPUTER', '机房', 'COMPUTER', 1, 3, 0, '教室类型'),
  ('classroom_type', 'LAB', '实验室', 'LAB', 1, 4, 0, '教室类型'),
  ('classroom_type', 'SPORT', '体育场馆', 'SPORT', 1, 5, 0, '教室类型'),
  ('classroom_type', 'SPECIAL', '专用教室', 'SPECIAL', 1, 6, 0, '教室类型'),

  ('teacher_title', 'LECTURER', '讲师', '讲师', 1, 1, 0, '教师职称'),
  ('teacher_title', 'ASSOCIATE_PROFESSOR', '副教授', '副教授', 1, 2, 0, '教师职称'),
  ('teacher_title', 'PROFESSOR', '教授', '教授', 1, 3, 0, '教师职称'),

  ('stage_type', 'PRIMARY', '小学', '小学', 1, 1, 0, '学段'),
  ('stage_type', 'JUNIOR', '初中', '初中', 1, 2, 0, '学段'),
  ('stage_type', 'SENIOR', '高中', '高中', 1, 3, 0, '学段'),
  ('stage_type', 'UNIVERSITY', '大学', '大学', 1, 4, 0, '学段')
ON DUPLICATE KEY UPDATE
  `item_name` = VALUES(`item_name`),
  `item_value` = VALUES(`item_value`),
  `status` = VALUES(`status`),
  `sort_no` = VALUES(`sort_no`),
  `is_default` = VALUES(`is_default`),
  `remark` = VALUES(`remark`);
