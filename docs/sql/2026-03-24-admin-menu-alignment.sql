-- 管理员菜单与前端路由对齐修复脚本
-- 适用对象：当前 Docker 容器 mysql8 中的 course_arrange_v2
-- 当前已确认的真实数据现状：
-- 1. sys_menu 中同时存在旧的 ROOT_WORKBENCH / MENU_* 菜单和新的顶层菜单，管理员左侧菜单结构混杂。
-- 2. ADMIN 当前缺少 /organization/college、/organization/stage、/system/config、/system/rbac 对应菜单。
-- 3. sys_permission 中仍保留旧权限编码 dashboard:view / course-plan:view / schedule:view / base-data:view / guide:view，
--    但前端路由与代码默认使用 page:* 风格权限。
-- 4. 当前 ADMIN 角色已绑定管理员账号 900001，因此只要补齐 ADMIN 的菜单和权限，当前管理员账号即可看到页面。
-- 5. 首页当前只保留单一 `/dashboard` 入口，前端样式采用编辑式控制台实现；历史上独立的
--    `dashboard-editorial` 菜单/权限仅作为回收清理对象，不再作为可见菜单保留。
--
-- 脚本目标：
-- 1. 建立与前端 router/index.js 对齐的管理员菜单结构：
--    /dashboard
--    /course-plan
--    /schedule
--    /base-data/*
--    /organization/*
--    /system/config
--    /system/rbac
--    /guide
-- 2. 补齐 page:* 权限与 ADMIN 的 role-permission 关联。
-- 3. 将旧的 ROOT_WORKBENCH / MENU_* 菜单及其 role-menu 关系软删除，避免左侧菜单重复或结构错乱。
-- 4. 回收历史 `dashboard-editorial` 菜单与权限，并把历史行的中文名称修正为可读文本，避免出现乱码。
-- 5. 该脚本可重复执行。

-- 执行前可先人工检查：
-- SELECT id, menu_code, parent_id, menu_name, route_path, permission_code, sort_no, deleted
-- FROM sys_menu
-- ORDER BY deleted, parent_id, sort_no, id;
--
-- SELECT id, permission_code, permission_name, resource_path, deleted
-- FROM sys_permission
-- ORDER BY deleted, id;
--
-- SELECT r.role_code, m.menu_code, m.menu_name, m.route_path, rm.deleted
-- FROM sys_role r
-- LEFT JOIN sys_role_menu rm ON rm.role_id = r.id
-- LEFT JOIN sys_menu m ON m.id = rm.menu_id
-- WHERE r.role_code = 'ADMIN'
-- ORDER BY rm.deleted, m.parent_id, m.sort_no, m.id;

-- 一、补齐前端实际使用的 page:* 页面权限
INSERT INTO sys_permission (
    permission_code, permission_name, permission_type, resource_path, http_method, status,
    remark, created_at, updated_at, deleted
)
SELECT
    'page:dashboard:view', '查看工作台', 'PAGE', '/dashboard', 'GET', 1,
    '管理员工作台页面权限', NOW(), NOW(), 0
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM sys_permission WHERE permission_code = 'page:dashboard:view' AND deleted = 0
);

INSERT INTO sys_permission (
    permission_code, permission_name, permission_type, resource_path, http_method, status,
    remark, created_at, updated_at, deleted
)
SELECT
    'page:course-plan:view', '查看排课任务', 'PAGE', '/course-plan', 'GET', 1,
    '管理员排课任务页面权限', NOW(), NOW(), 0
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM sys_permission WHERE permission_code = 'page:course-plan:view' AND deleted = 0
);

INSERT INTO sys_permission (
    permission_code, permission_name, permission_type, resource_path, http_method, status,
    remark, created_at, updated_at, deleted
)
SELECT
    'page:schedule:view', '查看课表管理', 'PAGE', '/schedule', 'GET', 1,
    '管理员课表管理页面权限', NOW(), NOW(), 0
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM sys_permission WHERE permission_code = 'page:schedule:view' AND deleted = 0
);

INSERT INTO sys_permission (
    permission_code, permission_name, permission_type, resource_path, http_method, status,
    remark, created_at, updated_at, deleted
)
SELECT
    'page:base-data:view', '查看基础数据', 'PAGE', '/base-data', 'GET', 1,
    '管理员基础数据页面权限', NOW(), NOW(), 0
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM sys_permission WHERE permission_code = 'page:base-data:view' AND deleted = 0
);

INSERT INTO sys_permission (
    permission_code, permission_name, permission_type, resource_path, http_method, status,
    remark, created_at, updated_at, deleted
)
SELECT
    'page:teacher:view', '查看教师管理', 'PAGE', '/base-data/teachers', 'GET', 1,
    '管理员教师管理页面权限', NOW(), NOW(), 0
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM sys_permission WHERE permission_code = 'page:teacher:view' AND deleted = 0
);

INSERT INTO sys_permission (
    permission_code, permission_name, permission_type, resource_path, http_method, status,
    remark, created_at, updated_at, deleted
)
SELECT
    'page:student:view', '查看学生管理', 'PAGE', '/base-data/students', 'GET', 1,
    '管理员学生管理页面权限', NOW(), NOW(), 0
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM sys_permission WHERE permission_code = 'page:student:view' AND deleted = 0
);

INSERT INTO sys_permission (
    permission_code, permission_name, permission_type, resource_path, http_method, status,
    remark, created_at, updated_at, deleted
)
SELECT
    'page:course:view', '查看课程管理', 'PAGE', '/base-data/courses', 'GET', 1,
    '管理员课程管理页面权限', NOW(), NOW(), 0
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM sys_permission WHERE permission_code = 'page:course:view' AND deleted = 0
);

INSERT INTO sys_permission (
    permission_code, permission_name, permission_type, resource_path, http_method, status,
    remark, created_at, updated_at, deleted
)
SELECT
    'page:classroom:view', '查看教室管理', 'PAGE', '/base-data/classrooms', 'GET', 1,
    '管理员教室管理页面权限', NOW(), NOW(), 0
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM sys_permission WHERE permission_code = 'page:classroom:view' AND deleted = 0
);

INSERT INTO sys_permission (
    permission_code, permission_name, permission_type, resource_path, http_method, status,
    remark, created_at, updated_at, deleted
)
SELECT
    'page:campus:view', '查看校区管理', 'PAGE', '/organization/campus', 'GET', 1,
    '管理员校区管理页面权限', NOW(), NOW(), 0
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM sys_permission WHERE permission_code = 'page:campus:view' AND deleted = 0
);

UPDATE sys_permission
SET
    permission_name = '查看校区管理',
    resource_path = '/organization/campus',
    permission_type = 'PAGE',
    http_method = 'GET',
    status = 1,
    updated_at = NOW()
WHERE permission_code = 'page:campus:view'
  AND deleted = 0;

INSERT INTO sys_permission (
    permission_code, permission_name, permission_type, resource_path, http_method, status,
    remark, created_at, updated_at, deleted
)
SELECT
    'page:college:view', '查看学院管理', 'PAGE', '/organization/college', 'GET', 1,
    '管理员学院管理页面权限', NOW(), NOW(), 0
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM sys_permission WHERE permission_code = 'page:college:view' AND deleted = 0
);

INSERT INTO sys_permission (
    permission_code, permission_name, permission_type, resource_path, http_method, status,
    remark, created_at, updated_at, deleted
)
SELECT
    'page:stage:view', '查看学段管理', 'PAGE', '/organization/stage', 'GET', 1,
    '管理员学段管理页面权限', NOW(), NOW(), 0
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM sys_permission WHERE permission_code = 'page:stage:view' AND deleted = 0
);

INSERT INTO sys_permission (
    permission_code, permission_name, permission_type, resource_path, http_method, status,
    remark, created_at, updated_at, deleted
)
SELECT
    'page:config:view', '查看系统配置', 'PAGE', '/system/config', 'GET', 1,
    '管理员系统配置页面权限', NOW(), NOW(), 0
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM sys_permission WHERE permission_code = 'page:config:view' AND deleted = 0
);

INSERT INTO sys_permission (
    permission_code, permission_name, permission_type, resource_path, http_method, status,
    remark, created_at, updated_at, deleted
)
SELECT
    'page:rbac:view', '查看权限管理', 'PAGE', '/system/rbac', 'GET', 1,
    '管理员权限管理页面权限', NOW(), NOW(), 0
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM sys_permission WHERE permission_code = 'page:rbac:view' AND deleted = 0
);

-- 二、建立规范化的管理员菜单结构
INSERT INTO sys_menu (
    menu_code, parent_id, menu_name, menu_type, route_name, route_path, component_path,
    icon, permission_code, is_hidden, is_keep_alive, sort_no, status,
    created_by, updated_by, created_at, updated_at, deleted, remark
)
SELECT
    'dashboard', 0, '工作台', 'MENU', 'Dashboard', '/dashboard', 'views/dashboard/DashboardView.vue',
    'House', 'page:dashboard:view', 0, 0, 1, 1,
    1, 1, NOW(), NOW(), 0, '管理员工作台菜单'
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE menu_code = 'dashboard' AND deleted = 0
);

INSERT INTO sys_menu (
    menu_code, parent_id, menu_name, menu_type, route_name, route_path, component_path,
    icon, permission_code, is_hidden, is_keep_alive, sort_no, status,
    created_by, updated_by, created_at, updated_at, deleted, remark
)
SELECT
    'base-data', 0, '基础数据', 'CATALOG', 'BaseDataCatalog', '/base-data', 'layouts/BasicLayout.vue',
    'Collection', NULL, 0, 0, 4, 1,
    1, 1, NOW(), NOW(), 0, '管理员基础数据目录'
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE menu_code = 'base-data' AND deleted = 0
);

INSERT INTO sys_menu (
    menu_code, parent_id, menu_name, menu_type, route_name, route_path, component_path,
    icon, permission_code, is_hidden, is_keep_alive, sort_no, status,
    created_by, updated_by, created_at, updated_at, deleted, remark
)
SELECT
    'base-teacher', parent_menu.id, '教师管理', 'MENU', 'TeacherManagePage', '/base-data/teachers', 'views/base/BaseDataView.vue',
    'User', 'page:teacher:view', 0, 0, 1, 1,
    1, 1, NOW(), NOW(), 0, '管理员教师管理菜单'
FROM sys_menu parent_menu
WHERE parent_menu.menu_code = 'base-data'
  AND parent_menu.deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM sys_menu WHERE menu_code = 'base-teacher' AND deleted = 0
  );

INSERT INTO sys_menu (
    menu_code, parent_id, menu_name, menu_type, route_name, route_path, component_path,
    icon, permission_code, is_hidden, is_keep_alive, sort_no, status,
    created_by, updated_by, created_at, updated_at, deleted, remark
)
SELECT
    'base-student', parent_menu.id, '学生管理', 'MENU', 'StudentManagePage', '/base-data/students', 'views/base/BaseDataView.vue',
    'UserFilled', 'page:student:view', 0, 0, 2, 1,
    1, 1, NOW(), NOW(), 0, '管理员学生管理菜单'
FROM sys_menu parent_menu
WHERE parent_menu.menu_code = 'base-data'
  AND parent_menu.deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM sys_menu WHERE menu_code = 'base-student' AND deleted = 0
  );

INSERT INTO sys_menu (
    menu_code, parent_id, menu_name, menu_type, route_name, route_path, component_path,
    icon, permission_code, is_hidden, is_keep_alive, sort_no, status,
    created_by, updated_by, created_at, updated_at, deleted, remark
)
SELECT
    'base-course', parent_menu.id, '课程管理', 'MENU', 'CourseManagePage', '/base-data/courses', 'views/base/BaseDataView.vue',
    'Reading', 'page:course:view', 0, 0, 3, 1,
    1, 1, NOW(), NOW(), 0, '管理员课程管理菜单'
FROM sys_menu parent_menu
WHERE parent_menu.menu_code = 'base-data'
  AND parent_menu.deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM sys_menu WHERE menu_code = 'base-course' AND deleted = 0
  );

INSERT INTO sys_menu (
    menu_code, parent_id, menu_name, menu_type, route_name, route_path, component_path,
    icon, permission_code, is_hidden, is_keep_alive, sort_no, status,
    created_by, updated_by, created_at, updated_at, deleted, remark
)
SELECT
    'base-classroom', parent_menu.id, '教室管理', 'MENU', 'ClassroomManagePage', '/base-data/classrooms', 'views/base/BaseDataView.vue',
    'OfficeBuilding', 'page:classroom:view', 0, 0, 4, 1,
    1, 1, NOW(), NOW(), 0, '管理员教室管理菜单'
FROM sys_menu parent_menu
WHERE parent_menu.menu_code = 'base-data'
  AND parent_menu.deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM sys_menu WHERE menu_code = 'base-classroom' AND deleted = 0
  );

INSERT INTO sys_menu (
    menu_code, parent_id, menu_name, menu_type, route_name, route_path, component_path,
    icon, permission_code, is_hidden, is_keep_alive, sort_no, status,
    created_by, updated_by, created_at, updated_at, deleted, remark
)
SELECT
    'organization', 0, '组织架构', 'CATALOG', 'Organization', '/organization', 'layouts/BasicLayout.vue',
    'OfficeBuilding', NULL, 0, 0, 6, 1,
    1, 1, NOW(), NOW(), 0, '管理员组织架构目录'
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE menu_code = 'organization' AND deleted = 0
);

INSERT INTO sys_menu (
    menu_code, parent_id, menu_name, menu_type, route_name, route_path, component_path,
    icon, permission_code, is_hidden, is_keep_alive, sort_no, status,
    created_by, updated_by, created_at, updated_at, deleted, remark
)
SELECT
    'campus', parent_menu.id, '校区管理', 'MENU', 'CampusPage', '/organization/campus', 'views/organization/CampusView.vue',
    'OfficeBuilding', 'page:campus:view', 0, 0, 1, 1,
    1, 1, NOW(), NOW(), 0, '管理员校区管理菜单'
FROM sys_menu parent_menu
WHERE parent_menu.menu_code = 'organization'
  AND parent_menu.deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM sys_menu WHERE menu_code = 'campus' AND deleted = 0
  );

INSERT INTO sys_menu (
    menu_code, parent_id, menu_name, menu_type, route_name, route_path, component_path,
    icon, permission_code, is_hidden, is_keep_alive, sort_no, status,
    created_by, updated_by, created_at, updated_at, deleted, remark
)
SELECT
    'college', parent_menu.id, '学院管理', 'MENU', 'CollegePage', '/organization/college', 'views/organization/CollegeView.vue',
    'School', 'page:college:view', 0, 0, 2, 1,
    1, 1, NOW(), NOW(), 0, '管理员学院管理菜单'
FROM sys_menu parent_menu
WHERE parent_menu.menu_code = 'organization'
  AND parent_menu.deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM sys_menu WHERE menu_code = 'college' AND deleted = 0
  );

INSERT INTO sys_menu (
    menu_code, parent_id, menu_name, menu_type, route_name, route_path, component_path,
    icon, permission_code, is_hidden, is_keep_alive, sort_no, status,
    created_by, updated_by, created_at, updated_at, deleted, remark
)
SELECT
    'stage', parent_menu.id, '学段管理', 'MENU', 'StagePage', '/organization/stage', 'views/organization/StageView.vue',
    'CollectionTag', 'page:stage:view', 0, 0, 3, 1,
    1, 1, NOW(), NOW(), 0, '管理员学段管理菜单'
FROM sys_menu parent_menu
WHERE parent_menu.menu_code = 'organization'
  AND parent_menu.deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM sys_menu WHERE menu_code = 'stage' AND deleted = 0
  );

INSERT INTO sys_menu (
    menu_code, parent_id, menu_name, menu_type, route_name, route_path, component_path,
    icon, permission_code, is_hidden, is_keep_alive, sort_no, status,
    created_by, updated_by, created_at, updated_at, deleted, remark
)
SELECT
    'system', 0, '系统管理', 'CATALOG', 'SystemManage', '/system', 'layouts/BasicLayout.vue',
    'Setting', NULL, 0, 0, 7, 1,
    1, 1, NOW(), NOW(), 0, '管理员系统管理目录'
FROM dual
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE menu_code = 'system' AND deleted = 0
);

INSERT INTO sys_menu (
    menu_code, parent_id, menu_name, menu_type, route_name, route_path, component_path,
    icon, permission_code, is_hidden, is_keep_alive, sort_no, status,
    created_by, updated_by, created_at, updated_at, deleted, remark
)
SELECT
    'system-config', parent_menu.id, '系统配置', 'MENU', 'SystemConfigPage', '/system/config', 'views/system/SystemConfigView.vue',
    'Setting', 'page:config:view', 0, 0, 1, 1,
    1, 1, NOW(), NOW(), 0, '管理员系统配置菜单'
FROM sys_menu parent_menu
WHERE parent_menu.menu_code = 'system'
  AND parent_menu.deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM sys_menu WHERE menu_code = 'system-config' AND deleted = 0
  );

INSERT INTO sys_menu (
    menu_code, parent_id, menu_name, menu_type, route_name, route_path, component_path,
    icon, permission_code, is_hidden, is_keep_alive, sort_no, status,
    created_by, updated_by, created_at, updated_at, deleted, remark
)
SELECT
    'system-rbac', parent_menu.id, '权限管理', 'MENU', 'RbacManagePage', '/system/rbac', 'views/system/RbacManageView.vue',
    'Lock', 'page:rbac:view', 0, 0, 2, 1,
    1, 1, NOW(), NOW(), 0, '管理员权限管理菜单'
FROM sys_menu parent_menu
WHERE parent_menu.menu_code = 'system'
  AND parent_menu.deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM sys_menu WHERE menu_code = 'system-rbac' AND deleted = 0
  );

-- 三、修正已存在的新菜单字段，确保与前端实际路由完全一致
UPDATE sys_menu
SET menu_name = '工作台', updated_at = NOW()
WHERE menu_code = 'dashboard' AND deleted = 0;

UPDATE sys_menu
SET menu_name = '排课任务', updated_at = NOW()
WHERE menu_code = 'course-plan' AND deleted = 0;

UPDATE sys_menu
SET menu_name = '课表管理', updated_at = NOW()
WHERE menu_code = 'schedule' AND deleted = 0;

UPDATE sys_menu
SET menu_name = '基础数据', updated_at = NOW()
WHERE menu_code = 'base-data' AND deleted = 0;

UPDATE sys_menu
SET menu_name = '组织架构', updated_at = NOW()
WHERE menu_code = 'organization' AND deleted = 0;

UPDATE sys_menu
SET menu_name = '系统管理', updated_at = NOW()
WHERE menu_code = 'system' AND deleted = 0;

UPDATE sys_menu
SET menu_name = '重构说明', updated_at = NOW()
WHERE menu_code = 'guide' AND deleted = 0;

UPDATE sys_menu
SET menu_name = '校区管理', updated_at = NOW()
WHERE menu_code = 'campus' AND deleted = 0;

UPDATE sys_menu
SET menu_name = '学院管理', updated_at = NOW()
WHERE menu_code = 'college' AND deleted = 0;

UPDATE sys_menu
SET menu_name = '学段管理', updated_at = NOW()
WHERE menu_code = 'stage' AND deleted = 0;

UPDATE sys_menu
SET menu_name = '系统配置', updated_at = NOW()
WHERE menu_code = 'system-config' AND deleted = 0;

UPDATE sys_menu
SET menu_name = '权限管理', updated_at = NOW()
WHERE menu_code = 'system-rbac' AND deleted = 0;

UPDATE sys_permission
SET permission_name = '查看工作台', resource_path = '/dashboard', updated_at = NOW()
WHERE permission_code = 'page:dashboard:view' AND deleted = 0;

UPDATE sys_permission
SET permission_name = '查看排课任务', resource_path = '/course-plan', updated_at = NOW()
WHERE permission_code = 'page:course-plan:view' AND deleted = 0;

UPDATE sys_permission
SET permission_name = '查看课表管理', resource_path = '/schedule', updated_at = NOW()
WHERE permission_code = 'page:schedule:view' AND deleted = 0;

UPDATE sys_permission
SET permission_name = '查看基础数据', resource_path = '/base-data', updated_at = NOW()
WHERE permission_code = 'page:base-data:view' AND deleted = 0;

UPDATE sys_permission
SET permission_name = '查看教师管理', resource_path = '/base-data/teachers', updated_at = NOW()
WHERE permission_code = 'page:teacher:view' AND deleted = 0;

UPDATE sys_permission
SET permission_name = '查看学生管理', resource_path = '/base-data/students', updated_at = NOW()
WHERE permission_code = 'page:student:view' AND deleted = 0;

UPDATE sys_permission
SET permission_name = '查看课程管理', resource_path = '/base-data/courses', updated_at = NOW()
WHERE permission_code = 'page:course:view' AND deleted = 0;

UPDATE sys_permission
SET permission_name = '查看教室管理', resource_path = '/base-data/classrooms', updated_at = NOW()
WHERE permission_code = 'page:classroom:view' AND deleted = 0;

UPDATE sys_permission
SET permission_name = '查看校区管理', resource_path = '/organization/campus', updated_at = NOW()
WHERE permission_code = 'page:campus:view' AND deleted = 0;

UPDATE sys_permission
SET permission_name = '查看学院管理', resource_path = '/organization/college', updated_at = NOW()
WHERE permission_code = 'page:college:view' AND deleted = 0;

UPDATE sys_permission
SET permission_name = '查看学段管理', resource_path = '/organization/stage', updated_at = NOW()
WHERE permission_code = 'page:stage:view' AND deleted = 0;

UPDATE sys_permission
SET permission_name = '查看系统配置', resource_path = '/system/config', updated_at = NOW()
WHERE permission_code = 'page:config:view' AND deleted = 0;

UPDATE sys_permission
SET permission_name = '查看权限管理', resource_path = '/system/rbac', updated_at = NOW()
WHERE permission_code = 'page:rbac:view' AND deleted = 0;

-- 历史独立工作台入口已下线，但仍把已删除记录的中文名称修正为正常文本，避免数据库中遗留乱码
UPDATE sys_menu
SET
    menu_name = '编辑式工作台',
    updated_at = NOW()
WHERE menu_code = 'dashboard-editorial';

UPDATE sys_permission
SET
    permission_name = '查看编辑式工作台',
    updated_at = NOW()
WHERE permission_code = 'page:dashboard-editorial:view';

UPDATE sys_menu
SET
    parent_id = 0,
    menu_name = '排课任务',
    menu_type = 'MENU',
    route_name = 'CoursePlan',
    route_path = '/course-plan',
    component_path = 'views/course/CoursePlanView.vue',
    icon = 'Tickets',
    permission_code = 'page:course-plan:view',
    is_hidden = 0,
    is_keep_alive = 0,
    sort_no = 2,
    status = 1,
    updated_at = NOW()
WHERE menu_code = 'course-plan'
  AND deleted = 0;

UPDATE sys_menu
SET
    parent_id = 0,
    menu_name = '课表管理',
    menu_type = 'MENU',
    route_name = 'Schedule',
    route_path = '/schedule',
    component_path = 'views/course/ScheduleView.vue',
    icon = 'Calendar',
    permission_code = 'page:schedule:view',
    is_hidden = 0,
    is_keep_alive = 0,
    sort_no = 3,
    status = 1,
    updated_at = NOW()
WHERE menu_code = 'schedule'
  AND deleted = 0;

UPDATE sys_menu
SET
    parent_id = 0,
    menu_name = '基础数据',
    menu_type = 'CATALOG',
    route_name = 'BaseDataCatalog',
    route_path = '/base-data',
    component_path = 'layouts/BasicLayout.vue',
    icon = 'Collection',
    permission_code = NULL,
    is_hidden = 0,
    is_keep_alive = 0,
    sort_no = 4,
    status = 1,
    updated_at = NOW()
WHERE menu_code = 'base-data'
  AND deleted = 0;

UPDATE sys_menu
SET
    parent_id = (SELECT id FROM (SELECT id FROM sys_menu WHERE menu_code = 'base-data' AND deleted = 0 LIMIT 1) tmp),
    menu_name = '教师管理',
    menu_type = 'MENU',
    route_name = 'TeacherManagePage',
    route_path = '/base-data/teachers',
    component_path = 'views/base/BaseDataView.vue',
    icon = 'User',
    permission_code = 'page:teacher:view',
    is_hidden = 0,
    is_keep_alive = 0,
    sort_no = 1,
    status = 1,
    updated_at = NOW()
WHERE menu_code = 'base-teacher'
  AND deleted = 0;

UPDATE sys_menu
SET
    parent_id = (SELECT id FROM (SELECT id FROM sys_menu WHERE menu_code = 'base-data' AND deleted = 0 LIMIT 1) tmp),
    menu_name = '学生管理',
    menu_type = 'MENU',
    route_name = 'StudentManagePage',
    route_path = '/base-data/students',
    component_path = 'views/base/BaseDataView.vue',
    icon = 'UserFilled',
    permission_code = 'page:student:view',
    is_hidden = 0,
    is_keep_alive = 0,
    sort_no = 2,
    status = 1,
    updated_at = NOW()
WHERE menu_code = 'base-student'
  AND deleted = 0;

UPDATE sys_menu
SET
    parent_id = (SELECT id FROM (SELECT id FROM sys_menu WHERE menu_code = 'base-data' AND deleted = 0 LIMIT 1) tmp),
    menu_name = '课程管理',
    menu_type = 'MENU',
    route_name = 'CourseManagePage',
    route_path = '/base-data/courses',
    component_path = 'views/base/BaseDataView.vue',
    icon = 'Reading',
    permission_code = 'page:course:view',
    is_hidden = 0,
    is_keep_alive = 0,
    sort_no = 3,
    status = 1,
    updated_at = NOW()
WHERE menu_code = 'base-course'
  AND deleted = 0;

UPDATE sys_menu
SET
    parent_id = (SELECT id FROM (SELECT id FROM sys_menu WHERE menu_code = 'base-data' AND deleted = 0 LIMIT 1) tmp),
    menu_name = '教室管理',
    menu_type = 'MENU',
    route_name = 'ClassroomManagePage',
    route_path = '/base-data/classrooms',
    component_path = 'views/base/BaseDataView.vue',
    icon = 'OfficeBuilding',
    permission_code = 'page:classroom:view',
    is_hidden = 0,
    is_keep_alive = 0,
    sort_no = 4,
    status = 1,
    updated_at = NOW()
WHERE menu_code = 'base-classroom'
  AND deleted = 0;

UPDATE sys_menu
SET
    parent_id = 0,
    menu_name = '重构说明',
    menu_type = 'MENU',
    route_name = 'Guide',
    route_path = '/guide',
    component_path = 'views/system/SystemGuideView.vue',
    icon = 'Document',
    permission_code = NULL,
    is_hidden = 0,
    is_keep_alive = 0,
    sort_no = 7,
    status = 1,
    updated_at = NOW()
WHERE menu_code = 'guide'
  AND deleted = 0;

UPDATE sys_menu
SET
    parent_id = (SELECT id FROM (SELECT id FROM sys_menu WHERE menu_code = 'organization' AND deleted = 0 LIMIT 1) tmp),
    menu_name = '校区管理',
    menu_type = 'MENU',
    route_name = 'CampusPage',
    route_path = '/organization/campus',
    component_path = 'views/organization/CampusView.vue',
    icon = 'OfficeBuilding',
    permission_code = 'page:campus:view',
    is_hidden = 0,
    is_keep_alive = 0,
    sort_no = 1,
    status = 1,
    updated_at = NOW()
WHERE menu_code = 'campus'
  AND deleted = 0;

UPDATE sys_menu
SET
    parent_id = 0,
    menu_name = '组织架构',
    menu_type = 'CATALOG',
    route_name = 'Organization',
    route_path = '/organization',
    component_path = 'layouts/BasicLayout.vue',
    icon = 'OfficeBuilding',
    permission_code = NULL,
    is_hidden = 0,
    is_keep_alive = 0,
    sort_no = 5,
    status = 1,
    updated_at = NOW()
WHERE menu_code = 'organization'
  AND deleted = 0;

UPDATE sys_menu
SET
    parent_id = 0,
    menu_name = '系统管理',
    menu_type = 'CATALOG',
    route_name = 'SystemManage',
    route_path = '/system',
    component_path = 'layouts/BasicLayout.vue',
    icon = 'Setting',
    permission_code = NULL,
    is_hidden = 0,
    is_keep_alive = 0,
    sort_no = 6,
    status = 1,
    updated_at = NOW()
WHERE menu_code = 'system'
  AND deleted = 0;

UPDATE sys_menu
SET
    parent_id = 0,
    menu_name = '工作台',
    menu_type = 'MENU',
    route_name = 'Dashboard',
    route_path = '/dashboard',
    component_path = 'views/dashboard/DashboardView.vue',
    icon = 'House',
    permission_code = 'page:dashboard:view',
    is_hidden = 0,
    is_keep_alive = 0,
    sort_no = 1,
    status = 1,
    updated_at = NOW()
WHERE menu_code = 'dashboard'
  AND deleted = 0;

-- 四、软删除旧的 seed 菜单和其激活态 role-menu 关系，避免左侧菜单重复
UPDATE sys_role_menu
SET
    deleted = 1,
    updated_at = NOW(),
    remark = '2026-03-24 菜单结构对齐，停用旧 ROOT_WORKBENCH/MENU_* 菜单授权'
WHERE deleted = 0
  AND menu_id IN (
      SELECT id
      FROM (
          SELECT id
          FROM sys_menu
          WHERE menu_code IN (
              'ROOT_WORKBENCH',
              'MENU_DASHBOARD',
              'MENU_COURSE_PLAN',
              'MENU_SCHEDULE',
              'MENU_BASE_DATA',
              'MENU_GUIDE',
              'MENU_CAMPUS',
              'dashboard-editorial'
          )
      ) legacy_menu_ids
  );

UPDATE sys_menu
SET
    deleted = 1,
    updated_at = NOW(),
    remark = '2026-03-24 菜单结构对齐后停用旧 seed 菜单'
WHERE deleted = 0
  AND menu_code IN (
      'ROOT_WORKBENCH',
      'MENU_DASHBOARD',
      'MENU_COURSE_PLAN',
      'MENU_SCHEDULE',
      'MENU_BASE_DATA',
      'MENU_GUIDE',
      'MENU_CAMPUS',
      'dashboard-editorial'
  );

UPDATE sys_role_permission
SET
    deleted = 1,
    updated_at = NOW(),
    remark = '2026-03-24 回收历史独立工作台权限，首页保留单一工作台入口'
WHERE deleted = 0
  AND permission_id IN (
      SELECT id
      FROM (
          SELECT id
          FROM sys_permission
          WHERE permission_code = 'page:dashboard-editorial:view'
      ) editorial_permission_ids
  );

UPDATE sys_permission
SET
    deleted = 1,
    updated_at = NOW(),
    remark = '2026-03-24 回收历史独立工作台权限，首页保留单一工作台入口'
WHERE deleted = 0
  AND permission_code = 'page:dashboard-editorial:view';

-- 五、为 ADMIN 补齐规范化菜单授权
INSERT INTO sys_role_menu (
    role_id, menu_id, created_by, updated_by, created_at, updated_at, deleted, remark
)
SELECT
    r.id, m.id, 1, 1, NOW(), NOW(), 0, '2026-03-24 管理员菜单结构对齐'
FROM sys_role r
JOIN sys_menu m ON m.deleted = 0
LEFT JOIN sys_role_menu rm ON rm.role_id = r.id AND rm.menu_id = m.id AND rm.deleted = 0
WHERE r.role_code = 'ADMIN'
  AND r.deleted = 0
  AND m.menu_code IN (
      'dashboard',
      'course-plan',
      'schedule',
      'base-data',
      'base-teacher',
      'base-student',
      'base-course',
      'base-classroom',
      'organization',
      'campus',
      'college',
      'stage',
      'system',
      'system-config',
      'system-rbac',
      'guide'
  )
  AND rm.id IS NULL;

-- 六、为 ADMIN 补齐规范化页面权限授权
INSERT INTO sys_role_permission (
    role_id, permission_id, created_by, updated_by, created_at, updated_at, deleted, remark
)
SELECT
    r.id, p.id, 1, 1, NOW(), NOW(), 0, '2026-03-24 管理员页面权限对齐'
FROM sys_role r
JOIN sys_permission p ON p.deleted = 0
LEFT JOIN sys_role_permission rp ON rp.role_id = r.id AND rp.permission_id = p.id AND rp.deleted = 0
WHERE r.role_code = 'ADMIN'
  AND r.deleted = 0
  AND p.permission_code IN (
      'page:dashboard:view',
      'page:course-plan:view',
      'page:schedule:view',
      'page:base-data:view',
      'page:teacher:view',
      'page:student:view',
      'page:course:view',
      'page:classroom:view',
      'page:campus:view',
      'page:college:view',
      'page:stage:view',
      'page:config:view',
      'page:rbac:view'
  )
  AND rp.id IS NULL;

-- 七、执行后建议校验
-- SELECT id, menu_code, parent_id, menu_name, route_path, permission_code, sort_no
-- FROM sys_menu
-- WHERE deleted = 0
-- ORDER BY parent_id, sort_no, id;
--
-- SELECT r.role_code, m.menu_code, m.menu_name, m.route_path
-- FROM sys_role r
-- JOIN sys_role_menu rm ON rm.role_id = r.id AND rm.deleted = 0
-- JOIN sys_menu m ON m.id = rm.menu_id AND m.deleted = 0
-- WHERE r.role_code = 'ADMIN'
-- ORDER BY m.parent_id, m.sort_no, m.id;
--
-- SELECT r.role_code, p.permission_code
-- FROM sys_role r
-- JOIN sys_role_permission rp ON rp.role_id = r.id AND rp.deleted = 0
-- JOIN sys_permission p ON p.id = rp.permission_id AND p.deleted = 0
-- WHERE r.role_code = 'ADMIN'
-- ORDER BY p.permission_code;
