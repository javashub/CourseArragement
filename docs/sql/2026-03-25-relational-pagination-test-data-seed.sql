-- 标准主链测试数据播种脚本
-- 目标库：mysql8 / course_arrange_v2
-- 执行命令：
-- docker exec -i mysql8 mysql --default-character-set=utf8mb4 -uroot -proot course_arrange_v2 < docs/sql/2026-03-25-relational-pagination-test-data-seed.sql
--
-- 设计说明：
-- 1. 仅播种 org_* / res_* / sch_* 主链测试数据，不改写 sys_* 权限菜单数据。
-- 2. 所有本轮主数据均带统一备注标记 seed:pagination:2026-03-25，重复执行时会先清理本轮数据再重放。
-- 3. 目标覆盖页面：
--    - 组织管理：校区管理、学院管理、学段管理
--    - 基础数据：教师管理、学生管理、课程管理、教室管理
--    - 排课任务：排课任务列表、执行日志
--    - 课表管理：班级/教师课表查询、调课日志
-- 4. 核心分页页默认每页 10 条，本脚本保证教师/学生/课程/教室/排课任务均达到 3 页以上。

SET NAMES utf8mb4;

SET @seed_tag := 'seed:pagination:2026-03-25';
SET @seed_prefix := 'SE26';
SET @semester_text := '2025-2026-1';
SET @seed_user_id := 910001;

-- ------------------------------------------------------------
-- 0. 清理上一轮同标识种子数据
-- ------------------------------------------------------------
DELETE sal
FROM sch_schedule_adjust_log sal
WHERE sal.adjust_code LIKE CONCAT(@seed_prefix, 'ADJ%');

DELETE sr
FROM sch_schedule_result sr
INNER JOIN sch_task st ON st.id = sr.task_id
WHERE st.task_code LIKE CONCAT(@seed_prefix, 'TK%');

DELETE srl
FROM sch_schedule_run_log srl
WHERE srl.run_code LIKE CONCAT(@seed_prefix, 'RUN%');

DELETE st
FROM sch_task st
WHERE st.task_code LIKE CONCAT(@seed_prefix, 'TK%');

DELETE rstc
FROM res_student_teaching_class rstc
INNER JOIN org_teaching_class otc ON otc.id = rstc.teaching_class_id
WHERE otc.remark LIKE CONCAT(@seed_tag, '%');

DELETE rtc
FROM res_teacher_course rtc
INNER JOIN res_teacher rt ON rt.id = rtc.teacher_id
WHERE rt.remark LIKE CONCAT(@seed_tag, '%');

DELETE
FROM res_student
WHERE remark LIKE CONCAT(@seed_tag, '%');

DELETE
FROM org_teaching_class
WHERE remark LIKE CONCAT(@seed_tag, '%');

DELETE
FROM org_admin_class
WHERE remark LIKE CONCAT(@seed_tag, '%');

DELETE
FROM res_classroom
WHERE remark LIKE CONCAT(@seed_tag, '%');

DELETE
FROM res_building
WHERE remark LIKE CONCAT(@seed_tag, '%');

DELETE
FROM res_course
WHERE remark LIKE CONCAT(@seed_tag, '%');

DELETE
FROM res_teacher
WHERE remark LIKE CONCAT(@seed_tag, '%');

DELETE
FROM org_grade
WHERE remark LIKE CONCAT(@seed_tag, '%');

DELETE
FROM org_major
WHERE remark LIKE CONCAT(@seed_tag, '%');

DELETE
FROM org_college
WHERE remark LIKE CONCAT(@seed_tag, '%');

DELETE
FROM org_campus
WHERE remark LIKE CONCAT(@seed_tag, '%');

DELETE
FROM org_stage
WHERE remark LIKE CONCAT(@seed_tag, '%');

DELETE
FROM org_term
WHERE remark LIKE CONCAT(@seed_tag, '%');

DELETE
FROM org_school_year
WHERE remark LIKE CONCAT(@seed_tag, '%');

-- ------------------------------------------------------------
-- 1. 临时基础数据
-- ------------------------------------------------------------
DROP TEMPORARY TABLE IF EXISTS tmp_seed_seq;
CREATE TEMPORARY TABLE tmp_seed_seq (
    seq INT PRIMARY KEY
);

INSERT INTO tmp_seed_seq (seq)
WITH RECURSIVE seq_cte AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq_cte WHERE n < 240
)
SELECT n FROM seq_cte;

DROP TEMPORARY TABLE IF EXISTS tmp_seed_stage;
CREATE TEMPORARY TABLE tmp_seed_stage (
    seq INT PRIMARY KEY,
    stage_code VARCHAR(32) NOT NULL,
    stage_name VARCHAR(64) NOT NULL,
    stage_level INT NOT NULL
);

INSERT INTO tmp_seed_stage (seq, stage_code, stage_name, stage_level) VALUES
    (1, 'SE26ST01', '本科', 1),
    (2, 'SE26ST02', '专科', 2),
    (3, 'SE26ST03', '研究生', 3),
    (4, 'SE26ST04', '继续教育', 4);

DROP TEMPORARY TABLE IF EXISTS tmp_seed_school_year;
CREATE TEMPORARY TABLE tmp_seed_school_year (
    seq INT PRIMARY KEY,
    year_code VARCHAR(32) NOT NULL,
    year_name VARCHAR(64) NOT NULL,
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    is_current TINYINT NOT NULL
);

INSERT INTO tmp_seed_school_year (seq, year_code, year_name, start_date, end_date, is_current) VALUES
    (1, 'SE26Y2425', '2024-2025学年', '2024-09-01 00:00:00', '2025-07-15 23:59:59', 0),
    (2, 'SE26Y2526', '2025-2026学年', '2025-09-01 00:00:00', '2026-07-15 23:59:59', 1);

DROP TEMPORARY TABLE IF EXISTS tmp_seed_campus;
CREATE TEMPORARY TABLE tmp_seed_campus (
    seq INT PRIMARY KEY,
    campus_code VARCHAR(32) NOT NULL,
    campus_name VARCHAR(64) NOT NULL,
    campus_type VARCHAR(32) NOT NULL,
    province_code VARCHAR(16),
    city_code VARCHAR(16),
    district_code VARCHAR(16),
    address VARCHAR(255) NOT NULL,
    sort_no INT NOT NULL
);

INSERT INTO tmp_seed_campus (
    seq, campus_code, campus_name, campus_type, province_code, city_code, district_code, address, sort_no
) VALUES
    (1, 'SE26C01', '东湖校区', 'MAIN', '420000', '420100', '420111', '湖北省武汉市洪山区珞喻路 188 号', 1),
    (2, 'SE26C02', '光谷校区', 'BRANCH', '420000', '420100', '420115', '湖北省武汉市江夏区高新大道 66 号', 2),
    (3, 'SE26C03', '临港校区', 'BRANCH', '310000', '310100', '310115', '上海市浦东新区临港大道 199 号', 3),
    (4, 'SE26C04', '南山校区', 'BRANCH', '440000', '440300', '440305', '广东省深圳市南山区学苑大道 88 号', 4),
    (5, 'SE26C05', '西咸校区', 'BRANCH', '610000', '610100', '610118', '陕西省西安市西咸新区沣柳路 18 号', 5),
    (6, 'SE26C06', '天府校区', 'BRANCH', '510000', '510100', '510116', '四川省成都市双流区科教路 20 号', 6),
    (7, 'SE26C07', '麒麟校区', 'BRANCH', '320000', '320100', '320115', '江苏省南京市江宁区麒麟路 30 号', 7),
    (8, 'SE26C08', '松山湖校区', 'BRANCH', '440000', '441900', '441900', '广东省东莞市松山湖高新路 51 号', 8),
    (9, 'SE26C09', '海河校区', 'BRANCH', '120000', '120100', '120111', '天津市西青区学府路 26 号', 9),
    (10, 'SE26C10', '象湖校区', 'BRANCH', '360000', '360100', '360112', '江西省南昌市新建区学院路 9 号', 10),
    (11, 'SE26C11', '星沙校区', 'BRANCH', '430000', '430100', '430121', '湖南省长沙县黄兴大道 108 号', 11),
    (12, 'SE26C12', '琴岛校区', 'BRANCH', '370000', '370200', '370211', '山东省青岛市黄岛区长江西路 66 号', 12);

DROP TEMPORARY TABLE IF EXISTS tmp_seed_term;
CREATE TEMPORARY TABLE tmp_seed_term (
    seq INT PRIMARY KEY,
    term_code VARCHAR(32) NOT NULL,
    term_name VARCHAR(64) NOT NULL,
    year_code VARCHAR(32) NOT NULL,
    campus_code VARCHAR(32),
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    week_count INT NOT NULL,
    is_current TINYINT NOT NULL
);

INSERT INTO tmp_seed_term (
    seq, term_code, term_name, year_code, campus_code, start_date, end_date, week_count, is_current
) VALUES
    (1, 'SE26T241', '2024-2025学年第一学期', 'SE26Y2425', NULL, '2024-09-01 00:00:00', '2025-01-18 23:59:59', 20, 0),
    (2, 'SE26T242', '2024-2025学年第二学期', 'SE26Y2425', NULL, '2025-02-17 00:00:00', '2025-07-05 23:59:59', 20, 0),
    (3, 'SE26T251', '2025-2026学年第一学期', 'SE26Y2526', NULL, '2025-09-01 00:00:00', '2026-01-17 23:59:59', 20, 1),
    (4, 'SE26T252', '2025-2026学年第二学期', 'SE26Y2526', NULL, '2026-02-23 00:00:00', '2026-07-11 23:59:59', 20, 0);

DROP TEMPORARY TABLE IF EXISTS tmp_seed_college;
CREATE TEMPORARY TABLE tmp_seed_college (
    seq INT PRIMARY KEY,
    college_code VARCHAR(32) NOT NULL,
    college_name VARCHAR(64) NOT NULL,
    campus_code VARCHAR(32) NOT NULL
);

INSERT INTO tmp_seed_college (seq, college_code, college_name, campus_code) VALUES
    (1, 'SE26CO01', '计算机与人工智能学院', 'SE26C01'),
    (2, 'SE26CO02', '数据科学学院', 'SE26C01'),
    (3, 'SE26CO03', '智能制造学院', 'SE26C02'),
    (4, 'SE26CO04', '电子信息学院', 'SE26C02'),
    (5, 'SE26CO05', '商学院', 'SE26C03'),
    (6, 'SE26CO06', '设计学院', 'SE26C04'),
    (7, 'SE26CO07', '外国语学院', 'SE26C04'),
    (8, 'SE26CO08', '建筑工程学院', 'SE26C05'),
    (9, 'SE26CO09', '航空工程学院', 'SE26C06'),
    (10, 'SE26CO10', '医学院', 'SE26C10'),
    (11, 'SE26CO11', '护理学院', 'SE26C10'),
    (12, 'SE26CO12', '教育学院', 'SE26C07'),
    (13, 'SE26CO13', '法学院', 'SE26C09'),
    (14, 'SE26CO14', '数学与统计学院', 'SE26C09'),
    (15, 'SE26CO15', '马克思主义学院', 'SE26C01'),
    (16, 'SE26CO16', '体育学院', 'SE26C11'),
    (17, 'SE26CO17', '艺术学院', 'SE26C12'),
    (18, 'SE26CO18', '继续教育学院', 'SE26C08');

DROP TEMPORARY TABLE IF EXISTS tmp_seed_major;
CREATE TEMPORARY TABLE tmp_seed_major (
    seq INT PRIMARY KEY,
    major_code VARCHAR(32) NOT NULL,
    major_name VARCHAR(64) NOT NULL,
    college_code VARCHAR(32) NOT NULL,
    stage_code VARCHAR(32) NOT NULL
);

INSERT INTO tmp_seed_major (seq, major_code, major_name, college_code, stage_code) VALUES
    (1, 'SE26M01', '软件工程', 'SE26CO01', 'SE26ST01'),
    (2, 'SE26M02', '人工智能', 'SE26CO01', 'SE26ST01'),
    (3, 'SE26M03', '数据科学与大数据技术', 'SE26CO02', 'SE26ST01'),
    (4, 'SE26M04', '网络空间安全', 'SE26CO02', 'SE26ST01'),
    (5, 'SE26M05', '智能制造工程', 'SE26CO03', 'SE26ST01'),
    (6, 'SE26M06', '机器人工程', 'SE26CO03', 'SE26ST01'),
    (7, 'SE26M07', '通信工程', 'SE26CO04', 'SE26ST01'),
    (8, 'SE26M08', '集成电路设计与集成系统', 'SE26CO04', 'SE26ST01'),
    (9, 'SE26M09', '工商管理', 'SE26CO05', 'SE26ST01'),
    (10, 'SE26M10', '会计学', 'SE26CO05', 'SE26ST01'),
    (11, 'SE26M11', '视觉传达设计', 'SE26CO06', 'SE26ST01'),
    (12, 'SE26M12', '数字媒体艺术', 'SE26CO06', 'SE26ST01'),
    (13, 'SE26M13', '商务英语', 'SE26CO07', 'SE26ST01'),
    (14, 'SE26M14', '翻译', 'SE26CO07', 'SE26ST01'),
    (15, 'SE26M15', '土木工程', 'SE26CO08', 'SE26ST01'),
    (16, 'SE26M16', '工程管理', 'SE26CO08', 'SE26ST01'),
    (17, 'SE26M17', '飞行器制造工程', 'SE26CO09', 'SE26ST01'),
    (18, 'SE26M18', '无人驾驶航空器系统工程', 'SE26CO09', 'SE26ST01'),
    (19, 'SE26M19', '临床医学', 'SE26CO10', 'SE26ST01'),
    (20, 'SE26M20', '护理学', 'SE26CO11', 'SE26ST01'),
    (21, 'SE26M21', '学前教育', 'SE26CO12', 'SE26ST02'),
    (22, 'SE26M22', '法律硕士', 'SE26CO13', 'SE26ST03'),
    (23, 'SE26M23', '数学与应用数学', 'SE26CO14', 'SE26ST01'),
    (24, 'SE26M24', '社会体育指导与管理', 'SE26CO16', 'SE26ST04');

DROP TEMPORARY TABLE IF EXISTS tmp_seed_grade;
CREATE TEMPORARY TABLE tmp_seed_grade (
    seq INT PRIMARY KEY,
    grade_code VARCHAR(32) NOT NULL,
    grade_name VARCHAR(64) NOT NULL,
    stage_code VARCHAR(32) NOT NULL,
    campus_code VARCHAR(32) NOT NULL,
    college_code VARCHAR(32) NOT NULL,
    major_code VARCHAR(32) NOT NULL,
    entry_year INT NOT NULL,
    graduate_year INT NOT NULL
);

INSERT INTO tmp_seed_grade (
    seq, grade_code, grade_name, stage_code, campus_code, college_code, major_code, entry_year, graduate_year
)
SELECT
    ROW_NUMBER() OVER (ORDER BY y.entry_year, m.seq) AS seq,
    CONCAT('SE26G', LPAD(ROW_NUMBER() OVER (ORDER BY y.entry_year, m.seq), 2, '0')) AS grade_code,
    CONCAT(y.entry_year, '级', m.major_name) AS grade_name,
    m.stage_code,
    c.campus_code,
    m.college_code,
    m.major_code,
    y.entry_year,
    CASE
        WHEN m.stage_code = 'SE26ST03' THEN y.entry_year + 3
        WHEN m.stage_code = 'SE26ST02' THEN y.entry_year + 3
        ELSE y.entry_year + 4
    END AS graduate_year
FROM tmp_seed_major m
INNER JOIN tmp_seed_college c ON c.college_code = m.college_code
INNER JOIN (
    SELECT 2024 AS entry_year
    UNION ALL
    SELECT 2025 AS entry_year
) y
WHERE m.seq <= 12;

DROP TEMPORARY TABLE IF EXISTS tmp_seed_teacher;
CREATE TEMPORARY TABLE tmp_seed_teacher (
    seq INT PRIMARY KEY,
    teacher_code VARCHAR(32) NOT NULL,
    teacher_name VARCHAR(64) NOT NULL,
    gender VARCHAR(8),
    mobile VARCHAR(20),
    email VARCHAR(128),
    title_name VARCHAR(64),
    campus_code VARCHAR(32) NOT NULL,
    college_code VARCHAR(32) NOT NULL,
    stage_code VARCHAR(32) NOT NULL,
    allow_cross_campus TINYINT NOT NULL,
    allow_cross_college TINYINT NOT NULL,
    max_week_hours INT NOT NULL,
    max_day_hours INT NOT NULL,
    forbidden_time_slots VARCHAR(255),
    user_id BIGINT
);

INSERT INTO tmp_seed_teacher (
    seq, teacher_code, teacher_name, gender, mobile, email, title_name,
    campus_code, college_code, stage_code, allow_cross_campus, allow_cross_college,
    max_week_hours, max_day_hours, forbidden_time_slots, user_id
) VALUES
    (1, 'T26001', '演示教师', '男', '13970000001', 'teacher01@demo.edu.cn', '教授', 'SE26C01', 'SE26CO01', 'SE26ST01', 1, 1, 16, 4, NULL, 910002),
    (2, 'T26002', '张知远', '男', '13970000002', 'teacher02@demo.edu.cn', '副教授', 'SE26C01', 'SE26CO01', 'SE26ST01', 1, 1, 18, 4, '2-7', NULL),
    (3, 'T26003', '林若川', '男', '13970000003', 'teacher03@demo.edu.cn', '教授', 'SE26C01', 'SE26CO02', 'SE26ST01', 0, 0, 16, 4, NULL, NULL),
    (4, 'T26004', '周以宁', '女', '13970000004', 'teacher04@demo.edu.cn', '讲师', 'SE26C01', 'SE26CO02', 'SE26ST01', 0, 0, 18, 4, NULL, NULL),
    (5, 'T26005', '许景澄', '男', '13970000005', 'teacher05@demo.edu.cn', '教授', 'SE26C02', 'SE26CO03', 'SE26ST01', 0, 0, 18, 4, '5-7', NULL),
    (6, 'T26006', '韩书尧', '男', '13970000006', 'teacher06@demo.edu.cn', '实验师', 'SE26C02', 'SE26CO03', 'SE26ST01', 0, 0, 20, 5, NULL, NULL),
    (7, 'T26007', '邵明哲', '男', '13970000007', 'teacher07@demo.edu.cn', '教授', 'SE26C02', 'SE26CO04', 'SE26ST01', 0, 0, 16, 4, NULL, NULL),
    (8, 'T26008', '顾清妍', '女', '13970000008', 'teacher08@demo.edu.cn', '副教授', 'SE26C02', 'SE26CO04', 'SE26ST01', 0, 0, 18, 4, NULL, NULL),
    (9, 'T26009', '宋亦凡', '男', '13970000009', 'teacher09@demo.edu.cn', '教授', 'SE26C03', 'SE26CO05', 'SE26ST01', 1, 0, 16, 4, NULL, NULL),
    (10, 'T26010', '沈嘉禾', '女', '13970000010', 'teacher10@demo.edu.cn', '讲师', 'SE26C03', 'SE26CO05', 'SE26ST01', 1, 0, 18, 4, '1-8', NULL),
    (11, 'T26011', '唐栖月', '女', '13970000011', 'teacher11@demo.edu.cn', '副教授', 'SE26C04', 'SE26CO06', 'SE26ST01', 0, 1, 18, 4, NULL, NULL),
    (12, 'T26012', '叶知夏', '女', '13970000012', 'teacher12@demo.edu.cn', '讲师', 'SE26C04', 'SE26CO06', 'SE26ST01', 0, 1, 20, 5, NULL, NULL),
    (13, 'T26013', '方言蹊', '女', '13970000013', 'teacher13@demo.edu.cn', '教授', 'SE26C04', 'SE26CO07', 'SE26ST01', 0, 1, 16, 4, NULL, NULL),
    (14, 'T26014', '纪安歌', '女', '13970000014', 'teacher14@demo.edu.cn', '副教授', 'SE26C04', 'SE26CO07', 'SE26ST01', 0, 1, 18, 4, '3-1', NULL),
    (15, 'T26015', '梁景行', '男', '13970000015', 'teacher15@demo.edu.cn', '教授', 'SE26C05', 'SE26CO08', 'SE26ST01', 0, 0, 18, 4, NULL, NULL),
    (16, 'T26016', '马会宁', '男', '13970000016', 'teacher16@demo.edu.cn', '实验师', 'SE26C05', 'SE26CO08', 'SE26ST01', 0, 0, 20, 5, NULL, NULL),
    (17, 'T26017', '程临风', '男', '13970000017', 'teacher17@demo.edu.cn', '教授', 'SE26C06', 'SE26CO09', 'SE26ST01', 1, 0, 16, 4, NULL, NULL),
    (18, 'T26018', '戚若航', '男', '13970000018', 'teacher18@demo.edu.cn', '副教授', 'SE26C06', 'SE26CO09', 'SE26ST01', 1, 0, 18, 4, NULL, NULL),
    (19, 'T26019', '欧阳宁', '男', '13970000019', 'teacher19@demo.edu.cn', '教授', 'SE26C10', 'SE26CO10', 'SE26ST01', 0, 0, 18, 4, NULL, NULL),
    (20, 'T26020', '苏清和', '女', '13970000020', 'teacher20@demo.edu.cn', '副教授', 'SE26C10', 'SE26CO10', 'SE26ST01', 0, 0, 16, 4, '4-5', NULL),
    (21, 'T26021', '宋雨桐', '女', '13970000021', 'teacher21@demo.edu.cn', '教授', 'SE26C10', 'SE26CO11', 'SE26ST01', 0, 0, 18, 4, NULL, NULL),
    (22, 'T26022', '许晴川', '女', '13970000022', 'teacher22@demo.edu.cn', '讲师', 'SE26C10', 'SE26CO11', 'SE26ST01', 0, 0, 18, 4, NULL, NULL),
    (23, 'T26023', '薛知予', '女', '13970000023', 'teacher23@demo.edu.cn', '副教授', 'SE26C07', 'SE26CO12', 'SE26ST02', 0, 0, 16, 4, NULL, NULL),
    (24, 'T26024', '郁青禾', '女', '13970000024', 'teacher24@demo.edu.cn', '讲师', 'SE26C07', 'SE26CO12', 'SE26ST02', 0, 0, 18, 4, NULL, NULL),
    (25, 'T26025', '邓叙白', '男', '13970000025', 'teacher25@demo.edu.cn', '教授', 'SE26C09', 'SE26CO13', 'SE26ST03', 0, 0, 14, 4, NULL, NULL),
    (26, 'T26026', '罗清越', '女', '13970000026', 'teacher26@demo.edu.cn', '副教授', 'SE26C09', 'SE26CO13', 'SE26ST03', 0, 0, 14, 4, NULL, NULL),
    (27, 'T26027', '魏安然', '男', '13970000027', 'teacher27@demo.edu.cn', '教授', 'SE26C09', 'SE26CO14', 'SE26ST01', 1, 0, 18, 4, NULL, NULL),
    (28, 'T26028', '秦若衡', '男', '13970000028', 'teacher28@demo.edu.cn', '副教授', 'SE26C09', 'SE26CO14', 'SE26ST01', 1, 0, 16, 4, NULL, NULL),
    (29, 'T26029', '祁闻道', '男', '13970000029', 'teacher29@demo.edu.cn', '教授', 'SE26C01', 'SE26CO15', 'SE26ST01', 0, 1, 18, 4, NULL, NULL),
    (30, 'T26030', '谢青岚', '女', '13970000030', 'teacher30@demo.edu.cn', '副教授', 'SE26C01', 'SE26CO15', 'SE26ST01', 0, 1, 16, 4, '2-1', NULL),
    (31, 'T26031', '傅景行', '男', '13970000031', 'teacher31@demo.edu.cn', '教授', 'SE26C11', 'SE26CO16', 'SE26ST04', 1, 0, 20, 5, NULL, NULL),
    (32, 'T26032', '岑知远', '男', '13970000032', 'teacher32@demo.edu.cn', '讲师', 'SE26C11', 'SE26CO16', 'SE26ST04', 1, 0, 20, 5, NULL, NULL),
    (33, 'T26033', '殷叙白', '男', '13970000033', 'teacher33@demo.edu.cn', '副教授', 'SE26C12', 'SE26CO17', 'SE26ST01', 0, 0, 18, 4, NULL, NULL),
    (34, 'T26034', '乔若溪', '女', '13970000034', 'teacher34@demo.edu.cn', '讲师', 'SE26C12', 'SE26CO17', 'SE26ST01', 0, 0, 18, 4, NULL, NULL),
    (35, 'T26035', '杜景澄', '男', '13970000035', 'teacher35@demo.edu.cn', '副教授', 'SE26C08', 'SE26CO18', 'SE26ST04', 0, 0, 16, 4, NULL, NULL),
    (36, 'T26036', '温知和', '男', '13970000036', 'teacher36@demo.edu.cn', '讲师', 'SE26C08', 'SE26CO18', 'SE26ST04', 0, 0, 16, 4, NULL, NULL);

DROP TEMPORARY TABLE IF EXISTS tmp_seed_course;
CREATE TEMPORARY TABLE tmp_seed_course (
    seq INT PRIMARY KEY,
    course_code VARCHAR(32) NOT NULL,
    course_name VARCHAR(128) NOT NULL,
    course_short_name VARCHAR(32),
    course_type VARCHAR(32) NOT NULL,
    course_attr_code VARCHAR(8) NOT NULL,
    stage_code VARCHAR(32) NOT NULL,
    college_code VARCHAR(32) NOT NULL,
    total_hours INT NOT NULL,
    week_hours INT NOT NULL,
    need_continuous TINYINT NOT NULL,
    continuous_size INT NOT NULL,
    need_special_room TINYINT NOT NULL,
    room_type VARCHAR(32),
    is_core_course TINYINT NOT NULL,
    allow_cross_day TINYINT NOT NULL,
    primary_teacher_code VARCHAR(32) NOT NULL
);

INSERT INTO tmp_seed_course (
    seq, course_code, course_name, course_short_name, course_type, course_attr_code,
    stage_code, college_code, total_hours, week_hours, need_continuous, continuous_size,
    need_special_room, room_type, is_core_course, allow_cross_day, primary_teacher_code
) VALUES
    (1, 'C26001', '程序设计基础', '程序设计', 'REQUIRED', '01', 'SE26ST01', 'SE26CO01', 32, 2, 0, 1, 0, NULL, 1, 1, 'T26001'),
    (2, 'C26002', '软件需求分析', '需求分析', 'REQUIRED', '02', 'SE26ST01', 'SE26CO01', 32, 2, 0, 1, 0, NULL, 1, 1, 'T26002'),
    (3, 'C26003', '机器学习导论', '机器学习', 'REQUIRED', '02', 'SE26ST01', 'SE26CO01', 32, 2, 0, 1, 1, 'COMPUTER', 1, 1, 'T26001'),
    (4, 'C26004', '智能系统设计', '智能系统', 'PRACTICE', '03', 'SE26ST01', 'SE26CO01', 32, 2, 1, 2, 1, 'COMPUTER', 0, 1, 'T26002'),
    (5, 'C26005', '数据结构与算法', '数据结构', 'REQUIRED', '01', 'SE26ST01', 'SE26CO02', 48, 3, 0, 1, 0, NULL, 1, 1, 'T26003'),
    (6, 'C26006', '数据仓库与数据挖掘', '数据挖掘', 'REQUIRED', '02', 'SE26ST01', 'SE26CO02', 32, 2, 0, 1, 1, 'COMPUTER', 1, 1, 'T26004'),
    (7, 'C26007', '网络安全基础', '网络安全', 'REQUIRED', '01', 'SE26ST01', 'SE26CO02', 32, 2, 0, 1, 0, NULL, 1, 1, 'T26003'),
    (8, 'C26008', '渗透测试实训', '渗透测试', 'PRACTICE', '03', 'SE26ST01', 'SE26CO02', 32, 2, 1, 2, 1, 'COMPUTER', 0, 1, 'T26004'),
    (9, 'C26009', '机械设计基础', '机械设计', 'REQUIRED', '01', 'SE26ST01', 'SE26CO03', 32, 2, 0, 1, 0, NULL, 1, 1, 'T26005'),
    (10, 'C26010', '智能制造系统', '智能制造', 'REQUIRED', '02', 'SE26ST01', 'SE26CO03', 32, 2, 1, 2, 1, 'LAB', 1, 1, 'T26006'),
    (11, 'C26011', '工业机器人编程', '机器人编程', 'TECHNOLOGY', '07', 'SE26ST01', 'SE26CO03', 32, 2, 1, 2, 1, 'LAB', 1, 1, 'T26005'),
    (12, 'C26012', '机器视觉检测', '机器视觉', 'PRACTICE', '03', 'SE26ST01', 'SE26CO03', 32, 2, 0, 1, 1, 'COMPUTER', 0, 1, 'T26006'),
    (13, 'C26013', '数字电路', '数字电路', 'REQUIRED', '01', 'SE26ST01', 'SE26CO04', 48, 3, 0, 1, 0, NULL, 1, 1, 'T26007'),
    (14, 'C26014', '通信原理', '通信原理', 'REQUIRED', '02', 'SE26ST01', 'SE26CO04', 48, 3, 0, 1, 0, NULL, 1, 1, 'T26008'),
    (15, 'C26015', '嵌入式系统开发', '嵌入式', 'TECHNOLOGY', '07', 'SE26ST01', 'SE26CO04', 32, 2, 1, 2, 1, 'COMPUTER', 1, 1, 'T26007'),
    (16, 'C26016', '集成电路工艺基础', '集成电路', 'PRACTICE', '03', 'SE26ST01', 'SE26CO04', 32, 2, 0, 1, 1, 'LAB', 0, 1, 'T26008'),
    (17, 'C26017', '管理学原理', '管理学', 'REQUIRED', '01', 'SE26ST01', 'SE26CO05', 32, 2, 0, 1, 0, NULL, 1, 1, 'T26009'),
    (18, 'C26018', '财务会计', '财务会计', 'REQUIRED', '02', 'SE26ST01', 'SE26CO05', 48, 3, 0, 1, 0, NULL, 1, 1, 'T26010'),
    (19, 'C26019', '市场营销学', '市场营销', 'REQUIRED', '02', 'SE26ST01', 'SE26CO05', 32, 2, 0, 1, 0, NULL, 1, 1, 'T26009'),
    (20, 'C26020', '财务分析实务', '财务分析', 'PRACTICE', '03', 'SE26ST01', 'SE26CO05', 32, 2, 0, 1, 1, 'COMPUTER', 0, 1, 'T26010'),
    (21, 'C26021', '设计素描', '素描', 'ART', '05', 'SE26ST01', 'SE26CO06', 32, 2, 0, 1, 1, 'ART', 1, 1, 'T26011'),
    (22, 'C26022', '品牌视觉设计', '品牌设计', 'ART', '05', 'SE26ST01', 'SE26CO06', 32, 2, 1, 2, 1, 'ART', 1, 1, 'T26012'),
    (23, 'C26023', '数字媒体创作基础', '媒体创作', 'TECHNOLOGY', '07', 'SE26ST01', 'SE26CO06', 32, 2, 0, 1, 1, 'COMPUTER', 1, 1, 'T26011'),
    (24, 'C26024', '交互界面设计', '交互设计', 'ART', '05', 'SE26ST01', 'SE26CO06', 32, 2, 0, 1, 1, 'ART', 1, 1, 'T26012'),
    (25, 'C26025', '大学英语A', '英语A', 'REQUIRED', '01', 'SE26ST01', 'SE26CO07', 48, 3, 0, 1, 0, NULL, 1, 1, 'T26013'),
    (26, 'C26026', '大学英语B', '英语B', 'REQUIRED', '01', 'SE26ST01', 'SE26CO07', 48, 3, 0, 1, 0, NULL, 1, 1, 'T26014'),
    (27, 'C26027', '学术写作', '学术写作', 'REQUIRED', '02', 'SE26ST01', 'SE26CO07', 32, 2, 0, 1, 0, NULL, 0, 1, 'T26013'),
    (28, 'C26028', '高等数学A', '高数A', 'REQUIRED', '01', 'SE26ST01', 'SE26CO14', 64, 4, 0, 1, 0, NULL, 1, 0, 'T26027'),
    (29, 'C26029', '高等数学B', '高数B', 'REQUIRED', '01', 'SE26ST01', 'SE26CO14', 48, 3, 0, 1, 0, NULL, 1, 0, 'T26028'),
    (30, 'C26030', '线性代数', '线性代数', 'REQUIRED', '02', 'SE26ST01', 'SE26CO14', 32, 2, 0, 1, 0, NULL, 1, 1, 'T26027'),
    (31, 'C26031', '概率统计', '概率统计', 'REQUIRED', '02', 'SE26ST01', 'SE26CO14', 32, 2, 0, 1, 0, NULL, 0, 1, 'T26028'),
    (32, 'C26032', '数据素养基础', '数据素养', 'TECHNOLOGY', '07', 'SE26ST01', 'SE26CO14', 32, 2, 0, 1, 1, 'COMPUTER', 0, 1, 'T26027'),
    (33, 'C26033', '思想道德与法治', '思法', 'REQUIRED', '02', 'SE26ST01', 'SE26CO15', 32, 2, 0, 1, 0, NULL, 0, 1, 'T26029'),
    (34, 'C26034', '中国近现代史纲要', '近现代史', 'REQUIRED', '02', 'SE26ST01', 'SE26CO15', 32, 2, 0, 1, 0, NULL, 0, 1, 'T26030'),
    (35, 'C26035', '大学生心理健康教育', '心理健康', 'REQUIRED', '02', 'SE26ST01', 'SE26CO12', 32, 2, 0, 1, 0, NULL, 0, 1, 'T26023'),
    (36, 'C26036', '职业发展与就业指导', '职业发展', 'REQUIRED', '02', 'SE26ST01', 'SE26CO12', 16, 1, 0, 1, 0, NULL, 0, 1, 'T26024'),
    (37, 'C26037', '创新创业基础', '创新创业', 'REQUIRED', '02', 'SE26ST01', 'SE26CO18', 16, 1, 0, 1, 0, NULL, 0, 1, 'T26035'),
    (38, 'C26038', '信息素养导论', '信息素养', 'TECHNOLOGY', '07', 'SE26ST01', 'SE26CO01', 16, 1, 0, 1, 1, 'COMPUTER', 0, 1, 'T26002'),
    (39, 'C26039', '设计思维导论', '设计思维', 'ART', '05', 'SE26ST01', 'SE26CO06', 16, 1, 0, 1, 1, 'ART', 0, 1, 'T26011'),
    (40, 'C26040', '大学体育I', '体育I', 'PHYSICAL', '04', 'SE26ST01', 'SE26CO16', 16, 1, 0, 1, 1, 'SPORTS', 0, 1, 'T26031'),
    (41, 'C26041', '大学体育II', '体育II', 'PHYSICAL', '04', 'SE26ST01', 'SE26CO16', 16, 1, 0, 1, 1, 'SPORTS', 0, 1, 'T26032'),
    (42, 'C26042', '工程实践基础', '工程实践', 'PRACTICE', '03', 'SE26ST01', 'SE26CO08', 16, 1, 1, 2, 1, 'LAB', 0, 1, 'T26016');

DROP TEMPORARY TABLE IF EXISTS tmp_seed_building;
CREATE TEMPORARY TABLE tmp_seed_building (
    seq INT PRIMARY KEY,
    building_code VARCHAR(32) NOT NULL,
    building_name VARCHAR(64) NOT NULL,
    campus_code VARCHAR(32) NOT NULL,
    college_code VARCHAR(32),
    building_type VARCHAR(32) NOT NULL,
    floor_count INT NOT NULL
);

INSERT INTO tmp_seed_building (seq, building_code, building_name, campus_code, college_code, building_type, floor_count) VALUES
    (1, 'SE26B01', '教学主楼A', 'SE26C01', NULL, 'TEACHING', 5),
    (2, 'SE26B02', '教学主楼B', 'SE26C01', NULL, 'TEACHING', 5),
    (3, 'SE26B03', '计算机实验楼', 'SE26C01', 'SE26CO01', 'LAB', 6),
    (4, 'SE26B04', '数据中心楼', 'SE26C01', 'SE26CO02', 'LAB', 6),
    (5, 'SE26B05', '智造实训楼', 'SE26C02', 'SE26CO03', 'LAB', 6),
    (6, 'SE26B06', '工业工程楼', 'SE26C02', 'SE26CO03', 'TEACHING', 5),
    (7, 'SE26B07', '临港商学楼', 'SE26C03', 'SE26CO05', 'TEACHING', 5),
    (8, 'SE26B08', '设计创意楼', 'SE26C04', 'SE26CO06', 'ART', 5),
    (9, 'SE26B09', '外语教学楼', 'SE26C04', 'SE26CO07', 'TEACHING', 5),
    (10, 'SE26B10', '土木实验楼', 'SE26C05', 'SE26CO08', 'LAB', 5),
    (11, 'SE26B11', '航空工程楼', 'SE26C06', 'SE26CO09', 'TEACHING', 5),
    (12, 'SE26B12', '教育综合楼', 'SE26C07', 'SE26CO12', 'TEACHING', 5),
    (13, 'SE26B13', '继续教育楼', 'SE26C08', 'SE26CO18', 'TEACHING', 4),
    (14, 'SE26B14', '数理楼', 'SE26C09', 'SE26CO14', 'TEACHING', 5),
    (15, 'SE26B15', '马院教学楼', 'SE26C01', 'SE26CO15', 'TEACHING', 4),
    (16, 'SE26B16', '综合体育馆', 'SE26C11', 'SE26CO16', 'SPORTS', 3),
    (17, 'SE26B17', '艺术工坊楼', 'SE26C12', 'SE26CO17', 'ART', 4),
    (18, 'SE26B18', '医学实训楼', 'SE26C10', 'SE26CO10', 'LAB', 5);

DROP TEMPORARY TABLE IF EXISTS tmp_seed_classroom;
CREATE TEMPORARY TABLE tmp_seed_classroom (
    seq INT PRIMARY KEY,
    classroom_code VARCHAR(32) NOT NULL,
    classroom_name VARCHAR(64) NOT NULL,
    campus_code VARCHAR(32) NOT NULL,
    college_code VARCHAR(32),
    building_code VARCHAR(32) NOT NULL,
    floor_no INT NOT NULL,
    room_type VARCHAR(32) NOT NULL,
    room_slot INT NOT NULL,
    seat_count INT NOT NULL,
    is_shared TINYINT NOT NULL
);

INSERT INTO tmp_seed_classroom (
    seq, classroom_code, classroom_name, campus_code, college_code, building_code,
    floor_no, room_type, room_slot, seat_count, is_shared
) VALUES
    (1, 'SE26B01-R101', 'A101', 'SE26C01', NULL, 'SE26B01', 1, 'NORMAL', 1, 56, 1),
    (2, 'SE26B01-R203', 'A203', 'SE26C01', NULL, 'SE26B01', 2, 'NORMAL', 2, 56, 1),
    (3, 'SE26B01-R305', 'A305', 'SE26C01', NULL, 'SE26B01', 3, 'NORMAL', 3, 64, 1),
    (4, 'SE26B02-R102', 'B102', 'SE26C01', NULL, 'SE26B02', 1, 'NORMAL', 4, 56, 1),
    (5, 'SE26B02-R204', 'B204', 'SE26C01', NULL, 'SE26B02', 2, 'NORMAL', 5, 56, 1),
    (6, 'SE26B02-R306', 'B306', 'SE26C01', NULL, 'SE26B02', 3, 'NORMAL', 6, 64, 1),
    (7, 'SE26B06-R101', '工101', 'SE26C02', 'SE26CO03', 'SE26B06', 1, 'NORMAL', 7, 48, 1),
    (8, 'SE26B06-R202', '工202', 'SE26C02', 'SE26CO03', 'SE26B06', 2, 'NORMAL', 8, 48, 1),
    (9, 'SE26B07-R301', '商301', 'SE26C03', 'SE26CO05', 'SE26B07', 3, 'NORMAL', 9, 58, 1),
    (10, 'SE26B07-R402', '商402', 'SE26C03', 'SE26CO05', 'SE26B07', 4, 'NORMAL', 10, 58, 1),
    (11, 'SE26B09-R201', '外201', 'SE26C04', 'SE26CO07', 'SE26B09', 2, 'NORMAL', 11, 52, 1),
    (12, 'SE26B09-R302', '外302', 'SE26C04', 'SE26CO07', 'SE26B09', 3, 'NORMAL', 12, 52, 1),
    (13, 'SE26B11-R103', '航103', 'SE26C06', 'SE26CO09', 'SE26B11', 1, 'NORMAL', 13, 48, 1),
    (14, 'SE26B11-R205', '航205', 'SE26C06', 'SE26CO09', 'SE26B11', 2, 'NORMAL', 14, 48, 1),
    (15, 'SE26B12-R101', '教101', 'SE26C07', 'SE26CO12', 'SE26B12', 1, 'NORMAL', 15, 54, 1),
    (16, 'SE26B12-R203', '教203', 'SE26C07', 'SE26CO12', 'SE26B12', 2, 'NORMAL', 16, 54, 1),
    (17, 'SE26B13-R202', '继202', 'SE26C08', 'SE26CO18', 'SE26B13', 2, 'NORMAL', 17, 50, 1),
    (18, 'SE26B13-R304', '继304', 'SE26C08', 'SE26CO18', 'SE26B13', 3, 'NORMAL', 18, 50, 1),
    (19, 'SE26B14-R101', '数101', 'SE26C09', 'SE26CO14', 'SE26B14', 1, 'NORMAL', 19, 58, 1),
    (20, 'SE26B14-R204', '数204', 'SE26C09', 'SE26CO14', 'SE26B14', 2, 'NORMAL', 20, 58, 1),
    (21, 'SE26B15-R102', '马102', 'SE26C01', 'SE26CO15', 'SE26B15', 1, 'NORMAL', 21, 50, 1),
    (22, 'SE26B15-R202', '马202', 'SE26C01', 'SE26CO15', 'SE26B15', 2, 'NORMAL', 22, 50, 1),
    (23, 'SE26B01-R401', 'A401', 'SE26C01', NULL, 'SE26B01', 4, 'NORMAL', 23, 64, 1),
    (24, 'SE26B02-R402', 'B402', 'SE26C01', NULL, 'SE26B02', 4, 'NORMAL', 24, 64, 1),
    (25, 'SE26B03-L201', '机实201', 'SE26C01', 'SE26CO01', 'SE26B03', 2, 'LAB', 1, 48, 0),
    (26, 'SE26B03-L301', '机实301', 'SE26C01', 'SE26CO01', 'SE26B03', 3, 'LAB', 2, 48, 0),
    (27, 'SE26B04-L202', '数实202', 'SE26C01', 'SE26CO02', 'SE26B04', 2, 'LAB', 3, 46, 0),
    (28, 'SE26B05-L203', '智造203', 'SE26C02', 'SE26CO03', 'SE26B05', 2, 'LAB', 4, 44, 0),
    (29, 'SE26B05-L303', '智造303', 'SE26C02', 'SE26CO03', 'SE26B05', 3, 'LAB', 5, 44, 0),
    (30, 'SE26B10-L201', '土木201', 'SE26C05', 'SE26CO08', 'SE26B10', 2, 'LAB', 6, 42, 0),
    (31, 'SE26B18-L202', '医实202', 'SE26C10', 'SE26CO10', 'SE26B18', 2, 'LAB', 7, 40, 0),
    (32, 'SE26B18-L302', '医实302', 'SE26C10', 'SE26CO10', 'SE26B18', 3, 'LAB', 8, 40, 0),
    (33, 'SE26B03-C401', '机房401', 'SE26C01', 'SE26CO01', 'SE26B03', 4, 'COMPUTER', 1, 50, 0),
    (34, 'SE26B04-C402', '数据机房402', 'SE26C01', 'SE26CO02', 'SE26B04', 4, 'COMPUTER', 2, 50, 0),
    (35, 'SE26B08-C301', '创意机房301', 'SE26C04', 'SE26CO06', 'SE26B08', 3, 'COMPUTER', 3, 42, 0),
    (36, 'SE26B08-C401', '创意机房401', 'SE26C04', 'SE26CO06', 'SE26B08', 4, 'COMPUTER', 4, 42, 0),
    (37, 'SE26B16-S101', '羽排馆', 'SE26C11', 'SE26CO16', 'SE26B16', 1, 'SPORTS', 1, 80, 0),
    (38, 'SE26B16-S201', '综合球馆', 'SE26C11', 'SE26CO16', 'SE26B16', 2, 'SPORTS', 2, 80, 0),
    (39, 'SE26B08-A201', '版式工坊', 'SE26C04', 'SE26CO06', 'SE26B08', 2, 'ART', 1, 36, 0),
    (40, 'SE26B08-A401', '视觉工坊', 'SE26C04', 'SE26CO06', 'SE26B08', 4, 'ART', 2, 36, 0),
    (41, 'SE26B17-A202', '陶艺工坊', 'SE26C12', 'SE26CO17', 'SE26B17', 2, 'ART', 3, 30, 0),
    (42, 'SE26B17-A402', '综合艺术工坊', 'SE26C12', 'SE26CO17', 'SE26B17', 4, 'ART', 4, 30, 0);

DROP TEMPORARY TABLE IF EXISTS tmp_seed_admin_class;
CREATE TEMPORARY TABLE tmp_seed_admin_class (
    seq INT PRIMARY KEY,
    class_code VARCHAR(32) NOT NULL,
    class_name VARCHAR(64) NOT NULL,
    grade_code VARCHAR(32) NOT NULL,
    campus_code VARCHAR(32) NOT NULL,
    college_code VARCHAR(32) NOT NULL,
    major_code VARCHAR(32) NOT NULL,
    stage_code VARCHAR(32) NOT NULL,
    head_teacher_code VARCHAR(32) NOT NULL,
    course_code_1 VARCHAR(32) NOT NULL,
    course_code_2 VARCHAR(32) NOT NULL,
    course_code_3 VARCHAR(32) NOT NULL,
    course_code_4 VARCHAR(32) NOT NULL,
    course_code_5 VARCHAR(32) NOT NULL
);

INSERT INTO tmp_seed_admin_class (
    seq, class_code, class_name, grade_code, campus_code, college_code, major_code, stage_code,
    head_teacher_code, course_code_1, course_code_2, course_code_3, course_code_4, course_code_5
) VALUES
    (1, 'SE26AC001', '2024级软件工程1班', 'SE26G01', 'SE26C01', 'SE26CO01', 'SE26M01', 'SE26ST01', 'T26001', 'C26001', 'C26002', 'C26025', 'C26028', 'C26040'),
    (2, 'SE26AC002', '2024级人工智能1班', 'SE26G02', 'SE26C01', 'SE26CO01', 'SE26M02', 'SE26ST01', 'T26002', 'C26003', 'C26004', 'C26026', 'C26028', 'C26040'),
    (3, 'SE26AC003', '2024级大数据1班', 'SE26G03', 'SE26C01', 'SE26CO02', 'SE26M03', 'SE26ST01', 'T26003', 'C26005', 'C26006', 'C26025', 'C26030', 'C26040'),
    (4, 'SE26AC004', '2024级网安1班', 'SE26G04', 'SE26C01', 'SE26CO02', 'SE26M04', 'SE26ST01', 'T26004', 'C26007', 'C26008', 'C26026', 'C26032', 'C26040'),
    (5, 'SE26AC005', '2024级智能制造1班', 'SE26G05', 'SE26C02', 'SE26CO03', 'SE26M05', 'SE26ST01', 'T26005', 'C26009', 'C26010', 'C26025', 'C26029', 'C26042'),
    (6, 'SE26AC006', '2024级机器人工程1班', 'SE26G06', 'SE26C02', 'SE26CO03', 'SE26M06', 'SE26ST01', 'T26006', 'C26011', 'C26012', 'C26026', 'C26029', 'C26040'),
    (7, 'SE26AC007', '2024级通信工程1班', 'SE26G07', 'SE26C02', 'SE26CO04', 'SE26M07', 'SE26ST01', 'T26007', 'C26013', 'C26014', 'C26025', 'C26028', 'C26040'),
    (8, 'SE26AC008', '2024级集成电路1班', 'SE26G08', 'SE26C02', 'SE26CO04', 'SE26M08', 'SE26ST01', 'T26008', 'C26015', 'C26016', 'C26026', 'C26030', 'C26042'),
    (9, 'SE26AC009', '2024级工商管理1班', 'SE26G09', 'SE26C03', 'SE26CO05', 'SE26M09', 'SE26ST01', 'T26009', 'C26017', 'C26019', 'C26025', 'C26031', 'C26040'),
    (10, 'SE26AC010', '2024级会计学1班', 'SE26G10', 'SE26C03', 'SE26CO05', 'SE26M10', 'SE26ST01', 'T26010', 'C26018', 'C26020', 'C26026', 'C26031', 'C26040'),
    (11, 'SE26AC011', '2024级视觉传达1班', 'SE26G11', 'SE26C04', 'SE26CO06', 'SE26M11', 'SE26ST01', 'T26011', 'C26021', 'C26022', 'C26027', 'C26039', 'C26041'),
    (12, 'SE26AC012', '2024级数字媒体1班', 'SE26G12', 'SE26C04', 'SE26CO06', 'SE26M12', 'SE26ST01', 'T26012', 'C26023', 'C26024', 'C26026', 'C26032', 'C26041');

-- ------------------------------------------------------------
-- 2. 组织域与资源域落库
-- ------------------------------------------------------------
INSERT INTO org_stage (
    stage_code, stage_name, stage_level, status, remark,
    created_by, updated_by, created_at, updated_at, deleted
)
SELECT
    stage_code,
    stage_name,
    stage_level,
    1,
    CONCAT(@seed_tag, '|stage|', stage_code),
    @seed_user_id,
    @seed_user_id,
    NOW(),
    NOW(),
    0
FROM tmp_seed_stage;

INSERT INTO org_school_year (
    year_code, year_name, start_date, end_date, is_current, status, remark,
    created_by, updated_by, created_at, updated_at, deleted
)
SELECT
    year_code, year_name, start_date, end_date, is_current, 1,
    CONCAT(@seed_tag, '|school_year|', year_code),
    @seed_user_id, @seed_user_id, NOW(), NOW(), 0
FROM tmp_seed_school_year;

INSERT INTO org_campus (
    campus_code, campus_name, campus_type, province_code, city_code, district_code, address,
    sort_no, status, remark, created_by, updated_by, created_at, updated_at, deleted
)
SELECT
    campus_code, campus_name, campus_type, province_code, city_code, district_code, address,
    sort_no, 1, CONCAT(@seed_tag, '|campus|', campus_code),
    @seed_user_id, @seed_user_id, NOW(), NOW(), 0
FROM tmp_seed_campus;

INSERT INTO org_term (
    term_code, term_name, school_year_id, campus_id, start_date, end_date, week_count,
    is_current, status, remark, created_by, updated_by, created_at, updated_at, deleted
)
SELECT
    t.term_code,
    t.term_name,
    y.id,
    c.id,
    t.start_date,
    t.end_date,
    t.week_count,
    t.is_current,
    1,
    CONCAT(@seed_tag, '|term|', t.term_code),
    @seed_user_id,
    @seed_user_id,
    NOW(),
    NOW(),
    0
FROM tmp_seed_term t
INNER JOIN org_school_year y ON y.year_code = t.year_code AND y.deleted = 0
LEFT JOIN org_campus c ON c.campus_code = t.campus_code AND c.deleted = 0;

INSERT INTO org_college (
    college_code, college_name, campus_id, dean_user_id, sort_no, status, remark,
    created_by, updated_by, created_at, updated_at, deleted
)
SELECT
    tc.college_code,
    tc.college_name,
    c.id,
    NULL,
    tc.seq,
    1,
    CONCAT(@seed_tag, '|college|', tc.college_code),
    @seed_user_id,
    @seed_user_id,
    NOW(),
    NOW(),
    0
FROM tmp_seed_college tc
INNER JOIN org_campus c ON c.campus_code = tc.campus_code AND c.deleted = 0;

INSERT INTO org_major (
    major_code, major_name, college_id, stage_id, status, remark,
    created_by, updated_by, created_at, updated_at, deleted
)
SELECT
    tm.major_code,
    tm.major_name,
    co.id,
    st.id,
    1,
    CONCAT(@seed_tag, '|major|', tm.major_code),
    @seed_user_id,
    @seed_user_id,
    NOW(),
    NOW(),
    0
FROM tmp_seed_major tm
INNER JOIN org_college co ON co.college_code = tm.college_code AND co.deleted = 0
INNER JOIN org_stage st ON st.stage_code = tm.stage_code AND st.deleted = 0;

INSERT INTO org_grade (
    grade_code, grade_name, stage_id, school_year_id, campus_id, college_id, major_id,
    entry_year, graduate_year, status, remark,
    created_by, updated_by, created_at, updated_at, deleted
)
SELECT
    tg.grade_code,
    tg.grade_name,
    st.id,
    sy.id,
    ca.id,
    co.id,
    mj.id,
    tg.entry_year,
    tg.graduate_year,
    1,
    CONCAT(@seed_tag, '|grade|', tg.grade_code),
    @seed_user_id,
    @seed_user_id,
    NOW(),
    NOW(),
    0
FROM tmp_seed_grade tg
INNER JOIN org_stage st ON st.stage_code = tg.stage_code AND st.deleted = 0
INNER JOIN org_school_year sy ON sy.year_code = 'SE26Y2526' AND sy.deleted = 0
INNER JOIN org_campus ca ON ca.campus_code = tg.campus_code AND ca.deleted = 0
INNER JOIN org_college co ON co.college_code = tg.college_code AND co.deleted = 0
INNER JOIN org_major mj ON mj.major_code = tg.major_code AND mj.deleted = 0;

INSERT INTO res_teacher (
    teacher_code, teacher_name, gender, mobile, email, title_name, campus_id, college_id, stage_id,
    allow_cross_campus, allow_cross_college, max_week_hours, max_day_hours, forbidden_time_slots,
    hire_status, user_id, status, remark,
    created_by, updated_by, created_at, updated_at, deleted
)
SELECT
    tt.teacher_code,
    tt.teacher_name,
    tt.gender,
    tt.mobile,
    tt.email,
    tt.title_name,
    ca.id,
    co.id,
    st.id,
    tt.allow_cross_campus,
    tt.allow_cross_college,
    tt.max_week_hours,
    tt.max_day_hours,
    tt.forbidden_time_slots,
    'ACTIVE',
    tt.user_id,
    1,
    CONCAT(@seed_tag, '|teacher|', tt.teacher_code),
    @seed_user_id,
    @seed_user_id,
    NOW(),
    NOW(),
    0
FROM tmp_seed_teacher tt
INNER JOIN org_campus ca ON ca.campus_code = tt.campus_code AND ca.deleted = 0
INNER JOIN org_college co ON co.college_code = tt.college_code AND co.deleted = 0
INNER JOIN org_stage st ON st.stage_code = tt.stage_code AND st.deleted = 0;

INSERT INTO res_course (
    course_code, course_name, course_short_name, course_type, stage_id, college_id,
    total_hours, week_hours, need_continuous, continuous_size, need_special_room, room_type,
    is_core_course, allow_cross_day, status, remark,
    created_by, updated_by, created_at, updated_at, deleted
)
SELECT
    tc.course_code,
    tc.course_name,
    tc.course_short_name,
    tc.course_type,
    st.id,
    co.id,
    tc.total_hours,
    tc.week_hours,
    tc.need_continuous,
    tc.continuous_size,
    tc.need_special_room,
    tc.room_type,
    tc.is_core_course,
    tc.allow_cross_day,
    1,
    CONCAT(@seed_tag, '|course|', tc.course_code),
    @seed_user_id,
    @seed_user_id,
    NOW(),
    NOW(),
    0
FROM tmp_seed_course tc
INNER JOIN org_stage st ON st.stage_code = tc.stage_code AND st.deleted = 0
INNER JOIN org_college co ON co.college_code = tc.college_code AND co.deleted = 0;

INSERT INTO res_building (
    building_code, building_name, campus_id, college_id, building_type, floor_count,
    status, remark, created_by, updated_by, created_at, updated_at, deleted
)
SELECT
    tb.building_code,
    tb.building_name,
    ca.id,
    co.id,
    tb.building_type,
    tb.floor_count,
    1,
    CONCAT(@seed_tag, '|building|', tb.building_code),
    @seed_user_id,
    @seed_user_id,
    NOW(),
    NOW(),
    0
FROM tmp_seed_building tb
INNER JOIN org_campus ca ON ca.campus_code = tb.campus_code AND ca.deleted = 0
LEFT JOIN org_college co ON co.college_code = tb.college_code AND co.deleted = 0;

INSERT INTO res_classroom (
    classroom_code, classroom_name, campus_id, college_id, building_id, floor_no,
    room_type, seat_count, is_shared, status, remark,
    created_by, updated_by, created_at, updated_at, deleted
)
SELECT
    tc.classroom_code,
    tc.classroom_name,
    ca.id,
    co.id,
    b.id,
    tc.floor_no,
    tc.room_type,
    tc.seat_count,
    tc.is_shared,
    1,
    CONCAT(@seed_tag, '|classroom|', tc.classroom_code),
    @seed_user_id,
    @seed_user_id,
    NOW(),
    NOW(),
    0
FROM tmp_seed_classroom tc
INNER JOIN org_campus ca ON ca.campus_code = tc.campus_code AND ca.deleted = 0
LEFT JOIN org_college co ON co.college_code = tc.college_code AND co.deleted = 0
INNER JOIN res_building b ON b.building_code = tc.building_code AND b.deleted = 0;

INSERT INTO org_admin_class (
    class_code, class_name, grade_id, campus_id, college_id, major_id, stage_id,
    head_teacher_id, student_count, class_mode, status, forbidden_time_slots, remark,
    created_by, updated_by, created_at, updated_at, deleted
)
SELECT
    ta.class_code,
    ta.class_name,
    g.id,
    ca.id,
    co.id,
    mj.id,
    st.id,
    rt.id,
    0,
    'FIXED',
    1,
    NULL,
    CONCAT(@seed_tag, '|admin_class|', ta.class_code),
    @seed_user_id,
    @seed_user_id,
    NOW(),
    NOW(),
    0
FROM tmp_seed_admin_class ta
INNER JOIN org_grade g ON g.grade_code = ta.grade_code AND g.deleted = 0
INNER JOIN org_campus ca ON ca.campus_code = ta.campus_code AND ca.deleted = 0
INNER JOIN org_college co ON co.college_code = ta.college_code AND co.deleted = 0
INNER JOIN org_major mj ON mj.major_code = ta.major_code AND mj.deleted = 0
INNER JOIN org_stage st ON st.stage_code = ta.stage_code AND st.deleted = 0
INNER JOIN res_teacher rt ON rt.teacher_code = ta.head_teacher_code AND rt.deleted = 0;

DROP TEMPORARY TABLE IF EXISTS tmp_seed_student;
CREATE TEMPORARY TABLE tmp_seed_student (
    seq INT PRIMARY KEY,
    student_code VARCHAR(32) NOT NULL,
    student_name VARCHAR(64) NOT NULL,
    gender VARCHAR(8),
    mobile VARCHAR(20),
    email VARCHAR(128),
    campus_code VARCHAR(32) NOT NULL,
    college_code VARCHAR(32) NOT NULL,
    stage_code VARCHAR(32) NOT NULL,
    grade_code VARCHAR(32) NOT NULL,
    major_code VARCHAR(32) NOT NULL,
    admin_class_code VARCHAR(32) NOT NULL,
    entry_year INT NOT NULL,
    user_id BIGINT
);

INSERT INTO tmp_seed_student (
    seq, student_code, student_name, gender, mobile, email,
    campus_code, college_code, stage_code, grade_code, major_code, admin_class_code,
    entry_year, user_id
)
SELECT
    ((ac.seq - 1) * 15) + slot.seq AS seq,
    CONCAT('2024', LPAD(((ac.seq - 1) * 15) + slot.seq, 4, '0')) AS student_code,
    CASE
        WHEN ((ac.seq - 1) * 15) + slot.seq = 1 THEN '演示学生'
        ELSE CONCAT(
            ELT((((ac.seq - 1) * 15) + slot.seq - 1) % 16 + 1,
                '陈', '林', '周', '许', '韩', '顾', '宋', '沈',
                '叶', '纪', '梁', '程', '苏', '薛', '魏', '乔'
            ),
            ELT((((ac.seq - 1) * 15) + slot.seq - 1) % 16 + 1,
                '知远', '若川', '以宁', '景澄', '书尧', '清妍', '亦凡', '嘉禾',
                '知夏', '安歌', '景行', '临风', '清和', '知予', '安然', '若溪'
            )
        )
    END AS student_name,
    IF(MOD(((ac.seq - 1) * 15) + slot.seq, 2) = 0, '女', '男') AS gender,
    CONCAT('188', LPAD(60000000 + ((ac.seq - 1) * 15) + slot.seq, 8, '0')) AS mobile,
    CONCAT('student', LPAD(((ac.seq - 1) * 15) + slot.seq, 3, '0'), '@demo.edu.cn') AS email,
    ac.campus_code,
    ac.college_code,
    ac.stage_code,
    ac.grade_code,
    ac.major_code,
    ac.class_code,
    2024,
    CASE WHEN ((ac.seq - 1) * 15) + slot.seq = 1 THEN 910003 ELSE NULL END AS user_id
FROM tmp_seed_admin_class ac
INNER JOIN tmp_seed_seq slot ON slot.seq BETWEEN 1 AND 15;

INSERT INTO res_student (
    student_code, student_name, gender, mobile, email, campus_id, college_id, stage_id,
    grade_id, major_id, admin_class_id, entry_year, user_id, status, remark,
    created_by, updated_by, created_at, updated_at, deleted
)
SELECT
    ts.student_code,
    ts.student_name,
    ts.gender,
    ts.mobile,
    ts.email,
    ca.id,
    co.id,
    st.id,
    g.id,
    mj.id,
    ac.id,
    ts.entry_year,
    ts.user_id,
    1,
    CONCAT(@seed_tag, '|student|', ts.student_code),
    @seed_user_id,
    @seed_user_id,
    NOW(),
    NOW(),
    0
FROM tmp_seed_student ts
INNER JOIN org_campus ca ON ca.campus_code = ts.campus_code AND ca.deleted = 0
INNER JOIN org_college co ON co.college_code = ts.college_code AND co.deleted = 0
INNER JOIN org_stage st ON st.stage_code = ts.stage_code AND st.deleted = 0
INNER JOIN org_grade g ON g.grade_code = ts.grade_code AND g.deleted = 0
INNER JOIN org_major mj ON mj.major_code = ts.major_code AND mj.deleted = 0
INNER JOIN org_admin_class ac ON ac.class_code = ts.admin_class_code AND ac.deleted = 0;

-- ------------------------------------------------------------
-- 3. 关联表与排课域播种
-- ------------------------------------------------------------
DROP TEMPORARY TABLE IF EXISTS tmp_seed_slot_profile;
CREATE TEMPORARY TABLE tmp_seed_slot_profile (
    slot_no INT PRIMARY KEY,
    weekday_base INT NOT NULL,
    period_no INT NOT NULL,
    need_fixed_time TINYINT NOT NULL,
    priority_level INT NOT NULL
);

INSERT INTO tmp_seed_slot_profile (slot_no, weekday_base, period_no, need_fixed_time, priority_level) VALUES
    (1, 1, 1, 0, 7),
    (2, 2, 3, 0, 6),
    (3, 3, 5, 1, 5),
    (4, 4, 2, 0, 4),
    (5, 5, 7, 1, 6);

DROP TEMPORARY TABLE IF EXISTS tmp_seed_task_plan;
CREATE TEMPORARY TABLE tmp_seed_task_plan (
    plan_no INT PRIMARY KEY,
    teaching_class_code VARCHAR(32) NOT NULL,
    teaching_class_name VARCHAR(64) NOT NULL,
    admin_class_code VARCHAR(32) NOT NULL,
    course_code VARCHAR(32) NOT NULL,
    teacher_code VARCHAR(32) NOT NULL,
    scheduled_weekday_no INT NOT NULL,
    scheduled_period_no INT NOT NULL,
    need_fixed_time TINYINT NOT NULL,
    fixed_weekday_no INT,
    fixed_period_no INT,
    priority_level INT NOT NULL,
    course_attr_code VARCHAR(8) NOT NULL,
    room_type VARCHAR(32),
    need_special_room TINYINT NOT NULL,
    need_continuous TINYINT NOT NULL,
    continuous_size INT NOT NULL,
    week_hours INT NOT NULL,
    total_hours INT NOT NULL
);

INSERT INTO tmp_seed_task_plan (
    plan_no, teaching_class_code, teaching_class_name, admin_class_code, course_code, teacher_code,
    scheduled_weekday_no, scheduled_period_no, need_fixed_time, fixed_weekday_no, fixed_period_no,
    priority_level, course_attr_code, room_type, need_special_room, need_continuous,
    continuous_size, week_hours, total_hours
)
SELECT
    ((ac.seq - 1) * 5) + sp.slot_no AS plan_no,
    CONCAT('SE26TC', LPAD(((ac.seq - 1) * 5) + sp.slot_no, 3, '0')) AS teaching_class_code,
    CONCAT(c.course_name, '-', ac.class_name) AS teaching_class_name,
    ac.class_code,
    CASE sp.slot_no
        WHEN 1 THEN ac.course_code_1
        WHEN 2 THEN ac.course_code_2
        WHEN 3 THEN ac.course_code_3
        WHEN 4 THEN ac.course_code_4
        ELSE ac.course_code_5
    END AS course_code,
    c.primary_teacher_code,
    MOD(sp.weekday_base + ac.seq - 2, 5) + 1 AS scheduled_weekday_no,
    sp.period_no AS scheduled_period_no,
    sp.need_fixed_time,
    CASE WHEN sp.need_fixed_time = 1 THEN MOD(sp.weekday_base + ac.seq - 2, 5) + 1 ELSE NULL END AS fixed_weekday_no,
    CASE WHEN sp.need_fixed_time = 1 THEN sp.period_no ELSE NULL END AS fixed_period_no,
    sp.priority_level,
    c.course_attr_code,
    c.room_type,
    c.need_special_room,
    c.need_continuous,
    c.continuous_size,
    c.week_hours,
    c.total_hours
FROM tmp_seed_admin_class ac
INNER JOIN tmp_seed_slot_profile sp ON 1 = 1
INNER JOIN tmp_seed_course c ON c.course_code = CASE sp.slot_no
    WHEN 1 THEN ac.course_code_1
    WHEN 2 THEN ac.course_code_2
    WHEN 3 THEN ac.course_code_3
    WHEN 4 THEN ac.course_code_4
    ELSE ac.course_code_5
END;

INSERT INTO org_teaching_class (
    teaching_class_code, teaching_class_name, term_id, campus_id, college_id, stage_id,
    grade_id, course_id, teacher_id, student_count, teaching_mode, status, remark,
    created_by, updated_by, created_at, updated_at, deleted
)
SELECT
    tp.teaching_class_code,
    tp.teaching_class_name,
    term_current.id,
    ac.campus_id,
    c.college_id,
    ac.stage_id,
    ac.grade_id,
    c.id,
    t.id,
    0,
    CASE
        WHEN COALESCE(tp.room_type, 'NORMAL') IN ('LAB', 'COMPUTER') THEN 'PRACTICE'
        WHEN COALESCE(tp.room_type, 'NORMAL') = 'SPORTS' THEN 'PHYSICAL'
        WHEN COALESCE(tp.room_type, 'NORMAL') = 'ART' THEN 'ART'
        ELSE 'NORMAL'
    END,
    1,
    CONCAT(@seed_tag, '|teaching_class|', tp.teaching_class_code),
    @seed_user_id,
    @seed_user_id,
    NOW(),
    NOW(),
    0
FROM tmp_seed_task_plan tp
INNER JOIN org_admin_class ac ON ac.class_code = tp.admin_class_code AND ac.deleted = 0
INNER JOIN res_course c ON c.course_code = tp.course_code AND c.deleted = 0
INNER JOIN res_teacher t ON t.teacher_code = tp.teacher_code AND t.deleted = 0
INNER JOIN org_term term_current ON term_current.term_code = 'SE26T251' AND term_current.deleted = 0;

INSERT INTO res_student_teaching_class (
    student_id, teaching_class_id, term_id, select_source,
    created_by, updated_by, created_at, updated_at, deleted
)
SELECT
    s.id,
    tc.id,
    term_current.id,
    'SYSTEM',
    @seed_user_id,
    @seed_user_id,
    NOW(),
    NOW(),
    0
FROM tmp_seed_student ss
INNER JOIN res_student s ON s.student_code = ss.student_code AND s.deleted = 0
INNER JOIN tmp_seed_task_plan tp ON tp.admin_class_code = ss.admin_class_code
INNER JOIN org_teaching_class tc ON tc.teaching_class_code = tp.teaching_class_code AND tc.deleted = 0
INNER JOIN org_term term_current ON term_current.term_code = 'SE26T251' AND term_current.deleted = 0;

UPDATE org_admin_class ac
INNER JOIN (
    SELECT admin_class_id, COUNT(*) AS student_total
    FROM res_student
    WHERE deleted = 0
      AND remark LIKE CONCAT(@seed_tag, '%')
    GROUP BY admin_class_id
) stat ON stat.admin_class_id = ac.id
SET ac.student_count = stat.student_total,
    ac.updated_by = @seed_user_id,
    ac.updated_at = NOW();

UPDATE org_teaching_class tc
INNER JOIN (
    SELECT teaching_class_id, COUNT(*) AS student_total
    FROM res_student_teaching_class
    WHERE deleted = 0
    GROUP BY teaching_class_id
) stat ON stat.teaching_class_id = tc.id
SET tc.student_count = stat.student_total,
    tc.updated_by = @seed_user_id,
    tc.updated_at = NOW()
WHERE tc.remark LIKE CONCAT(@seed_tag, '%');

INSERT INTO res_teacher_course (
    teacher_id, course_id, campus_id, college_id, stage_id, priority_level,
    created_by, updated_by, created_at, updated_at, deleted
)
SELECT DISTINCT
    t.id,
    c.id,
    t.campus_id,
    c.college_id,
    c.stage_id,
    CASE WHEN tp.need_continuous = 1 THEN 2 ELSE 1 END,
    @seed_user_id,
    @seed_user_id,
    NOW(),
    NOW(),
    0
FROM tmp_seed_task_plan tp
INNER JOIN res_teacher t ON t.teacher_code = tp.teacher_code AND t.deleted = 0
INNER JOIN res_course c ON c.course_code = tp.course_code AND c.deleted = 0;

DROP TEMPORARY TABLE IF EXISTS tmp_seed_plan_room_assignment;
CREATE TEMPORARY TABLE tmp_seed_plan_room_assignment (
    plan_no INT PRIMARY KEY,
    classroom_code VARCHAR(32) NOT NULL
);

INSERT INTO tmp_seed_plan_room_assignment (plan_no, classroom_code)
SELECT
    tp.plan_no,
    cr.classroom_code
FROM tmp_seed_task_plan tp
INNER JOIN tmp_seed_classroom cr
    ON cr.room_type = COALESCE(tp.room_type, 'NORMAL')
   AND cr.room_slot = CASE COALESCE(tp.room_type, 'NORMAL')
        WHEN 'LAB' THEN MOD(tp.plan_no - 1, 8) + 1
        WHEN 'COMPUTER' THEN MOD(tp.plan_no - 1, 4) + 1
        WHEN 'SPORTS' THEN MOD(tp.plan_no - 1, 2) + 1
        WHEN 'ART' THEN MOD(tp.plan_no - 1, 4) + 1
        ELSE MOD(tp.plan_no - 1, 24) + 1
   END;

INSERT INTO sch_task (
    task_code, school_year_id, term_id, campus_id, college_id, stage_id, grade_id, major_id,
    admin_class_id, teaching_class_id, course_id, teacher_id, student_count, week_hours, total_hours,
    need_continuous, continuous_size, need_fixed_room, fixed_room_id, need_fixed_time,
    fixed_weekday_no, fixed_period_no, priority_level, allow_conflict, task_status, source_type,
    status, remark, created_by, updated_by, created_at, updated_at, deleted
)
SELECT
    CONCAT('SE26TK', LPAD(tp.plan_no, 3, '0')) AS task_code,
    sy.id,
    term_current.id,
    ac.campus_id,
    c.college_id,
    ac.stage_id,
    ac.grade_id,
    ac.major_id,
    ac.id,
    tc.id,
    c.id,
    t.id,
    ac.student_count,
    tp.week_hours,
    tp.total_hours,
    tp.need_continuous,
    tp.continuous_size,
    tp.need_special_room,
    CASE WHEN tp.need_special_room = 1 THEN room.id ELSE NULL END AS fixed_room_id,
    tp.need_fixed_time,
    tp.fixed_weekday_no,
    tp.fixed_period_no,
    tp.priority_level,
    0,
    'SCHEDULED',
    'MANUAL',
    1,
    CONCAT(
        'semester=', @semester_text,
        ',classNo=', ac.class_code,
        ',courseNo=', c.course_code,
        ',teacherNo=', t.teacher_code,
        ',gradeNo=', g.grade_code,
        ',courseName=', c.course_name,
        ',courseAttr=', tp.course_attr_code,
        ',teacherName=', t.teacher_name
    ),
    @seed_user_id,
    @seed_user_id,
    NOW(),
    NOW(),
    0
FROM tmp_seed_task_plan tp
INNER JOIN org_school_year sy ON sy.year_code = 'SE26Y2526' AND sy.deleted = 0
INNER JOIN org_term term_current ON term_current.term_code = 'SE26T251' AND term_current.deleted = 0
INNER JOIN org_admin_class ac ON ac.class_code = tp.admin_class_code AND ac.deleted = 0
INNER JOIN org_grade g ON g.id = ac.grade_id AND g.deleted = 0
INNER JOIN org_teaching_class tc ON tc.teaching_class_code = tp.teaching_class_code AND tc.deleted = 0
INNER JOIN res_course c ON c.course_code = tp.course_code AND c.deleted = 0
INNER JOIN res_teacher t ON t.teacher_code = tp.teacher_code AND t.deleted = 0
LEFT JOIN tmp_seed_plan_room_assignment pra ON pra.plan_no = tp.plan_no
LEFT JOIN res_classroom room ON room.classroom_code = pra.classroom_code AND room.deleted = 0;

INSERT INTO sch_schedule_run_log (
    run_code, term_id, campus_id, college_id, stage_id, run_type, algorithm_type,
    task_total, task_success, task_failed, run_status, failure_reason, started_at, finished_at,
    operator_user_id, remark, created_by, updated_by, created_at, updated_at, deleted
)
SELECT
    'SE26RUN01',
    term_current.id,
    NULL,
    NULL,
    st.id,
    'MANUAL',
    'GENETIC',
    30,
    30,
    0,
    'SUCCESS',
    '首批 30 条排课任务的标准测试数据已写入',
    '2026-03-25 09:00:00',
    '2026-03-25 09:00:08',
    @seed_user_id,
    @semester_text,
    @seed_user_id,
    @seed_user_id,
    '2026-03-25 09:00:08',
    '2026-03-25 09:00:08',
    0
FROM org_term term_current
INNER JOIN org_stage st ON st.stage_code = 'SE26ST01' AND st.deleted = 0
WHERE term_current.term_code = 'SE26T251' AND term_current.deleted = 0;

INSERT INTO sch_schedule_run_log (
    run_code, term_id, campus_id, college_id, stage_id, run_type, algorithm_type,
    task_total, task_success, task_failed, run_status, failure_reason, started_at, finished_at,
    operator_user_id, remark, created_by, updated_by, created_at, updated_at, deleted
)
SELECT
    'SE26RUN02',
    term_current.id,
    NULL,
    NULL,
    st.id,
    'MANUAL',
    'GENETIC',
    30,
    30,
    0,
    'SUCCESS',
    '剩余 30 条排课任务与课表结果已回放完成',
    '2026-03-25 09:10:00',
    '2026-03-25 09:10:09',
    @seed_user_id,
    @semester_text,
    @seed_user_id,
    @seed_user_id,
    '2026-03-25 09:10:09',
    '2026-03-25 09:10:09',
    0
FROM org_term term_current
INNER JOIN org_stage st ON st.stage_code = 'SE26ST01' AND st.deleted = 0
WHERE term_current.term_code = 'SE26T251' AND term_current.deleted = 0;

INSERT INTO sch_schedule_result (
    run_log_id, task_id, school_year_id, term_id, campus_id, college_id, stage_id, grade_id,
    admin_class_id, teaching_class_id, course_id, teacher_id, classroom_id, weekday_no, period_no,
    week_range_type, week_range_text, is_locked, source_type, conflict_flag, status, remark,
    created_by, updated_by, created_at, updated_at, deleted
)
SELECT
    CASE WHEN tp.plan_no <= 30 THEN rl1.id ELSE rl2.id END AS run_log_id,
    task.id,
    sy.id,
    term_current.id,
    task.campus_id,
    task.college_id,
    task.stage_id,
    task.grade_id,
    task.admin_class_id,
    task.teaching_class_id,
    task.course_id,
    task.teacher_id,
    room.id,
    CASE
        WHEN tp.plan_no <= 12 THEN MOD(tp.scheduled_weekday_no, 5) + 1
        ELSE tp.scheduled_weekday_no
    END AS weekday_no,
    tp.scheduled_period_no,
    'ALL',
    CASE
        WHEN tp.week_hours >= 3 THEN '1-18周'
        ELSE '1-16周'
    END,
    tp.need_fixed_time,
    CASE WHEN tp.plan_no <= 12 THEN 'DRAG' ELSE 'AUTO' END,
    0,
    1,
    @semester_text,
    @seed_user_id,
    @seed_user_id,
    NOW(),
    NOW(),
    0
FROM tmp_seed_task_plan tp
INNER JOIN sch_task task ON task.task_code = CONCAT('SE26TK', LPAD(tp.plan_no, 3, '0')) AND task.deleted = 0
INNER JOIN org_school_year sy ON sy.year_code = 'SE26Y2526' AND sy.deleted = 0
INNER JOIN org_term term_current ON term_current.term_code = 'SE26T251' AND term_current.deleted = 0
INNER JOIN sch_schedule_run_log rl1 ON rl1.run_code = 'SE26RUN01' AND rl1.deleted = 0
INNER JOIN sch_schedule_run_log rl2 ON rl2.run_code = 'SE26RUN02' AND rl2.deleted = 0
INNER JOIN tmp_seed_plan_room_assignment pra ON pra.plan_no = tp.plan_no
INNER JOIN res_classroom room ON room.classroom_code = pra.classroom_code AND room.deleted = 0;

INSERT INTO sch_schedule_adjust_log (
    adjust_code, term_id, source_result_id, target_result_id, adjust_type,
    before_weekday_no, before_period_no, before_classroom_id,
    after_weekday_no, after_period_no, after_classroom_id,
    adjust_reason, operator_user_id, status, remark,
    created_by, updated_by, created_at, updated_at, deleted
)
SELECT
    CONCAT('SE26ADJ', LPAD(tp.plan_no, 3, '0')) AS adjust_code,
    term_current.id,
    result.id,
    NULL,
    'MOVE',
    CASE
        WHEN result.weekday_no = 1 THEN 5
        ELSE result.weekday_no - 1
    END AS before_weekday_no,
    result.period_no,
    result.classroom_id,
    result.weekday_no,
    result.period_no,
    result.classroom_id,
    CONCAT(ac.class_name, ' ', c.course_name, ' 拖拽调课演示'),
    @seed_user_id,
    1,
    @semester_text,
    @seed_user_id,
    @seed_user_id,
    NOW(),
    NOW(),
    0
FROM tmp_seed_task_plan tp
INNER JOIN org_term term_current ON term_current.term_code = 'SE26T251' AND term_current.deleted = 0
INNER JOIN sch_task task ON task.task_code = CONCAT('SE26TK', LPAD(tp.plan_no, 3, '0')) AND task.deleted = 0
INNER JOIN sch_schedule_result result ON result.task_id = task.id AND result.deleted = 0
INNER JOIN org_admin_class ac ON ac.id = task.admin_class_id AND ac.deleted = 0
INNER JOIN res_course c ON c.id = task.course_id AND c.deleted = 0
WHERE tp.plan_no <= 12;

-- ------------------------------------------------------------
-- 4. 执行后可直接复核的统计结果
-- ------------------------------------------------------------
SELECT 'org_campus' AS table_name, COUNT(*) AS row_count FROM org_campus WHERE deleted = 0 AND remark LIKE CONCAT(@seed_tag, '%')
UNION ALL
SELECT 'org_college', COUNT(*) FROM org_college WHERE deleted = 0 AND remark LIKE CONCAT(@seed_tag, '%')
UNION ALL
SELECT 'org_stage', COUNT(*) FROM org_stage WHERE deleted = 0 AND remark LIKE CONCAT(@seed_tag, '%')
UNION ALL
SELECT 'org_major', COUNT(*) FROM org_major WHERE deleted = 0 AND remark LIKE CONCAT(@seed_tag, '%')
UNION ALL
SELECT 'org_grade', COUNT(*) FROM org_grade WHERE deleted = 0 AND remark LIKE CONCAT(@seed_tag, '%')
UNION ALL
SELECT 'org_admin_class', COUNT(*) FROM org_admin_class WHERE deleted = 0 AND remark LIKE CONCAT(@seed_tag, '%')
UNION ALL
SELECT 'org_teaching_class', COUNT(*) FROM org_teaching_class WHERE deleted = 0 AND remark LIKE CONCAT(@seed_tag, '%')
UNION ALL
SELECT 'res_teacher', COUNT(*) FROM res_teacher WHERE deleted = 0 AND remark LIKE CONCAT(@seed_tag, '%')
UNION ALL
SELECT 'res_course', COUNT(*) FROM res_course WHERE deleted = 0 AND remark LIKE CONCAT(@seed_tag, '%')
UNION ALL
SELECT 'res_student', COUNT(*) FROM res_student WHERE deleted = 0 AND remark LIKE CONCAT(@seed_tag, '%')
UNION ALL
SELECT 'res_building', COUNT(*) FROM res_building WHERE deleted = 0 AND remark LIKE CONCAT(@seed_tag, '%')
UNION ALL
SELECT 'res_classroom', COUNT(*) FROM res_classroom WHERE deleted = 0 AND remark LIKE CONCAT(@seed_tag, '%')
UNION ALL
SELECT 'res_teacher_course', COUNT(*) FROM res_teacher_course rtc INNER JOIN res_teacher rt ON rt.id = rtc.teacher_id WHERE rtc.deleted = 0 AND rt.remark LIKE CONCAT(@seed_tag, '%')
UNION ALL
SELECT 'res_student_teaching_class', COUNT(*) FROM res_student_teaching_class rstc INNER JOIN org_teaching_class otc ON otc.id = rstc.teaching_class_id WHERE rstc.deleted = 0 AND otc.remark LIKE CONCAT(@seed_tag, '%')
UNION ALL
SELECT 'sch_task', COUNT(*) FROM sch_task WHERE deleted = 0 AND task_code LIKE CONCAT(@seed_prefix, 'TK%')
UNION ALL
SELECT 'sch_schedule_run_log', COUNT(*) FROM sch_schedule_run_log WHERE deleted = 0 AND run_code LIKE CONCAT(@seed_prefix, 'RUN%')
UNION ALL
SELECT 'sch_schedule_result', COUNT(*) FROM sch_schedule_result sr INNER JOIN sch_task st ON st.id = sr.task_id WHERE sr.deleted = 0 AND st.task_code LIKE CONCAT(@seed_prefix, 'TK%')
UNION ALL
SELECT 'sch_schedule_adjust_log', COUNT(*) FROM sch_schedule_adjust_log WHERE deleted = 0 AND adjust_code LIKE CONCAT(@seed_prefix, 'ADJ%');

SELECT
    ac.class_code,
    ac.class_name,
    tc.teaching_class_code,
    c.course_code,
    c.course_name,
    t.teacher_code,
    t.teacher_name,
    COUNT(rstc.id) AS selected_students
FROM org_teaching_class tc
INNER JOIN sch_task st ON st.teaching_class_id = tc.id AND st.deleted = 0
INNER JOIN org_admin_class ac ON ac.id = st.admin_class_id AND ac.deleted = 0
INNER JOIN res_course c ON c.id = st.course_id AND c.deleted = 0
INNER JOIN res_teacher t ON t.id = st.teacher_id AND t.deleted = 0
LEFT JOIN res_student_teaching_class rstc ON rstc.teaching_class_id = tc.id AND rstc.deleted = 0
WHERE tc.remark LIKE CONCAT(@seed_tag, '%')
GROUP BY ac.class_code, ac.class_name, tc.teaching_class_code, c.course_code, c.course_name, t.teacher_code, t.teacher_name
ORDER BY tc.teaching_class_code
LIMIT 12;

SELECT
    st.task_code,
    st.remark AS task_meta,
    sr.weekday_no,
    sr.period_no,
    rc.classroom_code,
    rc.classroom_name,
    sal.adjust_code,
    sal.adjust_reason
FROM sch_task st
INNER JOIN sch_schedule_result sr ON sr.task_id = st.id AND sr.deleted = 0
LEFT JOIN res_classroom rc ON rc.id = sr.classroom_id AND rc.deleted = 0
LEFT JOIN sch_schedule_adjust_log sal ON sal.source_result_id = sr.id AND sal.deleted = 0
WHERE st.task_code LIKE CONCAT(@seed_prefix, 'TK%')
ORDER BY st.task_code
LIMIT 12;

