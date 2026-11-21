-- 20261121_01_split_role_dashboard_menus.sql
-- 目标：教师/学生不再继承管理员工作台，改为各自的首页与课表入口。
-- 幂等：清理旧授权 + 菜单/权限 upsert + 授权去重，可重复执行。

USE `course_arrange_v2`;

-- 1. 清理 TEACHER/STUDENT 对管理员路由的菜单授权（含历史重复行）
DELETE rm
FROM sys_role_menu rm
JOIN sys_role r ON r.id = rm.role_id
JOIN sys_menu m ON m.id = rm.menu_id
WHERE r.role_code IN ('TEACHER', 'STUDENT')
  AND (
    m.route_path IN ('/', '/dashboard', '/schedule', '/guide')
    OR m.route_path LIKE '/base-data/%'
  );

-- 2. 写入教师/学生首页与课表菜单（按 menu_code 幂等）
INSERT INTO sys_menu
  (menu_code, parent_id, menu_name, menu_type, route_name, route_path, component_path,
   icon, permission_code, is_hidden, is_keep_alive, sort_no, status)
VALUES
  ('teacher-home', 0, '工作台', 'MENU', 'TeacherDashboard', '/teacher/dashboard',
   'views/teacher/TeacherDashboardView.vue', 'House', 'page:teacher-home:view', 0, 0, 1, 1),
  ('teacher-timetable', 0, '我的课表', 'MENU', 'TeacherTimetable', '/teacher/timetable',
   'views/teacher/TeacherTimetableView.vue', 'Calendar', 'page:teacher-timetable:view', 0, 0, 2, 1),
  ('student-home', 0, '工作台', 'MENU', 'StudentDashboard', '/student/dashboard',
   'views/student/StudentDashboardView.vue', 'House', 'page:student-home:view', 0, 0, 1, 1),
  ('student-timetable', 0, '我的课表', 'MENU', 'StudentTimetable', '/student/timetable',
   'views/student/StudentTimetableView.vue', 'Calendar', 'page:student-timetable:view', 0, 0, 2, 1)
ON DUPLICATE KEY UPDATE
  menu_name = VALUES(menu_name),
  route_path = VALUES(route_path),
  component_path = VALUES(component_path),
  permission_code = VALUES(permission_code),
  is_hidden = 0,
  status = 1,
  deleted = 0;

-- 3. 写入四条 PAGE 权限（按 permission_code 幂等）
INSERT INTO sys_permission
  (permission_code, permission_name, permission_type, resource_path, http_method, status)
VALUES
  ('page:teacher-home:view', '教师工作台', 'PAGE', '/teacher/dashboard', 'GET', 1),
  ('page:teacher-timetable:view', '教师我的课表', 'PAGE', '/teacher/timetable', 'GET', 1),
  ('page:student-home:view', '学生工作台', 'PAGE', '/student/dashboard', 'GET', 1),
  ('page:student-timetable:view', '学生我的课表', 'PAGE', '/student/timetable', 'GET', 1)
ON DUPLICATE KEY UPDATE
  permission_name = VALUES(permission_name),
  resource_path = VALUES(resource_path),
  status = 1,
  deleted = 0;

-- 4. 给 TEACHER 授权菜单
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r
JOIN sys_menu m ON m.menu_code IN ('teacher-home', 'teacher-timetable')
WHERE r.role_code = 'TEACHER'
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.id AND rm.menu_id = m.id
  );

-- 5. 给 STUDENT 授权菜单
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r
JOIN sys_menu m ON m.menu_code IN ('student-home', 'student-timetable')
WHERE r.role_code = 'STUDENT'
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.id AND rm.menu_id = m.id
  );

-- 6. 给 TEACHER 授权权限点
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p ON p.permission_code IN ('page:teacher-home:view', 'page:teacher-timetable:view')
WHERE r.role_code = 'TEACHER'
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_permission rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- 7. 给 STUDENT 授权权限点
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p ON p.permission_code IN ('page:student-home:view', 'page:student-timetable:view')
WHERE r.role_code = 'STUDENT'
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_permission rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );
