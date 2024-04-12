/*
 Navicat Premium Data Transfer

 Source Server         : localhost_myqsl8
 Source Server Type    : MySQL
 Source Server Version : 80200 (8.2.0)
 Source Host           : localhost:3306
 Source Schema         : db_course_arrangement

 Target Server Type    : MySQL
 Target Server Version : 80200 (8.2.0)
 File Encoding         : 65001

 Date: 12/04/2024 23:58:17
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tb_admin
-- ----------------------------
DROP TABLE IF EXISTS `tb_admin`;
CREATE TABLE `tb_admin` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `admin_no` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '管理员编号',
  `username` varchar(36) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '用户名',
  `password` varchar(36) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '密码',
  `realname` varchar(36) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '真实姓名',
  `user_type` tinyint(1) NOT NULL DEFAULT '1' COMMENT '用户类型',
  `jobtitle` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '职称',
  `license` varchar(1000) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '证件照地址',
  `teach` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '教授科目',
  `telephone` varchar(36) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '联系电话',
  `email` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '电子邮件',
  `address` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '地址',
  `age` int DEFAULT NULL COMMENT '年龄',
  `avatar` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '头像',
  `description` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '签名',
  `remark` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '备注',
  `type` tinyint(1) DEFAULT NULL COMMENT '管理员类型',
  `piority` int DEFAULT NULL COMMENT '优先级',
  `power` tinyint(1) DEFAULT NULL COMMENT '1为管理员，0为超级管理员',
  `status` tinyint(1) DEFAULT '0' COMMENT '账号状态',
  `deleted` tinyint(1) DEFAULT '0',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of tb_admin
-- ----------------------------
BEGIN;
INSERT INTO `tb_admin` (`id`, `admin_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `license`, `teach`, `telephone`, `email`, `address`, `age`, `avatar`, `description`, `remark`, `type`, `piority`, `power`, `status`, `deleted`, `create_time`, `update_time`) VALUES (1, '10011', 'admin', 'aizai2015', '梁主任', 1, '教务处主任', NULL, NULL, NULL, 'admin@guet.com', NULL, NULL, NULL, '认真对待工作', '务实', NULL, NULL, NULL, 0, 0, '2020-03-06 23:31:17', '2020-03-06 23:31:17');
INSERT INTO `tb_admin` (`id`, `admin_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `license`, `teach`, `telephone`, `email`, `address`, `age`, `avatar`, `description`, `remark`, `type`, `piority`, `power`, `status`, `deleted`, `create_time`, `update_time`) VALUES (2, '10012', '123', '123456', '张三丰', 1, '教务处副主任', NULL, NULL, NULL, 'admin@guet.com', NULL, NULL, NULL, '光线强的地方，影子也比较黑暗。', '零下1摄氏度', NULL, NULL, NULL, 0, 0, '2020-03-04 21:24:57', NULL);
INSERT INTO `tb_admin` (`id`, `admin_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `license`, `teach`, `telephone`, `email`, `address`, `age`, `avatar`, `description`, `remark`, `type`, `piority`, `power`, `status`, `deleted`, `create_time`, `update_time`) VALUES (3, '10013', 'admin1', '123456', '王五', 1, '教务处副主任', NULL, NULL, NULL, 'admin@guet.com', NULL, NULL, NULL, '加油每一天', NULL, NULL, NULL, NULL, 0, 0, NULL, NULL);
COMMIT;

-- ----------------------------
-- Table structure for tb_class_info
-- ----------------------------
DROP TABLE IF EXISTS `tb_class_info`;
CREATE TABLE `tb_class_info` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id,班级表',
  `class_no` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '班级编号',
  `class_name` varchar(36) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '班级名称',
  `num` int NOT NULL DEFAULT '0' COMMENT '班级人数',
  `teacher` int DEFAULT NULL COMMENT '班主任',
  `remark` varchar(2) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '直接用来做为年级编号的划分了',
  `deleted` tinyint(1) DEFAULT '0',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of tb_class_info
-- ----------------------------
BEGIN;
INSERT INTO `tb_class_info` (`id`, `class_no`, `class_name`, `num`, `teacher`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (1, '20200101', '20年高一1班', 0, 1, '01', 0, NULL, '2020-05-20 16:23:22');
INSERT INTO `tb_class_info` (`id`, `class_no`, `class_name`, `num`, `teacher`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (2, '20200102', '20年高一2班', 0, 1, '01', 0, NULL, '2020-05-20 16:23:22');
INSERT INTO `tb_class_info` (`id`, `class_no`, `class_name`, `num`, `teacher`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (3, '20200103', '20年高一3班', 0, 3, '01', 0, NULL, '2020-05-20 16:23:22');
INSERT INTO `tb_class_info` (`id`, `class_no`, `class_name`, `num`, `teacher`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (4, '20200104', '20年高一4班', 0, 5, '01', 0, NULL, '2020-05-20 16:23:22');
INSERT INTO `tb_class_info` (`id`, `class_no`, `class_name`, `num`, `teacher`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (5, '20200105', '20年高一5班', 0, 5, '01', 0, NULL, '2020-05-20 16:23:22');
INSERT INTO `tb_class_info` (`id`, `class_no`, `class_name`, `num`, `teacher`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (6, '20200201', '20年高二1班', 0, 6, '02', 0, NULL, '2020-05-20 16:23:22');
INSERT INTO `tb_class_info` (`id`, `class_no`, `class_name`, `num`, `teacher`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (7, '20200202', '20年高二2班', 0, 7, '02', 0, NULL, '2020-05-20 16:23:22');
INSERT INTO `tb_class_info` (`id`, `class_no`, `class_name`, `num`, `teacher`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (8, '20200203', '20年高二3班', 0, 8, '02', 0, NULL, '2020-05-20 16:23:22');
INSERT INTO `tb_class_info` (`id`, `class_no`, `class_name`, `num`, `teacher`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (9, '20200204', '20年高二4班', 0, 9, '02', 0, NULL, '2020-05-20 16:23:22');
INSERT INTO `tb_class_info` (`id`, `class_no`, `class_name`, `num`, `teacher`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (10, '20200205', '20年高二5班', 0, 10, '02', 0, NULL, '2020-05-20 16:23:22');
INSERT INTO `tb_class_info` (`id`, `class_no`, `class_name`, `num`, `teacher`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (11, '20200301', '20年高三1班', 0, 11, '03', 0, NULL, '2020-05-20 16:23:22');
INSERT INTO `tb_class_info` (`id`, `class_no`, `class_name`, `num`, `teacher`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (12, '20200302', '20年高三2班', 0, 12, '03', 0, NULL, '2020-05-20 16:23:22');
INSERT INTO `tb_class_info` (`id`, `class_no`, `class_name`, `num`, `teacher`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (13, '20200303', '20年高三3班', 0, 13, '03', 0, NULL, '2020-05-20 16:23:22');
INSERT INTO `tb_class_info` (`id`, `class_no`, `class_name`, `num`, `teacher`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (14, '20200304', '20年高三4班', 0, 14, '03', 0, NULL, '2020-05-20 16:23:22');
INSERT INTO `tb_class_info` (`id`, `class_no`, `class_name`, `num`, `teacher`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (15, '20200305', '20年高三5班', 0, 15, '03', 0, NULL, '2020-05-20 16:23:22');
INSERT INTO `tb_class_info` (`id`, `class_no`, `class_name`, `num`, `teacher`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (24, '20200106', '高一6班-测试班级', 0, 41, '01', 0, '2020-06-11 08:43:07', NULL);
COMMIT;

-- ----------------------------
-- Table structure for tb_class_task
-- ----------------------------
DROP TABLE IF EXISTS `tb_class_task`;
CREATE TABLE `tb_class_task` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id，即将要上课的，需要进行排课的',
  `semester` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '学期',
  `grade_no` varchar(2) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '年级编号',
  `class_no` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '班级编号',
  `course_no` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '课程编号',
  `course_name` varchar(36) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '课程名',
  `teacher_no` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '教师编号',
  `realname` varchar(36) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '教师姓名',
  `courseAttr` varchar(2) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '课程属性',
  `studentNum` int NOT NULL COMMENT '学生人数',
  `weeks_sum` int NOT NULL COMMENT '周数',
  `weeks_number` int NOT NULL COMMENT '周学时',
  `isFix` varchar(1) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '是否固定上课时间',
  `class_time` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '固定时间的话,2位为一个时间位置',
  `deleted` tinyint(1) DEFAULT '0',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=64 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of tb_class_task
-- ----------------------------
BEGIN;
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (1, '2019-2020-1', '01', '20200101', '100001', '高一语文必修1', '10010', '梁晓明', '01', 42, 20, 4, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (2, '2019-2020-1', '01', '20200101', '100033', '高一数学必修1', '10012', '李雪雪', '01', 37, 20, 6, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (3, '2019-2020-1', '01', '20200101', '100056', '高一英语必修1', '10013', '王小芳', '01', 39, 20, 6, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (4, '2019-2020-1', '01', '20200101', '100004', '高一物理1', '10025', '张德良', '02', 42, 20, 6, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (5, '2019-2020-1', '01', '20200101', '100014', '高一化学必修1', '10033', '韩云', '02', 40, 20, 4, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (6, '2019-2020-1', '01', '20200101', '100041', '高一思想政治必修1', '10045', '江大波', '02', 40, 20, 4, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (7, '2019-2020-1', '01', '20200101', '100021', '高一历史必修1', '10044', '吴天盛', '02', 40, 20, 4, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (8, '2019-2020-1', '01', '20200101', '100007', '高一地理必修1', '10043', '王杰', '02', 40, 20, 4, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (9, '2019-2020-1', '01', '20200101', '100027', '高一生物必修1：分子与细胞', '10042', '谭咏麟', '02', 40, 20, 4, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (10, '2019-2020-1', '01', '20200101', '100051', '体育课', '10041', '张杰', '04', 40, 20, 2, '2', '14', 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (11, '2019-2020-1', '01', '20200101', '100066', '物理实验', '10025', '张德良', '03', 40, 20, 4, '2', '1521', 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (12, '2019-2020-1', '01', '20200101', '100067', '化学实验', '10023', '张靓颖', '03', 40, 20, 2, '2', '11', 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (13, '2019-2020-1', '01', '20200102', '100001', '高一语文必修1', '10010', '梁晓明', '01', 42, 20, 6, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (14, '2019-2020-1', '01', '20200102', '100033', '高一数学必修1', '10012', '李雪雪', '01', 37, 20, 6, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (15, '2019-2020-1', '01', '20200102', '100056', '高一英语必修1', '10013', '王小芳', '01', 39, 20, 4, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (16, '2019-2020-1', '01', '20200102', '100004', '高一物理1', '10025', '张德良', '02', 42, 20, 2, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (17, '2019-2020-1', '01', '20200102', '100014', '高一化学必修1', '10033', '韩云', '02', 40, 20, 2, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (18, '2019-2020-1', '01', '20200102', '100041', '高一思想政治必修1', '10045', '江大波', '02', 40, 20, 4, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (19, '2019-2020-1', '01', '20200102', '100021', '高一历史必修1', '10044', '吴天盛', '02', 40, 20, 4, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (20, '2019-2020-1', '01', '20200102', '100007', '高一地理必修1', '10043', '王杰', '02', 40, 20, 4, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (21, '2019-2020-1', '01', '20200102', '100027', '高一生物必修1：分子与细胞', '10042', '谭咏麟', '02', 40, 20, 2, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (22, '2019-2020-1', '01', '20200102', '100051', '体育课', '10041', '张杰', '04', 40, 20, 2, '2', '19', 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (23, '2019-2020-1', '01', '20200102', '100066', '物理实验', '10025', '张德良', '03', 40, 20, 2, '2', '09', 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (24, '2019-2020-1', '01', '20200102', '100067', '化学实验', '10023', '张靓颖', '03', 40, 20, 2, '2', '20', 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (25, '2019-2020-1', '01', '20200103', '100001', '高一语文必修1', '10034', '韦雪琪', '01', 45, 20, 6, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (26, '2019-2020-1', '01', '20200103', '100003', '高一数学1', '10035', '张三封', '01', 45, 20, 6, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (27, '2019-2020-1', '01', '20200103', '100056', '高一英语必修1', '10029', '郑小红', '01', 45, 20, 6, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (28, '2019-2020-1', '01', '20200103', '100004', '高一物理1', '10025', '张德良', '02', 45, 20, 4, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (29, '2019-2020-1', '01', '20200103', '100015', '高一化学必修2', '10037', '莫小新', '02', 45, 20, 4, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (30, '2019-2020-1', '01', '20200103', '100028', '高一生物必修2：遗传与进化', '10038', '甘楠', '02', 45, 20, 4, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (31, '2019-2020-1', '01', '20200103', '100022', '高一历史必修2', '10036', '胡小小', '02', 45, 20, 2, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (32, '2019-2020-1', '01', '20200103', '100008', '高一地理必修2', '10031', '张小龙', '02', 45, 20, 2, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (33, '2019-2020-1', '01', '20200103', '100042', '高一思想政治必修2', '10040', '夏紫若', '02', 45, 20, 2, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (34, '2019-2020-1', '01', '20200103', '100062', '信息与技术1', '10039', '江晓东', '03', 45, 20, 2, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (35, '2019-2020-1', '01', '20200103', '100051', '体育课', '10041', '张杰', '04', 45, 20, 2, '2', '15', 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (36, '2019-2020-1', '01', '20200103', '100066', '物理实验', '10025', '张德良', '03', 45, 20, 2, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (37, '2019-2020-1', '01', '20200103', '100067', '化学实验', '10023', '张靓颖', '03', 45, 20, 2, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (38, '2019-2020-1', '01', '20200104', '100001', '高一语文必修1', '10029', '郑小红', '01', 45, 20, 6, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (39, '2019-2020-1', '01', '20200104', '100003', '高一数学1', '10033', '韩云', '01', 45, 20, 6, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (40, '2019-2020-1', '01', '20200104', '100056', '高一英语必修1', '10020', '胡冬梅', '01', 45, 20, 6, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (41, '2019-2020-1', '01', '20200104', '100004', '高一物理1', '10035', '张三封', '02', 45, 20, 4, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (42, '2019-2020-1', '01', '20200104', '100015', '高一化学必修2', '10039', '江晓东', '02', 45, 20, 4, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (43, '2019-2020-1', '01', '20200104', '100028', '高一生物必修2：遗传与进化', '10038', '甘楠', '02', 45, 20, 4, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (44, '2019-2020-1', '01', '20200104', '100022', '高一历史必修2', '10036', '胡小小', '02', 45, 20, 4, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (45, '2019-2020-1', '01', '20200104', '100008', '高一地理必修2', '10031', '张小龙', '02', 45, 20, 4, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (46, '2019-2020-1', '01', '20200104', '100042', '高一思想政治必修2', '10040', '夏紫若', '02', 45, 20, 4, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (47, '2019-2020-1', '01', '20200104', '100062', '信息与技术1', '10039', '江晓东', '03', 45, 20, 2, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (48, '2019-2020-1', '01', '20200104', '100051', '体育课', '10041', '张杰', '04', 45, 20, 2, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (49, '2019-2020-1', '01', '20200104', '100066', '物理实验', '10025', '张德良', '03', 45, 20, 2, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (50, '2019-2020-1', '01', '20200104', '100067', '化学实验', '10023', '张靓颖', '03', 45, 20, 2, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (51, '2019-2020-1', '01', '20200105', '100001', '高一语文必修1', '10029', '郑小红', '01', 45, 20, 6, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (52, '2019-2020-1', '01', '20200105', '100003', '高一数学1', '10033', '韩云', '01', 45, 20, 6, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (53, '2019-2020-1', '01', '20200105', '100056', '高一英语必修1', '10020', '胡冬梅', '01', 45, 20, 6, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (54, '2019-2020-1', '01', '20200105', '100004', '高一物理1', '10035', '张三封', '02', 45, 20, 4, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (55, '2019-2020-1', '01', '20200105', '100015', '高一化学必修2', '10039', '江晓东', '02', 45, 20, 4, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (56, '2019-2020-1', '01', '20200105', '100028', '高一生物必修2：遗传与进化', '10038', '甘楠', '02', 45, 20, 4, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (57, '2019-2020-1', '01', '20200105', '100022', '高一历史必修2', '10036', '胡小小', '02', 45, 20, 4, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (58, '2019-2020-1', '01', '20200105', '100008', '高一地理必修2', '10031', '张小龙', '02', 45, 20, 4, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (59, '2019-2020-1', '01', '20200105', '100042', '高一思想政治必修2', '10040', '夏紫若', '02', 45, 20, 4, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (60, '2019-2020-1', '01', '20200105', '100062', '信息与技术1', '10039', '江晓东', '03', 45, 20, 2, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (61, '2019-2020-1', '01', '20200105', '100051', '体育课', '10041', '张杰', '04', 45, 20, 2, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (62, '2019-2020-1', '01', '20200105', '100066', '物理实验', '10025', '张德良', '03', 45, 20, 2, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
INSERT INTO `tb_class_task` (`id`, `semester`, `grade_no`, `class_no`, `course_no`, `course_name`, `teacher_no`, `realname`, `courseAttr`, `studentNum`, `weeks_sum`, `weeks_number`, `isFix`, `class_time`, `deleted`, `create_time`, `update_time`) VALUES (63, '2019-2020-1', '01', '20200105', '100067', '化学实验', '10023', '张靓颖', '03', 45, 20, 2, '1', NULL, 0, '2024-04-10 04:24:36', NULL);
COMMIT;

-- ----------------------------
-- Table structure for tb_classroom
-- ----------------------------
DROP TABLE IF EXISTS `tb_classroom`;
CREATE TABLE `tb_classroom` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '教室id',
  `classroom_no` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '教室编号',
  `classroom_name` varchar(36) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '教室名称',
  `teachbuild_no` varchar(4) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '所在教学楼编号',
  `capacity` int NOT NULL COMMENT '教室人数容量',
  `attr` varchar(2) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '教室属性',
  `remark` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '备注',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除（默认0显示，1删除）',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=157 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of tb_classroom
-- ----------------------------
BEGIN;
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (1, '01-101', '01-101', '01', 50, '01', '备注', 0, NULL, '2020-04-11 14:21:48');
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (2, '01-102', '01-102', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (3, '01-103', '01-103', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (4, '01-104', '01-104', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (5, '01-105', '01-105', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (6, '01-201', '01-201', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (7, '01-202', '01-202', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (8, '01-203', '01-203', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (9, '01-204', '01-204', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (10, '01-205', '01-205', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (11, '01-301', '01-301', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (12, '01-302', '01-302', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (13, '01-303', '01-303', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (14, '01-304', '01-304', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (15, '01-305', '01-305', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (16, '01-401', '01-401', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (17, '01-402', '01-402', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (18, '01-403', '01-403', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (19, '01-404', '01-404', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (20, '01-405', '01-405', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (21, '01-501', '01-501', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (22, '01-502', '01-502', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (23, '01-503', '01-503', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (24, '01-504', '01-504', '01', 50, '01', NULL, 0, '2020-03-19 12:32:18', '2020-03-12 12:32:21');
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (25, '01-505', '01-505', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (26, '02-101', '02-101', '02', 60, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (27, '02-102', '02-102', '02', 60, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (28, '02-103', '02-103', '02', 60, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (29, '02-104', '02-104', '02', 60, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (30, '02-105', '02-105', '02', 60, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (31, '02-201', '02-202', '02', 60, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (32, '02-202', '02-202', '02', 60, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (33, '02-203', '02-203', '02', 60, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (34, '02-204', '02-204', '02', 60, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (35, '02-205', '02-205', '02', 60, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (36, '02-301', '02-301', '02', 60, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (37, '02-302', '02-302', '02', 60, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (38, '02-303', '02-303', '02', 60, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (39, '02-304', '02-304', '02', 60, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (40, '02-305', '02-305', '02', 60, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (41, '02-401', '02-401', '02', 60, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (42, '02-402', '02-402', '02', 60, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (43, '02-403', '02-403', '02', 60, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (44, '02-404', '02-404', '02', 60, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (45, '02-405', '02-405', '02', 60, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (46, '03-101', '03-101', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (47, '03-102', '03-102', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (48, '03-103', '03-103', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (49, '03-104', '03-104', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (50, '03-105', '03-105', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (51, '03-201', '03-201', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (52, '03-202', '03-202', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (53, '03-203', '03-203', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (54, '03-204', '03-204', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (55, '03-205', '03-205', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (56, '03-301', '03-301', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (57, '03-302', '03-302', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (58, '03-303', '03-303', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (59, '03-304', '03-304', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (60, '03-305', '03-305', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (61, '03-401', '03-401', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (62, '03-402', '03-402', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (63, '03-403', '03-403', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (64, '03-404', '03-404', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (65, '03-405', '03-405', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (66, '03-501', '03-501', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (67, '03-502', '03-502', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (68, '03-503', '03-503', '03', 50, '01', '', 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (69, '03-504', '03-504', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (70, '03-505', '03-505', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (71, '04-101', '04-101', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (72, '04-102', '04-102', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (73, '04-103', '04-103', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (74, '04-104', '04-104', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (75, '04-105', '04-105', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (76, '04-201', '04-201', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (77, '04-202', '04-202', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (78, '04-203', '04-203', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (79, '04-204', '04-204', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (80, '04-205', '04-205', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (81, '04-301', '04-301', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (82, '04-302', '04-302', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (83, '04-303', '04-303', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (84, '04-304', '04-304', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (85, '04-305', '04-305', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (86, '04-401', '04-401', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (87, '04-402', '04-402', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (88, '04-403', '04-403', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (99, '04-404', '04-404', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (100, '04-405', '04-405', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (101, '04-501', '04-501', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (102, '04-502', '04-502', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (103, '04-503', '04-503', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (104, '04-504', '04-504', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (105, '04-505', '04-505', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (106, '05-101', '05-101', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (107, '05-102', '05-102', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (108, '05-103', '05-103', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (109, '05-104', '05-104', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (110, '05-105', '05-105', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (111, '05-201', '05-201', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (112, '05-202', '05-202', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (113, '05-203', '05-203', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (114, '05-204', '05-204', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (115, '05-205', '05-205', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (116, '05-301', '05-301', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (117, '05-302', '05-302', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (118, '05-303', '05-303', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (119, '05-304', '05-304', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (120, '05-305', '05-305', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (121, '05-401', '05-401', '05', 50, '01', '', 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (122, '05-402', '05-402', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (123, '05-403', '05-403', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (124, '05-404', '05-404', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (125, '05-405', '05-405', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (126, '05-501', '05-501', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (127, '05-502', '05-502', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (128, '05-503', '05-503', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (129, '05-504', '05-504', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (130, '05-505', '05-505', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (133, '12-101', '12-101', '12', 120, '04', '体育楼', 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (134, '12-102', '12-102', '12', 120, '04', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (135, '12-103', '12-103', '12', 120, '04', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (136, '12-104', '12-104', '12', 120, '04', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (137, '12-201', '12-201', '12', 120, '04', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (138, '12-202', '12-202', '12', 120, '04', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (139, '12-203', '12-203', '12', 120, '04', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (140, '12-204', '12-204', '12', 120, '04', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (141, '08-101', '08-101', '08', 50, '03', '实验楼', 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (142, '08-102', '08-102', '08', 50, '03', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (143, '08-103', '08-103', '08', 50, '03', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (144, '08-104', '08-104', '08', 50, '03', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (145, '08-105', '08-105', '08', 50, '03', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (146, '08-201', '08-201', '08', 50, '03', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (147, '08-202', '08-202', '08', 50, '03', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (148, '08-203', '08-203', '08', 50, '03', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (149, '08-204', '08-204', '08', 50, '03', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (150, '08-205', '08-205', '08', 50, '03', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (151, '08-301', '08-301', '08', 50, '03', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (152, '08-302', '08-302', '08', 50, '03', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (153, '08-303', '08-303', '08', 50, '03', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (154, '08-304', '08-304', '08', 50, '03', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` (`id`, `classroom_no`, `classroom_name`, `teachbuild_no`, `capacity`, `attr`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (155, '08-305', '08-305', '08', 50, '03', NULL, 0, NULL, NULL);
COMMIT;

-- ----------------------------
-- Table structure for tb_course_info
-- ----------------------------
DROP TABLE IF EXISTS `tb_course_info`;
CREATE TABLE `tb_course_info` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `course_no` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '课程编号',
  `course_name` varchar(36) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '课程名',
  `course_attr` varchar(2) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '课程属性',
  `publisher` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '出版社',
  `status` tinyint(1) DEFAULT NULL COMMENT '课程状态',
  `piority` int DEFAULT NULL COMMENT '优先级',
  `remark` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '备注',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=76 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of tb_course_info
-- ----------------------------
BEGIN;
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (1, '100001', '高一语文必修1', '01', '桂电出版社', NULL, NULL, '测试添加', 0, NULL, '2020-05-02 20:59:58');
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (2, '100002', '高一语文必修2', '01', '清华大学出版社', NULL, NULL, '衡水中学使用教材', 0, NULL, '2020-05-02 21:01:07');
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (3, '100003', '高一数学1', '01', '北京大学出版社', NULL, NULL, '谭老师授课', 0, NULL, '2020-05-02 21:03:13');
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (4, '100004', '高一物理1', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (5, '100005', '高二语文必修5', '01', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (6, '100006', '高三语文必修6', '01', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (7, '100007', '高一地理必修1', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (8, '100008', '高一地理必修2', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (9, '100009', '高二地理必修3', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (10, '100010', '高三地理选修1', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (11, '100011', '高三地理选修2', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (12, '100012', '高三地理选修3', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (13, '100013', '高三地理选修4', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (14, '100014', '高一化学必修1', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (15, '100015', '高一化学必修2', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (16, '100016', '高二化学选修1', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (17, '100017', '高二化学选修2', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (18, '100018', '高三化学选修3', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (19, '100019', '高三化学选修4', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (20, '100020', '高三化学选修5', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (21, '100021', '高一历史必修1', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (22, '100022', '高一历史必修2', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (23, '100023', '高二历史必修3', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (24, '100024', '高二历史选修1', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (25, '100025', '高三历史选修2', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (26, '100026', '高三历史选修3', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (27, '100027', '高一生物必修1：分子与细胞', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (28, '100028', '高一生物必修2：遗传与进化', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (29, '100029', '高二生物必修3：稳态与环境', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (30, '100030', '高二生物选修1：生物技术与实践', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (31, '100031', '高三生物选修2：生物科学与社会', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (32, '100032', '高三生物选修3：现代生物科技专题', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (33, '100033', '高一数学必修1', '01', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (34, '100034', '高一数学必修2', '01', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (35, '100035', '高二数学必修3', '01', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (36, '100036', '高二数学必须4', '01', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (37, '100037', '高二数学选修1-2', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (38, '100038', '高二数学选修2-1', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (39, '100039', '高二数学选修2-2', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (40, '100040 ', '高三数学必修5', '01', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (41, '100041', '高一思想政治必修1', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (42, '100042', '高一思想政治必修2', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (43, '100043', '高二思想政治必修3', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (44, '100044', '高二思想政治必修4', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (45, '100045', '高三思想政治选修1', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (46, '100046', '高三思想政治选修2', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (47, '100047', '高三思想政治选修3', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (48, '100048', '高一物理必修1', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (49, '100049', '高一物理必修2', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (50, '100050', '高二物理选修1-1', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (51, '100051', '体育课', '04', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (52, '100052 ', '高一物理必修2', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (53, '100053', '音乐课1', '05', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (54, '100054', '高一物理必修2', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (55, '100055', '舞蹈课1', '05', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (56, '100056', '高一英语必修1', '01', '高一出版社', NULL, NULL, NULL, 0, NULL, '2020-05-02 22:46:51');
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (57, '100057', '高一英语必修2', '01', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (58, '100058', '高二英语必修3', '01', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (59, '100059', '高二英语必修4', '01', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (60, '100060', '高三英语选修1', '01', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (61, '100061', '高三英语选修2', '01', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (62, '100062', '信息与技术1', '03', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (63, '100063', '信息与技术2', '03', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (64, '100064', '音乐课2', '03', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (65, '100065', '舞蹈课2', '05', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (66, '100066', '物理实验', '03', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (67, '100067', '化学实验', '03', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (68, '100069', '测试课程', '02', '测试测试', NULL, NULL, '测试添加', 0, '2020-06-03 21:51:14', NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (69, '100070', '语文', '01', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (70, '100071', '数学', '01', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (71, '100072', '英语', '01', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (72, '100073', '化学', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (73, '100074', '政治', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (74, '100075', '地理', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` (`id`, `course_no`, `course_name`, `course_attr`, `publisher`, `status`, `piority`, `remark`, `deleted`, `create_time`, `update_time`) VALUES (75, '100076', '生物', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
COMMIT;

-- ----------------------------
-- Table structure for tb_course_plan
-- ----------------------------
DROP TABLE IF EXISTS `tb_course_plan`;
CREATE TABLE `tb_course_plan` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `grade_no` varchar(2) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '年级编号',
  `class_no` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '班级编号',
  `course_no` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '课程编号',
  `teacher_no` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '讲师编号',
  `classroom_no` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '教室编号',
  `class_time` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '上课时间',
  `weeks_sum` int DEFAULT NULL COMMENT '周数',
  `semester` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '学期',
  `deleted` tinyint(1) DEFAULT '0',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=118 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of tb_course_plan
-- ----------------------------
BEGIN;
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (1, '01', '20200105', '100001', '10029', '01-401', '06', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (2, '01', '20200105', '100001', '10029', '01-202', '16', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (3, '01', '20200105', '100001', '10029', '01-402', '14', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (4, '01', '20200105', '100003', '10033', '01-304', '05', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (5, '01', '20200105', '100003', '10033', '01-403', '22', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (6, '01', '20200105', '100003', '10033', '01-104', '10', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (7, '01', '20200105', '100056', '10020', '01-104', '04', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (8, '01', '20200105', '100056', '10020', '01-302', '21', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (9, '01', '20200105', '100056', '10020', '01-103', '12', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (10, '01', '20200105', '100004', '10035', '01-405', '07', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (11, '01', '20200105', '100004', '10035', '01-305', '18', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (12, '01', '20200105', '100015', '10039', '01-203', '25', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (13, '01', '20200105', '100015', '10039', '01-105', '03', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (14, '01', '20200105', '100028', '10038', '01-402', '19', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (15, '01', '20200105', '100028', '10038', '01-303', '24', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (16, '01', '20200105', '100022', '10036', '01-502', '02', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (17, '01', '20200105', '100022', '10036', '01-203', '20', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (18, '01', '20200105', '100008', '10031', '01-505', '13', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (19, '01', '20200105', '100008', '10031', '01-103', '08', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (20, '01', '20200105', '100042', '10040', '01-501', '23', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (21, '01', '20200105', '100042', '10040', '01-502', '17', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (22, '01', '20200105', '100062', '10039', '08-304', '09', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (23, '01', '20200105', '100051', '10041', '12-201', '01', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (24, '01', '20200105', '100066', '10025', '08-304', '15', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (25, '01', '20200105', '100067', '10023', '08-302', '11', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (26, '01', '20200104', '100001', '10029', '01-503', '22', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (27, '01', '20200104', '100001', '10029', '01-103', '10', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (28, '01', '20200104', '100001', '10029', '01-204', '03', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (29, '01', '20200104', '100003', '10033', '01-503', '21', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (30, '01', '20200104', '100003', '10033', '01-201', '23', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (31, '01', '20200104', '100003', '10033', '01-205', '25', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (32, '01', '20200104', '100056', '10020', '01-405', '11', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (33, '01', '20200104', '100056', '10020', '01-101', '19', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (34, '01', '20200104', '100056', '10020', '01-202', '08', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (35, '01', '20200104', '100004', '10035', '01-101', '24', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (36, '01', '20200104', '100004', '10035', '01-105', '17', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (37, '01', '20200104', '100015', '10039', '01-404', '13', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (38, '01', '20200104', '100015', '10039', '01-504', '05', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (39, '01', '20200104', '100028', '10038', '01-101', '15', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (40, '01', '20200104', '100028', '10038', '01-304', '01', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (41, '01', '20200104', '100022', '10036', '01-505', '07', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (42, '01', '20200104', '100022', '10036', '01-403', '06', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (43, '01', '20200104', '100008', '10031', '01-205', '04', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (44, '01', '20200104', '100008', '10031', '01-403', '12', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (45, '01', '20200104', '100042', '10040', '01-203', '02', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (46, '01', '20200104', '100042', '10040', '01-405', '18', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (47, '01', '20200104', '100062', '10039', '08-203', '20', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (48, '01', '20200104', '100051', '10041', '12-204', '16', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (49, '01', '20200104', '100066', '10025', '08-303', '14', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (50, '01', '20200104', '100067', '10023', '08-205', '09', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (51, '01', '20200103', '100051', '10041', '12-102', '15', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (52, '01', '20200103', '100001', '10034', '01-301', '06', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (53, '01', '20200103', '100001', '10034', '01-104', '17', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (54, '01', '20200103', '100001', '10034', '01-303', '21', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (55, '01', '20200103', '100003', '10035', '01-402', '16', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (56, '01', '20200103', '100003', '10035', '01-301', '08', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (57, '01', '20200103', '100003', '10035', '01-202', '13', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (58, '01', '20200103', '100056', '10029', '01-501', '24', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (59, '01', '20200103', '100056', '10029', '01-503', '07', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (60, '01', '20200103', '100056', '10029', '01-304', '02', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (61, '01', '20200103', '100004', '10025', '01-103', '18', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (62, '01', '20200103', '100004', '10025', '01-303', '12', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (63, '01', '20200103', '100015', '10037', '01-202', '05', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (64, '01', '20200103', '100015', '10037', '01-505', '25', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (65, '01', '20200103', '100028', '10038', '01-405', '23', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (66, '01', '20200103', '100028', '10038', '01-201', '14', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (67, '01', '20200103', '100022', '10036', '01-403', '04', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (68, '01', '20200103', '100008', '10031', '01-104', '20', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (69, '01', '20200103', '100042', '10040', '01-504', '03', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (70, '01', '20200103', '100062', '10039', '08-202', '19', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (71, '01', '20200103', '100066', '10025', '08-201', '09', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (72, '01', '20200103', '100067', '10023', '08-301', '22', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (73, '01', '20200102', '100051', '10041', '12-204', '19', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (74, '01', '20200102', '100066', '10025', '08-205', '13', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (75, '01', '20200102', '100067', '10023', '08-302', '08', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (76, '01', '20200102', '100001', '10010', '01-401', '14', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (77, '01', '20200102', '100001', '10010', '01-203', '07', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (78, '01', '20200102', '100001', '10010', '01-203', '21', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (79, '01', '20200102', '100033', '10012', '01-305', '12', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (80, '01', '20200102', '100033', '10012', '01-504', '04', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (81, '01', '20200102', '100033', '10012', '01-401', '11', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (82, '01', '20200102', '100056', '10013', '01-202', '01', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (83, '01', '20200102', '100056', '10013', '01-104', '06', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (84, '01', '20200102', '100004', '10025', '01-204', '25', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (85, '01', '20200102', '100014', '10033', '01-302', '17', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (86, '01', '20200102', '100041', '10045', '01-201', '18', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (87, '01', '20200102', '100041', '10045', '01-305', '15', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (88, '01', '20200102', '100021', '10044', '01-302', '09', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (89, '01', '20200102', '100021', '10044', '01-204', '20', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (90, '01', '20200102', '100007', '10043', '01-305', '05', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (91, '01', '20200102', '100007', '10043', '01-105', '22', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (92, '01', '20200102', '100027', '10042', '01-205', '03', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (93, '01', '20200101', '100051', '10041', '12-201', '05', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (94, '01', '20200101', '100066', '10025', '08-302', '06', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (95, '01', '20200101', '100066', '10025', '08-205', '02', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (96, '01', '20200101', '100067', '10023', '08-303', '19', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (97, '01', '20200101', '100001', '10010', '01-205', '23', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (98, '01', '20200101', '100001', '10010', '01-205', '17', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (99, '01', '20200101', '100033', '10012', '01-504', '22', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (100, '01', '20200101', '100033', '10012', '01-201', '01', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (101, '01', '20200101', '100033', '10012', '01-502', '18', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (102, '01', '20200101', '100056', '10013', '01-105', '16', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (103, '01', '20200101', '100056', '10013', '01-102', '07', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (104, '01', '20200101', '100056', '10013', '01-502', '09', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (105, '01', '20200101', '100004', '10025', '01-501', '08', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (106, '01', '20200101', '100004', '10025', '01-305', '03', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (107, '01', '20200101', '100004', '10025', '01-203', '10', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (108, '01', '20200101', '100014', '10033', '01-203', '24', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (109, '01', '20200101', '100014', '10033', '01-302', '13', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (110, '01', '20200101', '100041', '10045', '01-503', '20', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (111, '01', '20200101', '100041', '10045', '01-501', '25', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (112, '01', '20200101', '100021', '10044', '01-405', '12', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (113, '01', '20200101', '100021', '10044', '01-404', '04', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (114, '01', '20200101', '100007', '10043', '01-305', '11', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (115, '01', '20200101', '100007', '10043', '01-503', '15', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (116, '01', '20200101', '100027', '10042', '01-403', '14', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` (`id`, `grade_no`, `class_no`, `course_no`, `teacher_no`, `classroom_no`, `class_time`, `weeks_sum`, `semester`, `deleted`, `create_time`, `update_time`) VALUES (117, '01', '20200101', '100027', '10042', '01-101', '21', NULL, '2019-2020-1', 0, NULL, NULL);
COMMIT;

-- ----------------------------
-- Table structure for tb_grade_info
-- ----------------------------
DROP TABLE IF EXISTS `tb_grade_info`;
CREATE TABLE `tb_grade_info` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id,年级表',
  `grade_no` varchar(2) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '年级编号',
  `grade_name` varchar(36) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '年级名称',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of tb_grade_info
-- ----------------------------
BEGIN;
INSERT INTO `tb_grade_info` (`id`, `grade_no`, `grade_name`) VALUES (1, '01', '高一');
INSERT INTO `tb_grade_info` (`id`, `grade_no`, `grade_name`) VALUES (2, '02', '高二');
INSERT INTO `tb_grade_info` (`id`, `grade_no`, `grade_name`) VALUES (3, '03', '高三');
COMMIT;

-- ----------------------------
-- Table structure for tb_location_info
-- ----------------------------
DROP TABLE IF EXISTS `tb_location_info`;
CREATE TABLE `tb_location_info` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id,位置信息，高一在哪栋楼，高二在哪',
  `teachbuild_no` varchar(4) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '教学楼编号,放教学楼表中编号',
  `grade_no` varchar(2) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '年级编号,放年级表中的id',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of tb_location_info
-- ----------------------------
BEGIN;
INSERT INTO `tb_location_info` (`id`, `teachbuild_no`, `grade_no`, `deleted`, `create_time`, `update_time`) VALUES (1, '01', '01', 0, NULL, NULL);
INSERT INTO `tb_location_info` (`id`, `teachbuild_no`, `grade_no`, `deleted`, `create_time`, `update_time`) VALUES (2, '02', '02', 0, NULL, NULL);
INSERT INTO `tb_location_info` (`id`, `teachbuild_no`, `grade_no`, `deleted`, `create_time`, `update_time`) VALUES (15, '03', '03', 1, '2020-06-01 23:31:44', NULL);
INSERT INTO `tb_location_info` (`id`, `teachbuild_no`, `grade_no`, `deleted`, `create_time`, `update_time`) VALUES (16, '03', '03', 1, '2020-06-01 23:38:11', NULL);
INSERT INTO `tb_location_info` (`id`, `teachbuild_no`, `grade_no`, `deleted`, `create_time`, `update_time`) VALUES (17, '03', '03', 0, '2020-06-01 23:39:52', NULL);
COMMIT;

-- ----------------------------
-- Table structure for tb_student
-- ----------------------------
DROP TABLE IF EXISTS `tb_student`;
CREATE TABLE `tb_student` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '学生id',
  `student_no` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '学号，可以用于登录',
  `username` varchar(36) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '昵称，可以用于登录',
  `password` varchar(36) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '密码',
  `realname` varchar(36) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '真实姓名',
  `user_type` tinyint(1) NOT NULL DEFAULT '3' COMMENT '标记用户类型3',
  `grade` varchar(2) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '年级',
  `class_no` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '所在班级',
  `age` int DEFAULT NULL COMMENT '年龄',
  `address` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '当前住址',
  `telephone` varchar(36) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '联系电话',
  `email` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '头像',
  `description` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '签名',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除',
  `status` tinyint(1) DEFAULT '0' COMMENT '账号状态,0为正常，1为封禁',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of tb_student
-- ----------------------------
BEGIN;
INSERT INTO `tb_student` (`id`, `student_no`, `username`, `password`, `realname`, `user_type`, `grade`, `class_no`, `age`, `address`, `telephone`, `email`, `avatar`, `description`, `deleted`, `status`, `create_time`, `update_time`) VALUES (1, '2020011234', 'lequal', '123456', '梁同学', 3, '高三', '20200101', 18, '广西桂林市桂林电子科技大学附属中学', '13677731236', 'course@guet.com', NULL, '按时上课', 0, 0, '2020-02-24 10:24:58', '2020-03-06 10:25:04');
INSERT INTO `tb_student` (`id`, `student_no`, `username`, `password`, `realname`, `user_type`, `grade`, `class_no`, `age`, `address`, `telephone`, `email`, `avatar`, `description`, `deleted`, `status`, `create_time`, `update_time`) VALUES (2, '2019021541', 'litongxue', '123456', '李同学', 3, '高二', '20200203', 22, '广西桂林市', '15177989514', 'course@guet.com', NULL, '好好学习', 0, 0, '2020-03-10 20:51:26', '2020-04-06 14:32:22');
INSERT INTO `tb_student` (`id`, `student_no`, `username`, `password`, `realname`, `user_type`, `grade`, `class_no`, `age`, `address`, `telephone`, `email`, `avatar`, `description`, `deleted`, `status`, `create_time`, `update_time`) VALUES (3, '2020031235', 'gantongxue', '123456', '甘同学', 3, '高三', '20200302', 19, '湖北省武汉市', '17007891233', 'course@guet.com', NULL, '天天向上', 0, 1, '2020-03-07 20:52:17', NULL);
INSERT INTO `tb_student` (`id`, `student_no`, `username`, `password`, `realname`, `user_type`, `grade`, `class_no`, `age`, `address`, `telephone`, `email`, `avatar`, `description`, `deleted`, `status`, `create_time`, `update_time`) VALUES (4, '2020012589', 'xitongxue', '123456', '喜同学', 3, '高一', '20200103', 21, '广东省珠海市', '13677731456', 'course@guet.com', NULL, '找工作中', 0, 0, NULL, NULL);
INSERT INTO `tb_student` (`id`, `student_no`, `username`, `password`, `realname`, `user_type`, `grade`, `class_no`, `age`, `address`, `telephone`, `email`, `avatar`, `description`, `deleted`, `status`, `create_time`, `update_time`) VALUES (5, '2020017895', 'huangtongxue', '123456', '黄同学', 3, '高一', '20200101', 20, '广西钦州市', '17689541452', 'course@guet.com', NULL, '技术强才是真的强', 0, 1, NULL, NULL);
INSERT INTO `tb_student` (`id`, `student_no`, `username`, `password`, `realname`, `user_type`, `grade`, `class_no`, `age`, `address`, `telephone`, `email`, `avatar`, `description`, `deleted`, `status`, `create_time`, `update_time`) VALUES (6, '2020017836', 'caitongxue', '123456', '蔡同学', 3, '高一', '20200104', 18, '广西玉林', '18574562587', 'course@guet.com', NULL, '是时候好好学习了', 0, 0, NULL, NULL);
INSERT INTO `tb_student` (`id`, `student_no`, `username`, `password`, `realname`, `user_type`, `grade`, `class_no`, `age`, `address`, `telephone`, `email`, `avatar`, `description`, `deleted`, `status`, `create_time`, `update_time`) VALUES (7, '2020021936', 'suntongxue', '123456', '孙同学', 3, '高二', '20200201', 17, '湖南长沙', '18648983826', 'course@guet.com', NULL, '加油咯', 0, 0, NULL, NULL);
INSERT INTO `tb_student` (`id`, `student_no`, `username`, `password`, `realname`, `user_type`, `grade`, `class_no`, `age`, `address`, `telephone`, `email`, `avatar`, `description`, `deleted`, `status`, `create_time`, `update_time`) VALUES (8, '2020031245', 'hutongxue', '123456', '胡同学', 3, '高三', '20200301', 19, '湖北十堰', '17505127841', 'course@guet.com', NULL, '嘿嘿', 0, 0, NULL, NULL);
INSERT INTO `tb_student` (`id`, `student_no`, `username`, `password`, `realname`, `user_type`, `grade`, `class_no`, `age`, `address`, `telephone`, `email`, `avatar`, `description`, `deleted`, `status`, `create_time`, `update_time`) VALUES (9, '2020031278', 'litongxue2', '123456', '黎同学', 3, '高三', '20200302', 17, '安徽省', '13412596654', 'course@guet.com', NULL, '做喜欢做的事', 0, 0, NULL, NULL);
INSERT INTO `tb_student` (`id`, `student_no`, `username`, `password`, `realname`, `user_type`, `grade`, `class_no`, `age`, `address`, `telephone`, `email`, `avatar`, `description`, `deleted`, `status`, `create_time`, `update_time`) VALUES (10, '2020014596', 'shitongxue', '123456', '史同学', 3, '高一', '20200103', 18, '广西贵港', '13644527789', 'course@guet.com', NULL, '高一的同学', 0, 0, NULL, NULL);
INSERT INTO `tb_student` (`id`, `student_no`, `username`, `password`, `realname`, `user_type`, `grade`, `class_no`, `age`, `address`, `telephone`, `email`, `avatar`, `description`, `deleted`, `status`, `create_time`, `update_time`) VALUES (11, '2020024567', 'xiaotongxue', '123456', '萧同学', 3, '高二', '20200202', 17, '广东珠海', '13677735445', 'course@guet.com', NULL, '高二同学', 0, 0, NULL, NULL);
INSERT INTO `tb_student` (`id`, `student_no`, `username`, `password`, `realname`, `user_type`, `grade`, `class_no`, `age`, `address`, `telephone`, `email`, `avatar`, `description`, `deleted`, `status`, `create_time`, `update_time`) VALUES (12, '2020021456', 'student', '123456', '谭同学', 3, '高二', '20200204', 19, '广东汕头', '13677735559', 'course@guet.com', NULL, '高二8班', 0, 0, NULL, NULL);
INSERT INTO `tb_student` (`id`, `student_no`, `username`, `password`, `realname`, `user_type`, `grade`, `class_no`, `age`, `address`, `telephone`, `email`, `avatar`, `description`, `deleted`, `status`, `create_time`, `update_time`) VALUES (13, '2020034123', 'studens', '123456', '王同学', 3, '高三', '20200302', 22, '广东深圳', '17007895623', 'course@guet.com', NULL, NULL, 0, 0, NULL, '2020-05-02 21:44:02');
INSERT INTO `tb_student` (`id`, `student_no`, `username`, `password`, `realname`, `user_type`, `grade`, `class_no`, `age`, `address`, `telephone`, `email`, `avatar`, `description`, `deleted`, `status`, `create_time`, `update_time`) VALUES (14, '2020035468', 'liangyike', '123456', '梁同学', 3, '高三', '20200301', 20, '广西桂林市', '13677731235', 'course@guet.com', NULL, NULL, 0, 0, '2020-03-26 22:57:04', '2020-04-06 14:52:37');
INSERT INTO `tb_student` (`id`, `student_no`, `username`, `password`, `realname`, `user_type`, `grade`, `class_no`, `age`, `address`, `telephone`, `email`, `avatar`, `description`, `deleted`, `status`, `create_time`, `update_time`) VALUES (15, '2020016788', 'xiewutong', '123456', '谢童鞋', 3, '高一', '20200104', 16, '湖南省衡阳市', '15177959816', 'course@guet.com', NULL, NULL, 0, 0, '2020-03-26 23:15:06', NULL);
INSERT INTO `tb_student` (`id`, `student_no`, `username`, `password`, `realname`, `user_type`, `grade`, `class_no`, `age`, `address`, `telephone`, `email`, `avatar`, `description`, `deleted`, `status`, `create_time`, `update_time`) VALUES (16, '2020024182', 'qintongxue', '123456', '覃同学', 3, '高二', '20200203', 17, '广西桂平市', '18565412563', 'course@guet.com', NULL, NULL, 0, 0, '2020-03-26 23:18:02', NULL);
INSERT INTO `tb_student` (`id`, `student_no`, `username`, `password`, `realname`, `user_type`, `grade`, `class_no`, `age`, `address`, `telephone`, `email`, `avatar`, `description`, `deleted`, `status`, `create_time`, `update_time`) VALUES (17, '2020028242', 'lian', '123456', '梁先生', 3, '高二', '20200201', 17, '广东中山', '17585968745', 'course@guet.com', NULL, NULL, 0, 0, '2020-03-26 23:20:29', '2020-05-02 21:44:19');
INSERT INTO `tb_student` (`id`, `student_no`, `username`, `password`, `realname`, `user_type`, `grade`, `class_no`, `age`, `address`, `telephone`, `email`, `avatar`, `description`, `deleted`, `status`, `create_time`, `update_time`) VALUES (18, '2020038300', 'jiangtongxue', '123456', '蒋同学', 3, '高三', '20200305', 18, '广东省佛山市', '13596857412', 'jiang@guet.com', NULL, NULL, 0, 0, '2020-03-26 23:25:11', '2020-04-06 14:49:22');
INSERT INTO `tb_student` (`id`, `student_no`, `username`, `password`, `realname`, `user_type`, `grade`, `class_no`, `age`, `address`, `telephone`, `email`, `avatar`, `description`, `deleted`, `status`, `create_time`, `update_time`) VALUES (19, '2020027623', 'ganxiansheng', '123456', '甘先生', 3, '高二', '20200205', 17, '广西百色', '17015789654', 'course@guet.com', NULL, NULL, 0, 0, '2020-03-26 23:26:20', NULL);
INSERT INTO `tb_student` (`id`, `student_no`, `username`, `password`, `realname`, `user_type`, `grade`, `class_no`, `age`, `address`, `telephone`, `email`, `avatar`, `description`, `deleted`, `status`, `create_time`, `update_time`) VALUES (20, '2020027807', '2020027623', '123456', '王总', 3, '高二', '20200205', 17, '上海市', '15678415241', 'course@guet.com', NULL, NULL, 0, 0, '2020-03-26 23:27:40', '2020-06-01 10:20:42');
INSERT INTO `tb_student` (`id`, `student_no`, `username`, `password`, `realname`, `user_type`, `grade`, `class_no`, `age`, `address`, `telephone`, `email`, `avatar`, `description`, `deleted`, `status`, `create_time`, `update_time`) VALUES (21, '2020022351', '小幸运', '123456', '陈奕迅', 3, '高二', '20200101', 18, '广东省珠海市金湾区', '15177959814', NULL, NULL, NULL, 0, 0, '2020-05-20 17:11:25', NULL);
INSERT INTO `tb_student` (`id`, `student_no`, `username`, `password`, `realname`, `user_type`, `grade`, `class_no`, `age`, `address`, `telephone`, `email`, `avatar`, `description`, `deleted`, `status`, `create_time`, `update_time`) VALUES (22, '2020035084', '马同学', '123456', '马东良', 3, '高三', '20200301', 19, '广西贵港市', '15177959814', 'ma.dl@qq.com', NULL, NULL, 0, 0, '2020-06-01 10:40:30', '2020-06-01 12:25:22');
INSERT INTO `tb_student` (`id`, `student_no`, `username`, `password`, `realname`, `user_type`, `grade`, `class_no`, `age`, `address`, `telephone`, `email`, `avatar`, `description`, `deleted`, `status`, `create_time`, `update_time`) VALUES (23, '2020011062', '黄华斌', 'aizai2015', '黄华', 3, '高一', NULL, NULL, '广西贵港市', '15678675545', NULL, NULL, NULL, 0, 0, '2020-06-01 10:54:42', NULL);
INSERT INTO `tb_student` (`id`, `student_no`, `username`, `password`, `realname`, `user_type`, `grade`, `class_no`, `age`, `address`, `telephone`, `email`, `avatar`, `description`, `deleted`, `status`, `create_time`, `update_time`) VALUES (24, '2020021158', '梁丙光', 'aizai2015', '梁丙光', 3, '高二', NULL, NULL, '广西', '15177959814', '1576070851@qq.com', NULL, NULL, 0, 0, '2020-06-01 10:57:45', NULL);
INSERT INTO `tb_student` (`id`, `student_no`, `username`, `password`, `realname`, `user_type`, `grade`, `class_no`, `age`, `address`, `telephone`, `email`, `avatar`, `description`, `deleted`, `status`, `create_time`, `update_time`) VALUES (25, '2020014949', 'registerTest', '123456', '我是测试用户', 3, '高一', NULL, NULL, '桂电', '13677731234', 'test@qq.com', NULL, NULL, 0, 0, '2020-06-06 18:16:27', NULL);
INSERT INTO `tb_student` (`id`, `student_no`, `username`, `password`, `realname`, `user_type`, `grade`, `class_no`, `age`, `address`, `telephone`, `email`, `avatar`, `description`, `deleted`, `status`, `create_time`, `update_time`) VALUES (26, '2024011300', '法外狂徒', '123456', '张三丰', 3, '高一', NULL, NULL, '放松放松', '1231344', ' 感受感受', NULL, NULL, 0, 0, '2024-04-09 14:09:32', NULL);
COMMIT;

-- ----------------------------
-- Table structure for tb_teach_build_info
-- ----------------------------
DROP TABLE IF EXISTS `tb_teach_build_info`;
CREATE TABLE `tb_teach_build_info` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id,教学楼信息表',
  `teach_build_no` varchar(4) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '教学楼编号',
  `teach_build_name` varchar(36) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '教学楼名称',
  `teach_build_location` varchar(36) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '教学楼位置',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of tb_teach_build_info
-- ----------------------------
BEGIN;
INSERT INTO `tb_teach_build_info` (`id`, `teach_build_no`, `teach_build_name`, `teach_build_location`, `deleted`, `create_time`, `update_time`) VALUES (1, '01', '第1教学楼', '东校区', 0, NULL, '2020-04-10 21:51:09');
INSERT INTO `tb_teach_build_info` (`id`, `teach_build_no`, `teach_build_name`, `teach_build_location`, `deleted`, `create_time`, `update_time`) VALUES (2, '02', '2号教学楼', '1校区', 0, NULL, NULL);
INSERT INTO `tb_teach_build_info` (`id`, `teach_build_no`, `teach_build_name`, `teach_build_location`, `deleted`, `create_time`, `update_time`) VALUES (3, '03', '3号教学楼', '1校区', 0, NULL, NULL);
INSERT INTO `tb_teach_build_info` (`id`, `teach_build_no`, `teach_build_name`, `teach_build_location`, `deleted`, `create_time`, `update_time`) VALUES (4, '04', '4号教学楼', '1校区', 0, NULL, NULL);
INSERT INTO `tb_teach_build_info` (`id`, `teach_build_no`, `teach_build_name`, `teach_build_location`, `deleted`, `create_time`, `update_time`) VALUES (5, '05', '5号教学楼', '1校区', 0, NULL, NULL);
INSERT INTO `tb_teach_build_info` (`id`, `teach_build_no`, `teach_build_name`, `teach_build_location`, `deleted`, `create_time`, `update_time`) VALUES (6, '06', '音乐楼', '1校区', 0, NULL, NULL);
INSERT INTO `tb_teach_build_info` (`id`, `teach_build_no`, `teach_build_name`, `teach_build_location`, `deleted`, `create_time`, `update_time`) VALUES (7, '07', '美术楼', '1校区', 0, NULL, NULL);
INSERT INTO `tb_teach_build_info` (`id`, `teach_build_no`, `teach_build_name`, `teach_build_location`, `deleted`, `create_time`, `update_time`) VALUES (8, '08', '实验楼1', '1校区', 0, NULL, NULL);
INSERT INTO `tb_teach_build_info` (`id`, `teach_build_no`, `teach_build_name`, `teach_build_location`, `deleted`, `create_time`, `update_time`) VALUES (9, '09', '实验楼2', '1校区', 0, NULL, NULL);
INSERT INTO `tb_teach_build_info` (`id`, `teach_build_no`, `teach_build_name`, `teach_build_location`, `deleted`, `create_time`, `update_time`) VALUES (10, '10', '逸夫楼1', '1校区', 0, NULL, NULL);
INSERT INTO `tb_teach_build_info` (`id`, `teach_build_no`, `teach_build_name`, `teach_build_location`, `deleted`, `create_time`, `update_time`) VALUES (11, '11', '逸夫楼2', '1校区', 0, NULL, NULL);
INSERT INTO `tb_teach_build_info` (`id`, `teach_build_no`, `teach_build_name`, `teach_build_location`, `deleted`, `create_time`, `update_time`) VALUES (12, '12', '体育楼', '1校区', 0, NULL, NULL);
INSERT INTO `tb_teach_build_info` (`id`, `teach_build_no`, `teach_build_name`, `teach_build_location`, `deleted`, `create_time`, `update_time`) VALUES (13, '13', '化生楼', '1校区', 0, NULL, NULL);
INSERT INTO `tb_teach_build_info` (`id`, `teach_build_no`, `teach_build_name`, `teach_build_location`, `deleted`, `create_time`, `update_time`) VALUES (14, '14', '14号教学楼', '2校区', 0, '2020-03-23 00:05:03', NULL);
INSERT INTO `tb_teach_build_info` (`id`, `teach_build_no`, `teach_build_name`, `teach_build_location`, `deleted`, `create_time`, `update_time`) VALUES (15, '20', '测试楼', '花江校区', 0, '2020-06-02 11:58:16', NULL);
COMMIT;

-- ----------------------------
-- Table structure for tb_teacher
-- ----------------------------
DROP TABLE IF EXISTS `tb_teacher`;
CREATE TABLE `tb_teacher` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id，讲师表',
  `teacher_no` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '教师编号',
  `username` varchar(36) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '昵称（用户名）',
  `password` varchar(36) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '密码',
  `realname` varchar(36) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '真实姓名',
  `user_type` tinyint(1) NOT NULL DEFAULT '2' COMMENT '用户类型',
  `jobtitle` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '职称',
  `grade_no` varchar(2) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '所属年级',
  `license` varchar(1000) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '证件照(地址)',
  `teach` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '教授科目',
  `age` int DEFAULT NULL COMMENT '年龄',
  `telephone` varchar(36) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '联系电话',
  `email` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '电子邮件',
  `address` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '地址',
  `avatar` varchar(500) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '头像',
  `description` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '描述（签名）',
  `power` tinyint(1) DEFAULT '1' COMMENT '操作权限',
  `piority` int DEFAULT NULL COMMENT '优先级',
  `status` tinyint(1) DEFAULT '0' COMMENT '账号状态',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8mb3 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of tb_teacher
-- ----------------------------
BEGIN;
INSERT INTO `tb_teacher` (`id`, `teacher_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `grade_no`, `license`, `teach`, `age`, `telephone`, `email`, `address`, `avatar`, `description`, `power`, `piority`, `status`, `deleted`, `create_time`, `update_time`) VALUES (1, '10010', 'lequal(梁老师)', 'aizai2015', '梁晓明', 2, '教务处副主任', '01', NULL, '物理', 32, '13677731235', 'teacher@guet.com', '广西', NULL, '以身作则，教育好学生。', 1, 1, 0, 0, '2020-03-04 15:30:03', '2020-03-06 15:30:30');
INSERT INTO `tb_teacher` (`id`, `teacher_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `grade_no`, `license`, `teach`, `age`, `telephone`, `email`, `address`, `avatar`, `description`, `power`, `piority`, `status`, `deleted`, `create_time`, `update_time`) VALUES (3, '10012', 'msLi', 'lixuexue', '李雪雪', 2, '高级讲师', '01', NULL, '语文', 29, '13677731235', 'teacher@guet.com', '广西桂林市桂林电子科技大学', NULL, '做人民的好教师', 1, 2, 0, 0, '2020-03-06 23:39:39', '2020-03-06 23:39:39');
INSERT INTO `tb_teacher` (`id`, `teacher_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `grade_no`, `license`, `teach`, `age`, `telephone`, `email`, `address`, `avatar`, `description`, `power`, `piority`, `status`, `deleted`, `create_time`, `update_time`) VALUES (5, '10013', 'mswang', '123456', '王小芳', 2, '初级讲师', '01', NULL, '英语', 25, '13677731235', 'teacher@guet.com', '湖南省', NULL, '过好每一天', 1, 3, 0, 0, '2020-03-04 19:45:44', '2020-03-08 19:45:51');
INSERT INTO `tb_teacher` (`id`, `teacher_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `grade_no`, `license`, `teach`, `age`, `telephone`, `email`, `address`, `avatar`, `description`, `power`, `piority`, `status`, `deleted`, `create_time`, `update_time`) VALUES (6, '10014', 'mssun', '123456', '孙晓明', 2, '中级讲师', '01', NULL, '数学', 28, '13677731235', 'teacher@guet.com', '湖北省', NULL, '加油', 1, 2, 0, 0, '2020-03-06 19:47:11', '2020-03-30 19:47:14');
INSERT INTO `tb_teacher` (`id`, `teacher_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `grade_no`, `license`, `teach`, `age`, `telephone`, `email`, `address`, `avatar`, `description`, `power`, `piority`, `status`, `deleted`, `create_time`, `update_time`) VALUES (7, '10015', 'msming', '123456', '孙振东', 2, '实习生', '01', NULL, '化学', 22, '13677731235', 'teacher@guet.com', '江苏省', NULL, 'welcome', 1, 4, 0, 0, '2020-03-05 19:48:40', '2020-03-06 19:48:45');
INSERT INTO `tb_teacher` (`id`, `teacher_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `grade_no`, `license`, `teach`, `age`, `telephone`, `email`, `address`, `avatar`, `description`, `power`, `piority`, `status`, `deleted`, `create_time`, `update_time`) VALUES (8, '10016', 'mstan', '123456', '谭老师', 2, '初级讲师', '01', NULL, '英语', 23, '13677731235', 'teacher@guet.com', '广西', NULL, 'come on', 1, 3, 0, 0, NULL, NULL);
INSERT INTO `tb_teacher` (`id`, `teacher_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `grade_no`, `license`, `teach`, `age`, `telephone`, `email`, `address`, `avatar`, `description`, `power`, `piority`, `status`, `deleted`, `create_time`, `update_time`) VALUES (9, '10017', 'msliang', '123456', '梁老师', 2, '高级讲师', '01', NULL, '语文', 32, '13677731235', 'teacher@guet.com', '河北', NULL, '做更好的自己', 1, 1, 0, 0, NULL, NULL);
INSERT INTO `tb_teacher` (`id`, `teacher_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `grade_no`, `license`, `teach`, `age`, `telephone`, `email`, `address`, `avatar`, `description`, `power`, `piority`, `status`, `deleted`, `create_time`, `update_time`) VALUES (10, '10018', 'mrliang', '123456', '梁忠诚', 2, '高级讲师', '01', NULL, '数学', 35, '13677731235', 'teacher@guet.com', '湖北', NULL, '数学好', 1, 1, 0, 0, NULL, NULL);
INSERT INTO `tb_teacher` (`id`, `teacher_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `grade_no`, `license`, `teach`, `age`, `telephone`, `email`, `address`, `avatar`, `description`, `power`, `piority`, `status`, `deleted`, `create_time`, `update_time`) VALUES (11, '10019', 'mrwang', '123456', '汪莉莉', 2, '中级讲师', '01', NULL, '地理', 33, '13677731235', 'teacher@guet.com', '河北', NULL, '地理好', 1, 2, 0, 0, NULL, NULL);
INSERT INTO `tb_teacher` (`id`, `teacher_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `grade_no`, `license`, `teach`, `age`, `telephone`, `email`, `address`, `avatar`, `description`, `power`, `piority`, `status`, `deleted`, `create_time`, `update_time`) VALUES (12, '10020', 'mshu', '123456', '胡冬梅', 2, '中级讲师', '01', NULL, '化学', 46, '13677731235', 'teacher@guet.com', '搜索', NULL, '是', 1, 2, 0, 0, NULL, NULL);
INSERT INTO `tb_teacher` (`id`, `teacher_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `grade_no`, `license`, `teach`, `age`, `telephone`, `email`, `address`, `avatar`, `description`, `power`, `piority`, `status`, `deleted`, `create_time`, `update_time`) VALUES (13, '10021', 'mrlin', '123456', '林俊杰', 2, '初级讲师', '01', NULL, '生物', 41, '13677731235', 'teacher@guet.com', '试试', NULL, '就', 1, 3, 0, 0, NULL, NULL);
INSERT INTO `tb_teacher` (`id`, `teacher_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `grade_no`, `license`, `teach`, `age`, `telephone`, `email`, `address`, `avatar`, `description`, `power`, `piority`, `status`, `deleted`, `create_time`, `update_time`) VALUES (14, '10022', 'mrzhou', '123456', '周杰伦', 2, '高级讲师', '01', NULL, '生物', 39, '13677731235', 'teacher@guet.com', '看看', NULL, '看', 1, 1, 0, 0, NULL, NULL);
INSERT INTO `tb_teacher` (`id`, `teacher_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `grade_no`, `license`, `teach`, `age`, `telephone`, `email`, `address`, `avatar`, `description`, `power`, `piority`, `status`, `deleted`, `create_time`, `update_time`) VALUES (15, '10023', 'mrwang', '123456', '张靓颖', 2, '初级讲师', '01', NULL, '历史', 33, '13677731235', 'teacher@guet.com', '55', NULL, '555', 1, 3, 0, 0, NULL, NULL);
INSERT INTO `tb_teacher` (`id`, `teacher_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `grade_no`, `license`, `teach`, `age`, `telephone`, `email`, `address`, `avatar`, `description`, `power`, `piority`, `status`, `deleted`, `create_time`, `update_time`) VALUES (16, '10024', 'mrhou', '123456', '侯德南', 2, '高级讲师', '01', NULL, '政治', 37, '13677731235', 'teacher@guet.com', '54546', NULL, '8878878', 1, 1, 0, 0, NULL, NULL);
INSERT INTO `tb_teacher` (`id`, `teacher_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `grade_no`, `license`, `teach`, `age`, `telephone`, `email`, `address`, `avatar`, `description`, `power`, `piority`, `status`, `deleted`, `create_time`, `update_time`) VALUES (17, '10025', 'mrzhang', '123456', '张德良', 2, '高级讲师', '01', NULL, '物理', 34, '13677731235', 'teacher@guet.com', '78788', '', '878755', 1, 1, 0, 0, NULL, NULL);
INSERT INTO `tb_teacher` (`id`, `teacher_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `grade_no`, `license`, `teach`, `age`, `telephone`, `email`, `address`, `avatar`, `description`, `power`, `piority`, `status`, `deleted`, `create_time`, `update_time`) VALUES (18, '10026', 'mrzhang', '123456', '张勇', 2, '中级讲师', '02', NULL, '数学', 45, '13677731235', 'teacher@guet.com', '湖南', NULL, '565675', 1, 2, 0, 0, NULL, NULL);
INSERT INTO `tb_teacher` (`id`, `teacher_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `grade_no`, `license`, `teach`, `age`, `telephone`, `email`, `address`, `avatar`, `description`, `power`, `piority`, `status`, `deleted`, `create_time`, `update_time`) VALUES (19, '10027', '马老师', '123456', '马晓东', 2, '初级讲师', '02', NULL, '语文', 28, '13677731235', 'teacher@guet.com', '海南', NULL, '78688787', 1, 3, 0, 0, NULL, '2020-04-11 14:33:58');
INSERT INTO `tb_teacher` (`id`, `teacher_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `grade_no`, `license`, `teach`, `age`, `telephone`, `email`, `address`, `avatar`, `description`, `power`, `piority`, `status`, `deleted`, `create_time`, `update_time`) VALUES (20, '10028', '马老师', '123456', '马芸', 2, '中级讲师', '02', NULL, '英语', 29, '13677731235', 'teacher@guet.com', '河北省邢台市', NULL, '5654', 1, 2, 0, 0, NULL, '2020-04-05 21:10:11');
INSERT INTO `tb_teacher` (`id`, `teacher_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `grade_no`, `license`, `teach`, `age`, `telephone`, `email`, `address`, `avatar`, `description`, `power`, `piority`, `status`, `deleted`, `create_time`, `update_time`) VALUES (21, '10029', '郑老师', '123456', '郑小红', 2, '高级讲师', '02', NULL, '生物', 32, '13677731235', 'teacher@guet.com', '河南', NULL, '768567', 1, 1, 0, 0, NULL, '2020-04-11 14:33:47');
INSERT INTO `tb_teacher` (`id`, `teacher_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `grade_no`, `license`, `teach`, `age`, `telephone`, `email`, `address`, `avatar`, `description`, `power`, `piority`, `status`, `deleted`, `create_time`, `update_time`) VALUES (22, '10030', '韦老师', '123456', '韦小龙', 2, '中级讲师', '02', NULL, '物理', 33, '13677731235', 'teacher@guet.com', '江苏', NULL, '6875675', 1, 2, 0, 0, NULL, '2020-04-11 14:30:24');
INSERT INTO `tb_teacher` (`id`, `teacher_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `grade_no`, `license`, `teach`, `age`, `telephone`, `email`, `address`, `avatar`, `description`, `power`, `piority`, `status`, `deleted`, `create_time`, `update_time`) VALUES (23, '10031', '张老师', '123456', '张小龙', 2, '高级讲师', '02', NULL, '化学', 35, '13677731235', 'teacher@guet.com', '福建', NULL, '6785675', 1, 1, 0, 0, NULL, '2020-04-11 14:29:48');
INSERT INTO `tb_teacher` (`id`, `teacher_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `grade_no`, `license`, `teach`, `age`, `telephone`, `email`, `address`, `avatar`, `description`, `power`, `piority`, `status`, `deleted`, `create_time`, `update_time`) VALUES (24, '10032', '谭老师', '123456', '谭晓江', 2, '高级讲师', '02', NULL, '历史', 33, '13677731235', 'teacher@guet.com', '贵州', NULL, '78678', 1, 1, 0, 0, NULL, '2020-04-11 14:29:29');
INSERT INTO `tb_teacher` (`id`, `teacher_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `grade_no`, `license`, `teach`, `age`, `telephone`, `email`, `address`, `avatar`, `description`, `power`, `piority`, `status`, `deleted`, `create_time`, `update_time`) VALUES (25, '10033', '韩老师', '123456', '韩云', 2, '高级讲师', '02', NULL, '政治', 32, '13677731235', 'teacher@guet.com', '新疆', NULL, '67767', 1, 1, 0, 0, NULL, '2020-04-11 14:29:20');
INSERT INTO `tb_teacher` (`id`, `teacher_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `grade_no`, `license`, `teach`, `age`, `telephone`, `email`, `address`, `avatar`, `description`, `power`, `piority`, `status`, `deleted`, `create_time`, `update_time`) VALUES (26, '10034', '韦老师', '123456', '韦雪琪', 2, '中级讲师', '02', NULL, '历史', 28, '13677731235', 'teacher@guet.com', '贵州省贵阳市', NULL, NULL, 1, NULL, 0, 0, NULL, '2020-04-05 21:00:36');
INSERT INTO `tb_teacher` (`id`, `teacher_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `grade_no`, `license`, `teach`, `age`, `telephone`, `email`, `address`, `avatar`, `description`, `power`, `piority`, `status`, `deleted`, `create_time`, `update_time`) VALUES (27, '10046', '黄老师', '123456', '黄继光', 2, '高级讲师', '02', 'https://arrange.oss-cn-shenzhen.aliyuncs.com/timg.jfif', '地理', 31, '13677731235', 'huang@guet.com', '西藏', NULL, '6756', 1, 1, 0, 0, NULL, '2020-04-11 14:29:00');
INSERT INTO `tb_teacher` (`id`, `teacher_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `grade_no`, `license`, `teach`, `age`, `telephone`, `email`, `address`, `avatar`, `description`, `power`, `piority`, `status`, `deleted`, `create_time`, `update_time`) VALUES (28, '10035', '张老师', '123456', '张三封', 2, '高级讲师', '03', NULL, '语文', 33, '13677731235', 'teacher@guet.com', '甘肃', NULL, '7567', 1, 1, 0, 0, NULL, '2020-04-11 14:28:44');
INSERT INTO `tb_teacher` (`id`, `teacher_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `grade_no`, `license`, `teach`, `age`, `telephone`, `email`, `address`, `avatar`, `description`, `power`, `piority`, `status`, `deleted`, `create_time`, `update_time`) VALUES (29, '10036', '胡老师', '123456', '胡小小', 2, '高级讲师', '03', NULL, '数学', 33, '13677731235', 'teacher@guet.com', '广西', NULL, '5675467', 1, 1, 0, 0, NULL, '2020-04-11 14:28:16');
INSERT INTO `tb_teacher` (`id`, `teacher_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `grade_no`, `license`, `teach`, `age`, `telephone`, `email`, `address`, `avatar`, `description`, `power`, `piority`, `status`, `deleted`, `create_time`, `update_time`) VALUES (30, '10037', '莫老师', '123456', '莫小新', 2, '高级讲师', '03', NULL, '英语', 33, '13677731235', 'teacher@guet.com', '河北石家庄市', NULL, '7867', 1, 1, 0, 0, NULL, '2020-04-11 14:26:18');
INSERT INTO `tb_teacher` (`id`, `teacher_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `grade_no`, `license`, `teach`, `age`, `telephone`, `email`, `address`, `avatar`, `description`, `power`, `piority`, `status`, `deleted`, `create_time`, `update_time`) VALUES (31, '10038', '甘老师', '123456', '甘楠', 2, '高级讲师', '03', NULL, '物理', 33, '13677731235', 'teacher@guet.com', '北京', NULL, '5644', 1, 1, 0, 0, NULL, '2020-04-05 20:59:08');
INSERT INTO `tb_teacher` (`id`, `teacher_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `grade_no`, `license`, `teach`, `age`, `telephone`, `email`, `address`, `avatar`, `description`, `power`, `piority`, `status`, `deleted`, `create_time`, `update_time`) VALUES (32, '10039', '江老师', '123456', '江晓东', 2, '高级讲师', '03', NULL, '化学', 40, '13677731235', 'teacher@guet.com', '广东省中山市', NULL, '22222', 1, 1, 0, 0, NULL, '2020-04-05 20:26:13');
INSERT INTO `tb_teacher` (`id`, `teacher_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `grade_no`, `license`, `teach`, `age`, `telephone`, `email`, `address`, `avatar`, `description`, `power`, `piority`, `status`, `deleted`, `create_time`, `update_time`) VALUES (33, '10040', '夏老师', '123456', '夏紫若', 2, '高级讲师', '03', NULL, '生物', 33, '13677731235', 'teacher@guet.com', '广东省深圳市', NULL, '6758', 1, 1, 0, 0, NULL, '2020-04-05 20:22:34');
INSERT INTO `tb_teacher` (`id`, `teacher_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `grade_no`, `license`, `teach`, `age`, `telephone`, `email`, `address`, `avatar`, `description`, `power`, `piority`, `status`, `deleted`, `create_time`, `update_time`) VALUES (34, '10041', '张老师', '123456', '张杰', 2, '高级讲师', '03', NULL, '政治', 31, '13677731235', 'teacher@guet.com', '上海', NULL, '999999', 1, 1, 0, 0, NULL, '2020-04-05 18:16:19');
INSERT INTO `tb_teacher` (`id`, `teacher_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `grade_no`, `license`, `teach`, `age`, `telephone`, `email`, `address`, `avatar`, `description`, `power`, `piority`, `status`, `deleted`, `create_time`, `update_time`) VALUES (35, '10042', '谭老师', '123456', '谭咏麟', 2, '高级讲师', '03', NULL, '历史', 32, '13677731235', 'teacher@guet.com', '天津', NULL, '5353', 1, 1, 0, 0, NULL, '2020-04-05 18:04:37');
INSERT INTO `tb_teacher` (`id`, `teacher_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `grade_no`, `license`, `teach`, `age`, `telephone`, `email`, `address`, `avatar`, `description`, `power`, `piority`, `status`, `deleted`, `create_time`, `update_time`) VALUES (36, '10043', '王老师', '123456', '王杰', 2, '高级讲师', '03', NULL, '地理', 33, '13677731235', 'teacher@guet.com', '湖北省武汉市', NULL, '543453', 1, 1, 0, 0, NULL, '2020-04-05 18:04:09');
INSERT INTO `tb_teacher` (`id`, `teacher_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `grade_no`, `license`, `teach`, `age`, `telephone`, `email`, `address`, `avatar`, `description`, `power`, `piority`, `status`, `deleted`, `create_time`, `update_time`) VALUES (38, '10044', '吴老师', '123456', '吴天盛', 2, '高级讲师', '03', NULL, '物理', 32, '13677731235', 'teacher@guet.com', '福建省福州市', NULL, NULL, 1, NULL, 0, 0, '2020-05-18 21:56:41', NULL);
INSERT INTO `tb_teacher` (`id`, `teacher_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `grade_no`, `license`, `teach`, `age`, `telephone`, `email`, `address`, `avatar`, `description`, `power`, `piority`, `status`, `deleted`, `create_time`, `update_time`) VALUES (39, '10045', '江老师', '123456', '江小陆', 2, '中级讲师', '03', 'https://arrange.oss-cn-shenzhen.aliyuncs.com/timg.gif', '数学', 37, '13677731235', 'teacher@guet.com', '甘肃省兰州市', NULL, NULL, 1, NULL, 0, 0, '2020-05-18 22:02:25', '2020-05-19 09:19:05');
INSERT INTO `tb_teacher` (`id`, `teacher_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `grade_no`, `license`, `teach`, `age`, `telephone`, `email`, `address`, `avatar`, `description`, `power`, `piority`, `status`, `deleted`, `create_time`, `update_time`) VALUES (40, '10047', '马东锡', '123456', '马东锡', 2, '高级讲师', NULL, 'https://arrange.oss-cn-shenzhen.aliyuncs.com/timg.gif', '物理', 35, '13677731234', 'madx@guet.com', '广西河池市', NULL, NULL, 1, NULL, 0, 0, '2020-06-01 10:35:18', '2020-06-01 10:39:05');
INSERT INTO `tb_teacher` (`id`, `teacher_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `grade_no`, `license`, `teach`, `age`, `telephone`, `email`, `address`, `avatar`, `description`, `power`, `piority`, `status`, `deleted`, `create_time`, `update_time`) VALUES (41, '10048', '黄讲师', '123456', '黄桐', 2, '初级讲师', NULL, 'https://arrange.oss-cn-shenzhen.aliyuncs.com/timg (2).jfif', '数学', 32, '13677731235', 'tong@qq.com', '广西贵港市', NULL, NULL, 1, NULL, 0, 0, '2020-06-01 14:38:57', '2020-06-01 14:49:25');
INSERT INTO `tb_teacher` (`id`, `teacher_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `grade_no`, `license`, `teach`, `age`, `telephone`, `email`, `address`, `avatar`, `description`, `power`, `piority`, `status`, `deleted`, `create_time`, `update_time`) VALUES (42, '10049', '王刚', '123456', '王刚', 2, '高级讲师', NULL, 'https://arrange.oss-cn-shenzhen.aliyuncs.com/timg (1).jfif', '化学', 36, '18890786676', 'gang.w@qq.com', '广西壮族自治区贺州市', NULL, NULL, 1, NULL, 0, 0, '2020-06-01 14:53:56', '2020-06-05 15:30:43');
INSERT INTO `tb_teacher` (`id`, `teacher_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `grade_no`, `license`, `teach`, `age`, `telephone`, `email`, `address`, `avatar`, `description`, `power`, `piority`, `status`, `deleted`, `create_time`, `update_time`) VALUES (43, '10050', '黄三毛', '123456', '黄三毛', 2, '高级讲师', NULL, NULL, '化学', 34, '15876765634', 'huang@guet.com', '广东省汕头市', NULL, NULL, 1, NULL, 0, 0, NULL, NULL);
INSERT INTO `tb_teacher` (`id`, `teacher_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `grade_no`, `license`, `teach`, `age`, `telephone`, `email`, `address`, `avatar`, `description`, `power`, `piority`, `status`, `deleted`, `create_time`, `update_time`) VALUES (44, '10051', '燕双赢', '123456', '燕双鹰', 2, '中级讲师', NULL, NULL, '数学', 29, '13454349878', 'yan@guet.com', '山东省青岛市', NULL, NULL, 1, NULL, 0, 0, NULL, NULL);
INSERT INTO `tb_teacher` (`id`, `teacher_no`, `username`, `password`, `realname`, `user_type`, `jobtitle`, `grade_no`, `license`, `teach`, `age`, `telephone`, `email`, `address`, `avatar`, `description`, `power`, `piority`, `status`, `deleted`, `create_time`, `update_time`) VALUES (45, '10052', '张三', '123456', '张三老师', 2, '高级教师', NULL, NULL, '数学', 19, '13333334444', 'uyuiy@qq.com', 'jkghjkgjkhghkg', NULL, NULL, 1, NULL, 0, 0, '2024-04-09 12:27:29', NULL);
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
