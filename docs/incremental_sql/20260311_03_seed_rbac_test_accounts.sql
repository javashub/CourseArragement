-- =====================================================================
-- 初始化 RBAC 测试账号、角色、菜单、权限
-- 说明：
-- 1. 本脚本为增量脚本，面向 course_arrange_v2 执行
-- 2. 当前认证已统一收口到 sys_user / sys_role / sys_menu / sys_permission
-- 3. 默认测试密码统一为 123456（BCrypt）
--
-- 测试账号：
-- 管理员：username=900001  密码=123456
-- 教师：username=900101  密码=123456
-- 学生：username=900201  密码=123456
-- =====================================================================

SET NAMES utf8mb4;

-- 步骤1：清理旧测试数据，保证脚本可重复执行
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

-- 步骤2：写入 RBAC 角色
INSERT INTO `sys_role`
(`id`, `role_code`, `role_name`, `role_type`, `data_scope_type`, `sort_no`, `status`, `remark`, `created_at`, `updated_at`, `deleted`)
VALUES
(920001, 'ADMIN', '管理员', 'SYSTEM', 'ALL', 1, 1, '系统管理员角色', NOW(), NOW(), 0),
(920002, 'TEACHER', '教师', 'SYSTEM', 'SELF', 2, 1, '教师角色', NOW(), NOW(), 0),
(920003, 'STUDENT', '学生', 'SYSTEM', 'SELF', 3, 1, '学生角色', NOW(), NOW(), 0);

-- 步骤3：写入 RBAC 权限
INSERT INTO `sys_permission`
(`id`, `permission_code`, `permission_name`, `permission_type`, `resource_path`, `http_method`, `status`, `remark`, `created_at`, `updated_at`, `deleted`)
VALUES
(930001, 'dashboard:view', '查看工作台', 'PAGE', '/dashboard', 'GET', 1, '工作台页面权限', NOW(), NOW(), 0),
(930002, 'course-plan:view', '查看排课任务', 'PAGE', '/course-plan', 'GET', 1, '排课任务页面权限', NOW(), NOW(), 0),
(930003, 'schedule:view', '查看课表管理', 'PAGE', '/schedule', 'GET', 1, '课表管理页面权限', NOW(), NOW(), 0),
(930004, 'base-data:view', '查看基础数据', 'PAGE', '/base-data', 'GET', 1, '基础数据页面权限', NOW(), NOW(), 0),
(930005, 'guide:view', '查看系统说明', 'PAGE', '/guide', 'GET', 1, '系统说明页面权限', NOW(), NOW(), 0);

-- 步骤4：写入 RBAC 菜单
INSERT INTO `sys_menu`
(`id`, `menu_code`, `parent_id`, `menu_name`, `menu_type`, `route_name`, `route_path`, `component_path`, `icon`, `permission_code`, `is_hidden`, `is_keep_alive`, `sort_no`, `status`, `remark`, `created_at`, `updated_at`, `deleted`)
VALUES
(940000, 'ROOT_WORKBENCH', 0, '系统工作台', 'CATALOG', 'RootWorkbench', '/', 'layouts/BasicLayout', 'House', NULL, 0, 0, 1, 1, '前端主框架根节点', NOW(), NOW(), 0),
(940001, 'MENU_DASHBOARD', 940000, '工作台', 'MENU', 'Dashboard', '/dashboard', 'views/dashboard/DashboardView.vue', 'HomeFilled', 'dashboard:view', 0, 1, 10, 1, '工作台页面', NOW(), NOW(), 0),
(940002, 'MENU_COURSE_PLAN', 940000, '排课任务', 'MENU', 'CoursePlan', '/course-plan', 'views/course/CoursePlanView.vue', 'Tickets', 'course-plan:view', 0, 1, 20, 1, '排课任务页面', NOW(), NOW(), 0),
(940003, 'MENU_SCHEDULE', 940000, '课表管理', 'MENU', 'Schedule', '/schedule', 'views/course/ScheduleView.vue', 'Calendar', 'schedule:view', 0, 1, 30, 1, '课表管理页面', NOW(), NOW(), 0),
(940004, 'MENU_BASE_DATA', 940000, '基础数据', 'MENU', 'BaseData', '/base-data', 'views/base/BaseDataView.vue', 'DataBoard', 'base-data:view', 0, 1, 40, 1, '基础数据页面', NOW(), NOW(), 0),
(940005, 'MENU_GUIDE', 940000, '重构说明', 'MENU', 'Guide', '/guide', 'views/system/SystemGuideView.vue', 'Document', 'guide:view', 0, 1, 50, 1, '系统说明页面', NOW(), NOW(), 0);

-- 步骤5：写入标准认证账号
INSERT INTO `sys_user`
(`id`, `user_code`, `username`, `password_hash`, `password_salt`, `real_name`, `display_name`, `mobile`, `email`, `user_type`, `source_type`, `source_id`, `status`, `remark`, `created_at`, `updated_at`, `deleted`)
VALUES
(910001, 'U_ADMIN_900001', '900001', '$2a$10$jRUEIwye77.esrMYRi8UuOYTgtXuGKLy2/9k2SNoVN6WZcYRkfnHu', NULL, '系统管理员', '系统管理员', '13800000001', 'admin@test.local', 'ADMIN', NULL, NULL, 1, '标准 RBAC 管理员测试账号', NOW(), NOW(), 0),
(910002, 'U_TEACHER_900101', '900101', '$2a$10$jRUEIwye77.esrMYRi8UuOYTgtXuGKLy2/9k2SNoVN6WZcYRkfnHu', NULL, '演示教师', '演示教师', '13800000002', 'teacher@test.local', 'TEACHER', NULL, NULL, 1, '标准 RBAC 教师测试账号', NOW(), NOW(), 0),
(910003, 'U_STUDENT_900201', '900201', '$2a$10$jRUEIwye77.esrMYRi8UuOYTgtXuGKLy2/9k2SNoVN6WZcYRkfnHu', NULL, '演示学生', '演示学生', '13800000003', 'student@test.local', 'STUDENT', NULL, NULL, 1, '标准 RBAC 学生测试账号', NOW(), NOW(), 0);

-- 步骤6：分配用户角色
INSERT INTO `sys_user_role`
(`user_id`, `role_id`, `created_at`, `updated_at`, `deleted`)
VALUES
(910001, 920001, NOW(), NOW(), 0),
(910002, 920002, NOW(), NOW(), 0),
(910003, 920003, NOW(), NOW(), 0);

-- 步骤7：分配角色菜单
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

-- 步骤8：分配角色权限
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
