-- 20261123_01_add_region_division.sql
-- 目标：新增中国行政区划字典表「org_region_division」（省/市/区县三级），
--       在「组织架构」目录下新增「行政区划」菜单，并只给 ADMIN 授权菜单与
--       page:region:view、btn:region:sync 两个权限点。
-- 幂等：全部写入均带 information_schema / NOT EXISTS 判定，可重复执行；
--       不修改 doc/schema.sql 历史基线。

USE `course_arrange_v2`;

-- ---------------------------------------------------------------------------
-- 1. 建表 org_region_division（先查 information_schema 判断表是否已存在）
-- ---------------------------------------------------------------------------
SET @region_table_exists := (
    SELECT COUNT(*)
    FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'org_region_division'
);

SET @region_create_ddl := IF(
    @region_table_exists = 0,
    'CREATE TABLE `org_region_division` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT ''主键'',
  `region_code` varchar(12) NOT NULL COMMENT ''行政区划代码（GB/T 2260 风格，业务唯一键）'',
  `region_name` varchar(64) NOT NULL COMMENT ''行政区划名称'',
  `parent_code` varchar(12) NOT NULL DEFAULT ''0'' COMMENT ''父级行政区划代码，省级固定为 0'',
  `region_level` tinyint NOT NULL COMMENT ''层级：1 省 / 2 市 / 3 区县'',
  `sort_no` int NOT NULL DEFAULT 0 COMMENT ''排序号，按数据源出现顺序生成'',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT ''状态：1 启用 0 禁用'',
  `remark` varchar(255) DEFAULT NULL COMMENT ''备注'',
  `created_by` bigint DEFAULT NULL COMMENT ''创建人'',
  `updated_by` bigint DEFAULT NULL COMMENT ''更新人'',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间'',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间'',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT ''逻辑删除：0 正常 1 数据源已移除（软停用）'',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_org_region_code` (`region_code`),
  KEY `idx_org_region_parent` (`parent_code`),
  KEY `idx_org_region_level` (`region_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT=''中国行政区划字典表（省/市/区县三级）''',
    'SELECT ''org_region_division 已存在，跳过建表'' AS notice'
);

PREPARE region_stmt FROM @region_create_ddl;
EXECUTE region_stmt;
DEALLOCATE PREPARE region_stmt;

-- ---------------------------------------------------------------------------
-- 2. 校验父级目录：menu_code='organization' 且 menu_type='CATALOG' 必须恰好一行
--    （父级缺失或出现多行时中止脚本，避免菜单挂错位置）
-- ---------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS `tmp_assert_region_menu_parent`;
DELIMITER $$
CREATE PROCEDURE `tmp_assert_region_menu_parent`()
BEGIN
    DECLARE v_cnt INT DEFAULT -1;
    SELECT COUNT(*) INTO v_cnt
    FROM sys_menu
    WHERE menu_code = 'organization'
      AND menu_type = 'CATALOG';
    IF v_cnt <> 1 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '增量脚本中止：要求 menu_code=organization 且 menu_type=CATALOG 的目录恰好一行，实际不满足，请先核对 sys_menu 数据';
    END IF;
END$$
DELIMITER ;

CALL `tmp_assert_region_menu_parent`();
DROP PROCEDURE `tmp_assert_region_menu_parent`;

-- ---------------------------------------------------------------------------
-- 3. 插入「行政区划」菜单（挂在 organization 目录下，sort_no 取该目录下现有最大值 + 1，
--    保证排在学段之后；WHERE NOT EXISTS 保证重复执行不重复插入）
-- ---------------------------------------------------------------------------
SET @region_parent_id := (
    SELECT id FROM sys_menu
    WHERE menu_code = 'organization' AND menu_type = 'CATALOG'
    LIMIT 1
);

INSERT INTO sys_menu
    (menu_code, parent_id, menu_name, menu_type, route_name, route_path, component_path,
     icon, permission_code, is_hidden, is_keep_alive, sort_no, status)
SELECT 'region', @region_parent_id, '行政区划', 'MENU', 'RegionPage', '/organization/region',
       'views/organization/RegionView.vue', 'Location', 'page:region:view', 0, 0,
       (SELECT IFNULL(MAX(m.sort_no), 0) + 1 FROM sys_menu m WHERE m.parent_id = @region_parent_id),
       1
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE menu_code = 'region'
);

-- ---------------------------------------------------------------------------
-- 4. 插入权限点：page:region:view（页面级）与 btn:region:sync（按钮级）
--    （按 permission_code 判定存在性，重复执行不重复插入）
-- ---------------------------------------------------------------------------
INSERT INTO sys_permission
    (permission_code, permission_name, permission_type, resource_path, http_method, status)
SELECT 'page:region:view', '行政区划页面', 'PAGE', '/organization/region', NULL, 1
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM sys_permission WHERE permission_code = 'page:region:view'
);

INSERT INTO sys_permission
    (permission_code, permission_name, permission_type, resource_path, http_method, status)
SELECT 'btn:region:sync', '行政区划更新按钮', 'BUTTON', '/organization/region', NULL, 1
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM sys_permission WHERE permission_code = 'btn:region:sync'
);

-- ---------------------------------------------------------------------------
-- 5. 只给 ADMIN 角色授权：region 菜单 + 上述两个权限点
--    （教师/学生不插入任何行；NOT EXISTS 防重复）
-- ---------------------------------------------------------------------------
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r
JOIN sys_menu m ON m.menu_code = 'region'
WHERE r.role_code = 'ADMIN'
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu rm
      WHERE rm.role_id = r.id AND rm.menu_id = m.id
  );

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p ON p.permission_code IN ('page:region:view', 'btn:region:sync')
WHERE r.role_code = 'ADMIN'
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_permission rp
      WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );
