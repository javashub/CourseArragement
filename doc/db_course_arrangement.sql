/*
 Navicat Premium Data Transfer

 Source Server         : mysql8
 Source Server Type    : MySQL
 Source Server Version : 80015
 Source Host           : localhost:3306
 Source Schema         : db_course_arrangement

 Target Server Type    : MySQL
 Target Server Version : 80015
 File Encoding         : 65001

 Date: 22/05/2020 15:44:28
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tb_admin
-- ----------------------------
DROP TABLE IF EXISTS `tb_admin`;
CREATE TABLE `tb_admin`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `admin_no` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '管理员编号',
  `username` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户名',
  `password` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '密码',
  `realname` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '真实姓名',
  `jobtitle` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '职称',
  `license` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '证件照地址',
  `teach` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '教授科目',
  `telephone` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '联系电话',
  `email` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '电子邮件',
  `address` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '地址',
  `age` int(3) NULL DEFAULT NULL COMMENT '年龄',
  `avatar` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '头像',
  `description` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '签名',
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `type` int(1) NULL DEFAULT NULL COMMENT '管理员类型',
  `piority` int(2) NULL DEFAULT NULL COMMENT '优先级',
  `power` int(1) NULL DEFAULT NULL COMMENT '1为管理员，0为超级管理员',
  `status` int(1) NULL DEFAULT 0 COMMENT '账号状态',
  `deleted` int(1) NULL DEFAULT 0,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_admin
-- ----------------------------
INSERT INTO `tb_admin` VALUES (1, '10011', 'admin', '123456', '梁主任', '教务处主任', NULL, NULL, NULL, 'admin@guet.com', NULL, NULL, NULL, '认真对待工作', '务实', NULL, NULL, NULL, 0, 0, '2020-03-06 23:31:17', '2020-03-06 23:31:17');
INSERT INTO `tb_admin` VALUES (2, '10012', '123', '123456', '张三丰', '教务处副主任', NULL, NULL, NULL, 'admin@guet.com', NULL, NULL, NULL, '光线强的地方，影子也比较黑暗。', '零下1摄氏度', NULL, NULL, NULL, 0, 0, '2020-03-04 21:24:57', NULL);
INSERT INTO `tb_admin` VALUES (3, '10013', 'admin1', '123456', '王五', '教务处副主任', NULL, NULL, NULL, 'admin@guet.com', NULL, NULL, NULL, '加油每一天', NULL, NULL, NULL, NULL, 0, 0, NULL, NULL);

-- ----------------------------
-- Table structure for tb_class_info
-- ----------------------------
DROP TABLE IF EXISTS `tb_class_info`;
CREATE TABLE `tb_class_info`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id,班级表',
  `class_no` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '班级编号',
  `class_name` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '班级名称',
  `num` int(11) NOT NULL DEFAULT 0 COMMENT '班级人数',
  `teacher` int(11) NULL DEFAULT NULL COMMENT '班主任',
  `remark` varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '直接用来做为年级编号的划分了',
  `deleted` int(11) NULL DEFAULT 0,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_class_info
-- ----------------------------
INSERT INTO `tb_class_info` VALUES (1, '20200101', '20年高一1班', 0, 1, '01', 0, NULL, '2020-05-20 16:23:22');
INSERT INTO `tb_class_info` VALUES (2, '20200102', '20年高一2班', 0, 1, '01', 0, NULL, '2020-05-20 16:23:22');
INSERT INTO `tb_class_info` VALUES (3, '20200103', '20年高一3班', 0, 3, '01', 0, NULL, '2020-05-20 16:23:22');
INSERT INTO `tb_class_info` VALUES (4, '20200104', '20年高一4班', 0, 5, '01', 0, NULL, '2020-05-20 16:23:22');
INSERT INTO `tb_class_info` VALUES (5, '20200105', '20年高一5班', 0, 5, '01', 0, NULL, '2020-05-20 16:23:22');
INSERT INTO `tb_class_info` VALUES (6, '20200201', '20年高二1班', 0, 6, '02', 0, NULL, '2020-05-20 16:23:22');
INSERT INTO `tb_class_info` VALUES (7, '20200202', '20年高二2班', 0, 7, '02', 0, NULL, '2020-05-20 16:23:22');
INSERT INTO `tb_class_info` VALUES (8, '20200203', '20年高二3班', 0, 8, '02', 0, NULL, '2020-05-20 16:23:22');
INSERT INTO `tb_class_info` VALUES (9, '20200204', '20年高二4班', 0, 9, '02', 0, NULL, '2020-05-20 16:23:22');
INSERT INTO `tb_class_info` VALUES (10, '20200205', '20年高二5班', 0, 10, '02', 0, NULL, '2020-05-20 16:23:22');
INSERT INTO `tb_class_info` VALUES (11, '20200301', '20年高三1班', 0, 11, '03', 0, NULL, '2020-05-20 16:23:22');
INSERT INTO `tb_class_info` VALUES (12, '20200302', '20年高三2班', 0, 12, '03', 0, NULL, '2020-05-20 16:23:22');
INSERT INTO `tb_class_info` VALUES (13, '20200303', '20年高三3班', 0, 13, '03', 0, NULL, '2020-05-20 16:23:22');
INSERT INTO `tb_class_info` VALUES (14, '20200304', '20年高三4班', 0, 14, '03', 0, NULL, '2020-05-20 16:23:22');
INSERT INTO `tb_class_info` VALUES (15, '20200305', '20年高三5班', 0, 15, '03', 0, NULL, '2020-05-20 16:23:22');

-- ----------------------------
-- Table structure for tb_class_task
-- ----------------------------
DROP TABLE IF EXISTS `tb_class_task`;
CREATE TABLE `tb_class_task`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id，即将要上课的，需要进行排课的',
  `semester` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '学期',
  `grade_no` varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '年级编号',
  `class_no` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '班级编号',
  `course_no` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '课程编号',
  `course_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '课程名',
  `teacher_no` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '教师编号',
  `realname` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '教师姓名',
  `courseAttr` varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '课程属性',
  `studentNum` int(11) NOT NULL COMMENT '学生人数',
  `weeks_sum` int(11) NOT NULL COMMENT '周数',
  `weeks_number` int(11) NOT NULL COMMENT '周学时',
  `isFix` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '是否固定上课时间',
  `class_time` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '固定时间的话,2位为一个时间位置',
  `deleted` tinyint(1) NULL DEFAULT 0,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_class_task
-- ----------------------------
INSERT INTO `tb_class_task` VALUES (1, '2019-2020-1', '01', '20200101', '100001', '高一语文必修1', '10010', '梁晓明', '01', 42, 20, 6, '1', NULL, 0, '2020-05-22 15:22:28', NULL);
INSERT INTO `tb_class_task` VALUES (2, '2019-2020-1', '01', '20200101', '100033', '高一数学必修1', '10012', '李雪雪', '01', 37, 20, 6, '1', NULL, 0, '2020-05-22 15:22:28', NULL);
INSERT INTO `tb_class_task` VALUES (3, '2019-2020-1', '01', '20200101', '100056', '高一英语必修1', '10013', '王小芳', '01', 39, 20, 6, '1', NULL, 0, '2020-05-22 15:22:28', NULL);
INSERT INTO `tb_class_task` VALUES (4, '2019-2020-1', '01', '20200101', '100004', '高一物理1', '10025', '张德良', '02', 42, 20, 4, '1', NULL, 0, '2020-05-22 15:22:28', NULL);
INSERT INTO `tb_class_task` VALUES (5, '2019-2020-1', '01', '20200101', '100014', '高一化学必修1', '10033', '韩云', '02', 40, 20, 4, '1', NULL, 0, '2020-05-22 15:22:28', NULL);
INSERT INTO `tb_class_task` VALUES (6, '2019-2020-1', '01', '20200101', '100041', '高一思想政治必修1', '10045', '江大波', '02', 40, 20, 2, '1', NULL, 0, '2020-05-22 15:22:28', NULL);
INSERT INTO `tb_class_task` VALUES (7, '2019-2020-1', '01', '20200101', '100021', '高一历史必修1', '10044', '吴天盛', '02', 40, 20, 2, '1', NULL, 0, '2020-05-22 15:22:28', NULL);
INSERT INTO `tb_class_task` VALUES (8, '2019-2020-1', '01', '20200101', '100007', '高一地理必修1', '10043', '王杰', '02', 40, 20, 2, '1', NULL, 0, '2020-05-22 15:22:28', NULL);
INSERT INTO `tb_class_task` VALUES (9, '2019-2020-1', '01', '20200101', '100027', '高一生物必修1：分子与细胞', '10042', '谭咏麟', '02', 40, 20, 4, '1', NULL, 0, '2020-05-22 15:22:28', NULL);
INSERT INTO `tb_class_task` VALUES (10, '2019-2020-1', '01', '20200101', '100051', '体育课', '10041', '张杰', '04', 40, 20, 2, '2', '14', 0, '2020-05-22 15:22:28', NULL);
INSERT INTO `tb_class_task` VALUES (11, '2019-2020-1', '01', '20200101', '100066', '物理实验', '10025', '张德良', '03', 40, 20, 2, '2', '04', 0, '2020-05-22 15:22:28', NULL);
INSERT INTO `tb_class_task` VALUES (12, '2019-2020-1', '01', '20200101', '100067', '化学实验', '10023', '张靓颖', '03', 40, 20, 2, '2', '15', 0, '2020-05-22 15:22:29', NULL);
INSERT INTO `tb_class_task` VALUES (13, '2019-2020-1', '01', '20200102', '100001', '高一语文必修1', '10010', '梁晓明', '01', 42, 20, 6, '1', NULL, 0, '2020-05-22 15:22:29', NULL);
INSERT INTO `tb_class_task` VALUES (14, '2019-2020-1', '01', '20200102', '100033', '高一数学必修1', '10012', '李雪雪', '01', 37, 20, 6, '1', NULL, 0, '2020-05-22 15:22:29', NULL);
INSERT INTO `tb_class_task` VALUES (15, '2019-2020-1', '01', '20200102', '100056', '高一英语必修1', '10013', '王小芳', '01', 39, 20, 6, '1', NULL, 0, '2020-05-22 15:22:29', NULL);
INSERT INTO `tb_class_task` VALUES (16, '2019-2020-1', '01', '20200102', '100004', '高一物理1', '10025', '张德良', '02', 42, 20, 2, '1', NULL, 0, '2020-05-22 15:22:29', NULL);
INSERT INTO `tb_class_task` VALUES (17, '2019-2020-1', '01', '20200102', '100014', '高一化学必修1', '10033', '韩云', '02', 40, 20, 2, '1', NULL, 0, '2020-05-22 15:22:29', NULL);
INSERT INTO `tb_class_task` VALUES (18, '2019-2020-1', '01', '20200102', '100041', '高一思想政治必修1', '10045', '江大波', '02', 40, 20, 4, '1', NULL, 0, '2020-05-22 15:22:29', NULL);
INSERT INTO `tb_class_task` VALUES (19, '2019-2020-1', '01', '20200102', '100021', '高一历史必修1', '10044', '吴天盛', '02', 40, 20, 4, '1', NULL, 0, '2020-05-22 15:22:29', NULL);
INSERT INTO `tb_class_task` VALUES (20, '2019-2020-1', '01', '20200102', '100007', '高一地理必修1', '10043', '王杰', '02', 40, 20, 4, '1', NULL, 0, '2020-05-22 15:22:29', NULL);
INSERT INTO `tb_class_task` VALUES (21, '2019-2020-1', '01', '20200102', '100027', '高一生物必修1：分子与细胞', '10042', '谭咏麟', '02', 40, 20, 2, '1', NULL, 0, '2020-05-22 15:22:29', NULL);
INSERT INTO `tb_class_task` VALUES (22, '2019-2020-1', '01', '20200102', '100051', '体育课', '10041', '张杰', '04', 40, 20, 2, '2', '19', 0, '2020-05-22 15:22:29', NULL);
INSERT INTO `tb_class_task` VALUES (23, '2019-2020-1', '01', '20200102', '100066', '物理实验', '10025', '张德良', '03', 40, 20, 2, '2', '09', 0, '2020-05-22 15:22:29', NULL);
INSERT INTO `tb_class_task` VALUES (24, '2019-2020-1', '01', '20200102', '100067', '化学实验', '10023', '张靓颖', '03', 40, 20, 2, '2', '20', 0, '2020-05-22 15:22:29', NULL);

-- ----------------------------
-- Table structure for tb_classroom
-- ----------------------------
DROP TABLE IF EXISTS `tb_classroom`;
CREATE TABLE `tb_classroom`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '教室id',
  `classroom_no` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '教室编号',
  `classroom_name` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '教室名称',
  `teachbuild_no` varchar(4) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '所在教学楼编号',
  `capacity` int(11) NOT NULL COMMENT '教室人数容量',
  `attr` varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '教室属性',
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` int(11) NULL DEFAULT 0 COMMENT '逻辑删除（默认0显示，1删除）',
  `create_time` datetime(0) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 156 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_classroom
-- ----------------------------
INSERT INTO `tb_classroom` VALUES (1, '01-101', '01-101', '01', 50, '01', '备注', 0, NULL, '2020-04-11 14:21:48');
INSERT INTO `tb_classroom` VALUES (2, '01-102', '01-102', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (3, '01-103', '01-103', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (4, '01-104', '01-104', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (5, '01-105', '01-105', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (6, '01-201', '01-201', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (7, '01-202', '01-202', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (8, '01-203', '01-203', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (9, '01-204', '01-204', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (10, '01-205', '01-205', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (11, '01-301', '01-301', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (12, '01-302', '01-302', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (13, '01-303', '01-303', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (14, '01-304', '01-304', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (15, '01-305', '01-305', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (16, '01-401', '01-401', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (17, '01-402', '01-402', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (18, '01-403', '01-403', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (19, '01-404', '01-404', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (20, '01-405', '01-405', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (21, '01-501', '01-501', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (22, '01-502', '01-502', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (23, '01-503', '01-503', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (24, '01-504', '01-504', '01', 50, '01', NULL, 0, '2020-03-19 12:32:18', '2020-03-12 12:32:21');
INSERT INTO `tb_classroom` VALUES (25, '01-505', '01-505', '01', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (26, '02-101', '02-101', '02', 60, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (27, '02-102', '02-102', '02', 60, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (28, '02-103', '02-103', '02', 60, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (29, '02-104', '02-104', '02', 60, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (30, '02-105', '02-105', '02', 60, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (31, '02-201', '02-202', '02', 60, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (32, '02-202', '02-202', '02', 60, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (33, '02-203', '02-203', '02', 60, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (34, '02-204', '02-204', '02', 60, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (35, '02-205', '02-205', '02', 60, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (36, '02-301', '02-301', '02', 60, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (37, '02-302', '02-302', '02', 60, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (38, '02-303', '02-303', '02', 60, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (39, '02-304', '02-304', '02', 60, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (40, '02-305', '02-305', '02', 60, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (41, '02-401', '02-401', '02', 60, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (42, '02-402', '02-402', '02', 60, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (43, '02-403', '02-403', '02', 60, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (44, '02-404', '02-404', '02', 60, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (45, '02-405', '02-405', '02', 60, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (46, '03-101', '03-101', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (47, '03-102', '03-102', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (48, '03-103', '03-103', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (49, '03-104', '03-104', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (50, '03-105', '03-105', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (51, '03-201', '03-201', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (52, '03-202', '03-202', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (53, '03-203', '03-203', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (54, '03-204', '03-204', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (55, '03-205', '03-205', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (56, '03-301', '03-301', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (57, '03-302', '03-302', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (58, '03-303', '03-303', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (59, '03-304', '03-304', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (60, '03-305', '03-305', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (61, '03-401', '03-401', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (62, '03-402', '03-402', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (63, '03-403', '03-403', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (64, '03-404', '03-404', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (65, '03-405', '03-405', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (66, '03-501', '03-501', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (67, '03-502', '03-502', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (68, '03-503', '03-503', '03', 50, '01', '', 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (69, '03-504', '03-504', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (70, '03-505', '03-505', '03', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (71, '04-101', '04-101', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (72, '04-102', '04-102', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (73, '04-103', '04-103', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (74, '04-104', '04-104', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (75, '04-105', '04-105', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (76, '04-201', '04-201', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (77, '04-202', '04-202', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (78, '04-203', '04-203', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (79, '04-204', '04-204', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (80, '04-205', '04-205', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (81, '04-301', '04-301', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (82, '04-302', '04-302', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (83, '04-303', '04-303', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (84, '04-304', '04-304', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (85, '04-305', '04-305', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (86, '04-401', '04-401', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (87, '04-402', '04-402', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (88, '04-403', '04-403', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (99, '04-404', '04-404', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (100, '04-405', '04-405', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (101, '04-501', '04-501', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (102, '04-502', '04-502', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (103, '04-503', '04-503', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (104, '04-504', '04-504', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (105, '04-505', '04-505', '04', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (106, '05-101', '05-101', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (107, '05-102', '05-102', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (108, '05-103', '05-103', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (109, '05-104', '05-104', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (110, '05-105', '05-105', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (111, '05-201', '05-201', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (112, '05-202', '05-202', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (113, '05-203', '05-203', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (114, '05-204', '05-204', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (115, '05-205', '05-205', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (116, '05-301', '05-301', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (117, '05-302', '05-302', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (118, '05-303', '05-303', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (119, '05-304', '05-304', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (120, '05-305', '05-305', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (121, '05-401', '05-401', '05', 50, '01', '', 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (122, '05-402', '05-402', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (123, '05-403', '05-403', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (124, '05-404', '05-404', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (125, '05-405', '05-405', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (126, '05-501', '05-501', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (127, '05-502', '05-502', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (128, '05-503', '05-503', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (129, '05-504', '05-504', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (130, '05-505', '05-505', '05', 50, '01', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (133, '12-101', '12-101', '12', 120, '04', '体育楼', 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (134, '12-102', '12-102', '12', 120, '04', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (135, '12-103', '12-103', '12', 120, '04', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (136, '12-104', '12-104', '12', 120, '04', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (137, '12-201', '12-201', '12', 120, '04', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (138, '12-202', '12-202', '12', 120, '04', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (139, '12-203', '12-203', '12', 120, '04', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (140, '12-204', '12-204', '12', 120, '04', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (141, '08-101', '08-101', '08', 50, '03', '实验楼', 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (142, '08-102', '08-102', '08', 50, '03', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (143, '08-103', '08-103', '08', 50, '03', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (144, '08-104', '08-104', '08', 50, '03', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (145, '08-105', '08-105', '08', 50, '03', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (146, '08-201', '08-201', '08', 50, '03', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (147, '08-202', '08-202', '08', 50, '03', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (148, '08-203', '08-203', '08', 50, '03', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (149, '08-204', '08-204', '08', 50, '03', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (150, '08-205', '08-205', '08', 50, '03', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (151, '08-301', '08-301', '08', 50, '03', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (152, '08-302', '08-302', '08', 50, '03', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (153, '08-303', '08-303', '08', 50, '03', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (154, '08-304', '08-304', '08', 50, '03', NULL, 0, NULL, NULL);
INSERT INTO `tb_classroom` VALUES (155, '08-305', '08-305', '08', 50, '03', NULL, 0, NULL, NULL);

-- ----------------------------
-- Table structure for tb_course_info
-- ----------------------------
DROP TABLE IF EXISTS `tb_course_info`;
CREATE TABLE `tb_course_info`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `course_no` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '课程编号',
  `course_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '课程名',
  `course_attr` varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '课程属性',
  `publisher` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '出版社',
  `status` int(1) NULL DEFAULT NULL COMMENT '课程状态',
  `piority` int(2) NULL DEFAULT NULL COMMENT '优先级',
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `deleted` int(11) NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 68 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_course_info
-- ----------------------------
INSERT INTO `tb_course_info` VALUES (1, '100001', '高一语文必修1 ', '01', '桂电出版社', NULL, NULL, '语文书', 0, NULL, '2020-05-02 20:59:58');
INSERT INTO `tb_course_info` VALUES (2, '100002', '高一语文必修2', '01', '清华大学出版社', NULL, NULL, '衡水中学使用教材', 0, NULL, '2020-05-02 21:01:07');
INSERT INTO `tb_course_info` VALUES (3, '100003', '高一数学1', '01', '北京大学出版社', NULL, NULL, '谭老师授课', 0, NULL, '2020-05-02 21:03:13');
INSERT INTO `tb_course_info` VALUES (4, '100004', '高一物理1', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (5, '100005', '高二语文必修5', '01', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (6, '100006', '高三语文必修6', '01', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (7, '100007', '高一地理必修1', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (8, '100008', '高一地理必修2', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (9, '100009', '高二地理必修3', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (10, '100010', '高三地理选修1', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (11, '100011', '高三地理选修2', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (12, '100012', '高三地理选修3', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (13, '100013', '高三地理选修4', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (14, '100014', '高一化学必修1', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (15, '100015', '高一化学必修2', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (16, '100016', '高二化学选修1', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (17, '100017', '高二化学选修2', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (18, '100018', '高三化学选修3', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (19, '100019', '高三化学选修4', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (20, '100020', '高三化学选修5', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (21, '100021', '高一历史必修1', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (22, '100022', '高一历史必修2', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (23, '100023', '高二历史必修3', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (24, '100024', '高二历史选修1', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (25, '100025', '高三历史选修2', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (26, '100026', '高三历史选修3', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (27, '100027', '高一生物必修1：分子与细胞', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (28, '100028', '高一生物必修2：遗传与进化', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (29, '100029', '高二生物必修3：稳态与环境', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (30, '100030', '高二生物选修1：生物技术与实践', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (31, '100031', '高三生物选修2：生物科学与社会', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (32, '100032', '高三生物选修3：现代生物科技专题', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (33, '100033', '高一数学必修1', '01', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (34, '100034', '高一数学必修2', '01', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (35, '100035', '高二数学必修3', '01', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (36, '100036', '高二数学必须4', '01', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (37, '100037', '高二数学选修1-2', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (38, '100038', '高二数学选修2-1', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (39, '100039', '高二数学选修2-2', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (40, '100040 ', '高三数学必修5', '01', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (41, '100041', '高一思想政治必修1', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (42, '100042', '高一思想政治必修2', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (43, '100043', '高二思想政治必修3', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (44, '100044', '高二思想政治必修4', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (45, '100045', '高三思想政治选修1', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (46, '100046', '高三思想政治选修2', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (47, '100047', '高三思想政治选修3', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (48, '100048', '高一物理必修1', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (49, '100049', '高一物理必修2', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (50, '100050', '高二物理选修1-1', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (51, '100051', '体育课', '04', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (52, '100052 ', '高一物理必修2', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (53, '100053', '音乐课1', '05', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (54, '100054', '高一物理必修2', '02', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (55, '100055', '舞蹈课1', '05', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (56, '100056', '高一英语必修1', '01', '高一出版社', NULL, NULL, NULL, 0, NULL, '2020-05-02 22:46:51');
INSERT INTO `tb_course_info` VALUES (57, '100057', '高一英语必修2', '01', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (58, '100058', '高二英语必修3', '01', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (59, '100059', '高二英语必修4', '01', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (60, '100060', '高三英语选修1', '01', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (61, '100061', '高三英语选修2', '01', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (62, '100062', '信息与技术1', '03', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (63, '100063', '信息与技术2', '03', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (64, '100064', '音乐课2', '03', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (65, '100065', '舞蹈课2', '05', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (66, '100066', '物理实验', '03', NULL, NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `tb_course_info` VALUES (67, '100067', '化学实验', '03', NULL, NULL, NULL, NULL, 0, NULL, NULL);

-- ----------------------------
-- Table structure for tb_course_plan
-- ----------------------------
DROP TABLE IF EXISTS `tb_course_plan`;
CREATE TABLE `tb_course_plan`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `grade_no` varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '年级编号',
  `class_no` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '班级编号',
  `course_no` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '课程编号',
  `teacher_no` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '讲师编号',
  `classroom_no` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '教室编号',
  `class_time` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '上课时间',
  `weeks_sum` int(5) NULL DEFAULT NULL COMMENT '周数',
  `semester` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '学期',
  `deleted` int(1) NULL DEFAULT 0,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 35 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_course_plan
-- ----------------------------
INSERT INTO `tb_course_plan` VALUES (1, '01', '20200102', '100051', '10041', '12-103', '19', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` VALUES (2, '01', '20200102', '100066', '10025', '08-201', '09', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` VALUES (3, '01', '20200102', '100067', '10023', '08-103', '20', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` VALUES (4, '01', '20200102', '100001', '10010', '01-503', '10', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` VALUES (5, '01', '20200102', '100001', '10010', '01-401', '17', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` VALUES (6, '01', '20200102', '100001', '10010', '01-404', '14', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` VALUES (7, '01', '20200102', '100033', '10012', '01-401', '04', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` VALUES (8, '01', '20200102', '100033', '10012', '01-404', '24', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` VALUES (9, '01', '20200102', '100033', '10012', '01-104', '15', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` VALUES (10, '01', '20200102', '100056', '10013', '01-201', '16', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` VALUES (11, '01', '20200102', '100056', '10013', '01-205', '23', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` VALUES (12, '01', '20200102', '100056', '10013', '01-205', '05', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` VALUES (13, '01', '20200102', '100004', '10025', '01-301', '12', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` VALUES (14, '01', '20200102', '100014', '10033', '01-104', '13', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` VALUES (15, '01', '20200102', '100041', '10045', '01-504', '21', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` VALUES (16, '01', '20200102', '100041', '10045', '01-303', '22', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` VALUES (17, '01', '20200102', '100021', '10044', '01-105', '11', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` VALUES (18, '01', '20200102', '100021', '10044', '01-103', '01', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` VALUES (19, '01', '20200102', '100007', '10043', '01-504', '02', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` VALUES (20, '01', '20200102', '100007', '10043', '01-503', '08', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` VALUES (21, '01', '20200102', '100027', '10042', '01-103', '06', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` VALUES (22, '01', '20200101', '100051', '10041', '12-103', '14', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` VALUES (23, '01', '20200101', '100066', '10025', '08-205', '04', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` VALUES (24, '01', '20200101', '100067', '10023', '08-103', '15', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` VALUES (25, '01', '20200101', '100001', '10010', '01-303', '11', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` VALUES (26, '01', '20200101', '100001', '10010', '01-404', '03', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` VALUES (27, '01', '20200101', '100001', '10010', '01-201', '07', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` VALUES (28, '01', '20200101', '100033', '10012', '01-203', '06', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` VALUES (29, '01', '20200101', '100033', '10012', '01-104', '12', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` VALUES (30, '01', '20200101', '100033', '10012', '01-304', '21', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` VALUES (31, '01', '20200101', '100056', '10013', '01-304', '25', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` VALUES (32, '01', '20200101', '100056', '10013', '01-403', '20', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` VALUES (33, '01', '20200101', '100056', '10013', '01-305', '09', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` VALUES (34, '01', '20200101', '100004', '10025', '01-501', '01', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` VALUES (35, '01', '20200101', '100004', '10025', '01-202', '22', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` VALUES (36, '01', '20200101', '100014', '10033', '01-505', '08', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` VALUES (37, '01', '20200101', '100014', '10033', '01-502', '19', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` VALUES (38, '01', '20200101', '100041', '10045', '01-505', '10', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` VALUES (39, '01', '20200101', '100021', '10044', '01-202', '16', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` VALUES (40, '01', '20200101', '100007', '10043', '01-301', '24', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` VALUES (41, '01', '20200101', '100027', '10042', '01-501', '02', NULL, '2019-2020-1', 0, NULL, NULL);
INSERT INTO `tb_course_plan` VALUES (42, '01', '20200101', '100027', '10042', '01-505', '05', NULL, '2019-2020-1', 0, NULL, NULL);

-- ----------------------------
-- Table structure for tb_doc
-- ----------------------------
DROP TABLE IF EXISTS `tb_doc`;
CREATE TABLE `tb_doc`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `doc_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'doc文件名',
  `doc_url` varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '文件路径',
  `desc` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '文件描述',
  `to_class_no` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '目标班级',
  `from_user_id` int(11) NULL DEFAULT NULL COMMENT '发布者id',
  `from_user_name` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '发布者名字',
  `from_user_type` tinyint(1) NULL DEFAULT 2 COMMENT '来自的用户类型1:管理员。2：讲师',
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `clicks` bigint(20) NULL DEFAULT NULL COMMENT '阅读次数',
  `expire` int(11) NULL DEFAULT NULL COMMENT '有效天数',
  `deleted` int(1) NULL DEFAULT 0,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_exercise
-- ----------------------------
DROP TABLE IF EXISTS `tb_exercise`;
CREATE TABLE `tb_exercise`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `category_id` int(11) NULL DEFAULT NULL COMMENT '题目类别id',
  `class_no` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '题目所属班级，编号',
  `exercise_title` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '题目名称',
  `multiselect` int(1) NULL DEFAULT 0 COMMENT '是否多选，默认0单选，，1多选',
  `answer` varchar(12) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '选项',
  `option_a` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '选项A的值',
  `option_b` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `option_c` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `option_d` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `option_e` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `option_f` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `fraction` int(3) NULL DEFAULT NULL COMMENT '分值',
  `deleted` int(1) NULL DEFAULT 0,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_exercise
-- ----------------------------
INSERT INTO `tb_exercise` VALUES (1, 1, '20200101', 'Java语言具有许多优点和特点，下列选项中，哪个反映了Java程序并行机制的特点？', 0, 'B', '安全性', '多线程', '跨平台', '可移植', NULL, NULL, 5, 0, NULL, NULL);
INSERT INTO `tb_exercise` VALUES (2, 1, '20200101', '下面哪个不是JAVA关键字', 0, 'A', 'integer', 'double', 'float', 'default', NULL, NULL, 5, 0, NULL, NULL);
INSERT INTO `tb_exercise` VALUES (3, 1, '20200101', '构造函数何时被调用( )', 0, 'B', '类定义时', '创建对象时', '调用对象方法时', '使用对象的变量时', NULL, NULL, 5, 0, NULL, NULL);
INSERT INTO `tb_exercise` VALUES (4, 1, '20200101', '这样的语句会产生什么样的输出?  System.out.println(4&amp;7);', 0, 'A', '4', '5', '6', '7', NULL, NULL, 5, 0, NULL, NULL);
INSERT INTO `tb_exercise` VALUES (5, 1, '20200101', 'JDBC中，用于表示数据库连接的对象是:', 0, 'B', 'Statement', 'Connection', 'DriverManager', 'PreparedStatement', NULL, NULL, 5, 0, NULL, NULL);
INSERT INTO `tb_exercise` VALUES (6, 1, '20200101', '编写一个Filter，需要', 0, 'B', '继承Filter 类', '实现Filter 接口', '继承HttpFilter 类', '实现HttpFilter接口', NULL, NULL, 5, 0, NULL, NULL);
INSERT INTO `tb_exercise` VALUES (7, 1, '20200101', '在配置tomcat虚拟目录时，需要打开哪个文件？', 0, 'B', 'index.jsp', 'web.xml', 'server.xml', '以上都不是', NULL, NULL, 5, 0, NULL, NULL);
INSERT INTO `tb_exercise` VALUES (8, 1, '20200101', '在J2EE中属于Web层的组件有:', 0, 'A', 'Servlet', 'EJB', 'Applet', 'HTML', NULL, NULL, 5, 0, NULL, NULL);
INSERT INTO `tb_exercise` VALUES (9, 1, '20200101', '每个使用Swing构件的程序必须有一个(   )', 0, 'D', '按钮', '标签', '菜单', '容器', NULL, NULL, 5, 0, NULL, NULL);
INSERT INTO `tb_exercise` VALUES (10, 3, '20200202', '1+1=?', 0, 'A', '2', '5', '4', '3', NULL, NULL, 5, 0, NULL, NULL);
INSERT INTO `tb_exercise` VALUES (11, 3, '20200202', 'z=x+y=5,x=1,y=?', 0, 'B', '3', '4', '5', '6', NULL, NULL, 5, 0, NULL, NULL);
INSERT INTO `tb_exercise` VALUES (12, 3, '20200202', 'z=x+y=5,x=2,y=?', 0, 'A', '3', '4', '5', '6', NULL, NULL, 5, 0, NULL, NULL);
INSERT INTO `tb_exercise` VALUES (13, 3, '20200202', '1+3=？', 0, 'D', '1', '2', '3', '4', NULL, NULL, 5, 0, NULL, NULL);
INSERT INTO `tb_exercise` VALUES (16, 1, '00000000', 'JAVA只是点', 0, 'A', 'AAAA', 'BBBB', 'CCCC', 'DDDD', '', '', 10, 0, '2020-05-21 22:26:01', NULL);
INSERT INTO `tb_exercise` VALUES (17, 15, '00000000', '常见的非关系型数据库有', 0, 'C', 'Redis', 'MonGoDb', 'MySQL', ' MemCache', '', '', 5, 0, '2020-05-22 08:56:38', NULL);
INSERT INTO `tb_exercise` VALUES (18, 14, '00000000', '台式计算机中的CPU是指（   ）。', 0, 'A', '中央处理器', '控制器', '存储器', '输出设备', '', '', 5, 0, '2020-05-22 09:11:52', NULL);
INSERT INTO `tb_exercise` VALUES (19, 14, '00000000', '十六进制数30转换成二进制数是（）', 0, 'A', '110000', '100000', '111000', '101000', '', '', 5, 0, '2020-05-22 09:14:18', NULL);
INSERT INTO `tb_exercise` VALUES (20, 13, '00000000', '采用资源的有序分配法解决死锁问题的原理是破坏了（） 条件。', 0, 'A', '不可剥夺', '互斥', '环路', '部分分配', '', '', 5, 0, '2020-05-22 09:20:46', NULL);
INSERT INTO `tb_exercise` VALUES (21, 4, '00000000', 'exercise的意思有', 1, 'ABCD', '运动', '测试', '使用', '运用', '', '', 5, 0, '2020-05-22 09:24:43', NULL);
INSERT INTO `tb_exercise` VALUES (22, 2, '00000000', '选出下列加点字注音全对的一项（ ）', 0, 'C', '折本（shé） 干瘪（biě) 谬种(miù) 沸反盈天(fèi)', '吮吸(shǔn) 涎皮（yán） 敕造（chì） 百无聊赖(lài)', '蹙缩（cù） 驯熟(xùn) 两靥（yàn） 鸡豚狗彘（zhì）', '讪讪(shàn) 庠序（xiáng） 俨然(yǎn) 少不更事(jīng)', NULL, NULL, 5, 0, '2020-05-22 09:27:25', NULL);

-- ----------------------------
-- Table structure for tb_exercise_category
-- ----------------------------
DROP TABLE IF EXISTS `tb_exercise_category`;
CREATE TABLE `tb_exercise_category`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `category_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '题目类别名称',
  `deleted` int(1) NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` datetime(0) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_exercise_category
-- ----------------------------
INSERT INTO `tb_exercise_category` VALUES (1, 'Java知识点', 0, NULL, NULL);
INSERT INTO `tb_exercise_category` VALUES (2, '语文试题', 0, NULL, NULL);
INSERT INTO `tb_exercise_category` VALUES (3, '数学试题', 0, NULL, NULL);
INSERT INTO `tb_exercise_category` VALUES (4, '英语试题', 0, NULL, NULL);
INSERT INTO `tb_exercise_category` VALUES (5, '物理试题', 0, NULL, NULL);
INSERT INTO `tb_exercise_category` VALUES (6, '化学试题', 0, NULL, NULL);
INSERT INTO `tb_exercise_category` VALUES (7, '生物试题', 0, NULL, NULL);
INSERT INTO `tb_exercise_category` VALUES (8, '政治试题', 0, NULL, NULL);
INSERT INTO `tb_exercise_category` VALUES (9, '历史试题', 0, NULL, NULL);
INSERT INTO `tb_exercise_category` VALUES (10, '地理试题', 0, NULL, NULL);
INSERT INTO `tb_exercise_category` VALUES (11, '心理学', 0, '2020-05-21 20:41:37', NULL);
INSERT INTO `tb_exercise_category` VALUES (12, '数据结构', 0, '2020-05-22 08:52:55', NULL);
INSERT INTO `tb_exercise_category` VALUES (13, '操作系统', 0, '2020-05-22 08:54:01', NULL);
INSERT INTO `tb_exercise_category` VALUES (14, '计算机组成原理', 0, '2020-05-22 08:54:31', NULL);
INSERT INTO `tb_exercise_category` VALUES (15, '数据库', 0, '2020-05-22 08:55:21', NULL);

-- ----------------------------
-- Table structure for tb_grade_info
-- ----------------------------
DROP TABLE IF EXISTS `tb_grade_info`;
CREATE TABLE `tb_grade_info`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id,年级表',
  `grade_no` varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '年级编号',
  `grade_name` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '年级名称',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_grade_info
-- ----------------------------
INSERT INTO `tb_grade_info` VALUES (1, '01', '高一');
INSERT INTO `tb_grade_info` VALUES (2, '02', '高二');
INSERT INTO `tb_grade_info` VALUES (3, '03', '高三');

-- ----------------------------
-- Table structure for tb_learn_resource
-- ----------------------------
DROP TABLE IF EXISTS `tb_learn_resource`;
CREATE TABLE `tb_learn_resource`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `file_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '资料名称',
  `description` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '描述',
  `type` tinyint(1) NULL DEFAULT NULL COMMENT '文件类型，1为文档，2为视频',
  `file_url` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '文件所在路径',
  `create_time` datetime(0) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_location_info
-- ----------------------------
DROP TABLE IF EXISTS `tb_location_info`;
CREATE TABLE `tb_location_info`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id,位置信息，高一在哪栋楼，高二在哪',
  `teachbuild_no` varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '教学楼编号,放教学楼表中编号',
  `grade_no` varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '年级编号,放年级表中的id',
  `deleted` int(11) NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` datetime(0) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_location_info
-- ----------------------------
INSERT INTO `tb_location_info` VALUES (1, '01', '01', 0, NULL, NULL);
INSERT INTO `tb_location_info` VALUES (2, '02', '02', 0, NULL, NULL);
INSERT INTO `tb_location_info` VALUES (3, '03', '03', 0, NULL, NULL);

-- ----------------------------
-- Table structure for tb_student
-- ----------------------------
DROP TABLE IF EXISTS `tb_student`;
CREATE TABLE `tb_student`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '学生id',
  `student_no` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '学号，可以用于登录',
  `username` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '昵称，可以用于登录',
  `password` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '密码',
  `realname` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '真实姓名',
  `grade` varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '年级',
  `class_no` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '所在班级',
  `age` int(3) NULL DEFAULT NULL COMMENT '年龄',
  `address` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '当前住址',
  `telephone` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '联系电话',
  `email` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '头像',
  `description` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '签名',
  `deleted` int(1) NULL DEFAULT 0 COMMENT '逻辑删除',
  `status` int(1) NULL DEFAULT 0 COMMENT '账号状态,0为正常，1为封禁',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_student
-- ----------------------------
INSERT INTO `tb_student` VALUES (1, '2020011234', 'lequal', '123456', '梁同学', '高三', '20200101', 18, '广西桂林市桂林电子科技大学附属中学', '13677731236', 'course@guet.com', NULL, '按时上课', 0, 0, '2020-02-24 10:24:58', '2020-03-06 10:25:04');
INSERT INTO `tb_student` VALUES (2, '2019021541', 'litongxue', '123456', '李同学', '高二', '20200203', 22, '广西桂林市', '15177989514', 'course@guet.com', NULL, '好好学习', 0, 0, '2020-03-10 20:51:26', '2020-04-06 14:32:22');
INSERT INTO `tb_student` VALUES (3, '2020031235', 'gantongxue', '123456', '甘同学', '高三', '20200302', 19, '湖北省武汉市', '17007891233', 'course@guet.com', NULL, '天天向上', 0, 1, '2020-03-07 20:52:17', NULL);
INSERT INTO `tb_student` VALUES (4, '2020012589', 'xitongxue', '123456', '喜同学', '高一', '20200103', 21, '广东省珠海市', '13677731456', 'course@guet.com', NULL, '找工作中', 0, 0, NULL, NULL);
INSERT INTO `tb_student` VALUES (5, '2020017895', 'huangtongxue', '123456', '黄同学', '高一', '20200101', 20, '广西钦州市', '17689541452', 'course@guet.com', NULL, '技术强才是真的强', 0, 1, NULL, NULL);
INSERT INTO `tb_student` VALUES (6, '2020017836', 'caitongxue', '123456', '蔡同学', '高一', '20200104', 18, '广西玉林', '18574562587', 'course@guet.com', NULL, '是时候好好学习了', 0, 0, NULL, NULL);
INSERT INTO `tb_student` VALUES (7, '2020021936', 'suntongxue', '123456', '孙同学', '高二', '20200201', 17, '湖南长沙', '18648983826', 'course@guet.com', NULL, '加油咯', 0, 0, NULL, NULL);
INSERT INTO `tb_student` VALUES (8, '2020031245', 'hutongxue', '123456', '胡同学', '高三', '20200301', 19, '湖北十堰', '17505127841', 'course@guet.com', NULL, '嘿嘿', 0, 0, NULL, NULL);
INSERT INTO `tb_student` VALUES (9, '2020031278', 'litongxue2', '123456', '黎同学', '高三', '20200302', 17, '安徽省', '13412596654', 'course@guet.com', NULL, '做喜欢做的事', 0, 0, NULL, NULL);
INSERT INTO `tb_student` VALUES (10, '2020014596', 'shitongxue', '123456', '史同学', '高一', '20200103', 18, '广西贵港', '13644527789', 'course@guet.com', NULL, '高一的同学', 0, 0, NULL, NULL);
INSERT INTO `tb_student` VALUES (11, '2020024567', 'xiaotongxue', '123456', '萧同学', '高二', '20200202', 17, '广东珠海', '13677735445', 'course@guet.com', NULL, '高二同学', 0, 0, NULL, NULL);
INSERT INTO `tb_student` VALUES (12, '2020021456', 'student', '123456', '谭同学', '高二', '20200204', 19, '广东汕头', '13677735559', 'course@guet.com', NULL, '高二8班', 0, 0, NULL, NULL);
INSERT INTO `tb_student` VALUES (13, '2020034123', 'studens', '123456', '王同学', '高三', '20200302', 22, '广东深圳', '17007895623', 'course@guet.com', NULL, NULL, 0, 0, NULL, '2020-05-02 21:44:02');
INSERT INTO `tb_student` VALUES (14, '2020035468', 'liangyike', '123456', '梁同学', '高三', '20200301', 20, '广西桂林市', '13677731235', 'course@guet.com', NULL, NULL, 0, 0, '2020-03-26 22:57:04', '2020-04-06 14:52:37');
INSERT INTO `tb_student` VALUES (15, '2020016788', 'xiewutong', '123456', '谢童鞋', '高一', '20200104', 16, '湖南省衡阳市', '15177959816', 'course@guet.com', NULL, NULL, 0, 0, '2020-03-26 23:15:06', NULL);
INSERT INTO `tb_student` VALUES (16, '2020024182', 'qintongxue', '123456', '覃同学', '高二', '20200203', 17, '广西桂平市', '18565412563', 'course@guet.com', NULL, NULL, 0, 0, '2020-03-26 23:18:02', NULL);
INSERT INTO `tb_student` VALUES (17, '2020028242', 'liangxiansheng', '123456', '梁先生', '高二', '20200201', 17, '广东中山', '17585968745', 'course@guet.com', NULL, NULL, 0, 0, '2020-03-26 23:20:29', '2020-05-02 21:44:19');
INSERT INTO `tb_student` VALUES (18, '2020038300', 'jiangtongxue', '123456', '蒋同学', '高三', '20200305', 18, '广东省佛山市', '13596857412', 'course@guet.com', NULL, NULL, 0, 0, '2020-03-26 23:25:11', '2020-04-06 14:49:22');
INSERT INTO `tb_student` VALUES (19, '2020027623', 'ganxiansheng', '123456', '甘先生', '高二', '20200205', 17, '广西百色', '17015789654', 'course@guet.com', NULL, NULL, 0, 0, '2020-03-26 23:26:20', NULL);
INSERT INTO `tb_student` VALUES (20, '2020027807', '2020027623', '123456', '999999', '高二', '20200205', 17, '上海市', '15678415241', 'course@guet.com', NULL, NULL, 0, 0, '2020-03-26 23:27:40', NULL);
INSERT INTO `tb_student` VALUES (21, '2020022351', '小幸运', '123456', '陈奕迅', '高二', NULL, NULL, '广东省珠海市金湾区', '15177959814', NULL, NULL, NULL, 0, 0, '2020-05-20 17:11:25', NULL);

-- ----------------------------
-- Table structure for tb_teach_build_info
-- ----------------------------
DROP TABLE IF EXISTS `tb_teach_build_info`;
CREATE TABLE `tb_teach_build_info`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id,教学楼信息表',
  `teach_build_no` varchar(4) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '教学楼编号',
  `teach_build_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '教学楼名称',
  `teach_build_location` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '教学楼位置',
  `deleted` int(11) NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_teach_build_info
-- ----------------------------
INSERT INTO `tb_teach_build_info` VALUES (1, '01', '第1教学楼', '东校区', 0, NULL, '2020-04-10 21:51:09');
INSERT INTO `tb_teach_build_info` VALUES (2, '02', '2号教学楼', '1校区', 0, NULL, NULL);
INSERT INTO `tb_teach_build_info` VALUES (3, '03', '3号教学楼', '1校区', 0, NULL, NULL);
INSERT INTO `tb_teach_build_info` VALUES (4, '04', '4号教学楼', '1校区', 0, NULL, NULL);
INSERT INTO `tb_teach_build_info` VALUES (5, '05', '5号教学楼', '1校区', 0, NULL, NULL);
INSERT INTO `tb_teach_build_info` VALUES (6, '06', '音乐楼', '1校区', 0, NULL, NULL);
INSERT INTO `tb_teach_build_info` VALUES (7, '07', '美术楼', '1校区', 0, NULL, NULL);
INSERT INTO `tb_teach_build_info` VALUES (8, '08', '实验楼1', '1校区', 0, NULL, NULL);
INSERT INTO `tb_teach_build_info` VALUES (9, '09', '实验楼2', '1校区', 0, NULL, NULL);
INSERT INTO `tb_teach_build_info` VALUES (10, '10', '逸夫楼1', '1校区', 0, NULL, NULL);
INSERT INTO `tb_teach_build_info` VALUES (11, '11', '逸夫楼2', '1校区', 0, NULL, NULL);
INSERT INTO `tb_teach_build_info` VALUES (12, '12', '体育楼', '1校区', 0, NULL, NULL);
INSERT INTO `tb_teach_build_info` VALUES (13, '13', '化生楼', '1校区', 0, NULL, NULL);
INSERT INTO `tb_teach_build_info` VALUES (14, '14', '14号教学楼', '2校区', 0, '2020-03-23 00:05:03', NULL);

-- ----------------------------
-- Table structure for tb_teacher
-- ----------------------------
DROP TABLE IF EXISTS `tb_teacher`;
CREATE TABLE `tb_teacher`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id，讲师表',
  `teacher_no` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '教师编号',
  `username` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '昵称（用户名）',
  `password` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '密码',
  `realname` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '真实姓名',
  `jobtitle` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '职称',
  `grade_no` varchar(2) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '所属年级',
  `license` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '证件照(地址)',
  `teach` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '教授科目',
  `age` int(3) NULL DEFAULT NULL COMMENT '年龄',
  `telephone` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '联系电话',
  `email` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '电子邮件',
  `address` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '地址',
  `avatar` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '头像',
  `description` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '描述（签名）',
  `power` int(1) NULL DEFAULT 1 COMMENT '操作权限',
  `piority` int(2) NULL DEFAULT NULL COMMENT '优先级',
  `status` int(1) NULL DEFAULT 0 COMMENT '账号状态',
  `deleted` int(1) NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 40 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_teacher
-- ----------------------------
INSERT INTO `tb_teacher` VALUES (1, '10010', 'lequal(梁老师)', '123456', '梁晓明', '教务处副主任', '01', NULL, '物理', 32, '13677731235', 'teacher@guet.com', '广西', NULL, '以身作则，教育好学生。', 1, 1, 0, 0, '2020-03-04 15:30:03', '2020-03-06 15:30:30');
INSERT INTO `tb_teacher` VALUES (3, '10012', 'msLi', '123456', '李雪雪', '高级讲师', '01', NULL, '语文', 29, '13677731235', 'teacher@guet.com', '广西桂林市桂林电子科技大学', NULL, '做人民的好教师', 1, 2, 0, 1, '2020-03-06 23:39:39', '2020-03-06 23:39:39');
INSERT INTO `tb_teacher` VALUES (5, '10013', 'mswang', '123456', '王小芳', '初级讲师', '01', NULL, '英语', 25, '13677731235', 'teacher@guet.com', '湖南省', NULL, '过好每一天', 1, 3, 0, 0, '2020-03-04 19:45:44', '2020-03-08 19:45:51');
INSERT INTO `tb_teacher` VALUES (6, '10014', 'mssun', '123456', '孙晓明', '中级讲师', '01', NULL, '数学', 28, '13677731235', 'teacher@guet.com', '湖北省', NULL, '加油', 1, 2, 0, 0, '2020-03-06 19:47:11', '2020-03-30 19:47:14');
INSERT INTO `tb_teacher` VALUES (7, '10015', 'msming', '123456', '名小伙', '实习生', '01', NULL, '化学', 22, '13677731235', 'teacher@guet.com', '江苏省', NULL, 'welcome', 1, 4, 0, 0, '2020-03-05 19:48:40', '2020-03-06 19:48:45');
INSERT INTO `tb_teacher` VALUES (8, '10016', 'mstan', '123456', '谭老师', '初级讲师', '01', NULL, '英语', 23, '13677731235', 'teacher@guet.com', '广西', NULL, 'come on', 1, 3, 0, 0, NULL, NULL);
INSERT INTO `tb_teacher` VALUES (9, '10017', 'msliang', '123456', '梁老师', '高级讲师', '01', NULL, '语文', 32, '13677731235', 'teacher@guet.com', '河北', NULL, '做更好的自己', 1, 1, 0, 0, NULL, NULL);
INSERT INTO `tb_teacher` VALUES (10, '10018', 'mrliang', '123456', '梁忠诚', '高级讲师', '01', NULL, '数学', 35, '13677731235', 'teacher@guet.com', '湖北', NULL, '数学好', 1, 1, 0, 0, NULL, NULL);
INSERT INTO `tb_teacher` VALUES (11, '10019', 'mrwang', '123456', '汪老师', '中级讲师', '01', NULL, '地理', 33, '13677731235', 'teacher@guet.com', '河北', NULL, '地理好', 1, 2, 0, 0, NULL, NULL);
INSERT INTO `tb_teacher` VALUES (12, '10020', 'mshu', '123456', '胡冬梅', '中级讲师', '01', NULL, '化学', 46, '13677731235', 'teacher@guet.com', '搜索', NULL, '是', 1, 2, 0, 0, NULL, NULL);
INSERT INTO `tb_teacher` VALUES (13, '10021', 'mrlin', '123456', '林俊杰', '初级讲师', '01', NULL, '生物', 41, '13677731235', 'teacher@guet.com', '试试', NULL, '就', 1, 3, 0, 0, NULL, NULL);
INSERT INTO `tb_teacher` VALUES (14, '10022', 'mrzhou', '123456', '周杰伦', '高级讲师', '01', NULL, '生物', 39, '13677731235', 'teacher@guet.com', '看看', NULL, '看', 1, 1, 0, 0, NULL, NULL);
INSERT INTO `tb_teacher` VALUES (15, '10023', 'mrwang', '123456', '张靓颖', '初级讲师', '01', NULL, '历史', 33, '13677731235', 'teacher@guet.com', '55', NULL, '555', 1, 3, 0, 0, NULL, NULL);
INSERT INTO `tb_teacher` VALUES (16, '10024', 'mrhou', '123456', '侯德南', '高级讲师', '01', NULL, '政治', 37, '13677731235', 'teacher@guet.com', '54546', NULL, '8878878', 1, 1, 0, 0, NULL, NULL);
INSERT INTO `tb_teacher` VALUES (17, '10025', 'mrzhang', '123456', '张德良', '高级讲师', '01', NULL, '物理', 34, '13677731235', 'teacher@guet.com', '78788', '', '878755', 1, 1, 0, 0, NULL, NULL);
INSERT INTO `tb_teacher` VALUES (18, '10026', 'mrzhang', '123456', '张勇', '中级讲师', '02', NULL, '数学', 45, '13677731235', 'teacher@guet.com', '湖南', NULL, '565675', 1, 2, 0, 0, NULL, NULL);
INSERT INTO `tb_teacher` VALUES (19, '10027', '马老师', '123456', '马晓东', '初级讲师', '02', NULL, '语文', 28, '13677731235', 'teacher@guet.com', '海南', NULL, '78688787', 1, 3, 0, 0, NULL, '2020-04-11 14:33:58');
INSERT INTO `tb_teacher` VALUES (20, '10028', '马老师', '123456', '马芸', '中级讲师', '02', NULL, '英语', 29, '13677731235', 'teacher@guet.com', '河北省邢台市', NULL, '5654', 1, 2, 0, 0, NULL, '2020-04-05 21:10:11');
INSERT INTO `tb_teacher` VALUES (21, '10029', '郑老师', '123456', '郑小红', '高级讲师', '02', NULL, '生物', 32, '13677731235', 'teacher@guet.com', '河南', NULL, '768567', 1, 1, 0, 0, NULL, '2020-04-11 14:33:47');
INSERT INTO `tb_teacher` VALUES (22, '10030', '韦老师', '123456', '韦小龙', '中级讲师', '02', NULL, '物理', 33, '13677731235', 'teacher@guet.com', '江苏', NULL, '6875675', 1, 2, 0, 0, NULL, '2020-04-11 14:30:24');
INSERT INTO `tb_teacher` VALUES (23, '10031', '张老师', '123456', '张小龙', '高级讲师', '02', NULL, '化学', 35, '13677731235', 'teacher@guet.com', '福建', NULL, '6785675', 1, 1, 0, 0, NULL, '2020-04-11 14:29:48');
INSERT INTO `tb_teacher` VALUES (24, '10032', '谭老师', '123456', '谭晓江', '高级讲师', '02', NULL, '历史', 33, '13677731235', 'teacher@guet.com', '贵州', NULL, '78678', 1, 1, 0, 0, NULL, '2020-04-11 14:29:29');
INSERT INTO `tb_teacher` VALUES (25, '10033', '韩老师', '123456', '韩云', '高级讲师', '02', NULL, '政治', 32, '13677731235', 'teacher@guet.com', '新疆', NULL, '67767', 1, 1, 0, 0, NULL, '2020-04-11 14:29:20');
INSERT INTO `tb_teacher` VALUES (26, '10034', '韦老师', '123456', '韦雪琪', '中级讲师', '02', NULL, '历史', 28, '13677731235', 'teacher@guet.com', '贵州省贵阳市', NULL, NULL, 1, NULL, 0, 0, NULL, '2020-04-05 21:00:36');
INSERT INTO `tb_teacher` VALUES (27, '10034', '黄老师', '123456', '黄继光', '高级讲师', '02', NULL, '地理', 31, '13677731235', 'teacher@guet.com', '西藏', NULL, '6756', 1, 1, 0, 0, NULL, '2020-04-11 14:29:00');
INSERT INTO `tb_teacher` VALUES (28, '10035', '张老师', '123456', '张三封', '高级讲师', '03', NULL, '语文', 33, '13677731235', 'teacher@guet.com', '甘肃', NULL, '7567', 1, 1, 0, 0, NULL, '2020-04-11 14:28:44');
INSERT INTO `tb_teacher` VALUES (29, '10036', '胡老师', '123456', '胡小小', '高级讲师', '03', NULL, '数学', 33, '13677731235', 'teacher@guet.com', '广西', NULL, '5675467', 1, 1, 0, 0, NULL, '2020-04-11 14:28:16');
INSERT INTO `tb_teacher` VALUES (30, '10037', '莫老师', '123456', '莫小新', '高级讲师', '03', NULL, '英语', 33, '13677731235', 'teacher@guet.com', '河北石家庄市', NULL, '7867', 1, 1, 0, 0, NULL, '2020-04-11 14:26:18');
INSERT INTO `tb_teacher` VALUES (31, '10038', '甘老师', '123456', '甘楠', '高级讲师', '03', NULL, '物理', 33, '13677731235', 'teacher@guet.com', '北京', NULL, '5644', 1, 1, 0, 0, NULL, '2020-04-05 20:59:08');
INSERT INTO `tb_teacher` VALUES (32, '10039', '江老师', '123456', '江晓东', '高级讲师', '03', NULL, '化学', 40, '13677731235', 'teacher@guet.com', '广东省中山市', NULL, '22222', 1, 1, 0, 0, NULL, '2020-04-05 20:26:13');
INSERT INTO `tb_teacher` VALUES (33, '10040', '厦老师', '123456', '夏紫若', '高级讲师', '03', NULL, '生物', 33, '13677731235', 'teacher@guet.com', '广东省深圳市', NULL, '6758', 1, 1, 0, 0, NULL, '2020-04-05 20:22:34');
INSERT INTO `tb_teacher` VALUES (34, '10041', '张老师', '123456', '张杰', '高级讲师', '03', NULL, '政治', 31, '13677731235', 'teacher@guet.com', '上海', NULL, '999999', 1, 1, 0, 0, NULL, '2020-04-05 18:16:19');
INSERT INTO `tb_teacher` VALUES (35, '10042', '谭老师', '123456', '谭咏麟', '高级讲师', '03', NULL, '历史', 32, '13677731235', 'teacher@guet.com', '天津', NULL, '5353', 1, 1, 0, 0, NULL, '2020-04-05 18:04:37');
INSERT INTO `tb_teacher` VALUES (36, '10043', '王老师', '123456', '王杰', '高级讲师', '03', NULL, '地理', 33, '13677731235', 'teacher@guet.com', '湖北省武汉市', NULL, '543453', 1, 1, 0, 0, NULL, '2020-04-05 18:04:09');
INSERT INTO `tb_teacher` VALUES (38, '10044', '吴老师', '123456', '吴天盛', '高级讲师', '03', NULL, '物理', 32, '13677731235', 'teacher@guet.com', '福建省福州市', NULL, NULL, 1, NULL, 0, 0, '2020-05-18 21:56:41', NULL);
INSERT INTO `tb_teacher` VALUES (39, '10045', '江老师', '123456', '江大波', '中级讲师', '03', NULL, '数学', 37, '13677731235', 'teacher@guet.com', '甘肃省兰州市', NULL, NULL, 1, NULL, 0, 0, '2020-05-18 22:02:25', '2020-05-19 09:19:05');

SET FOREIGN_KEY_CHECKS = 1;
