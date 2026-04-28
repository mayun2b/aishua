/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80042 (8.0.42)
 Source Host           : localhost:3306
 Source Schema         : coursedb

 Target Server Type    : MySQL
 Target Server Version : 80042 (8.0.42)
 File Encoding         : 65001

 Date: 28/04/2026 00:40:45
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for edu_course
-- ----------------------------
DROP TABLE IF EXISTS `edu_course`;
CREATE TABLE `edu_course`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `course_cno` varchar(11) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '课程号',
  `course_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT '' COMMENT '课程名称',
  `teacher_id` int NULL DEFAULT 0 COMMENT '教师id',
  `is_valid` tinyint(1) NULL DEFAULT 1 COMMENT '课程有效性（1-上架，2-下架）',
  `price` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT '0' COMMENT '是否已删除 0-未删除 1-已删除',
  `sort` tinyint NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `logo_url` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `is_deleted` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 80 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of edu_course
-- ----------------------------
INSERT INTO `edu_course` VALUES (11, '0001', '民间鉴赏', 6, 2, '19', 0, NULL, '2025-10-22 18:48:37', NULL, NULL);
INSERT INTO `edu_course` VALUES (12, '0005', '猫和老鼠鉴赏', 7, 1, '14', 0, '2024-12-29 22:13:31', '2025-10-21 08:41:20', NULL, NULL);
INSERT INTO `edu_course` VALUES (13, '0004', '猫和老鼠鉴赏', 3, 1, '14', 0, '2024-12-29 22:14:04', '2025-11-03 22:08:39', 'http://localhost:9000/course/20251022/jpg.jpg', NULL);
INSERT INTO `edu_course` VALUES (15, '0006', '中华武术', 8, 1, '16', 0, '2025-06-26 16:16:15', '2025-10-22 18:51:02', NULL, NULL);
INSERT INTO `edu_course` VALUES (60, '006', 'test008', 6, 1, '23.0', 2, '2025-10-20 15:04:43', '2025-11-04 10:09:47', 'http://localhost:9000/course/20251022/jpg.jpg', NULL);
INSERT INTO `edu_course` VALUES (61, '0036', 'test005', 6, 1, '22.0', 1, '2025-10-20 15:04:57', '2025-10-20 15:04:57', NULL, NULL);
INSERT INTO `edu_course` VALUES (71, '123', 'test112', 6, 1, '23.0', 32, '2025-10-20 16:47:51', '2025-10-20 21:41:33', NULL, NULL);
INSERT INTO `edu_course` VALUES (72, '001', 'test002', 8, 1, '12.0', 3, '2025-10-20 16:48:08', '2025-10-20 21:40:26', NULL, NULL);
INSERT INTO `edu_course` VALUES (73, '0002', '互动实践', 7, 1, '111.0', 1, '2025-10-20 16:48:52', '2025-10-20 21:44:23', NULL, NULL);
INSERT INTO `edu_course` VALUES (75, '132', '112', 6, 2, '13.0', NULL, '2025-10-20 21:51:28', '2025-10-20 21:51:42', NULL, NULL);
INSERT INTO `edu_course` VALUES (76, '322', '332', 6, 2, '21.0', NULL, '2025-10-20 21:52:12', '2025-10-20 21:52:12', NULL, NULL);
INSERT INTO `edu_course` VALUES (77, '00123', '速腾23', 3, 2, '32.0', NULL, '2025-10-20 21:54:18', '2025-10-20 21:54:24', NULL, NULL);
INSERT INTO `edu_course` VALUES (78, '123', '13', 3, 1, '2.0', 3, '2025-11-02 22:58:54', '2025-11-02 22:58:54', NULL, NULL);
INSERT INTO `edu_course` VALUES (79, '00012', '二员闪击西大', 1, 1, '99.0', 1, '2025-11-03 16:44:07', '2025-11-03 16:44:57', NULL, NULL);

-- ----------------------------
-- Table structure for edu_course_studyhistory
-- ----------------------------
DROP TABLE IF EXISTS `edu_course_studyhistory`;
CREATE TABLE `edu_course_studyhistory`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `course_id` int NULL DEFAULT 0 COMMENT '课程id',
  `student_id` int NULL DEFAULT 0 COMMENT '学生id',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `course_status` int NULL DEFAULT 0 COMMENT '课程状态（0-可申请，1-待审核，2-已审核）',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否已删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 60 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of edu_course_studyhistory
-- ----------------------------
INSERT INTO `edu_course_studyhistory` VALUES (54, 10, 8, '2024-12-31 16:28:41', '2024-12-31 16:28:41', 0, 0, 0);
INSERT INTO `edu_course_studyhistory` VALUES (55, 10, 8, '2025-01-02 14:16:20', '2025-01-02 14:16:20', 0, 0, 0);
INSERT INTO `edu_course_studyhistory` VALUES (56, 10, 8, '2025-01-02 14:16:40', '2025-01-02 14:16:40', 0, 0, 0);
INSERT INTO `edu_course_studyhistory` VALUES (57, 10, 8, '2025-01-02 14:24:26', '2025-01-02 14:24:26', 0, 0, 0);
INSERT INTO `edu_course_studyhistory` VALUES (58, 10, 8, '2025-01-02 14:26:02', '2025-01-02 14:26:02', 0, 0, 0);
INSERT INTO `edu_course_studyhistory` VALUES (59, 10, 8, '2025-01-05 16:13:07', '2025-01-05 16:13:07', 0, 0, 0);

-- ----------------------------
-- Table structure for spring_ai_chat_memory
-- ----------------------------
DROP TABLE IF EXISTS `spring_ai_chat_memory`;
CREATE TABLE `spring_ai_chat_memory`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `conversation_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `type` enum('USER','ASSISTANT','SYSTEM','TOOL') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `timestamp` timestamp NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `SPRING_AI_CHAT_MEMORY_CONVERSATION_ID_TIMESTAMP_IDX`(`conversation_id` ASC, `timestamp` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of spring_ai_chat_memory
-- ----------------------------
INSERT INTO `spring_ai_chat_memory` VALUES (7, 'yyds', '我叫熊猫', 'USER', '2025-12-25 17:18:22');
INSERT INTO `spring_ai_chat_memory` VALUES (8, 'yyds', '你好呀，熊猫！很高兴认识你～ 🐼  \n是喜欢熊猫才取这个名字，还是有其他特别的原因呢？（或者…你真的是只熊猫？👀）  \n\n有什么想聊的、想问的，或者需要帮忙的，随时告诉我！ （比如：推荐竹子食谱？教你怎么打滚卖萌？还是…拯救被卡住的代码？）', 'ASSISTANT', '2025-12-25 17:18:23');
INSERT INTO `spring_ai_chat_memory` VALUES (9, 'yyds', '你还记得我是谁吗', 'USER', '2025-12-25 17:18:24');
INSERT INTO `spring_ai_chat_memory` VALUES (10, 'yyds', '当然记得啦！你刚刚告诉我你叫**熊猫**🐼～  \n虽然我们才刚开始聊天，但你的名字我已经牢牢记住啦！（毕竟谁会忘记一只可爱又特别的熊猫呢？）\n\n如果你之前和我聊过更多内容，可能因为技术原因我无法记住历史对话（每次新对话我都会“重置记忆”😅），但只要你告诉我，我会立刻接上！\n\n所以——今天的熊猫想做什么呢？继续聊天、解决问题，还是来点轻松有趣的互动？✨', 'ASSISTANT', '2025-12-25 17:18:25');
INSERT INTO `spring_ai_chat_memory` VALUES (12, 'dsa', '你还记得我是谁吗', 'USER', '2025-12-25 17:18:39');
INSERT INTO `spring_ai_chat_memory` VALUES (13, 'dsa', '嗨！说实话，我无法记住你是谁。每次对话对我来说都是全新的开始，我不会保留之前的聊天记录或个人信息（这是为了保护大家的隐私安全✨）。不过没关系呀！你可以随时告诉我你的名字、兴趣或者我们之前聊过的话题，我会立刻“进入状态”，和你继续愉快地聊天～ 😊\n\n比如你现在想聊聊什么？或者需要我帮忙解决什么问题吗？', 'ASSISTANT', '2025-12-25 17:18:40');

-- ----------------------------
-- Table structure for ums_student
-- ----------------------------
DROP TABLE IF EXISTS `ums_student`;
CREATE TABLE `ums_student`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `mobile` varchar(11) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '手机号',
  `password` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '密码',
  `name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '用户名',
  `gender` tinyint(1) NULL DEFAULT 1 COMMENT '性别  1男  2女',
  `age` tinyint NULL DEFAULT 0 COMMENT '年龄',
  `create_time` datetime NULL DEFAULT NULL COMMENT '注册时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否已删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 58 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ums_student
-- ----------------------------
INSERT INTO `ums_student` VALUES (5, '13618504364', '123', '张三', 1, 20, '2023-09-09 18:02:56', '2023-12-18 23:45:37', 0);
INSERT INTO `ums_student` VALUES (8, '123', '123', '李四', 2, 0, '2023-09-09 18:02:56', '2023-12-18 23:45:37', 0);
INSERT INTO `ums_student` VALUES (16, 'admin', 'admin', 'admin', 1, 12, '2023-09-09 18:02:56', '2023-09-12 11:48:30', 0);
INSERT INTO `ums_student` VALUES (25, '中文手机号', '123', '中文用户名', 1, 18, '2023-12-26 01:15:49', '2023-12-26 01:15:49', 0);
INSERT INTO `ums_student` VALUES (26, '111', '123', '测试用户1号', 1, 18, '2023-12-27 11:56:50', '2023-12-27 11:56:50', 0);
INSERT INTO `ums_student` VALUES (27, '222', '222', '222', 1, 22, '2024-12-25 00:35:10', '2024-12-25 00:35:10', 0);
INSERT INTO `ums_student` VALUES (50, '188888', '123', '代毅', 2, 11, '2025-09-22 15:26:38', '2025-09-22 15:26:38', 0);
INSERT INTO `ums_student` VALUES (51, '2332', '32', '33', NULL, 23, '2025-09-22 22:36:52', '2025-09-22 22:36:52', 0);
INSERT INTO `ums_student` VALUES (52, '123u', '323', '32', 2, 23, '2025-09-22 23:28:51', '2025-09-22 23:28:51', 0);
INSERT INTO `ums_student` VALUES (53, '1234', '123', '2313', NULL, 123, '2025-09-23 11:15:11', '2025-09-23 11:15:11', 0);
INSERT INTO `ums_student` VALUES (54, '1114 ', '333', '123', NULL, 34, '2025-09-23 15:28:19', '2025-09-23 15:28:19', 0);
INSERT INTO `ums_student` VALUES (55, '1232', '123', '大大大', NULL, 11, '2025-09-27 00:47:55', '2025-09-27 00:47:55', 0);
INSERT INTO `ums_student` VALUES (56, '123423', '1234', '12', NULL, 123, '2025-09-27 01:22:39', '2025-09-27 01:22:39', 0);
INSERT INTO `ums_student` VALUES (57, '1233', '22', '13', NULL, 22, '2025-10-15 11:34:11', '2025-10-15 11:34:11', 0);

-- ----------------------------
-- Table structure for ums_teacher
-- ----------------------------
DROP TABLE IF EXISTS `ums_teacher`;
CREATE TABLE `ums_teacher`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `work_no` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT '' COMMENT '教职工号',
  `name` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT '' COMMENT '教师姓名',
  `gender` tinyint(1) NULL DEFAULT 1 COMMENT '性别  1男  2女',
  `mobile` varchar(11) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '手机号',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '是否已删除 0-未删除 1-已删除',
  `password` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ums_teacher
-- ----------------------------
INSERT INTO `ums_teacher` VALUES (3, 'ZY0001', 'admin', 2, '1', '2023-09-30 18:45:39', '2023-10-07 18:45:42', 0, '1');
INSERT INTO `ums_teacher` VALUES (6, 'ZY0002', '张老师', 1, '13600000002', '2023-10-04 08:46:31', '2023-10-05 08:46:33', 0, '456');
INSERT INTO `ums_teacher` VALUES (7, 'ZY0003', '李老师', 1, '13600000002', '2023-10-04 08:46:31', '2023-10-05 08:46:33', 0, '789');
INSERT INTO `ums_teacher` VALUES (8, 'ZY0004', '王老师', 1, '13600000002', '2023-10-04 08:46:31', '2023-10-05 08:46:33', 0, '012');

SET FOREIGN_KEY_CHECKS = 1;
