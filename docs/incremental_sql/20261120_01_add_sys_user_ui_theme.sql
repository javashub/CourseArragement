-- 20261120_01_add_sys_user_ui_theme.sql
-- 为 sys_user 增加界面主题偏好字段（账号级持久化）。
-- 幂等写法：先查 information_schema，只在字段缺失时执行 ALTER，重复执行不报错。
USE `course_arrange_v2`;

SET @schema_name := DATABASE();

SET @column_exists := (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'sys_user'
      AND COLUMN_NAME = 'ui_theme'
);

SET @ddl := IF(@column_exists = 0,
    'ALTER TABLE `sys_user` ADD COLUMN `ui_theme` varchar(16) NOT NULL DEFAULT ''daylight'' COMMENT ''界面主题：daylight/deep-teal/top-nav'' AFTER `last_login_ip`',
    'SELECT ''sys_user.ui_theme 已存在，跳过新增'' AS message');

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
