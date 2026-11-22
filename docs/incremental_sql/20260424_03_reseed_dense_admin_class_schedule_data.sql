START TRANSACTION;

SET SESSION sql_safe_updates = 0;

DELETE FROM sch_schedule_adjust_log;
DELETE FROM sch_schedule_result;
DELETE FROM sch_schedule_run_detail;
DELETE FROM sch_schedule_run_log;
DELETE FROM sch_task;

UPDATE org_admin_class
SET class_name = '2024级软件工程1班',
    student_count = 46,
    campus_id = 16,
    college_id = 16,
    stage_id = 8,
    status = 1,
    deleted = 0,
    remark = 'dense-seed:2026-04-24|keep'
WHERE id = 16;

UPDATE org_admin_class
SET class_name = '2024级人工智能1班',
    student_count = 44,
    campus_id = 16,
    college_id = 16,
    stage_id = 8,
    status = 1,
    deleted = 0,
    remark = 'dense-seed:2026-04-24|keep'
WHERE id = 17;

UPDATE org_admin_class
SET class_name = '2024级数据科学与大数据技术1班',
    student_count = 42,
    campus_id = 16,
    college_id = 16,
    stage_id = 8,
    status = 1,
    deleted = 0,
    remark = 'dense-seed:2026-04-24|keep'
WHERE id = 18;

UPDATE org_admin_class
SET status = 0,
    deleted = 1,
    remark = 'dense-seed:2026-04-24|disabled'
WHERE id IN (19, 20, 21, 22, 23, 24, 25);

UPDATE res_teacher
SET teacher_name = '贺知行',
    title_name = '讲师',
    campus_id = 16,
    college_id = 16,
    stage_id = 8,
    max_week_hours = 18,
    max_day_hours = 4,
    status = 1,
    deleted = 0,
    remark = 'dense-seed:2026-04-24|software'
WHERE id = 64;

UPDATE res_teacher
SET teacher_name = '林若川',
    title_name = '讲师',
    campus_id = 16,
    college_id = 16,
    stage_id = 8,
    max_week_hours = 22,
    max_day_hours = 5,
    status = 1,
    deleted = 0,
    remark = 'dense-seed:2026-04-24|ai'
WHERE id = 66;

UPDATE res_teacher
SET teacher_name = '周以宁',
    title_name = '讲师',
    campus_id = 16,
    college_id = 16,
    stage_id = 8,
    max_week_hours = 22,
    max_day_hours = 5,
    status = 1,
    deleted = 0,
    remark = 'dense-seed:2026-04-24|bigdata'
WHERE id = 67;

UPDATE res_teacher
SET teacher_name = '顾清妍',
    title_name = '体育教师',
    campus_id = 16,
    college_id = 16,
    stage_id = 8,
    max_week_hours = 10,
    max_day_hours = 3,
    status = 1,
    deleted = 0,
    remark = 'dense-seed:2026-04-24|sports'
WHERE id = 71;

UPDATE res_teacher
SET teacher_name = '宋亦凡',
    title_name = '思政教师',
    campus_id = 16,
    college_id = 16,
    stage_id = 8,
    max_week_hours = 12,
    max_day_hours = 4,
    status = 1,
    deleted = 0,
    remark = 'dense-seed:2026-04-24|politics'
WHERE id = 72;

UPDATE res_teacher
SET teacher_name = '沈嘉禾',
    title_name = '辅导员',
    campus_id = 16,
    college_id = 16,
    stage_id = 8,
    max_week_hours = 12,
    max_day_hours = 3,
    status = 1,
    deleted = 0,
    remark = 'dense-seed:2026-04-24|student-affairs'
WHERE id = 73;

UPDATE res_teacher
SET teacher_name = '祁闻道',
    title_name = '英语教师',
    campus_id = 16,
    college_id = 16,
    stage_id = 8,
    max_week_hours = 14,
    max_day_hours = 4,
    status = 1,
    deleted = 0,
    remark = 'dense-seed:2026-04-24|english'
WHERE id = 92;

UPDATE res_teacher
SET teacher_name = '谢青岚',
    title_name = '数学教师',
    campus_id = 16,
    college_id = 16,
    stage_id = 8,
    max_week_hours = 16,
    max_day_hours = 4,
    status = 1,
    deleted = 0,
    remark = 'dense-seed:2026-04-24|math-core'
WHERE id = 93;

INSERT INTO res_teacher (
    id, teacher_code, teacher_name, gender, mobile, email, title_name,
    campus_id, college_id, stage_id, allow_cross_campus, allow_cross_college,
    max_week_hours, max_day_hours, forbidden_time_slots, hire_status, user_id,
    status, remark, created_by, updated_by, deleted
) VALUES (
    94, 'T26031', '苏怀瑾', '女', '13800002631', 'suhuaijin@example.com', '数学教师',
    16, 16, 8, 0, 0,
    14, 4, NULL, 'ACTIVE', NULL,
    1, 'dense-seed:2026-04-24|math-advanced', 910001, 910001, 0
)
ON DUPLICATE KEY UPDATE
teacher_name = VALUES(teacher_name),
gender = VALUES(gender),
mobile = VALUES(mobile),
email = VALUES(email),
title_name = VALUES(title_name),
campus_id = VALUES(campus_id),
college_id = VALUES(college_id),
stage_id = VALUES(stage_id),
allow_cross_campus = VALUES(allow_cross_campus),
allow_cross_college = VALUES(allow_cross_college),
max_week_hours = VALUES(max_week_hours),
max_day_hours = VALUES(max_day_hours),
forbidden_time_slots = VALUES(forbidden_time_slots),
hire_status = VALUES(hire_status),
status = VALUES(status),
remark = VALUES(remark),
updated_by = VALUES(updated_by),
deleted = VALUES(deleted);

UPDATE res_course SET week_hours = 4, total_hours = 64, status = 1, deleted = 0, remark = 'dense-seed:2026-04-24' WHERE id = 64;
UPDATE res_course SET week_hours = 3, total_hours = 48, status = 1, deleted = 0, remark = 'dense-seed:2026-04-24' WHERE id = 65;
UPDATE res_course SET week_hours = 4, total_hours = 64, status = 1, deleted = 0, remark = 'dense-seed:2026-04-24' WHERE id = 66;
UPDATE res_course SET week_hours = 4, total_hours = 64, need_continuous = 1, continuous_size = 2, need_special_room = 1, room_type = 'COMPUTER', status = 1, deleted = 0, remark = 'dense-seed:2026-04-24' WHERE id = 67;
UPDATE res_course SET week_hours = 3, total_hours = 48, status = 1, deleted = 0, remark = 'dense-seed:2026-04-24' WHERE id = 68;
UPDATE res_course SET week_hours = 4, total_hours = 64, need_special_room = 1, room_type = 'COMPUTER', status = 1, deleted = 0, remark = 'dense-seed:2026-04-24' WHERE id = 69;
UPDATE res_course SET week_hours = 4, total_hours = 64, status = 1, deleted = 0, remark = 'dense-seed:2026-04-24' WHERE id = 88;
UPDATE res_course SET week_hours = 4, total_hours = 64, status = 1, deleted = 0, remark = 'dense-seed:2026-04-24' WHERE id = 89;
UPDATE res_course SET week_hours = 2, total_hours = 32, status = 1, deleted = 0, remark = 'dense-seed:2026-04-24' WHERE id = 90;
UPDATE res_course SET week_hours = 5, total_hours = 80, status = 1, deleted = 0, remark = 'dense-seed:2026-04-24' WHERE id = 91;
UPDATE res_course SET week_hours = 4, total_hours = 64, status = 1, deleted = 0, remark = 'dense-seed:2026-04-24' WHERE id = 92;
UPDATE res_course SET week_hours = 2, total_hours = 32, status = 1, deleted = 0, remark = 'dense-seed:2026-04-24' WHERE id = 93;
UPDATE res_course SET week_hours = 2, total_hours = 32, status = 1, deleted = 0, remark = 'dense-seed:2026-04-24' WHERE id = 94;
UPDATE res_course SET week_hours = 2, total_hours = 32, need_special_room = 1, room_type = 'COMPUTER', status = 1, deleted = 0, remark = 'dense-seed:2026-04-24' WHERE id = 95;
UPDATE res_course SET week_hours = 2, total_hours = 32, status = 1, deleted = 0, remark = 'dense-seed:2026-04-24' WHERE id = 96;
UPDATE res_course SET week_hours = 2, total_hours = 32, status = 1, deleted = 0, remark = 'dense-seed:2026-04-24' WHERE id = 97;
UPDATE res_course SET week_hours = 1, total_hours = 16, status = 1, deleted = 0, remark = 'dense-seed:2026-04-24' WHERE id = 98;
UPDATE res_course SET week_hours = 1, total_hours = 16, status = 1, deleted = 0, remark = 'dense-seed:2026-04-24' WHERE id = 99;
UPDATE res_course SET week_hours = 1, total_hours = 16, status = 1, deleted = 0, remark = 'dense-seed:2026-04-24' WHERE id = 100;
UPDATE res_course SET week_hours = 2, total_hours = 32, need_special_room = 1, room_type = 'COMPUTER', status = 1, deleted = 0, remark = 'dense-seed:2026-04-24' WHERE id = 101;
UPDATE res_course SET week_hours = 2, total_hours = 32, need_special_room = 1, room_type = 'SPORTS', status = 1, deleted = 0, remark = 'dense-seed:2026-04-24' WHERE id = 103;
UPDATE res_course SET week_hours = 2, total_hours = 32, need_special_room = 1, room_type = 'SPORTS', status = 1, deleted = 0, remark = 'dense-seed:2026-04-24' WHERE id = 104;
UPDATE res_course SET week_hours = 2, total_hours = 32, need_continuous = 1, continuous_size = 2, need_special_room = 1, room_type = 'LAB', status = 1, deleted = 0, remark = 'dense-seed:2026-04-24' WHERE id = 105;

INSERT INTO sch_task (
    task_code, school_year_id, term_id, campus_id, college_id, stage_id, grade_id, major_id,
    admin_class_id, teaching_class_id, course_id, teacher_id, student_count, week_hours, total_hours,
    need_continuous, continuous_size, need_fixed_room, fixed_room_id, need_fixed_time, fixed_weekday_no,
    fixed_period_no, priority_level, allow_conflict, task_status, source_type, status, remark,
    created_by, updated_by, deleted
) VALUES
('D26TK001', 5, 10, 16, 16, 8, 32, NULL, 16, NULL, 91, 93, 46, 5, 80, 0, 1, 0, NULL, 0, NULL, NULL, 9, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC001,courseNo=C26028,teacherNo=T26030,gradeNo=SE26G01,courseName=高等数学A,courseAttr=01,teacherName=谢青岚', 910001, 910001, 0),
('D26TK002', 5, 10, 16, 16, 8, 32, NULL, 16, NULL, 88, 92, 46, 4, 64, 0, 1, 0, NULL, 0, NULL, NULL, 8, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC001,courseNo=C26025,teacherNo=T26029,gradeNo=SE26G01,courseName=大学英语A,courseAttr=01,teacherName=祁闻道', 910001, 910001, 0),
('D26TK003', 5, 10, 16, 16, 8, 32, NULL, 16, NULL, 64, 64, 46, 4, 64, 0, 1, 0, NULL, 0, NULL, NULL, 8, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC001,courseNo=C26001,teacherNo=T26001,gradeNo=SE26G01,courseName=程序设计基础,courseAttr=01,teacherName=贺知行', 910001, 910001, 0),
('D26TK004', 5, 10, 16, 16, 8, 32, NULL, 16, NULL, 68, 64, 46, 4, 64, 0, 1, 0, NULL, 0, NULL, NULL, 8, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC001,courseNo=C26005,teacherNo=T26001,gradeNo=SE26G01,courseName=数据结构与算法,courseAttr=01,teacherName=贺知行', 910001, 910001, 0),
('D26TK005', 5, 10, 16, 16, 8, 32, NULL, 16, NULL, 65, 64, 46, 3, 48, 0, 1, 0, NULL, 0, NULL, NULL, 7, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC001,courseNo=C26002,teacherNo=T26001,gradeNo=SE26G01,courseName=软件需求分析,courseAttr=01,teacherName=贺知行', 910001, 910001, 0),
('D26TK006', 5, 10, 16, 16, 8, 32, NULL, 16, NULL, 93, 94, 46, 2, 32, 0, 1, 0, NULL, 0, NULL, NULL, 7, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC001,courseNo=C26030,teacherNo=T26031,gradeNo=SE26G01,courseName=线性代数,courseAttr=01,teacherName=苏怀瑾', 910001, 910001, 0),
('D26TK007', 5, 10, 16, 16, 8, 32, NULL, 16, NULL, 94, 94, 46, 2, 32, 0, 1, 0, NULL, 0, NULL, NULL, 7, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC001,courseNo=C26031,teacherNo=T26031,gradeNo=SE26G01,courseName=概率统计,courseAttr=01,teacherName=苏怀瑾', 910001, 910001, 0),
('D26TK008', 5, 10, 16, 16, 8, 32, NULL, 16, NULL, 96, 72, 46, 2, 32, 0, 1, 0, NULL, 0, NULL, NULL, 6, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC001,courseNo=C26033,teacherNo=T26009,gradeNo=SE26G01,courseName=思想道德与法治,courseAttr=01,teacherName=宋亦凡', 910001, 910001, 0),
('D26TK009', 5, 10, 16, 16, 8, 32, NULL, 16, NULL, 97, 72, 46, 2, 32, 0, 1, 0, NULL, 0, NULL, NULL, 6, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC001,courseNo=C26034,teacherNo=T26009,gradeNo=SE26G01,courseName=中国近现代史纲要,courseAttr=01,teacherName=宋亦凡', 910001, 910001, 0),
('D26TK010', 5, 10, 16, 16, 8, 32, NULL, 16, NULL, 103, 71, 46, 2, 32, 0, 1, 0, NULL, 0, NULL, NULL, 5, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC001,courseNo=C26040,teacherNo=T26008,gradeNo=SE26G01,courseName=大学体育I,courseAttr=02,teacherName=顾清妍', 910001, 910001, 0),
('D26TK011', 5, 10, 16, 16, 8, 32, NULL, 16, NULL, 101, 64, 46, 3, 48, 0, 1, 0, NULL, 0, NULL, NULL, 7, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC001,courseNo=C26038,teacherNo=T26001,gradeNo=SE26G01,courseName=信息素养导论,courseAttr=02,teacherName=贺知行', 910001, 910001, 0),
('D26TK012', 5, 10, 16, 16, 8, 32, NULL, 16, NULL, 105, 64, 46, 2, 32, 1, 2, 0, NULL, 0, NULL, NULL, 7, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC001,courseNo=C26042,teacherNo=T26001,gradeNo=SE26G01,courseName=工程实践基础,courseAttr=02,teacherName=贺知行', 910001, 910001, 0),
('D26TK013', 5, 10, 16, 16, 8, 32, NULL, 16, NULL, 98, 73, 46, 1, 16, 0, 1, 0, NULL, 0, NULL, NULL, 4, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC001,courseNo=C26035,teacherNo=T26010,gradeNo=SE26G01,courseName=大学生心理健康教育,courseAttr=01,teacherName=沈嘉禾', 910001, 910001, 0),
('D26TK014', 5, 10, 16, 16, 8, 32, NULL, 16, NULL, 99, 73, 46, 1, 16, 0, 1, 0, NULL, 0, NULL, NULL, 4, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC001,courseNo=C26036,teacherNo=T26010,gradeNo=SE26G01,courseName=职业发展与就业指导,courseAttr=01,teacherName=沈嘉禾', 910001, 910001, 0),
('D26TK015', 5, 10, 16, 16, 8, 32, NULL, 16, NULL, 100, 73, 46, 1, 16, 0, 1, 0, NULL, 0, NULL, NULL, 4, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC001,courseNo=C26037,teacherNo=T26010,gradeNo=SE26G01,courseName=创新创业基础,courseAttr=01,teacherName=沈嘉禾', 910001, 910001, 0),
('D26TK016', 5, 10, 16, 16, 8, 32, NULL, 16, NULL, 90, 92, 46, 2, 32, 0, 1, 0, NULL, 0, NULL, NULL, 5, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC001,courseNo=C26027,teacherNo=T26029,gradeNo=SE26G01,courseName=学术写作,courseAttr=01,teacherName=祁闻道', 910001, 910001, 0),

('D26TK017', 5, 10, 16, 16, 8, 33, NULL, 17, NULL, 91, 93, 44, 5, 80, 0, 1, 0, NULL, 0, NULL, NULL, 9, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC002,courseNo=C26028,teacherNo=T26030,gradeNo=SE26G02,courseName=高等数学A,courseAttr=01,teacherName=谢青岚', 910001, 910001, 0),
('D26TK018', 5, 10, 16, 16, 8, 33, NULL, 17, NULL, 88, 92, 44, 4, 64, 0, 1, 0, NULL, 0, NULL, NULL, 8, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC002,courseNo=C26025,teacherNo=T26029,gradeNo=SE26G02,courseName=大学英语A,courseAttr=01,teacherName=祁闻道', 910001, 910001, 0),
('D26TK019', 5, 10, 16, 16, 8, 33, NULL, 17, NULL, 64, 66, 44, 3, 48, 0, 1, 0, NULL, 0, NULL, NULL, 8, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC002,courseNo=C26001,teacherNo=T26003,gradeNo=SE26G02,courseName=程序设计基础,courseAttr=01,teacherName=林若川', 910001, 910001, 0),
('D26TK020', 5, 10, 16, 16, 8, 33, NULL, 17, NULL, 68, 66, 44, 3, 48, 0, 1, 0, NULL, 0, NULL, NULL, 8, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC002,courseNo=C26005,teacherNo=T26003,gradeNo=SE26G02,courseName=数据结构与算法,courseAttr=01,teacherName=林若川', 910001, 910001, 0),
('D26TK021', 5, 10, 16, 16, 8, 33, NULL, 17, NULL, 66, 66, 44, 4, 64, 0, 1, 0, NULL, 0, NULL, NULL, 8, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC002,courseNo=C26003,teacherNo=T26003,gradeNo=SE26G02,courseName=机器学习导论,courseAttr=02,teacherName=林若川', 910001, 910001, 0),
('D26TK022', 5, 10, 16, 16, 8, 33, NULL, 17, NULL, 67, 66, 44, 4, 64, 1, 2, 0, NULL, 0, NULL, NULL, 8, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC002,courseNo=C26004,teacherNo=T26003,gradeNo=SE26G02,courseName=智能系统设计,courseAttr=02,teacherName=林若川', 910001, 910001, 0),
('D26TK023', 5, 10, 16, 16, 8, 33, NULL, 17, NULL, 93, 94, 44, 2, 32, 0, 1, 0, NULL, 0, NULL, NULL, 7, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC002,courseNo=C26030,teacherNo=T26031,gradeNo=SE26G02,courseName=线性代数,courseAttr=01,teacherName=苏怀瑾', 910001, 910001, 0),
('D26TK024', 5, 10, 16, 16, 8, 33, NULL, 17, NULL, 96, 72, 44, 2, 32, 0, 1, 0, NULL, 0, NULL, NULL, 6, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC002,courseNo=C26033,teacherNo=T26009,gradeNo=SE26G02,courseName=思想道德与法治,courseAttr=01,teacherName=宋亦凡', 910001, 910001, 0),
('D26TK025', 5, 10, 16, 16, 8, 33, NULL, 17, NULL, 97, 72, 44, 2, 32, 0, 1, 0, NULL, 0, NULL, NULL, 6, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC002,courseNo=C26034,teacherNo=T26009,gradeNo=SE26G02,courseName=中国近现代史纲要,courseAttr=01,teacherName=宋亦凡', 910001, 910001, 0),
('D26TK026', 5, 10, 16, 16, 8, 33, NULL, 17, NULL, 103, 71, 44, 2, 32, 0, 1, 0, NULL, 0, NULL, NULL, 5, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC002,courseNo=C26040,teacherNo=T26008,gradeNo=SE26G02,courseName=大学体育I,courseAttr=02,teacherName=顾清妍', 910001, 910001, 0),
('D26TK027', 5, 10, 16, 16, 8, 33, NULL, 17, NULL, 101, 66, 44, 2, 32, 0, 1, 0, NULL, 0, NULL, NULL, 7, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC002,courseNo=C26038,teacherNo=T26003,gradeNo=SE26G02,courseName=信息素养导论,courseAttr=02,teacherName=林若川', 910001, 910001, 0),
('D26TK028', 5, 10, 16, 16, 8, 33, NULL, 17, NULL, 105, 66, 44, 2, 32, 1, 2, 0, NULL, 0, NULL, NULL, 7, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC002,courseNo=C26042,teacherNo=T26003,gradeNo=SE26G02,courseName=工程实践基础,courseAttr=02,teacherName=林若川', 910001, 910001, 0),
('D26TK029', 5, 10, 16, 16, 8, 33, NULL, 17, NULL, 98, 73, 44, 1, 16, 0, 1, 0, NULL, 0, NULL, NULL, 4, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC002,courseNo=C26035,teacherNo=T26010,gradeNo=SE26G02,courseName=大学生心理健康教育,courseAttr=01,teacherName=沈嘉禾', 910001, 910001, 0),
('D26TK030', 5, 10, 16, 16, 8, 33, NULL, 17, NULL, 99, 73, 44, 1, 16, 0, 1, 0, NULL, 0, NULL, NULL, 4, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC002,courseNo=C26036,teacherNo=T26010,gradeNo=SE26G02,courseName=职业发展与就业指导,courseAttr=01,teacherName=沈嘉禾', 910001, 910001, 0),
('D26TK031', 5, 10, 16, 16, 8, 33, NULL, 17, NULL, 100, 73, 44, 1, 16, 0, 1, 0, NULL, 0, NULL, NULL, 4, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC002,courseNo=C26037,teacherNo=T26010,gradeNo=SE26G02,courseName=创新创业基础,courseAttr=01,teacherName=沈嘉禾', 910001, 910001, 0),
('D26TK032', 5, 10, 16, 16, 8, 33, NULL, 17, NULL, 95, 66, 44, 2, 32, 0, 1, 0, NULL, 0, NULL, NULL, 7, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC002,courseNo=C26032,teacherNo=T26003,gradeNo=SE26G02,courseName=数据素养基础,courseAttr=02,teacherName=林若川', 910001, 910001, 0),

('D26TK033', 5, 10, 16, 16, 8, 34, NULL, 18, NULL, 92, 93, 42, 4, 64, 0, 1, 0, NULL, 0, NULL, NULL, 9, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC003,courseNo=C26029,teacherNo=T26030,gradeNo=SE26G03,courseName=高等数学B,courseAttr=01,teacherName=谢青岚', 910001, 910001, 0),
('D26TK034', 5, 10, 16, 16, 8, 34, NULL, 18, NULL, 89, 92, 42, 4, 64, 0, 1, 0, NULL, 0, NULL, NULL, 8, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC003,courseNo=C26026,teacherNo=T26029,gradeNo=SE26G03,courseName=大学英语B,courseAttr=01,teacherName=祁闻道', 910001, 910001, 0),
('D26TK035', 5, 10, 16, 16, 8, 34, NULL, 18, NULL, 64, 67, 42, 3, 48, 0, 1, 0, NULL, 0, NULL, NULL, 8, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC003,courseNo=C26001,teacherNo=T26004,gradeNo=SE26G03,courseName=程序设计基础,courseAttr=01,teacherName=周以宁', 910001, 910001, 0),
('D26TK036', 5, 10, 16, 16, 8, 34, NULL, 18, NULL, 68, 67, 42, 3, 48, 0, 1, 0, NULL, 0, NULL, NULL, 8, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC003,courseNo=C26005,teacherNo=T26004,gradeNo=SE26G03,courseName=数据结构与算法,courseAttr=01,teacherName=周以宁', 910001, 910001, 0),
('D26TK037', 5, 10, 16, 16, 8, 34, NULL, 18, NULL, 69, 67, 42, 4, 64, 0, 1, 0, NULL, 0, NULL, NULL, 8, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC003,courseNo=C26006,teacherNo=T26004,gradeNo=SE26G03,courseName=数据仓库与数据挖掘,courseAttr=02,teacherName=周以宁', 910001, 910001, 0),
('D26TK038', 5, 10, 16, 16, 8, 34, NULL, 18, NULL, 95, 67, 42, 2, 32, 0, 1, 0, NULL, 0, NULL, NULL, 7, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC003,courseNo=C26032,teacherNo=T26004,gradeNo=SE26G03,courseName=数据素养基础,courseAttr=02,teacherName=周以宁', 910001, 910001, 0),
('D26TK039', 5, 10, 16, 16, 8, 34, NULL, 18, NULL, 66, 67, 42, 3, 48, 0, 1, 0, NULL, 0, NULL, NULL, 7, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC003,courseNo=C26003,teacherNo=T26004,gradeNo=SE26G03,courseName=机器学习导论,courseAttr=02,teacherName=周以宁', 910001, 910001, 0),
('D26TK040', 5, 10, 16, 16, 8, 34, NULL, 18, NULL, 93, 94, 42, 2, 32, 0, 1, 0, NULL, 0, NULL, NULL, 7, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC003,courseNo=C26030,teacherNo=T26031,gradeNo=SE26G03,courseName=线性代数,courseAttr=01,teacherName=苏怀瑾', 910001, 910001, 0),
('D26TK041', 5, 10, 16, 16, 8, 34, NULL, 18, NULL, 94, 94, 42, 2, 32, 0, 1, 0, NULL, 0, NULL, NULL, 7, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC003,courseNo=C26031,teacherNo=T26031,gradeNo=SE26G03,courseName=概率统计,courseAttr=01,teacherName=苏怀瑾', 910001, 910001, 0),
('D26TK042', 5, 10, 16, 16, 8, 34, NULL, 18, NULL, 96, 72, 42, 2, 32, 0, 1, 0, NULL, 0, NULL, NULL, 6, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC003,courseNo=C26033,teacherNo=T26009,gradeNo=SE26G03,courseName=思想道德与法治,courseAttr=01,teacherName=宋亦凡', 910001, 910001, 0),
('D26TK043', 5, 10, 16, 16, 8, 34, NULL, 18, NULL, 97, 72, 42, 2, 32, 0, 1, 0, NULL, 0, NULL, NULL, 6, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC003,courseNo=C26034,teacherNo=T26009,gradeNo=SE26G03,courseName=中国近现代史纲要,courseAttr=01,teacherName=宋亦凡', 910001, 910001, 0),
('D26TK044', 5, 10, 16, 16, 8, 34, NULL, 18, NULL, 104, 71, 42, 2, 32, 0, 1, 0, NULL, 0, NULL, NULL, 5, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC003,courseNo=C26041,teacherNo=T26008,gradeNo=SE26G03,courseName=大学体育II,courseAttr=02,teacherName=顾清妍', 910001, 910001, 0),
('D26TK045', 5, 10, 16, 16, 8, 34, NULL, 18, NULL, 105, 67, 42, 2, 32, 1, 2, 0, NULL, 0, NULL, NULL, 7, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC003,courseNo=C26042,teacherNo=T26004,gradeNo=SE26G03,courseName=工程实践基础,courseAttr=02,teacherName=周以宁', 910001, 910001, 0),
('D26TK046', 5, 10, 16, 16, 8, 34, NULL, 18, NULL, 98, 73, 42, 1, 16, 0, 1, 0, NULL, 0, NULL, NULL, 4, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC003,courseNo=C26035,teacherNo=T26010,gradeNo=SE26G03,courseName=大学生心理健康教育,courseAttr=01,teacherName=沈嘉禾', 910001, 910001, 0),
('D26TK047', 5, 10, 16, 16, 8, 34, NULL, 18, NULL, 99, 73, 42, 1, 16, 0, 1, 0, NULL, 0, NULL, NULL, 4, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC003,courseNo=C26036,teacherNo=T26010,gradeNo=SE26G03,courseName=职业发展与就业指导,courseAttr=01,teacherName=沈嘉禾', 910001, 910001, 0),
('D26TK048', 5, 10, 16, 16, 8, 34, NULL, 18, NULL, 100, 73, 42, 1, 16, 0, 1, 0, NULL, 0, NULL, NULL, 4, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC003,courseNo=C26037,teacherNo=T26010,gradeNo=SE26G03,courseName=创新创业基础,courseAttr=01,teacherName=沈嘉禾', 910001, 910001, 0),
('D26TK049', 5, 10, 16, 16, 8, 34, NULL, 18, NULL, 90, 92, 42, 2, 32, 0, 1, 0, NULL, 0, NULL, NULL, 5, 0, 'PENDING', 'MANUAL', 1, 'semester=2025-2026-1,classNo=SE26AC003,courseNo=C26027,teacherNo=T26029,gradeNo=SE26G03,courseName=学术写作,courseAttr=01,teacherName=祁闻道', 910001, 910001, 0);

COMMIT;
