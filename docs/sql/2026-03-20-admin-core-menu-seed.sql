-- 管理员核心菜单补齐
-- 作用：
-- 1. 为 sys_menu 补齐新前端已经存在但管理员左侧菜单缺失的入口。
-- 2. 为 ADMIN 角色补齐 sys_role_menu 关联。
-- 3. 该 SQL 只做增量插入，重复执行不会产生重复数据。

-- 1. 补齐顶层菜单：排课任务
INSERT INTO sys_menu (
    menu_code, parent_id, menu_name, menu_type, route_name, route_path, component_path,
    icon, permission_code, is_hidden, is_keep_alive, sort_no, status,
    created_by, updated_by, created_at, updated_at, deleted, remark
)
SELECT
    'course-plan', 0, '排课任务', 'MENU', 'CoursePlan', '/course-plan', 'views/course/CoursePlanView.vue',
    'Tickets', 'page:course-plan:view', 0, 0, 2, 1,
    1, 1, NOW(), NOW(), 0, '补齐管理员排课任务菜单'
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE menu_code = 'course-plan' AND deleted = 0
);

-- 2. 补齐顶层菜单：课表管理
INSERT INTO sys_menu (
    menu_code, parent_id, menu_name, menu_type, route_name, route_path, component_path,
    icon, permission_code, is_hidden, is_keep_alive, sort_no, status,
    created_by, updated_by, created_at, updated_at, deleted, remark
)
SELECT
    'schedule', 0, '课表管理', 'MENU', 'Schedule', '/schedule', 'views/course/ScheduleView.vue',
    'Calendar', 'page:schedule:view', 0, 0, 3, 1,
    1, 1, NOW(), NOW(), 0, '补齐管理员课表管理菜单'
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE menu_code = 'schedule' AND deleted = 0
);

-- 3. 补齐基础数据目录
INSERT INTO sys_menu (
    menu_code, parent_id, menu_name, menu_type, route_name, route_path, component_path,
    icon, permission_code, is_hidden, is_keep_alive, sort_no, status,
    created_by, updated_by, created_at, updated_at, deleted, remark
)
SELECT
    'base-data', 0, '基础数据', 'CATALOG', 'BaseDataCatalog', '/base-data', 'layouts/BasicLayout.vue',
    'Collection', NULL, 0, 0, 4, 1,
    1, 1, NOW(), NOW(), 0, '补齐管理员基础数据目录'
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE menu_code = 'base-data' AND deleted = 0
);

-- 4. 补齐基础数据子菜单：教师管理
INSERT INTO sys_menu (
    menu_code, parent_id, menu_name, menu_type, route_name, route_path, component_path,
    icon, permission_code, is_hidden, is_keep_alive, sort_no, status,
    created_by, updated_by, created_at, updated_at, deleted, remark
)
SELECT
    'base-teacher', parent_menu.id, '教师管理', 'MENU', 'TeacherManagePage', '/base-data/teachers', 'views/base/BaseDataView.vue',
    'User', 'page:teacher:view', 0, 0, 1, 1,
    1, 1, NOW(), NOW(), 0, '补齐管理员教师管理菜单'
FROM sys_menu parent_menu
WHERE parent_menu.menu_code = 'base-data'
  AND parent_menu.deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM sys_menu WHERE menu_code = 'base-teacher' AND deleted = 0
  );

-- 5. 补齐基础数据子菜单：学生管理
INSERT INTO sys_menu (
    menu_code, parent_id, menu_name, menu_type, route_name, route_path, component_path,
    icon, permission_code, is_hidden, is_keep_alive, sort_no, status,
    created_by, updated_by, created_at, updated_at, deleted, remark
)
SELECT
    'base-student', parent_menu.id, '学生管理', 'MENU', 'StudentManagePage', '/base-data/students', 'views/base/BaseDataView.vue',
    'UserFilled', 'page:student:view', 0, 0, 2, 1,
    1, 1, NOW(), NOW(), 0, '补齐管理员学生管理菜单'
FROM sys_menu parent_menu
WHERE parent_menu.menu_code = 'base-data'
  AND parent_menu.deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM sys_menu WHERE menu_code = 'base-student' AND deleted = 0
  );

-- 6. 补齐基础数据子菜单：课程管理
INSERT INTO sys_menu (
    menu_code, parent_id, menu_name, menu_type, route_name, route_path, component_path,
    icon, permission_code, is_hidden, is_keep_alive, sort_no, status,
    created_by, updated_by, created_at, updated_at, deleted, remark
)
SELECT
    'base-course', parent_menu.id, '课程管理', 'MENU', 'CourseManagePage', '/base-data/courses', 'views/base/BaseDataView.vue',
    'Reading', 'page:course:view', 0, 0, 3, 1,
    1, 1, NOW(), NOW(), 0, '补齐管理员课程管理菜单'
FROM sys_menu parent_menu
WHERE parent_menu.menu_code = 'base-data'
  AND parent_menu.deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM sys_menu WHERE menu_code = 'base-course' AND deleted = 0
  );

-- 7. 补齐基础数据子菜单：教室管理
INSERT INTO sys_menu (
    menu_code, parent_id, menu_name, menu_type, route_name, route_path, component_path,
    icon, permission_code, is_hidden, is_keep_alive, sort_no, status,
    created_by, updated_by, created_at, updated_at, deleted, remark
)
SELECT
    'base-classroom', parent_menu.id, '教室管理', 'MENU', 'ClassroomManagePage', '/base-data/classrooms', 'views/base/BaseDataView.vue',
    'OfficeBuilding', 'page:classroom:view', 0, 0, 4, 1,
    1, 1, NOW(), NOW(), 0, '补齐管理员教室管理菜单'
FROM sys_menu parent_menu
WHERE parent_menu.menu_code = 'base-data'
  AND parent_menu.deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM sys_menu WHERE menu_code = 'base-classroom' AND deleted = 0
  );

-- 8. 补齐顶层菜单：重构说明
INSERT INTO sys_menu (
    menu_code, parent_id, menu_name, menu_type, route_name, route_path, component_path,
    icon, permission_code, is_hidden, is_keep_alive, sort_no, status,
    created_by, updated_by, created_at, updated_at, deleted, remark
)
SELECT
    'guide', 0, '重构说明', 'MENU', 'Guide', '/guide', 'views/system/SystemGuideView.vue',
    'Document', NULL, 0, 0, 7, 1,
    1, 1, NOW(), NOW(), 0, '补齐管理员重构说明菜单'
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE menu_code = 'guide' AND deleted = 0
);

-- 9. 为 ADMIN 角色补齐菜单授权
INSERT INTO sys_role_menu (
    role_id, menu_id, created_by, updated_by, created_at, updated_at, deleted, remark
)
SELECT
    r.id, m.id, 1, 1, NOW(), NOW(), 0, '补齐管理员默认菜单授权'
FROM sys_role r
JOIN sys_menu m ON m.deleted = 0
LEFT JOIN sys_role_menu rm ON rm.role_id = r.id AND rm.menu_id = m.id AND rm.deleted = 0
WHERE r.role_code = 'ADMIN'
  AND r.deleted = 0
  AND m.menu_code IN (
      'course-plan',
      'schedule',
      'base-data',
      'base-teacher',
      'base-student',
      'base-course',
      'base-classroom',
      'guide'
  )
  AND rm.id IS NULL;
