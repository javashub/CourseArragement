-- =====================================================================
-- 初始化 RBAC 测试账号、角色、菜单、权限
-- 说明：
-- 1. 本脚本为增量脚本，面向 course_arrange_v2 执行
-- 2. 由于当前登录仍依赖 tb_admin / tb_teacher / tb_student，
--    所以这里同时初始化旧登录表和新 sys_user 绑定关系
-- 3. 默认测试密码统一为 123456
-- 4. 本脚本会先扩容旧表密码字段，避免 BCrypt 长度被截断
--
-- 测试账号：
-- 管理员：adminNo=900001  密码=123456
-- 教师：teacherNo=900101  密码=123456
-- 学生：studentNo=900201  密码=123456
-- =====================================================================

SET NAMES utf8mb4;

-- 步骤1：保底创建旧登录表，兼容当前登录链路
CREATE TABLE IF NOT EXISTS `tb_admin` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `admin_no` varchar(20) NOT NULL COMMENT '管理员编号',
  `username` varchar(36) DEFAULT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `realname` varchar(36) NOT NULL COMMENT '真实姓名',
  `user_type` tinyint(1) NOT NULL DEFAULT '1' COMMENT '用户类型',
  `jobtitle` varchar(20) DEFAULT NULL COMMENT '职称',
  `license` varchar(1000) DEFAULT NULL COMMENT '证件照地址',
  `teach` varchar(20) DEFAULT NULL COMMENT '教授科目',
  `telephone` varchar(36) DEFAULT NULL COMMENT '联系电话',
  `email` varchar(50) DEFAULT NULL COMMENT '电子邮件',
  `address` varchar(255) DEFAULT NULL COMMENT '地址',
  `age` int DEFAULT NULL COMMENT '年龄',
  `avatar` varchar(500) DEFAULT NULL COMMENT '头像',
  `description` varchar(100) DEFAULT NULL COMMENT '签名',
  `remark` varchar(100) DEFAULT NULL COMMENT '备注',
  `type` tinyint(1) DEFAULT NULL COMMENT '管理员类型',
  `piority` int DEFAULT NULL COMMENT '优先级',
  `power` tinyint(1) DEFAULT NULL COMMENT '1为管理员，0为超级管理员',
  `status` tinyint(1) DEFAULT '0' COMMENT '账号状态',
  `deleted` tinyint(1) DEFAULT '0',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='旧管理员登录表';

CREATE TABLE IF NOT EXISTS `tb_teacher` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '讲师表主键',
  `teacher_no` varchar(20) NOT NULL COMMENT '教师编号',
  `username` varchar(36) DEFAULT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `realname` varchar(36) NOT NULL COMMENT '真实姓名',
  `user_type` tinyint(1) NOT NULL DEFAULT '2' COMMENT '用户类型',
  `jobtitle` varchar(20) DEFAULT NULL COMMENT '职称',
  `grade_no` varchar(20) DEFAULT NULL COMMENT '所属年级',
  `license` varchar(1000) DEFAULT NULL COMMENT '证件照',
  `teach` varchar(20) DEFAULT NULL COMMENT '教授科目',
  `age` int DEFAULT NULL COMMENT '年龄',
  `telephone` varchar(36) DEFAULT NULL COMMENT '联系电话',
  `email` varchar(50) DEFAULT NULL COMMENT '电子邮件',
  `address` varchar(255) DEFAULT NULL COMMENT '地址',
  `avatar` varchar(500) DEFAULT NULL COMMENT '头像',
  `description` varchar(100) DEFAULT NULL COMMENT '签名',
  `power` tinyint(1) DEFAULT NULL COMMENT '操作权限',
  `piority` int DEFAULT NULL COMMENT '优先级',
  `status` tinyint(1) DEFAULT '0' COMMENT '账号状态',
  `deleted` tinyint(1) DEFAULT '0',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='旧教师登录表';

CREATE TABLE IF NOT EXISTS `tb_student` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '学生表主键',
  `student_no` varchar(20) NOT NULL COMMENT '学号',
  `username` varchar(36) DEFAULT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `user_type` tinyint(1) NOT NULL DEFAULT '3' COMMENT '用户类型',
  `realname` varchar(36) NOT NULL COMMENT '真实姓名',
  `grade` varchar(20) DEFAULT NULL COMMENT '年级',
  `class_no` varchar(20) DEFAULT NULL COMMENT '班级编号',
  `age` int DEFAULT NULL COMMENT '年龄',
  `address` varchar(255) DEFAULT NULL COMMENT '地址',
  `telephone` varchar(36) DEFAULT NULL COMMENT '联系电话',
  `email` varchar(50) DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(500) DEFAULT NULL COMMENT '头像',
  `description` varchar(100) DEFAULT NULL COMMENT '签名',
  `deleted` tinyint(1) DEFAULT '0',
  `status` tinyint(1) DEFAULT '0' COMMENT '账号状态',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='旧学生登录表';

-- 步骤2：扩容旧登录表密码字段，兼容 BCrypt
ALTER TABLE `tb_admin` MODIFY COLUMN `password` varchar(255) NOT NULL COMMENT '密码';
ALTER TABLE `tb_teacher` MODIFY COLUMN `password` varchar(255) NOT NULL COMMENT '密码';
ALTER TABLE `tb_student` MODIFY COLUMN `password` varchar(255) NOT NULL COMMENT '密码';

-- 步骤3：清理旧测试数据，保证脚本可重复执行
DELETE FROM `sys_user_role` WHERE `user_id` IN (910001, 910002, 910003);
DELETE FROM `sys_role_menu` WHERE `role_id` IN (920001, 920002, 920003);
DELETE FROM `sys_role_permission` WHERE `role_id` IN (920001, 920002, 920003);

DELETE FROM `sys_menu`
WHERE `id` IN (940000, 940001, 940002, 940003, 940004, 940005)
   OR `menu_code` IN ('ROOT_WORKBENCH', 'MENU_DASHBOARD', 'MENU_COURSE_PLAN', 'MENU_SCHEDULE', 'MENU_BASE_DATA', 'MENU_GUIDE');

DELETE FROM `sys_permission`
WHERE `id` IN (930001, 930002, 930003, 930004, 930005)
   OR `permission_code` IN ('dashboard:view', 'course-plan:view', 'schedule:view', 'base-data:view', 'guide:view');

DELETE FROM `sys_role`
WHERE `id` IN (920001, 920002, 920003)
   OR `role_code` IN ('ADMIN', 'TEACHER', 'STUDENT');

DELETE FROM `sys_user`
WHERE `id` IN (910001, 910002, 910003)
   OR `user_code` IN ('U_ADMIN_900001', 'U_TEACHER_900101', 'U_STUDENT_900201')
   OR `username` IN ('900001', '900101', '900201');

DELETE FROM `tb_admin` WHERE `id` = 900001 OR `admin_no` = '900001';
DELETE FROM `tb_teacher` WHERE `id` = 900101 OR `teacher_no` = '900101';
DELETE FROM `tb_student` WHERE `id` = 900201 OR `student_no` = '900201';

-- 步骤4：写入旧登录表测试账号
INSERT INTO `tb_admin`
(`id`, `admin_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `telephone`, `email`, `address`, `description`, `remark`, `power`, `status`, `deleted`, `create_time`, `update_time`)
VALUES
(900001, '900001', 'system_admin', '$2a$10$jRUEIwye77.esrMYRi8UuOYTgtXuGKLy2/9k2SNoVN6WZcYRkfnHu', '系统管理员', 1, '系统管理员', '13800000001', 'admin@test.local', '主校区', '系统初始化管理员账号', 'RBAC测试账号', 0, 0, 0, NOW(), NOW());

INSERT INTO `tb_teacher`
(`id`, `teacher_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `grade_no`, `teach`, `telephone`, `email`, `address`, `description`, `power`, `piority`, `status`, `deleted`, `create_time`, `update_time`)
VALUES
(900101, '900101', 'teacher_demo', '$2a$10$jRUEIwye77.esrMYRi8UuOYTgtXuGKLy2/9k2SNoVN6WZcYRkfnHu', '演示教师', 2, '讲师', '01', '数学', '13800000002', 'teacher@test.local', '主校区', 'RBAC测试教师账号', 1, 1, 0, 0, NOW(), NOW());

INSERT INTO `tb_student`
(`id`, `student_no`, `username`, `password`, `realname`, `user_type`, `grade`, `class_no`, `address`, `telephone`, `email`, `description`, `deleted`, `status`, `create_time`, `update_time`)
VALUES
(900201, '900201', 'student_demo', '$2a$10$jRUEIwye77.esrMYRi8UuOYTgtXuGKLy2/9k2SNoVN6WZcYRkfnHu', '演示学生', 3, '高一', '20200101', '主校区', '13800000003', 'student@test.local', 'RBAC测试学生账号', 0, 0, NOW(), NOW());

-- 步骤5：写入 RBAC 角色
INSERT INTO `sys_role`
(`id`, `role_code`, `role_name`, `role_type`, `data_scope_type`, `sort_no`, `status`, `remark`, `created_at`, `updated_at`, `deleted`)
VALUES
(920001, 'ADMIN', '管理员', 'SYSTEM', 'ALL', 1, 1, '系统管理员角色', NOW(), NOW(), 0),
(920002, 'TEACHER', '教师', 'SYSTEM', 'SELF', 2, 1, '教师角色', NOW(), NOW(), 0),
(920003, 'STUDENT', '学生', 'SYSTEM', 'SELF', 3, 1, '学生角色', NOW(), NOW(), 0);

-- 步骤6：写入 RBAC 权限
INSERT INTO `sys_permission`
(`id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `status`, `remark`, `created_at`, `updated_at`, `deleted`)
VALUES
(930001, 'dashboard:view', '查看工作台', 'PAGE', '/dashboard', 'GET', 1, '工作台页面权限', NOW(), NOW(), 0),
(930002, 'course-plan:view', '查看排课任务', 'PAGE', '/course-plan', 'GET', 1, '排课任务页面权限', NOW(), NOW(), 0),
(930003, 'schedule:view', '查看课表管理', 'PAGE', '/schedule', 'GET', 1, '课表管理页面权限', NOW(), NOW(), 0),
(930004, 'base-data:view', '查看基础数据', 'PAGE', '/base-data', 'GET', 1, '基础数据页面权限', NOW(), NOW(), 0),
(930005, 'guide:view', '查看系统说明', 'PAGE', '/guide', 'GET', 1, '系统说明页面权限', NOW(), NOW(), 0);

-- 步骤7：写入 RBAC 菜单
INSERT INTO `sys_menu`
(`id`, `menu_code`, `parent_id`, `menu_name`, `menu_type`, `route_name`, `route_path`, `component_path`, `icon`, `permission_code`, `is_hidden`, `is_keep_alive`, `sort_no`, `status`, `remark`, `created_at`, `updated_at`, `deleted`)
VALUES
(940000, 'ROOT_WORKBENCH', 0, '系统工作台', 'CATALOG', 'RootWorkbench', '/', 'layouts/BasicLayout', 'House', NULL, 0, 0, 1, 1, '前端主框架根节点', NOW(), NOW(), 0),
(940001, 'MENU_DASHBOARD', 940000, '工作台', 'MENU', 'Dashboard', '/dashboard', 'views/dashboard/DashboardView.vue', 'HomeFilled', 'dashboard:view', 0, 1, 10, 1, '工作台页面', NOW(), NOW(), 0),
(940002, 'MENU_COURSE_PLAN', 940000, '排课任务', 'MENU', 'CoursePlan', '/course-plan', 'views/course/CoursePlanView.vue', 'Tickets', 'course-plan:view', 0, 1, 20, 1, '排课任务页面', NOW(), NOW(), 0),
(940003, 'MENU_SCHEDULE', 940000, '课表管理', 'MENU', 'Schedule', '/schedule', 'views/course/ScheduleView.vue', 'Calendar', 'schedule:view', 0, 1, 30, 1, '课表管理页面', NOW(), NOW(), 0),
(940004, 'MENU_BASE_DATA', 940000, '基础数据', 'MENU', 'BaseData', '/base-data', 'views/base/BaseDataView.vue', 'DataBoard', 'base-data:view', 0, 1, 40, 1, '基础数据页面', NOW(), NOW(), 0),
(940005, 'MENU_GUIDE', 940000, '重构说明', 'MENU', 'Guide', '/guide', 'views/system/SystemGuideView.vue', 'Document', 'guide:view', 0, 1, 50, 1, '系统说明页面', NOW(), NOW(), 0);

-- 步骤8：写入新 RBAC 用户，并绑定旧登录表主键
INSERT INTO `sys_user`
(`id`, `user_code`, `username`, `password_hash`, `password_salt`, `real_name`, `display_name`, `mobile`, `email`, `user_type`, `source_type`, `source_id`, `status`, `remark`, `created_at`, `updated_at`, `deleted`)
VALUES
(910001, 'U_ADMIN_900001', '900001', '$2a$10$jRUEIwye77.esrMYRi8UuOYTgtXuGKLy2/9k2SNoVN6WZcYRkfnHu', NULL, '系统管理员', '系统管理员', '13800000001', 'admin@test.local', 'ADMIN', 'ADMIN', 900001, 1, '绑定旧管理员登录表', NOW(), NOW(), 0),
(910002, 'U_TEACHER_900101', '900101', '$2a$10$jRUEIwye77.esrMYRi8UuOYTgtXuGKLy2/9k2SNoVN6WZcYRkfnHu', NULL, '演示教师', '演示教师', '13800000002', 'teacher@test.local', 'TEACHER', 'TEACHER', 900101, 1, '绑定旧教师登录表', NOW(), NOW(), 0),
(910003, 'U_STUDENT_900201', '900201', '$2a$10$jRUEIwye77.esrMYRi8UuOYTgtXuGKLy2/9k2SNoVN6WZcYRkfnHu', NULL, '演示学生', '演示学生', '13800000003', 'student@test.local', 'STUDENT', 'STUDENT', 900201, 1, '绑定旧学生登录表', NOW(), NOW(), 0);

-- 步骤9：分配用户角色
INSERT INTO `sys_user_role`
(`user_id`, `role_id`, `created_at`, `updated_at`, `deleted`)
VALUES
(910001, 920001, NOW(), NOW(), 0),
(910002, 920002, NOW(), NOW(), 0),
(910003, 920003, NOW(), NOW(), 0);

-- 步骤10：分配角色菜单
INSERT INTO `sys_role_menu`
(`role_id`, `menu_id`, `created_at`, `updated_at`, `deleted`)
VALUES
(920001, 940000, NOW(), NOW(), 0),
(920001, 940001, NOW(), NOW(), 0),
(920001, 940002, NOW(), NOW(), 0),
(920001, 940003, NOW(), NOW(), 0),
(920001, 940004, NOW(), NOW(), 0),
(920001, 940005, NOW(), NOW(), 0),
(920002, 940000, NOW(), NOW(), 0),
(920002, 940001, NOW(), NOW(), 0),
(920002, 940003, NOW(), NOW(), 0),
(920002, 940005, NOW(), NOW(), 0),
(920003, 940000, NOW(), NOW(), 0),
(920003, 940001, NOW(), NOW(), 0),
(920003, 940003, NOW(), NOW(), 0),
(920003, 940005, NOW(), NOW(), 0);

-- 步骤11：分配角色权限
INSERT INTO `sys_role_permission`
(`role_id`, `permission_id`, `created_at`, `updated_at`, `deleted`)
VALUES
(920001, 930001, NOW(), NOW(), 0),
(920001, 930002, NOW(), NOW(), 0),
(920001, 930003, NOW(), NOW(), 0),
(920001, 930004, NOW(), NOW(), 0),
(920001, 930005, NOW(), NOW(), 0),
(920002, 930001, NOW(), NOW(), 0),
(920002, 930003, NOW(), NOW(), 0),
(920002, 930005, NOW(), NOW(), 0),
(920003, 930001, NOW(), NOW(), 0),
(920003, 930003, NOW(), NOW(), 0),
(920003, 930005, NOW(), NOW(), 0);
