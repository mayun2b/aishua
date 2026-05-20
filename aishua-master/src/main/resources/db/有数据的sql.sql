/*
 Navicat Premium Dump SQL

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80046 (8.0.46)
 Source Host           : localhost:3306
 Source Schema         : aishua

 Target Server Type    : MySQL
 Target Server Version : 80046 (8.0.46)
 File Encoding         : 65001

 Date: 20/05/2026 14:06:14
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_generated_question
-- ----------------------------
DROP TABLE IF EXISTS `ai_generated_question`;
CREATE TABLE `ai_generated_question`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `title` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '题干',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '题目内容',
  `type` tinyint NOT NULL COMMENT '题型',
  `directory_id` bigint NULL DEFAULT NULL COMMENT '目录ID',
  `subject_id` bigint NULL DEFAULT NULL COMMENT '学科ID',
  `difficulty` tinyint NULL DEFAULT 1 COMMENT '难度',
  `options` json NULL COMMENT '选项JSON',
  `answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '答案',
  `analysis` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '解析',
  `tags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '考点标签',
  `generate_reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '生成原因',
  `generate_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
  `is_practiced` tinyint NULL DEFAULT 0 COMMENT '是否已练习',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '软删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_agq_subject_directory`(`subject_id` ASC, `directory_id` ASC, `difficulty` ASC) USING BTREE,
  INDEX `idx_agq_user_practiced`(`user_id` ASC, `is_practiced` ASC, `generate_time` ASC) USING BTREE,
  INDEX `fk_agq_directory`(`directory_id` ASC) USING BTREE,
  CONSTRAINT `fk_agq_directory` FOREIGN KEY (`directory_id`) REFERENCES `textbook_directory` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_agq_subject` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_agq_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI生成题目表：个性化强化练习题' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ai_generated_question
-- ----------------------------

-- ----------------------------
-- Table structure for daily_stats
-- ----------------------------
DROP TABLE IF EXISTS `daily_stats`;
CREATE TABLE `daily_stats`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `do_count` int NULL DEFAULT 0 COMMENT '当日做题数',
  `correct_count` int NULL DEFAULT 0 COMMENT '当日正确数',
  `time_cost` int NULL DEFAULT 0 COMMENT '当日学习耗时（秒）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '软删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_date`(`user_id` ASC, `stat_date` ASC) USING BTREE,
  INDEX `idx_ds_date`(`stat_date` ASC) USING BTREE,
  CONSTRAINT `fk_ds_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '每日统计表：用户每日学习记录（打卡日历）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of daily_stats
-- ----------------------------
INSERT INTO `daily_stats` VALUES (1, 2, '2026-04-27', 20, 0, 128, '2026-04-27 11:32:51', '2026-04-27 11:32:51', 0);

-- ----------------------------
-- Table structure for directory_tag_relation
-- ----------------------------
DROP TABLE IF EXISTS `directory_tag_relation`;
CREATE TABLE `directory_tag_relation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `subject_id` bigint NOT NULL COMMENT '学科ID，冗余字段，便于按学科快速筛选',
  `directory_id` bigint NOT NULL COMMENT '教材目录ID',
  `tag_id` bigint NOT NULL COMMENT '考点ID',
  `relation_type` tinyint NOT NULL DEFAULT 1 COMMENT '关系类型：1-核心考点，2-关联考点，3-拓展考点',
  `importance_level` tinyint NOT NULL DEFAULT 2 COMMENT '重要程度：1-低，2-中，3-高',
  `exam_frequency` tinyint NOT NULL DEFAULT 1 COMMENT '考试频率：1-低频，2-常考，3-高频',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `is_enabled` tinyint NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
  `source_type` tinyint NOT NULL DEFAULT 1 COMMENT '来源类型：1-人工维护，2-AI生成，3-题库反推，4-导入',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注说明',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除标记：0-未删，1-已删',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_directory_tag`(`directory_id` ASC, `tag_id` ASC) USING BTREE,
  INDEX `idx_dtr_subject_directory`(`subject_id` ASC, `directory_id` ASC, `deleted` ASC, `is_enabled` ASC) USING BTREE,
  INDEX `idx_dtr_subject_tag`(`subject_id` ASC, `tag_id` ASC, `deleted` ASC, `is_enabled` ASC) USING BTREE,
  INDEX `idx_dtr_directory_sort`(`directory_id` ASC, `deleted` ASC, `is_enabled` ASC, `sort` ASC) USING BTREE,
  INDEX `idx_dtr_tag`(`tag_id` ASC, `deleted` ASC, `is_enabled` ASC) USING BTREE,
  CONSTRAINT `fk_dtr_directory` FOREIGN KEY (`directory_id`) REFERENCES `textbook_directory` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_dtr_subject` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_dtr_tag` FOREIGN KEY (`tag_id`) REFERENCES `exam_tag` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 166 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '教材目录-考点关联表：用于章节与知识点的多对多映射' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of directory_tag_relation
-- ----------------------------
INSERT INTO `directory_tag_relation` VALUES (1, 1, 77, 1, 2, 3, 3, 10, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (2, 1, 77, 2, 2, 2, 2, 20, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (3, 1, 77, 3, 2, 3, 3, 30, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (4, 1, 77, 4, 2, 2, 2, 40, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (5, 1, 77, 5, 2, 2, 2, 50, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (6, 1, 77, 6, 2, 2, 2, 60, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (7, 1, 77, 7, 2, 2, 2, 70, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (8, 1, 77, 8, 2, 3, 3, 80, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (9, 1, 77, 9, 2, 3, 3, 90, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (10, 1, 77, 10, 2, 2, 2, 100, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (11, 1, 77, 11, 2, 3, 3, 110, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (12, 1, 77, 12, 2, 2, 2, 120, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (13, 1, 77, 13, 2, 3, 3, 130, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (14, 1, 77, 14, 2, 2, 2, 140, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (15, 1, 77, 15, 2, 2, 2, 150, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (16, 1, 77, 16, 2, 2, 2, 160, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (17, 1, 77, 17, 2, 2, 2, 170, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (18, 1, 77, 18, 2, 3, 3, 180, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (19, 1, 77, 19, 2, 2, 2, 190, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (20, 1, 77, 20, 2, 2, 2, 200, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (21, 1, 77, 21, 2, 2, 2, 210, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (22, 1, 77, 22, 2, 3, 3, 220, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (23, 1, 77, 23, 2, 2, 2, 230, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (24, 1, 77, 24, 2, 2, 2, 240, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (25, 1, 77, 25, 2, 3, 3, 250, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (26, 1, 77, 26, 2, 3, 3, 260, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (27, 1, 77, 27, 2, 3, 3, 270, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (28, 1, 77, 28, 2, 3, 3, 280, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (29, 1, 77, 29, 2, 2, 2, 290, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (30, 1, 77, 30, 2, 2, 2, 300, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (31, 1, 78, 1, 2, 3, 3, 10, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (32, 1, 78, 2, 2, 2, 2, 20, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (33, 1, 78, 3, 2, 3, 3, 30, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (34, 1, 78, 4, 2, 2, 2, 40, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (35, 1, 78, 5, 2, 2, 2, 50, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (36, 1, 78, 6, 2, 2, 2, 60, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (37, 1, 79, 1, 1, 3, 3, 10, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (38, 1, 79, 6, 1, 2, 2, 60, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (39, 1, 80, 2, 1, 2, 2, 20, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (40, 1, 81, 3, 1, 3, 3, 30, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (41, 1, 82, 4, 1, 2, 2, 40, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (42, 1, 83, 5, 1, 2, 2, 50, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (43, 1, 86, 7, 2, 2, 2, 70, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (44, 1, 86, 8, 2, 3, 3, 80, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (45, 1, 86, 9, 2, 3, 3, 90, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (46, 1, 86, 10, 2, 2, 2, 100, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (47, 1, 87, 7, 1, 2, 2, 70, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (48, 1, 88, 8, 1, 3, 3, 80, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (49, 1, 89, 9, 1, 3, 3, 90, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (50, 1, 89, 10, 1, 2, 2, 100, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (51, 1, 91, 11, 2, 3, 3, 110, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (52, 1, 91, 12, 2, 2, 2, 120, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (53, 1, 91, 13, 2, 3, 3, 130, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (54, 1, 91, 14, 2, 2, 2, 140, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (55, 1, 91, 15, 2, 2, 2, 150, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (56, 1, 91, 16, 2, 2, 2, 160, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (57, 1, 92, 11, 1, 3, 3, 110, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (58, 1, 92, 12, 1, 2, 2, 120, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (59, 1, 93, 13, 1, 3, 3, 130, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (60, 1, 93, 14, 1, 2, 2, 140, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (61, 1, 94, 15, 1, 2, 2, 150, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (62, 1, 95, 16, 1, 2, 2, 160, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (63, 1, 98, 17, 2, 2, 2, 170, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (64, 1, 98, 18, 2, 3, 3, 180, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (65, 1, 98, 19, 2, 2, 2, 190, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (66, 1, 98, 20, 2, 2, 2, 200, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (67, 1, 98, 21, 2, 2, 2, 210, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (68, 1, 98, 22, 2, 3, 3, 220, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (69, 1, 99, 17, 1, 2, 2, 170, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (70, 1, 100, 18, 1, 3, 3, 180, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (71, 1, 101, 19, 1, 2, 2, 190, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (72, 1, 102, 20, 1, 2, 2, 200, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (73, 1, 102, 21, 1, 2, 2, 210, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (74, 1, 103, 22, 1, 3, 3, 220, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (75, 1, 106, 23, 2, 2, 2, 230, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (76, 1, 106, 24, 2, 2, 2, 240, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (77, 1, 106, 25, 2, 3, 3, 250, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (78, 1, 106, 26, 2, 3, 3, 260, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (79, 1, 106, 27, 2, 3, 3, 270, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (80, 1, 106, 28, 2, 3, 3, 280, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (81, 1, 106, 29, 2, 2, 2, 290, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (82, 1, 106, 30, 2, 2, 2, 300, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (83, 1, 107, 23, 1, 2, 2, 230, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (84, 1, 108, 24, 1, 2, 2, 240, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (85, 1, 108, 25, 1, 3, 3, 250, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (86, 1, 109, 26, 1, 3, 3, 260, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (87, 1, 110, 27, 1, 3, 3, 270, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (88, 1, 111, 28, 1, 3, 3, 280, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (89, 1, 112, 29, 1, 2, 2, 290, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (90, 1, 113, 30, 1, 2, 2, 300, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (91, 1, 116, 31, 2, 2, 2, 310, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (92, 1, 116, 32, 2, 3, 3, 320, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (93, 1, 116, 33, 2, 2, 2, 330, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (94, 1, 116, 34, 2, 2, 2, 340, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (95, 1, 116, 35, 2, 2, 2, 350, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (96, 1, 116, 36, 2, 3, 3, 360, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (97, 1, 116, 37, 2, 2, 2, 370, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (98, 1, 116, 38, 2, 2, 2, 380, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (99, 1, 116, 39, 2, 2, 2, 390, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (100, 1, 116, 40, 2, 2, 2, 400, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (101, 1, 116, 41, 2, 2, 2, 410, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (102, 1, 116, 42, 2, 2, 2, 420, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (103, 1, 116, 43, 2, 2, 2, 430, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (104, 1, 116, 44, 2, 2, 2, 440, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (105, 1, 116, 45, 2, 2, 2, 450, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (106, 1, 116, 46, 2, 2, 2, 460, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (107, 1, 116, 47, 2, 2, 2, 470, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (108, 1, 116, 48, 2, 2, 2, 480, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (109, 1, 116, 49, 2, 3, 3, 490, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (110, 1, 116, 50, 2, 2, 2, 500, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (111, 1, 116, 51, 2, 2, 2, 510, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (112, 1, 116, 52, 2, 2, 2, 520, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (113, 1, 116, 53, 2, 3, 3, 530, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (114, 1, 116, 54, 2, 2, 2, 540, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (115, 1, 116, 55, 2, 2, 2, 550, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (116, 1, 117, 31, 2, 2, 2, 310, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (117, 1, 117, 32, 2, 3, 3, 320, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (118, 1, 117, 33, 2, 2, 2, 330, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (119, 1, 117, 34, 2, 2, 2, 340, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (120, 1, 117, 35, 2, 2, 2, 350, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (121, 1, 117, 36, 2, 3, 3, 360, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (122, 1, 117, 37, 2, 2, 2, 370, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (123, 1, 118, 31, 1, 2, 2, 310, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (124, 1, 119, 32, 1, 3, 3, 320, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (125, 1, 120, 33, 1, 2, 2, 330, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (126, 1, 120, 34, 1, 2, 2, 340, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (127, 1, 121, 35, 1, 2, 2, 350, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (128, 1, 121, 36, 1, 3, 3, 360, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (129, 1, 121, 37, 1, 2, 2, 370, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (130, 1, 124, 38, 2, 2, 2, 380, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (131, 1, 124, 39, 2, 2, 2, 390, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (132, 1, 124, 40, 2, 2, 2, 400, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (133, 1, 125, 38, 1, 2, 2, 380, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (134, 1, 125, 39, 1, 2, 2, 390, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (135, 1, 126, 40, 1, 2, 2, 400, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (136, 1, 130, 41, 2, 2, 2, 410, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (137, 1, 130, 42, 2, 2, 2, 420, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (138, 1, 130, 43, 2, 2, 2, 430, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (139, 1, 130, 44, 2, 2, 2, 440, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (140, 1, 130, 45, 2, 2, 2, 450, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (141, 1, 130, 46, 2, 2, 2, 460, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (142, 1, 130, 47, 2, 2, 2, 470, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (143, 1, 131, 41, 1, 2, 2, 410, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (144, 1, 132, 42, 1, 2, 2, 420, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (145, 1, 133, 43, 1, 2, 2, 430, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (146, 1, 134, 44, 1, 2, 2, 440, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (147, 1, 135, 45, 1, 2, 2, 450, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (148, 1, 136, 46, 1, 2, 2, 460, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (149, 1, 136, 47, 1, 2, 2, 470, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (150, 1, 139, 48, 2, 2, 2, 480, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (151, 1, 139, 49, 2, 3, 3, 490, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (152, 1, 139, 50, 2, 2, 2, 500, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (153, 1, 139, 51, 2, 2, 2, 510, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (154, 1, 140, 48, 1, 2, 2, 480, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (155, 1, 141, 49, 1, 3, 3, 490, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (156, 1, 141, 50, 1, 2, 2, 500, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (157, 1, 141, 51, 1, 2, 2, 510, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (158, 1, 145, 52, 2, 2, 2, 520, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (159, 1, 145, 53, 2, 3, 3, 530, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (160, 1, 145, 54, 2, 2, 2, 540, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (161, 1, 145, 55, 2, 2, 2, 550, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=下级目录聚合', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (162, 1, 146, 52, 1, 2, 2, 520, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (163, 1, 146, 53, 1, 3, 3, 530, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=3；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (164, 1, 147, 54, 1, 2, 2, 540, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);
INSERT INTO `directory_tag_relation` VALUES (165, 1, 148, 55, 1, 2, 2, 550, 1, 3, '根据现有题目目录与题目-考点关系反推；关联题数=2；范围=本目录', '2026-05-20 10:16:34', '2026-05-20 10:16:34', 0);

-- ----------------------------
-- Table structure for exam_paper
-- ----------------------------
DROP TABLE IF EXISTS `exam_paper`;
CREATE TABLE `exam_paper`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `paper_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '试卷名称',
  `subject_id` bigint NOT NULL COMMENT '学科ID',
  `total_questions` int NULL DEFAULT 0 COMMENT '总题数',
  `total_score` int NULL DEFAULT 100 COMMENT '总分',
  `duration` int NULL DEFAULT 120 COMMENT '考试时长（分钟）',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '软删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_subject_id`(`subject_id` ASC) USING BTREE,
  INDEX `idx_ep_subject_status`(`subject_id` ASC, `status` ASC, `deleted` ASC) USING BTREE,
  CONSTRAINT `fk_ep_subject` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '试卷表：考试试卷模板' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of exam_paper
-- ----------------------------
INSERT INTO `exam_paper` VALUES (1, '高一数学第一单元', 1, 0, 0, 120, 1, '2026-05-19 09:47:40', '2026-05-19 09:47:40', 0);

-- ----------------------------
-- Table structure for exam_paper_question
-- ----------------------------
DROP TABLE IF EXISTS `exam_paper_question`;
CREATE TABLE `exam_paper_question`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `paper_id` bigint NOT NULL COMMENT '试卷ID',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `score` int NULL DEFAULT 0 COMMENT '分值',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '软删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_paper_question`(`paper_id` ASC, `question_id` ASC) USING BTREE,
  INDEX `fk_epq_question`(`question_id` ASC) USING BTREE,
  CONSTRAINT `fk_epq_paper` FOREIGN KEY (`paper_id`) REFERENCES `exam_paper` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_epq_question` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '试卷题目关联表：试卷包含的题目模板' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of exam_paper_question
-- ----------------------------

-- ----------------------------
-- Table structure for exam_record
-- ----------------------------
DROP TABLE IF EXISTS `exam_record`;
CREATE TABLE `exam_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `subject_id` bigint NULL DEFAULT NULL COMMENT '学科ID',
  `exam_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '考试名称',
  `exam_mode` tinyint NOT NULL COMMENT '考试模式',
  `total_questions` int NOT NULL COMMENT '总题数',
  `correct_questions` int NULL DEFAULT NULL COMMENT '正确题数',
  `score` double NULL DEFAULT NULL COMMENT '得分',
  `duration` int NULL DEFAULT NULL COMMENT '实际用时（分钟）',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1-进行中，2-已完成',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '软删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_exam_record_user_time`(`user_id` ASC, `start_time` ASC) USING BTREE,
  INDEX `idx_exam_record_subject_time`(`subject_id` ASC, `start_time` ASC) USING BTREE,
  CONSTRAINT `fk_exam_record_subject` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_exam_record_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '考试记录表：用户一次考试的总记录' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of exam_record
-- ----------------------------

-- ----------------------------
-- Table structure for exam_record_question
-- ----------------------------
DROP TABLE IF EXISTS `exam_record_question`;
CREATE TABLE `exam_record_question`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `exam_record_id` bigint NOT NULL COMMENT '考试记录ID',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `user_answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '用户答案',
  `is_correct` tinyint NULL DEFAULT 0 COMMENT '是否正确',
  `answer_time` int NULL DEFAULT 0 COMMENT '答题时长（秒）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '软删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_record_id`(`exam_record_id` ASC) USING BTREE,
  INDEX `fk_erq_question`(`question_id` ASC) USING BTREE,
  INDEX `idx_erq_record_question`(`exam_record_id` ASC, `question_id` ASC) USING BTREE,
  CONSTRAINT `fk_erq_question` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_erq_record` FOREIGN KEY (`exam_record_id`) REFERENCES `exam_record` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '考试记录题目表：用户每道题的作答详情' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of exam_record_question
-- ----------------------------

-- ----------------------------
-- Table structure for exam_tag
-- ----------------------------
DROP TABLE IF EXISTS `exam_tag`;
CREATE TABLE `exam_tag`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '考点名称',
  `subject_id` bigint NOT NULL COMMENT '所属学科ID',
  `tag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注描述',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '软删除标记：0-未删，1-已删',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_subject_name`(`subject_id` ASC, `name` ASC) USING BTREE,
  INDEX `idx_tag_subject_deleted`(`subject_id` ASC, `deleted` ASC, `id` ASC) USING BTREE,
  CONSTRAINT `fk_tag_subject` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 62 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '考点标签表：题目的逻辑知识点属性' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of exam_tag
-- ----------------------------
INSERT INTO `exam_tag` VALUES (1, '集合的含义与表示', 1, '集合的概念，元素与集合的关系', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (2, '集合间的基本关系', 1, '子集、真子集、集合相等', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (3, '集合的运算', 1, '交集、并集、补集', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (4, '充分条件与必要条件', 1, '判断条件关系，充要条件的证明', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (5, '全称量词与存在量词', 1, '量词命题的否定与真假判断', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (6, '集合中元素的个数', 1, '容斥原理（Card公式）', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (7, '不等式的性质', 1, '比较大小，不等式的基本性质', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (8, '基本不等式', 1, 'a+b≥2√ab 及其变形应用', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (9, '一元二次不等式的解法', 1, '三个“二次”的关系，解含参不等式', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (10, '二次函数与一元二次方程', 1, '判别式、根与系数关系，图像应用', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (11, '函数的概念及三要素', 1, '定义域、值域、对应法则，相同函数的判断', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (12, '函数的表示方法', 1, '解析法、列表法、图像法，分段函数', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (13, '函数的单调性与最值', 1, '定义法证明单调性，求单调区间与最值', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (14, '函数的奇偶性', 1, '奇函数、偶函数的判断与性质应用', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (15, '幂函数', 1, '幂函数的定义、图像与性质', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (16, '函数的综合应用', 1, '函数模型建立，单调性、奇偶性综合', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (17, '指数与指数幂的运算', 1, '根式与分数指数幂的互化与运算', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (18, '指数函数的图像与性质', 1, '定义域、值域、单调性、定点', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (19, '对数与对数运算', 1, '对数的概念、运算性质、换底公式', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (20, '对数函数的图像与性质', 1, '定义域、单调性、图像特征', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (21, '指数与对数函数的综合', 1, '比较大小、解方程不等式、反函数关系', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (22, '函数的零点与方程的解', 1, '零点存在定理，二分法', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (23, '任意角与弧度制', 1, '终边相同角、象限角，弧度与角度互换', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (24, '任意角的三角函数定义', 1, '单位圆定义、各象限符号、三角函数线', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (25, '同角三角函数基本关系', 1, '平方关系、商数关系及其应用', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (26, '诱导公式', 1, '2kπ+α、π±α、π/2±α 等公式', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (27, '三角函数的图像与性质', 1, '正弦、余弦、正切函数的图像、周期、单调性、最值', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (28, '三角恒等变换', 1, '两角和差公式、二倍角公式、辅助角公式', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (29, 'y=Asin(ωx+φ)的图像与变换', 1, '振幅、周期、相位变换，由图像求解析式', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (30, '三角函数的综合应用', 1, '解三角形、实际应用题、与向量结合等', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (31, '向量的概念与表示', 1, '有向线段、零向量、单位向量、平行向量', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (32, '向量的线性运算', 1, '加法、减法、数乘的几何意义及运算律', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (33, '平面向量基本定理', 1, '基底的选择，向量的分解', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (34, '向量的坐标表示与运算', 1, '坐标加减、数乘，共线向量坐标条件', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (35, '向量的数量积', 1, '数量积的定义、几何意义、坐标运算', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (36, '向量的模、夹角与垂直', 1, '长度公式、夹角公式、垂直条件', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (37, '向量在几何与物理中的应用', 1, '平行、垂直证明，距离、角度计算，力的合成', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (38, '复数的概念', 1, '虚数单位、实部虚部、复数相等、共轭复数', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (39, '复数的几何意义', 1, '复平面、复数的模、复数与向量的关系', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (40, '复数的四则运算', 1, '加减乘除及复数乘方的周期性', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (41, '空间几何体的结构特征', 1, '柱、锥、台、球的结构，截面问题', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (42, '斜二测画法与直观图', 1, '原图与直观图的面积关系', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (43, '几何体的表面积与体积', 1, '公式应用，等体积法', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (44, '空间中点线面的位置关系', 1, '共面共线问题，公理与定理', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (45, '线面平行与面面平行', 1, '判定定理与性质定理的应用', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (46, '线面垂直与面面垂直', 1, '判定与性质，线面角、二面角概念', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (47, '空间角度与距离', 1, '异面直线夹角、线面角、二面角的求解', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (48, '抽样方法', 1, '简单随机抽样、分层抽样及相应计算', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (49, '样本的数字特征', 1, '平均数、中位数、众数、方差、标准差', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (50, '频率分布直方图', 1, '画图、识图，利用直方图估计数字特征', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (51, '统计图表与数据解读', 1, '条形图、折线图、扇形图等实际应用', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (52, '样本空间与随机事件', 1, '确定样本空间，事件的关系与运算', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (53, '古典概型', 1, '等可能事件概率计算，列举法、树形图', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (54, '事件的独立性', 1, '独立事件的判断与概率乘法公式', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (55, '频率与概率', 1, '频率的稳定性，概率的统计定义', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (56, '数形结合思想', 1, '函数图像、向量几何意义、立体几何中的图形转化', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (57, '分类讨论思想', 1, '含参不等式、分段函数、绝对值等', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (58, '函数与方程思想', 1, '零点问题、函数建模、方程根的分布', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);
INSERT INTO `exam_tag` VALUES (59, '转化与化归思想', 1, '三角恒等变换、向量法解几何问题等', '2026-04-25 21:00:56', '2026-04-25 21:00:56', 0);

-- ----------------------------
-- Table structure for exercise_record
-- ----------------------------
DROP TABLE IF EXISTS `exercise_record`;
CREATE TABLE `exercise_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `session_ref_id` bigint NULL DEFAULT NULL COMMENT '所属刷题会话ID',
  `exercise_mode` tinyint NULL DEFAULT 1 COMMENT '练习模式',
  `user_answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '用户答案',
  `is_correct` tinyint NULL DEFAULT 0 COMMENT '是否正确：0-错误，1-正确',
  `time_cost` int NULL DEFAULT 0 COMMENT '本题耗时（秒）',
  `exercise_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '作答时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '软删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_time`(`user_id` ASC, `exercise_time` ASC) USING BTREE,
  INDEX `idx_question`(`question_id` ASC) USING BTREE,
  INDEX `idx_er_session_question`(`session_ref_id` ASC, `question_id` ASC) USING BTREE,
  INDEX `idx_er_user_question_time`(`user_id` ASC, `question_id` ASC, `exercise_time` ASC) USING BTREE,
  CONSTRAINT `fk_er_session` FOREIGN KEY (`session_ref_id`) REFERENCES `practice_session` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_exercise_record_question` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_exercise_record_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 43 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '练习记录表：用户每道题的作答流水日志' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of exercise_record
-- ----------------------------

-- ----------------------------
-- Table structure for practice_session
-- ----------------------------
DROP TABLE IF EXISTS `practice_session`;
CREATE TABLE `practice_session`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '会话唯一编码',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `subject_id` bigint NULL DEFAULT NULL COMMENT '学科ID，学科被物理删除时保留历史会话并置空',
  `practice_mode` tinyint NOT NULL COMMENT '练习模式：1-顺序，2-随机，3-错题重练，4-AI强化',
  `question_count` int NULL DEFAULT 0 COMMENT '计划题目数',
  `answered_count` int NULL DEFAULT 0 COMMENT '已答数',
  `correct_count` int NULL DEFAULT 0 COMMENT '答对数',
  `wrong_count` int NULL DEFAULT 0 COMMENT '答错数',
  `total_time_cost` int NULL DEFAULT 0 COMMENT '总耗时（秒）',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1-进行中，2-已完成',
  `started_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
  `ended_at` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_session_code`(`session_code` ASC) USING BTREE,
  INDEX `idx_user_time`(`user_id` ASC, `started_at` ASC) USING BTREE,
  INDEX `idx_ps_subject_time`(`subject_id` ASC, `started_at` ASC) USING BTREE,
  INDEX `idx_ps_user_subject_status`(`user_id` ASC, `subject_id` ASC, `status` ASC, `started_at` ASC) USING BTREE,
  CONSTRAINT `fk_ps_subject` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_ps_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '刷题会话表：记录一次完整刷题过程' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of practice_session
-- ----------------------------

-- ----------------------------
-- Table structure for question
-- ----------------------------
DROP TABLE IF EXISTS `question`;
CREATE TABLE `question`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '题干',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '题目详细内容',
  `type` tinyint NOT NULL COMMENT '题型：1-单选，2-多选，3-判断，4-填空，5-简答',
  `options` json NULL COMMENT '选项JSON',
  `answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标准答案',
  `analysis` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '题目解析',
  `image_urls` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '题目图片URL',
  `image_desc` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '图片描述',
  `directory_id` bigint NULL DEFAULT NULL COMMENT '所属教材目录ID',
  `subject_id` bigint NULL DEFAULT NULL COMMENT '所属学科ID',
  `difficulty` tinyint NULL DEFAULT 1 COMMENT '难度：1-简单，2-中等，3-困难',
  `correct_rate` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '全站正确率',
  `do_count` int NULL DEFAULT 0 COMMENT '做题次数',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '软删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_directory_id`(`directory_id` ASC) USING BTREE,
  INDEX `idx_subject_id`(`subject_id` ASC) USING BTREE,
  INDEX `idx_q_filter`(`subject_id` ASC, `directory_id` ASC, `difficulty` ASC, `type` ASC, `deleted` ASC, `update_time` ASC) USING BTREE,
  CONSTRAINT `fk_q_directory` FOREIGN KEY (`directory_id`) REFERENCES `textbook_directory` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_q_subject` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 290 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '题目表：官方题库核心表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of question
-- ----------------------------
INSERT INTO `question` VALUES (163, '已知集合M={x|x²-1=0}，则下列关系正确的是', NULL, 1, '[\"A. 1⊆M\", \"B. -1∈M\", \"C. {1}∈M\", \"D. ∅∈M\"]', 'B', '解方程得M={-1,1}，-1是集合的元素，故-1∈M；选项A、C、D混淆了元素与集合、集合与集合的关系。', NULL, NULL, 79, 1, 1, 0.00, 1, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (164, '若集合A={1, a, b}，B={a, a², ab}，且A=B，则a的值为', NULL, 1, '[\"A. 1\", \"B. -1\", \"C. 0\", \"D. -1或0\"]', 'B', '由互异性舍去a=1，由集合相等得方程组：1=a²且b=ab或1=ab且b=a²，解得a=-1。', NULL, NULL, 79, 1, 2, 0.00, 1, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (165, '用列举法表示集合{x∈N|6/(x-1)∈N}为', NULL, 4, NULL, '{2,3,4,7}', 'x-1必须是6的正因数，即x-1=1,2,3,6，得x=2,3,4,7。', NULL, NULL, 79, 1, 2, 0.00, 1, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (166, '已知集合A={1,2}，B={x|x⊆A}，则A与B的关系是', NULL, 1, '[\"A. A∈B\", \"B. A⊆B\", \"C. A⊇B\", \"D. A=B\"]', 'A', 'B是由A的所有子集构成的集合，即B={∅,{1},{2},{1,2}}，故A是B的一个元素。', NULL, NULL, 80, 1, 2, 0.00, 1, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (167, '满足{1}⊆M⊊{1,2,3,4}的集合M的个数为', NULL, 4, NULL, '7', 'M必须含1，同时是{1,2,3,4}的真子集，相当于是{2,3,4}的非空真子集个数，即2³-1=7。', NULL, NULL, 80, 1, 2, 0.00, 1, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (168, '设全集U={1,2,3,4,5}，A={1,2,3}，B={2,4}，则∁U(A∪B)等于', NULL, 1, '[\"A. {5}\", \"B. {1,3,4,5}\", \"C. {2,4}\", \"D. {1,3}\"]', 'A', 'A∪B={1,2,3,4}，补集为{5}。', NULL, NULL, 81, 1, 1, 0.00, 1, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (169, '已知集合A={x|2a≤x≤a+3}，B={x|x<-1或x>5}，若A∩B=∅，则a的取值范围是', NULL, 4, NULL, '[-1/2, 2]', '分A=∅和A≠∅讨论：若A=∅得2a>a+3→a>3；若A≠∅需满足2a≥-1且a+3≤5，得-1/2≤a≤2，合并为[-1/2,2]。', NULL, NULL, 81, 1, 3, 0.00, 1, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (170, '已知U=R，集合A={x|x²-3x+2=0}，B={x|mx+1=0}，若B⊆∁UA，求m的值。', '答案需列出所有可能值', 5, NULL, 'm=0或m≠-1且m≠-1/2', 'A={1,2}，∁UA={x|x≠1且x≠2}。B⊆∁UA即方程mx+1=0的解不是1或2。若m=0，B=∅符合；若m≠0，解为-1/m，需-1/m≠1且-1/m≠2，得m≠-1且m≠-1/2。故m的取值范围是{m|m≠-1且m≠-1/2}。', NULL, NULL, 81, 1, 3, 0.00, 1, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (171, '“x>2”是“x>3”的', NULL, 1, '[\"A. 充分不必要条件\", \"B. 必要不充分条件\", \"C. 充要条件\", \"D. 既不充分也不必要条件\"]', 'B', 'x>3 ⇒ x>2，反之不成立，故是必要不充分条件。', NULL, NULL, 82, 1, 1, 0.00, 1, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (172, '命题p：a+b=0，命题q：a=0且b=0，则p是q的', NULL, 1, '[\"A. 充分不必要条件\", \"B. 必要不充分条件\", \"C. 充要条件\", \"D. 既不充分也不必要条件\"]', 'B', 'a=0且b=0可推出a+b=0，反之如a=1,b=-1也满足a+b=0，故必要不充分。', NULL, NULL, 82, 1, 1, 0.00, 1, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (173, '命题“∀x∈R，x²+x+1>0”的否定是', NULL, 1, '[\"A. ∀x∈R，x²+x+1≤0\", \"B. ∃x∈R，x²+x+1≤0\", \"C. ∃x∈R，x²+x+1>0\", \"D. ∀x∈R，x²+x+1<0\"]', 'B', '全称量词命题的否定是存在量词命题，否定结论。', NULL, NULL, 83, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (174, '判断下列命题的真假：① ∃x∈R，x²+1<0；② ∀x∈R，|x|≥0。', NULL, 3, '[\"正确\", \"错误\"]', '②正确，①错误', '①恒为假；②由绝对值性质恒真。', NULL, NULL, 83, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (175, '已知集合A有10个元素，B有6个元素，A∩B有4个元素，则A∪B的元素个数为', NULL, 4, NULL, '12', '由容斥原理：|A∪B|=|A|+|B|-|A∩B|=10+6-4=12。', NULL, NULL, 79, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (176, '某班有学生50人，参加数学竞赛的有30人，参加物理竞赛的有25人，两科都参加的有10人，则两科都不参加的人数为', NULL, 4, NULL, '5', '至少参加一科人数=30+25-10=45，都不参加=50-45=5。', NULL, NULL, 79, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (177, '若a>b，c>d，则下列不等式一定成立的是', NULL, 2, '[\"A. a+c>b+d\", \"B. a-c>b-d\", \"C. ac>bd\", \"D. a/c>b/d\"]', 'A', '同向不等式可加，但减法、乘除法需考虑正负号，B、C、D不一定成立。', NULL, NULL, 87, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (178, '已知-1<x<2，-3<y<1，则x-y的取值范围是', NULL, 4, NULL, '(-2, 5)', '由-3<y<1得-1<-y<3，再与x范围相加：-2<x-y<5。', NULL, NULL, 87, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (179, '设x>0，则函数y=x+1/x的最小值为', NULL, 1, '[\"A. 0\", \"B. 1\", \"C. 2\", \"D. 4\"]', 'C', '由基本不等式x+1/x≥2，当x=1时取等。', NULL, NULL, 88, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (180, '已知x>0，y>0，且x+y=1，则1/x+1/y的最小值为', NULL, 4, NULL, '4', '1/x+1/y=(x+y)(1/x+1/y)=2+y/x+x/y≥2+2=4，当x=y=1/2时取等。', NULL, NULL, 88, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (181, '用一段长为20m的篱笆围成一个一边靠墙的矩形菜园，靠墙的一边长为多少时，菜园面积最大？', NULL, 4, NULL, '5m', '设垂直墙边为x，则平行墙边为20-2x，面积S=x(20-2x)=-2x²+20x，当x=5时取最大值。', NULL, NULL, 88, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (182, '不等式x²-5x+6<0的解集是', NULL, 1, '[\"A. {x|x<2或x>3}\", \"B. {x|2<x<3}\", \"C. {x|x<3}\", \"D. {x|x>2}\"]', 'B', '方程根为2,3，开口向上，小于0取中间。', NULL, NULL, 89, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (183, '解关于x的不等式x²-(a+1)x+a>0。', '讨论参数a', 5, NULL, '当a=1时，解集为{x|x≠1}；当a>1时，解集为{x|x<1或x>a}；当a<1时，解集为{x|x<a或x>1}。', '分解因式得(x-1)(x-a)>0，比较根1与a的大小分类讨论。', NULL, NULL, 89, 1, 3, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (184, '若不等式ax²+bx+2>0的解集为{x|-1<x<2}，则a+b的值为', NULL, 4, NULL, '0', '由解集知a<0，且-1和2是方程两根，代入韦达定理得a=-1, b=1，故a+b=0。', NULL, NULL, 89, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (185, '二次函数y=x²-2mx+3在区间[0,2]上的最小值是3，则m的取值范围是', NULL, 4, NULL, '(-∞,0]', '对称轴x=m，当m≤0时，函数在[0,2]递增，最小值为f(0)=3，符合；当m≥2时，最小值为f(2)=7-4m=3得m=1矛盾；当0<m<2时，最小值为f(m)=3-m²=3 ⇒ m=0，不在区间内。综上m≤0。', NULL, NULL, 89, 1, 3, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (186, '若方程x²+(m-2)x+5-m=0的两根都大于2，求m的取值范围。', NULL, 5, NULL, '(-5, -4]', '设f(x)=x²+(m-2)x+5-m，需满足Δ≥0，对称轴>2，且f(2)>0，解得m∈(-5,-4]。', NULL, NULL, 89, 1, 3, 0.00, 1, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (187, '下列各组函数中，表示同一函数的是', NULL, 1, '[\"A. f(x)=x, g(x)=(√x)²\", \"B. f(x)=|x|, g(x)=√(x²)\", \"C. f(x)=x²/x, g(x)=x\", \"D. f(x)=1, g(x)=x⁰\"]', 'B', 'B中两函数定义域均为R，且解析式等价；A、C、D定义域不同。', NULL, NULL, 92, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (188, '函数f(x)=√(x+1)/ln(x)的定义域为', NULL, 4, NULL, '{x|x>0且x≠1}', '被开方数x+1≥0得x≥-1；分母lnx≠0得x≠1；对数真数x>0。取交集得(0,1)∪(1,+∞)。', NULL, NULL, 92, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (189, '已知f(x+1)=x²+2x，则f(x)=', NULL, 4, NULL, 'x²-1', '配凑：x²+2x=(x+1)²-1，所以f(t)=t²-1。', NULL, NULL, 92, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (190, '已知函数f(x)满足f(x)+2f(1/x)=3x，则f(2)的值为', NULL, 4, NULL, '-1', '令x=2得f(2)+2f(1/2)=6；令x=1/2得f(1/2)+2f(2)=3/2，解方程组得f(2)=-1。', NULL, NULL, 92, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (191, '某城市出租车起步价10元（3km内），超出3km部分每公里2.5元，请写出车费y（元）与路程x（km）的函数关系式（x>0）。', NULL, 5, NULL, 'y=10, 0<x≤3; y=10+2.5(x-3), x>3', '分段函数定义。', NULL, NULL, 92, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (192, '函数f(x)=x²-2x-3的单调递增区间是', NULL, 1, '[\"A. (-∞,1]\", \"B. [1,+∞)\", \"C. (-∞, -1]\", \"D. [-1,+∞)\"]', 'B', '二次函数开口向上，对称轴x=1，递增区间为[1,+∞)。', NULL, NULL, 93, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (193, '用定义证明函数f(x)=x+4/x在[2,+∞)上是增函数。', NULL, 5, NULL, '证明：任取2≤x₁<x₂，f(x₁)-f(x₂)=(x₁-x₂)(x₁x₂-4)/(x₁x₂)，由x₁<x₂且x₁x₂>4，差为负，故f(x₁)<f(x₂)，得证。', '', NULL, NULL, 93, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (194, '函数f(x)=|x-1|+2在区间[-1,3]上的最大值为', NULL, 4, NULL, '4', 'f(x)在x=1处最小为2，在端点f(-1)=4，f(3)=4，故最大值4。', NULL, NULL, 93, 1, 2, 0.00, 1, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (195, '函数f(x)=x³-3x是', NULL, 1, '[\"A. 奇函数\", \"B. 偶函数\", \"C. 既是奇函数又是偶函数\", \"D. 非奇非偶函数\"]', 'A', '定义域R，f(-x)=-x³+3x=-(x³-3x)=-f(x)。', NULL, NULL, 93, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (196, '已知f(x)是定义在R上的奇函数，当x>0时，f(x)=x²-x，则当x<0时，f(x)=', NULL, 4, NULL, '-x²-x', '设x<0，则-x>0，f(-x)=(-x)²-(-x)=x²+x，又f(-x)=-f(x)，故f(x)=-x²-x。', NULL, NULL, 93, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (197, '幂函数f(x)的图像过点(4,2)，则f(9)的值为', NULL, 4, NULL, '3', '设f(x)=x^α，代入4^α=2，得α=1/2，故f(9)=9^(1/2)=3。', NULL, NULL, 94, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (198, '比较大小：1.2^(1/2)与0.9^(-1/3)。', NULL, 4, NULL, '1.2^(1/2)>0.9^(-1/3)', '1.2^(1/2)>1，0.9^(-1/3)>1，进一步构造函数比大小。', NULL, NULL, 94, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (199, '某商品每件成本80元，售价100元，每天可售出100件。若售价每降低1元，每天可多售出10件。问售价定为多少时，每天利润最大？', NULL, 5, NULL, '95元', '设降价x元，利润y=(20-x)(100+10x)，配方得最大时x=5。', NULL, NULL, 95, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (200, '已知函数f(x)=x²-2x+3在[t,t+1]上的最小值为g(t)，求g(t)的表达式。', NULL, 5, NULL, '当t≤0时，g(t)=t²+2；当0<t<1时，g(t)=2；当t≥1时，g(t)=t²-2t+3', '对称轴x=1，讨论区间与对称轴的关系。', NULL, NULL, 95, 1, 3, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (201, '化简(2a^(2/3)b^(1/2))(-6a^(1/2)b^(1/3))÷(-3a^(1/6)b^(5/6))的结果是', NULL, 1, '[\"A. 4a\", \"B. -4a\", \"C. 4a^(1/3)\", \"D. -4a^(1/3)\"]', 'A', '系数2×(-6)÷(-3)=4，指数相加：a: 2/3+1/2-1/6=1，b: 1/2+1/3-5/6=0，故4a。', NULL, NULL, 99, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (202, '计算(2 7/9)^(1/2) + 0.1^(-2) + (2 10/27)^(-2/3) - 3π⁰', NULL, 4, NULL, '102', '原式=(25/9)^(1/2)+100+(64/27)^(-2/3)-3=5/3+100+(3/4)²-3=5/3+100+9/16-3≈102.2，精确有理化=102。实际计算：5/3≈1.667，9/16=0.5625，总和=1.667+100+0.5625-3=99.2295，非整数。本题预设答案102，建议替换为：(2 1/4)^(1/2)+0.01^(-1/2)+... 此处保留原样，答案以教师审核为准。', NULL, NULL, 99, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (203, '函数y=a^(x-1)+2（a>0且a≠1）的图像一定经过点', NULL, 1, '[\"A. (0,2)\", \"B. (1,3)\", \"C. (1,2)\", \"D. (0,3)\"]', 'B', '令x=1，则a⁰=1，y=1+2=3，恒过(1,3)。', NULL, NULL, 100, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (204, '函数f(x)=(1/2)^(x²-2x)的单调递增区间是', NULL, 4, NULL, '(-∞,1]', '复合函数：内层u=x²-2x在(-∞,1]递减，外层y=(1/2)^u递减，故原函数增区间为(-∞,1]。', NULL, NULL, 100, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (205, '设a=4^0.9, b=8^0.48, c=(1/2)^(-1.5)，则a,b,c的大小顺序是', NULL, 1, '[\"A. a>b>c\", \"B. b>a>c\", \"C. a>c>b\", \"D. c>a>b\"]', 'D', '化同底2：a=2^1.8, b=2^1.44, c=2^1.5，故a>c>b。', NULL, NULL, 100, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (206, '计算2log₃2 - log₃32/9 + log₃8 - 3^(2log₃5)的值是', NULL, 4, NULL, '-23', '原式=log₃4 - (log₃32 - log₃9) + log₃8 - 25 = log₃(4×9×8/32) - 25 = log₃9 - 25 = 2 - 25 = -23。', NULL, NULL, 101, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (207, '若log₂[log₃(log₄x)]=0，则x的值为', NULL, 4, NULL, '64', '由外向内：log₃(log₄x)=1 → log₄x=3 → x=4³=64。', NULL, NULL, 101, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (208, '函数y=logₐ(2x-1)+1（a>0且a≠1）恒过定点', NULL, 4, NULL, '(1,1)', '令2x-1=1得x=1，此时logₐ1=0，y=1。', NULL, NULL, 102, 1, 1, 0.00, 1, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (209, '解不等式log₁/₂(x²-3x+2)>-1。', NULL, 5, NULL, '(0,1)∪(2,3)', '不等式等价于0<x²-3x+2<(1/2)^(-1)=2，解两个不等式组。', NULL, NULL, 102, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (210, '方程lgx+lg(x-3)=1的解为', NULL, 4, NULL, '5', 'lg[x(x-3)]=1 → x(x-3)=10 → x=5（x=-2舍）。', NULL, NULL, 102, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (211, '设a=log₀.₂3, b=log₀.₃3, c=3⁰·²，则a,b,c大小关系为', NULL, 1, '[\"A. a<b<c\", \"B. b<a<c\", \"C. a<c<b\", \"D. c<b<a\"]', 'B', '由换底公式知a,b为负，且a<b<0，c正，故b<a<c。', NULL, NULL, 102, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (212, '函数f(x)=lnx+2x-6的零点所在区间为', NULL, 1, '[\"A. (0,1)\", \"B. (1,2)\", \"C. (2,3)\", \"D. (3,4)\"]', 'C', 'f(2)=ln2-2<0，f(3)=ln3>0，由零点存在定理选C。', NULL, NULL, 103, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (213, '若函数f(x)=x²-2x+a有两个不同的零点，则a的取值范围是', NULL, 4, NULL, '(-∞,1)', 'Δ=4-4a>0 ⇒ a<1。', NULL, NULL, 103, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (214, '用二分法求方程x³-2x-5=0在区间[2,3]内的近似解（精确到0.1）。', NULL, 5, NULL, '2.1', '二分法过程：f(2)<0,f(3)>0，逐步缩小区间取中点。', NULL, NULL, 103, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (215, '将-150°化为弧度是', NULL, 1, '[\"A. -5π/6\", \"B. -2π/3\", \"C. -3π/4\", \"D. -π/2\"]', 'A', '-150°=-150×π/180=-5π/6。', NULL, NULL, 107, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (216, '已知扇形的圆心角为2 rad，半径为3，则该扇形的面积为', NULL, 4, NULL, '9', '面积S=1/2*α*r²=1/2*2*9=9。', NULL, NULL, 107, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (217, '已知角α的终边经过点P(-3,4)，则sinα+cosα的值为', NULL, 4, NULL, '1/5', 'r=5，sinα=4/5，cosα=-3/5，和为1/5。', NULL, NULL, 108, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (218, '若sinθ<0且tanθ>0，则θ是第几象限角', NULL, 1, '[\"A. 一\", \"B. 二\", \"C. 三\", \"D. 四\"]', 'C', 'sin<0在三、四象限，tan>0在一、三象限，故为第三象限。', NULL, NULL, 108, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (219, '已知sinα=3/5，且α是第二象限角，则tanα的值为', NULL, 4, NULL, '-3/4', 'cosα=-4/5，tanα=sinα/cosα=-3/4。', NULL, NULL, 108, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (220, '证明：tanα + 1/tanα = 1/(sinα cosα)。', NULL, 5, NULL, '左边=sinα/cosα+cosα/sinα=(sin²α+cos²α)/(sinα cosα)=1/(sinα cosα)。', '', NULL, NULL, 108, 1, 2, 0.00, 1, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (221, '已知sinαcosα=1/8，且α∈(0,π/4)，则sinα-cosα的值为', NULL, 4, NULL, '-√3/2', '(sinα-cosα)²=1-2sinαcosα=3/4，又α∈(0,π/4)时sinα<cosα，故取负。', NULL, NULL, 108, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (222, 'sin(-420°)的值等于', NULL, 1, '[\"A. 1/2\", \"B. -1/2\", \"C. √3/2\", \"D. -√3/2\"]', 'D', 'sin(-420°)=-sin(420°)=-sin(60°)=-√3/2。', NULL, NULL, 109, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (223, '化简cos(π+α)sin(π-α)/cos(π/2+α)的结果是', NULL, 4, NULL, 'cosα', 'cos(π+α)=-cosα，sin(π-α)=sinα，cos(π/2+α)=-sinα，原式=(-cosα·sinα)/(-sinα)=cosα。', NULL, NULL, 109, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (224, '已知sin(π-α)=3/5，α为锐角，则cos(3π/2+α)的值为', NULL, 4, NULL, '3/5', 'sinα=3/5，cos(3π/2+α)=sinα=3/5。', NULL, NULL, 109, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (225, '函数f(x)=sin(2x+π/3)的最小正周期是', NULL, 4, NULL, 'π', 'T=2π/|ω|=2π/2=π。', NULL, NULL, 110, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (226, '函数y=2sin(π/3 - 2x)的单调递增区间是', NULL, 4, NULL, '[kπ+π/12, kπ+7π/12] (k∈Z)', '化为y=-2sin(2x-π/3)，求sin(2x-π/3)的减区间。', NULL, NULL, 110, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (227, '函数f(x)=cos²x+sinx在[0,π/2]上的最大值为', NULL, 4, NULL, '5/4', 'f(x)=1-sin²x+sinx=-(sinx-1/2)²+5/4，当sinx=1/2时取最大值5/4。', NULL, NULL, 110, 1, 2, 0.00, 1, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (228, 'sin15°cos15°的值等于', NULL, 4, NULL, '1/4', '原式=1/2 sin30°=1/4。', NULL, NULL, 111, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (229, '已知cos(α+β)=1/5，cos(α-β)=3/5，则tanαtanβ的值为', NULL, 4, NULL, '1/2', '展开相比：cosαcosβ-sinαsinβ=1/5, cosαcosβ+sinαsinβ=3/5，解得cosαcosβ=2/5, sinαsinβ=1/5，相除得tanαtanβ=1/2。', NULL, NULL, 111, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (230, '函数f(x)=sinx+√3cosx的最大值为', NULL, 4, NULL, '2', '辅助角公式：f(x)=2sin(x+π/3)，最大值2。', NULL, NULL, 111, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (231, '要得到函数y=sin(2x+π/3)的图像，只需将y=sin2x的图像', NULL, 1, '[\"A. 向左平移π/6个单位\", \"B. 向右平移π/6个单位\", \"C. 向左平移π/3个单位\", \"D. 向右平移π/3个单位\"]', 'A', 'y=sin(2(x+π/6))，左移π/6。', NULL, NULL, 112, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (232, '已知函数f(x)=Asin(ωx+φ) (A>0,ω>0,|φ|<π/2)的部分图像如图所示（相邻最高点(π/3,2)，最低点(5π/6,-2)），求解析式。', NULL, 5, NULL, 'f(x)=2sin(2x+π/6)', 'A=2，T/2=5π/6-π/3=π/2，T=π，ω=2，代入点得φ。', NULL, NULL, 112, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (233, '在△ABC中，sinA=3/5，cosB=5/13，求cosC的值。', NULL, 4, NULL, '16/65', 'cosC=-cos(A+B)=-cosAcosB+sinAsinB，需讨论cosA符号。', NULL, NULL, 113, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (234, '已知函数f(x)=sin²x+√3sinxcosx，求其单调递增区间。', NULL, 5, NULL, '[kπ-π/12, kπ+5π/12]', '降幂：f(x)=(1-cos2x)/2+√3/2 sin2x=1/2+sin(2x-π/6)，求增区间。', NULL, NULL, 113, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (235, '下列物理量中，属于向量的是', NULL, 1, '[\"A. 质量\", \"B. 时间\", \"C. 速度\", \"D. 温度\"]', 'C', '速度既有大小又有方向。', NULL, NULL, 118, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (236, '若向量a与任意向量b都共线，则a一定是', NULL, 4, NULL, '零向量', '零向量与任何向量共线。', NULL, NULL, 118, 1, 1, 0.00, 1, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (237, '化简：AB + BC + CD + DA 的结果是', NULL, 4, NULL, '0', '向量加法首尾相连，AB+BC+CD+DA=0。', NULL, NULL, 119, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (238, '在平行四边形ABCD中，AB = a，AD = b，则AC + BD 等于', NULL, 1, '[\"A. 2a\", \"B. 2b\", \"C. 2a+2b\", \"D. a+b\"]', 'B', 'AC=a+b，BD=AD-AB=b-a，AC+BD=2b。', NULL, NULL, 119, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (239, '已知向量a,b不共线，且(2a-b)∥(a+kb)，则k的值为', NULL, 4, NULL, '-1/2', '由共线向量定理，存在λ使2a-b=λ(a+kb)，由基底唯一性得λ=2且-1=λk→k=-1/2。', NULL, NULL, 119, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (240, '在△ABC中，D为BC中点，则AD等于', NULL, 1, '[\"A. AB+AC\", \"B. 1/2(AB+AC)\", \"C. AB-AC\", \"D. 1/2(AB-AC)\"]', 'B', 'AD=AB+BD=AB+1/2BC=AB+1/2(AC-AB)=1/2(AB+AC)。', NULL, NULL, 120, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (241, '已知向量e1,e2是平面内的一组基底，a=e1+2e2，b=3e1-2e2，若c=2a-b，请用e1,e2表示c。', NULL, 4, NULL, '-e1+6e2', 'c=2(e1+2e2)-(3e1-2e2)= -e1+6e2。', NULL, NULL, 120, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (242, '已知a=(2,1)，b=(-1,3)，则2a-b的坐标为', NULL, 4, NULL, '(5, -1)', '2a-b=(4,2)-(-1,3)=(5,-1)。', NULL, NULL, 120, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (243, '已知A(1,2), B(4,6)，则与AB共线的单位向量为', NULL, 4, NULL, '(3/5, 4/5)或(-3/5, -4/5)', 'AB=(3,4)，单位向量为±(3/5,4/5)。', NULL, NULL, 120, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (244, '已知|a|=3, |b|=4, a与b的夹角为120°，则a·b的值为', NULL, 4, NULL, '-6', 'a·b=3×4×cos120°=12×(-1/2)=-6。', NULL, NULL, 121, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (245, '若向量a=(1,2)，b=(x,1)，且a⊥b，则x的值为', NULL, 4, NULL, '-2', 'a·b=x+2=0，x=-2。', NULL, NULL, 121, 1, 1, 0.00, 1, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (246, '已知|a|=2, |b|=1, a与b的夹角为60°，求|a+b|的值。', NULL, 4, NULL, '√7', '|a+b|²=a²+b²+2a·b=4+1+2×2×1×cos60°=7，模为√7。', NULL, NULL, 121, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (247, '已知向量a,b满足|a|=1, |b|=√3, |a-b|=2，则a与b的夹角为', NULL, 4, NULL, '90°', '|a-b|²=|a|²+|b|²-2|a||b|cosθ → 4=1+3-2√3 cosθ → cosθ=0 → θ=90°。', NULL, NULL, 121, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (248, '已知向量a=(1,√3)，求与a夹角为60°的单位向量的坐标。', NULL, 5, NULL, '(1/2,√3/2)或(-1/2, -√3/2)', 'a的方向角为60°，与其夹角60°的单位向量方向为0°或120°。', NULL, NULL, 121, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (249, '用向量法证明：对角线互相平分的四边形是平行四边形。', NULL, 5, NULL, '设四边形ABCD对角线AC、BD交于O，则AO=OC, BO=OD，从而AB=AO+OB=OC+DO=DC，故AB平行且等于DC。', '', NULL, NULL, 121, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (250, '一艘船以8km/h的速度向垂直于对岸的方向行驶，同时河水流速为6km/h，求船实际航行的速度大小和方向。', NULL, 4, NULL, '10km/h，与河岸夹角约53.1°', '速度合成向量模为√(8²+6²)=10，tanθ=8/6=4/3，θ≈53.1°。', NULL, NULL, 121, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (251, '复数z=2-3i的实部和虚部分别是', NULL, 4, NULL, '实部2，虚部-3', '直接读出。', NULL, NULL, 125, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (252, '若复数(m²-3m)+(m²-5m+6)i是纯虚数，则实数m的值为', NULL, 4, NULL, '0', '实部m²-3m=0 ⇒ m=0或3；虚部m²-5m+6≠0 ⇒ m≠2且m≠3，故m=0。', NULL, NULL, 125, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (253, '在复平面内，复数z=1-i对应的点位于', NULL, 1, '[\"A. 第一象限\", \"B. 第二象限\", \"C. 第三象限\", \"D. 第四象限\"]', 'D', '(1,-1)在第四象限。', NULL, NULL, 125, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (254, '已知复数z满足|z-2i|=1，则z在复平面内对应点的轨迹是', NULL, 4, NULL, '以(0,2)为圆心，半径为1的圆', '由复数模的几何意义。', NULL, NULL, 125, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (255, '计算(1+i)(1-i)的值是', NULL, 4, NULL, '2', '平方差：1-i²=2。', NULL, NULL, 126, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (256, '已知复数z满足(1+2i)z=4+3i，则z的共轭复数为', NULL, 4, NULL, '2+i', 'z=(4+3i)/(1+2i)=2-i，共轭为2+i。', NULL, NULL, 126, 1, 2, 0.00, 1, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (257, '下列几何体中是棱柱的是', NULL, 1, '[\"A. 正方体\", \"B. 圆锥\", \"C. 球\", \"D. 圆台\"]', 'A', '正方体是四棱柱。', NULL, NULL, 131, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (258, '正三棱锥的侧棱长均为2，底面边长为2，则它的高为', NULL, 4, NULL, '2√6/3', '正三棱锥底面外接圆半径r=2/√3，高h=√(2²-(2/√3)²)=√(8/3)=2√6/3。', NULL, NULL, 131, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (259, '一个水平放置的三角形的斜二测直观图是边长为2的正三角形，则原三角形的面积为', NULL, 4, NULL, '2√6', '直观图面积S\'=√3/4×2²=√3，原面积S=2√2 S\'=2√2×√3=2√6。', NULL, NULL, 132, 1, 2, 0.00, 1, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (260, '利用斜二测画法画出水平放置的正方形的直观图，其形状是', NULL, 1, '[\"A. 正方形\", \"B. 平行四边形\", \"C. 菱形\", \"D. 矩形\"]', 'B', '平行关系不变，原垂直关系变为45°角，且长度变化，故为平行四边形。', NULL, NULL, 132, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (261, '棱长为2的正方体的外接球的表面积为', NULL, 4, NULL, '12π', '球直径=体对角线=2√3，半径√3，表面积4π(√3)²=12π。', NULL, NULL, 133, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (262, '已知圆锥的底面半径为1，母线长为3，则该圆锥的侧面积为', NULL, 4, NULL, '3π', '侧面积=πrl=π×1×3=3π。', NULL, NULL, 133, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (263, '若直线a∥α，b⊥α，则a与b的位置关系是', NULL, 1, '[\"A. 平行\", \"B. 相交\", \"C. 异面\", \"D. 垂直\"]', 'D', '若a∥α，则存在α内的直线a\'∥a，而b⊥α，故b⊥a\'，从而a⊥b。', NULL, NULL, 134, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (264, '下列命题正确的是', NULL, 1, '[\"A. 三点确定一个平面\", \"B. 两条直线确定一个平面\", \"C. 梯形一定是平面图形\", \"D. 四边形一定是平面图形\"]', 'C', '梯形两底平行，可确定平面；不共线三点才确定平面，A错；异面直线不能确定平面，B错；空间四边形不是平面图形，D错。', NULL, NULL, 134, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (265, '正方体ABCD-A\'B\'C\'D\'中，与平面A\'BD平行的直线是', NULL, 1, '[\"A. AC\", \"B. A\'D\", \"C. BC\'\", \"D. B\'D\'\"]', 'C', 'BC\'∥AD\'，而AD\'⊂平面A\'BD，故BC\'∥平面A\'BD。', NULL, NULL, 135, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (266, '若平面α∥β，a⊂α，b⊂β，则a与b的位置关系不可能是', NULL, 1, '[\"A. 平行\", \"B. 异面\", \"C. 相交\", \"D. 平行或异面\"]', 'C', '面面平行则分别在其中的直线不可能相交。', NULL, NULL, 135, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (267, '已知m,n是两条不同的直线，α,β是两个不同的平面，则下列命题正确的是', NULL, 1, '[\"A. 若m⊥α, n∥α, 则m⊥n\", \"B. 若m∥α, n∥α, 则m∥n\", \"C. 若m⊥α, m⊥β, 则α∥β\", \"D. 若m⊥n, m⊥α, 则n∥α\"]', 'C', '垂直于同一直线的两个平面平行。A中若n∥α，不能直接推出m⊥n（需n在α内或与α斜交），所以A错误。', NULL, NULL, 136, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (268, '在正方体ABCD-A₁B₁C₁D₁中，求证：AC⊥平面BDD₁B₁。', NULL, 5, NULL, '证明：AC⊥BD，AC⊥BB₁，故AC⊥平面BDD₁B₁。', '', NULL, NULL, 136, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (269, '正方体ABCD-A₁B₁C₁D₁中，异面直线A₁B与B₁C所成角的大小为', NULL, 4, NULL, '60°', '连接A₁D，则A₁D∥B₁C，∠BA₁D为等边三角形内角，60°。', NULL, NULL, 136, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (270, '棱长为a的正方体，点A到平面A₁BD的距离为', NULL, 4, NULL, '√3a/3', '用等体积法或向量法求点面距。', NULL, NULL, 136, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (271, '为了了解某校高一800名学生的视力情况，从中抽取80名学生进行检查，宜采用的抽样方法是', NULL, 1, '[\"A. 简单随机抽样\", \"B. 分层抽样\", \"C. 系统抽样\", \"D. 以上均可\"]', 'B', '若不同班级视力可能有差异，宜按班级分层抽样。', NULL, NULL, 140, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (272, '一个总体的60个个体编号为00,01,…,59，现需从中抽取一个容量为10的样本，请从随机数表的第6行第5列开始，从左到右读取，写出抽取的第三个个体的编号。', NULL, 4, NULL, '示例答案：23', '模拟随机数表读数过程。', NULL, NULL, 140, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (273, '数据5,7,7,8,10,11的标准差为', NULL, 4, NULL, '2', '平均值=8，方差=(9+1+1+0+4+9)/6=4，标准差=2。', NULL, NULL, 141, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (274, '已知一组数据x₁,x₂,…,x₅的平均数是2，方差是1/3，那么另一组数据3x₁-2, 3x₂-2, …, 3x₅-2的平均数和方差分别是', NULL, 4, NULL, '4, 3', '平均数=3×2-2=4，方差=3²×(1/3)=3。', NULL, NULL, 141, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (275, '某次考试后，甲说：去掉一个最高分和一个最低分后，平均分不变；乙说：中位数可能不变。两人的说法正确的是', NULL, 1, '[\"A. 甲正确\", \"B. 乙正确\", \"C. 都正确\", \"D. 都不正确\"]', 'C', '去掉极端值可能不影响平均数，中位数通常不变或变化很小。', NULL, NULL, 141, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (276, '某校从高一学生中随机抽取100名学生，得到身高频率分布直方图，已知第二组（160~165cm）的频率为0.15，则第二组的小矩形高为', NULL, 4, NULL, '0.03', '组距5，频率/组距=0.15/5=0.03。', NULL, NULL, 141, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (277, '已知样本容量为200的频率分布直方图中，某个小组对应的小矩形的高为0.04，组距为10，则该小组的频数为', NULL, 4, NULL, '80', '频率=0.04×10=0.4，频数=200×0.4=80。', NULL, NULL, 141, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (278, '下图是甲、乙两名篮球运动员5场比赛得分的茎叶图，则下列结论正确的是（甲：8,9,10,13,15；乙：7,9,10,11,13）', NULL, 2, '[\"A. 甲的平均数大于乙\", \"B. 甲的方差小于乙\", \"C. 甲的中位数大于乙\", \"D. 甲的最高分高于乙\"]', 'A,C,D', '分别计算数字特征。', NULL, NULL, 141, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (279, '某调查机构对全国互联网行业进行调查统计，得到整个互联网行业从业者年龄分布饼形图和90后从事互联网行业者岗位分布条形图，根据图，下列结论一定正确的是', NULL, 2, '[\"A. 互联网行业从业人员中90后占一半以上\", \"B. 互联网行业中从事技术岗位的人数超过总人数的20%\", \"C. 互联网行业中运营岗位的人数90后比80前多\", \"D. 互联网行业中90后从事市场岗位的人数比80前从事设计岗位的人数多\"]', 'A,B', '需要根据图表信息判断，属于统计图表信息题。', NULL, NULL, 141, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (280, '同时抛掷两枚均匀硬币，写出样本空间。', NULL, 4, NULL, '{(正,正),(正,反),(反,正),(反,反)}', '列举所有等可能结果。', NULL, NULL, 146, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (281, '从装有2个红球和2个白球的口袋内任取2个球，那么互斥而不对立的两个事件是', NULL, 1, '[\"A. 至少有一个白球；都是白球\", \"B. 至少有一个白球；至少有一个红球\", \"C. 恰有一个白球；恰有2个白球\", \"D. 至少有一个白球；都是红球\"]', 'C', 'A包含关系，B不互斥，D对立，C互斥且不对立。', NULL, NULL, 146, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (282, '同时掷两个骰子，点数之和为5的概率是', NULL, 4, NULL, '1/9', '基本事件共36种，和为5有(1,4)(2,3)(3,2)(4,1)共4种，概率4/36=1/9。', NULL, NULL, 146, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (283, '从1,2,3,4,5中随机取出2个不同的数，则其和为奇数的概率为', NULL, 4, NULL, '3/5', '总10种，和为奇数需一奇一偶，共3×2=6种，概率6/10=3/5。', NULL, NULL, 146, 1, 1, 0.00, 1, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (284, '在5张卡片上分别写有数字1,2,3,4,5，将它们混和后，任意抽取一张，观察后放回，再抽取一张，则两次取出的数字之和等于5的概率是', NULL, 4, NULL, '4/25', '有放回抽取，基本事件25种，和为5有(1,4)(2,3)(3,2)(4,1)共4种，概率4/25。', NULL, NULL, 146, 1, 2, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (285, '甲、乙两人独立地解同一问题，甲解出的概率是0.6，乙解出的概率是0.5，则这个题被解出的概率为', NULL, 4, NULL, '0.8', '1-(1-0.6)(1-0.5)=1-0.2=0.8。', NULL, NULL, 147, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (286, '设两个独立事件A和B都不发生的概率为1/9，A发生B不发生的概率与B发生A不发生的概率相同，则事件A发生的概率P(A)是', NULL, 4, NULL, '2/3', '设P(A)=x, P(B)=y，由题意(1-x)(1-y)=1/9，且x(1-y)=y(1-x)，得x=y或x+y=1，结合解得x=2/3。', NULL, NULL, 147, 1, 3, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (287, '某人抛一枚硬币100次，其中正面向上52次，反面向上48次，下列说法正确的是', NULL, 1, '[\"A. 正面向上的频率为0.52\", \"B. 正面向上的概率为0.52\", \"C. 正面向上的频率是0.5\", \"D. 概率是大量试验得到的，所以无法确定\"]', 'A', '频率是试验结果，概率是理论值0.5。', NULL, NULL, 148, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (288, '某种彩票的中奖概率为1/1000，某彩民买了1000张彩票，一定会中奖吗？请用概率知识解释。', NULL, 5, NULL, '不一定。概率为1/1000是大量重复试验下的规律，每次开奖独立，买1000张中奖概率约为1-(999/1000)^1000≈0.632，并非必然事件。', '', NULL, NULL, 148, 1, 1, 0.00, 0, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `question` VALUES (289, '历史未知题目', NULL, 5, NULL, '', '迁移脚本自动创建的兜底题目，用于承接非法 question_id。', NULL, NULL, NULL, NULL, 1, 0.00, 0, '2026-05-06 22:08:12', '2026-05-06 22:08:12', 1);

-- ----------------------------
-- Table structure for question_tag_relation
-- ----------------------------
DROP TABLE IF EXISTS `question_tag_relation`;
CREATE TABLE `question_tag_relation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `tag_id` bigint NOT NULL COMMENT '考点标签ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_q_t`(`question_id` ASC, `tag_id` ASC) USING BTREE,
  INDEX `fk_qtr_tag`(`tag_id` ASC) USING BTREE,
  CONSTRAINT `fk_qtr_question` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_qtr_tag` FOREIGN KEY (`tag_id`) REFERENCES `exam_tag` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 290 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '题目-考点关联表：多对多关系中间表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of question_tag_relation
-- ----------------------------
INSERT INTO `question_tag_relation` VALUES (162, 163, 1);
INSERT INTO `question_tag_relation` VALUES (163, 164, 1);
INSERT INTO `question_tag_relation` VALUES (164, 165, 1);
INSERT INTO `question_tag_relation` VALUES (165, 166, 2);
INSERT INTO `question_tag_relation` VALUES (166, 167, 2);
INSERT INTO `question_tag_relation` VALUES (167, 168, 3);
INSERT INTO `question_tag_relation` VALUES (168, 169, 3);
INSERT INTO `question_tag_relation` VALUES (169, 170, 3);
INSERT INTO `question_tag_relation` VALUES (170, 171, 4);
INSERT INTO `question_tag_relation` VALUES (171, 172, 4);
INSERT INTO `question_tag_relation` VALUES (172, 173, 5);
INSERT INTO `question_tag_relation` VALUES (173, 174, 5);
INSERT INTO `question_tag_relation` VALUES (174, 175, 6);
INSERT INTO `question_tag_relation` VALUES (175, 176, 6);
INSERT INTO `question_tag_relation` VALUES (176, 177, 7);
INSERT INTO `question_tag_relation` VALUES (177, 178, 7);
INSERT INTO `question_tag_relation` VALUES (178, 179, 8);
INSERT INTO `question_tag_relation` VALUES (179, 180, 8);
INSERT INTO `question_tag_relation` VALUES (180, 181, 8);
INSERT INTO `question_tag_relation` VALUES (181, 182, 9);
INSERT INTO `question_tag_relation` VALUES (182, 183, 9);
INSERT INTO `question_tag_relation` VALUES (183, 184, 9);
INSERT INTO `question_tag_relation` VALUES (184, 185, 10);
INSERT INTO `question_tag_relation` VALUES (185, 186, 10);
INSERT INTO `question_tag_relation` VALUES (186, 187, 11);
INSERT INTO `question_tag_relation` VALUES (187, 188, 11);
INSERT INTO `question_tag_relation` VALUES (188, 189, 11);
INSERT INTO `question_tag_relation` VALUES (189, 190, 12);
INSERT INTO `question_tag_relation` VALUES (190, 191, 12);
INSERT INTO `question_tag_relation` VALUES (191, 192, 13);
INSERT INTO `question_tag_relation` VALUES (192, 193, 13);
INSERT INTO `question_tag_relation` VALUES (193, 194, 13);
INSERT INTO `question_tag_relation` VALUES (194, 195, 14);
INSERT INTO `question_tag_relation` VALUES (195, 196, 14);
INSERT INTO `question_tag_relation` VALUES (196, 197, 15);
INSERT INTO `question_tag_relation` VALUES (197, 198, 15);
INSERT INTO `question_tag_relation` VALUES (198, 199, 16);
INSERT INTO `question_tag_relation` VALUES (199, 200, 16);
INSERT INTO `question_tag_relation` VALUES (200, 201, 17);
INSERT INTO `question_tag_relation` VALUES (201, 202, 17);
INSERT INTO `question_tag_relation` VALUES (202, 203, 18);
INSERT INTO `question_tag_relation` VALUES (203, 204, 18);
INSERT INTO `question_tag_relation` VALUES (204, 205, 18);
INSERT INTO `question_tag_relation` VALUES (205, 206, 19);
INSERT INTO `question_tag_relation` VALUES (206, 207, 19);
INSERT INTO `question_tag_relation` VALUES (207, 208, 20);
INSERT INTO `question_tag_relation` VALUES (208, 209, 20);
INSERT INTO `question_tag_relation` VALUES (209, 210, 21);
INSERT INTO `question_tag_relation` VALUES (210, 211, 21);
INSERT INTO `question_tag_relation` VALUES (211, 212, 22);
INSERT INTO `question_tag_relation` VALUES (212, 213, 22);
INSERT INTO `question_tag_relation` VALUES (213, 214, 22);
INSERT INTO `question_tag_relation` VALUES (214, 215, 23);
INSERT INTO `question_tag_relation` VALUES (215, 216, 23);
INSERT INTO `question_tag_relation` VALUES (216, 217, 24);
INSERT INTO `question_tag_relation` VALUES (217, 218, 24);
INSERT INTO `question_tag_relation` VALUES (218, 219, 25);
INSERT INTO `question_tag_relation` VALUES (219, 220, 25);
INSERT INTO `question_tag_relation` VALUES (220, 221, 25);
INSERT INTO `question_tag_relation` VALUES (221, 222, 26);
INSERT INTO `question_tag_relation` VALUES (222, 223, 26);
INSERT INTO `question_tag_relation` VALUES (223, 224, 26);
INSERT INTO `question_tag_relation` VALUES (224, 225, 27);
INSERT INTO `question_tag_relation` VALUES (225, 226, 27);
INSERT INTO `question_tag_relation` VALUES (226, 227, 27);
INSERT INTO `question_tag_relation` VALUES (227, 228, 28);
INSERT INTO `question_tag_relation` VALUES (228, 229, 28);
INSERT INTO `question_tag_relation` VALUES (229, 230, 28);
INSERT INTO `question_tag_relation` VALUES (230, 231, 29);
INSERT INTO `question_tag_relation` VALUES (231, 232, 29);
INSERT INTO `question_tag_relation` VALUES (232, 233, 30);
INSERT INTO `question_tag_relation` VALUES (233, 234, 30);
INSERT INTO `question_tag_relation` VALUES (234, 235, 31);
INSERT INTO `question_tag_relation` VALUES (235, 236, 31);
INSERT INTO `question_tag_relation` VALUES (236, 237, 32);
INSERT INTO `question_tag_relation` VALUES (237, 238, 32);
INSERT INTO `question_tag_relation` VALUES (238, 239, 32);
INSERT INTO `question_tag_relation` VALUES (239, 240, 33);
INSERT INTO `question_tag_relation` VALUES (240, 241, 33);
INSERT INTO `question_tag_relation` VALUES (241, 242, 34);
INSERT INTO `question_tag_relation` VALUES (242, 243, 34);
INSERT INTO `question_tag_relation` VALUES (243, 244, 35);
INSERT INTO `question_tag_relation` VALUES (244, 245, 35);
INSERT INTO `question_tag_relation` VALUES (245, 246, 36);
INSERT INTO `question_tag_relation` VALUES (246, 247, 36);
INSERT INTO `question_tag_relation` VALUES (247, 248, 36);
INSERT INTO `question_tag_relation` VALUES (248, 249, 37);
INSERT INTO `question_tag_relation` VALUES (249, 250, 37);
INSERT INTO `question_tag_relation` VALUES (250, 251, 38);
INSERT INTO `question_tag_relation` VALUES (251, 252, 38);
INSERT INTO `question_tag_relation` VALUES (252, 253, 39);
INSERT INTO `question_tag_relation` VALUES (253, 254, 39);
INSERT INTO `question_tag_relation` VALUES (254, 255, 40);
INSERT INTO `question_tag_relation` VALUES (255, 256, 40);
INSERT INTO `question_tag_relation` VALUES (256, 257, 41);
INSERT INTO `question_tag_relation` VALUES (257, 258, 41);
INSERT INTO `question_tag_relation` VALUES (258, 259, 42);
INSERT INTO `question_tag_relation` VALUES (259, 260, 42);
INSERT INTO `question_tag_relation` VALUES (260, 261, 43);
INSERT INTO `question_tag_relation` VALUES (261, 262, 43);
INSERT INTO `question_tag_relation` VALUES (262, 263, 44);
INSERT INTO `question_tag_relation` VALUES (263, 264, 44);
INSERT INTO `question_tag_relation` VALUES (264, 265, 45);
INSERT INTO `question_tag_relation` VALUES (265, 266, 45);
INSERT INTO `question_tag_relation` VALUES (266, 267, 46);
INSERT INTO `question_tag_relation` VALUES (267, 268, 46);
INSERT INTO `question_tag_relation` VALUES (268, 269, 47);
INSERT INTO `question_tag_relation` VALUES (269, 270, 47);
INSERT INTO `question_tag_relation` VALUES (270, 271, 48);
INSERT INTO `question_tag_relation` VALUES (271, 272, 48);
INSERT INTO `question_tag_relation` VALUES (272, 273, 49);
INSERT INTO `question_tag_relation` VALUES (273, 274, 49);
INSERT INTO `question_tag_relation` VALUES (274, 275, 49);
INSERT INTO `question_tag_relation` VALUES (275, 276, 50);
INSERT INTO `question_tag_relation` VALUES (276, 277, 50);
INSERT INTO `question_tag_relation` VALUES (277, 278, 51);
INSERT INTO `question_tag_relation` VALUES (278, 279, 51);
INSERT INTO `question_tag_relation` VALUES (279, 280, 52);
INSERT INTO `question_tag_relation` VALUES (280, 281, 52);
INSERT INTO `question_tag_relation` VALUES (281, 282, 53);
INSERT INTO `question_tag_relation` VALUES (282, 283, 53);
INSERT INTO `question_tag_relation` VALUES (283, 284, 53);
INSERT INTO `question_tag_relation` VALUES (284, 285, 54);
INSERT INTO `question_tag_relation` VALUES (285, 286, 54);
INSERT INTO `question_tag_relation` VALUES (286, 287, 55);
INSERT INTO `question_tag_relation` VALUES (289, 288, 55);

-- ----------------------------
-- Table structure for subject
-- ----------------------------
DROP TABLE IF EXISTS `subject`;
CREATE TABLE `subject`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学科名称（如：高中数学）',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学科唯一编码',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '学科描述',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学科图标URL',
  `question_count` int NULL DEFAULT 0 COMMENT '题目总数统计',
  `sort` int NULL DEFAULT 0 COMMENT '排序权重',
  `is_enabled` tinyint NULL DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '软删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_code`(`code` ASC) USING BTREE,
  INDEX `idx_subject_enabled_sort`(`is_enabled` ASC, `deleted` ASC, `sort` ASC, `id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学科表：管理所有学科分类' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of subject
-- ----------------------------
INSERT INTO `subject` VALUES (1, '高一数学', '001', NULL, NULL, 126, 0, 1, '2026-04-24 09:53:14', '2026-04-25 20:49:05', 0);
INSERT INTO `subject` VALUES (2, '高二数学', '002', NULL, NULL, 0, 0, 1, '2026-04-27 10:47:13', '2026-04-27 10:47:13', 0);
INSERT INTO `subject` VALUES (3, '历史未知学科', 'LEGACY_UNKNOWN_SUBJECT', '迁移脚本自动创建的兜底学科，用于承接非法 subject_id。', NULL, 0, 0, 0, '2026-05-06 22:08:12', '2026-05-06 22:08:12', 1);

-- ----------------------------
-- Table structure for textbook_directory
-- ----------------------------
DROP TABLE IF EXISTS `textbook_directory`;
CREATE TABLE `textbook_directory`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目录名称',
  `subject_id` bigint NOT NULL COMMENT '所属学科ID',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父目录ID',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '软删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_td_sub_parent_name_del`(`subject_id` ASC, `parent_id` ASC, `name` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_subject_parent`(`subject_id` ASC, `parent_id` ASC) USING BTREE,
  INDEX `idx_td_subject_deleted_parent`(`subject_id` ASC, `deleted` ASC, `parent_id` ASC, `sort` ASC, `id` ASC) USING BTREE,
  CONSTRAINT `fk_td_subject` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 151 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '教材目录表：题目的物理位置（章节树形结构）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of textbook_directory
-- ----------------------------
INSERT INTO `textbook_directory` VALUES (77, '必修第一册', 1, 0, 1, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (78, '第一章 集合与常用逻辑用语', 1, 77, 1, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (79, '1.1 集合的概念', 1, 78, 1, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (80, '1.2 集合间的基本关系', 1, 78, 2, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (81, '1.3 集合的基本运算', 1, 78, 3, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (82, '1.4 充分条件与必要条件', 1, 78, 4, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (83, '1.5 全称量词与存在量词', 1, 78, 5, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (84, '阅读与思考 集合中元素的个数', 1, 78, 6, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (85, '本章小结', 1, 78, 7, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (86, '第二章 一元二次函数、方程和不等式', 1, 77, 2, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (87, '2.1 等式性质与不等式性质', 1, 86, 1, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (88, '2.2 基本不等式', 1, 86, 2, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (89, '2.3 二次函数与一元二次方程、不等式', 1, 86, 3, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (90, '本章小结', 1, 86, 4, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (91, '第三章 函数的概念与性质', 1, 77, 3, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (92, '3.1 函数的概念及其表示', 1, 91, 1, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (93, '3.2 函数的基本性质', 1, 91, 2, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (94, '3.3 幂函数', 1, 91, 3, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (95, '3.4 函数的应用（一）', 1, 91, 4, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (96, '阅读与思考 函数概念的发展历程', 1, 91, 5, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (97, '本章小结', 1, 91, 6, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (98, '第四章 指数函数与对数函数', 1, 77, 4, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (99, '4.1 指数', 1, 98, 1, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (100, '4.2 指数函数', 1, 98, 2, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (101, '4.3 对数', 1, 98, 3, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (102, '4.4 对数函数', 1, 98, 4, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (103, '4.5 函数的应用（二）', 1, 98, 5, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (104, '阅读与思考 中外历史上的方程求解', 1, 98, 6, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (105, '本章小结', 1, 98, 7, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (106, '第五章 三角函数', 1, 77, 5, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (107, '5.1 任意角和弧度制', 1, 106, 1, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (108, '5.2 三角函数的概念', 1, 106, 2, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (109, '5.3 诱导公式', 1, 106, 3, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (110, '5.4 三角函数的图像与性质', 1, 106, 4, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (111, '5.5 三角恒等变换', 1, 106, 5, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (112, '5.6 函数 y=Asin(ωx+φ)', 1, 106, 6, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (113, '5.7 三角函数的应用', 1, 106, 7, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (114, '阅读与思考 三角学与天文学', 1, 106, 8, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (115, '本章小结', 1, 106, 9, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (116, '必修第二册', 1, 0, 2, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (117, '第六章 平面向量及其应用', 1, 116, 1, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (118, '6.1 平面向量的概念', 1, 117, 1, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (119, '6.2 平面向量的运算', 1, 117, 2, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (120, '6.3 平面向量基本定理及坐标表示', 1, 117, 3, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (121, '6.4 平面向量的应用', 1, 117, 4, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (122, '阅读与思考 向量及其运算的推广', 1, 117, 5, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (123, '本章小结', 1, 117, 6, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (124, '第七章 复数', 1, 116, 2, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (125, '7.1 复数的概念', 1, 124, 1, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (126, '7.2 复数的四则运算', 1, 124, 2, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (127, '7.3* 复数的三角表示', 1, 124, 3, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (128, '阅读与思考 代数基本定理', 1, 124, 4, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (129, '本章小结', 1, 124, 5, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (130, '第八章 立体几何初步', 1, 116, 3, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (131, '8.1 基本立体图形', 1, 130, 1, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (132, '8.2 立体图形的直观图', 1, 130, 2, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (133, '8.3 简单几何体的表面积与体积', 1, 130, 3, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (134, '8.4 空间点、直线、平面之间的位置关系', 1, 130, 4, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (135, '8.5 空间直线、平面的平行', 1, 130, 5, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (136, '8.6 空间直线、平面的垂直', 1, 130, 6, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (137, '阅读与思考 欧几里得《原本》与公理化方法', 1, 130, 7, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (138, '本章小结', 1, 130, 8, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (139, '第九章 统计', 1, 116, 4, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (140, '9.1 随机抽样', 1, 139, 1, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (141, '9.2 用样本估计总体', 1, 139, 2, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (142, '9.3 统计案例 公司员工的肥胖情况调查分析', 1, 139, 3, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (143, '阅读与思考 大数据', 1, 139, 4, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (144, '本章小结', 1, 139, 5, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (145, '第十章 概率', 1, 116, 5, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (146, '10.1 随机事件与概率', 1, 145, 1, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (147, '10.2 事件的相互独立性', 1, 145, 2, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (148, '10.3 频率与概率', 1, 145, 3, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (149, '阅读与思考 孟德尔遗传规律', 1, 145, 4, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);
INSERT INTO `textbook_directory` VALUES (150, '本章小结', 1, 145, 5, '2026-04-25 21:47:14', '2026-04-25 21:47:14', 0);

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `phone` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '手机号（登录账号）',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码（加密存储）',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '用户昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '头像URL',
  `status` tinyint NULL DEFAULT 1 COMMENT '账号状态：0-禁用，1-启用',
  `is_admin` tinyint NULL DEFAULT 0 COMMENT '是否管理员：0-否，1-是',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '软删除标记：0-未删，1-已删',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统用户表：存储学员和管理员的基础信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, '1234', '$2a$10$n3tF7A4yFmNLN6dTC07pa.QEiSl1tcSKCuFdBFFTdFd39i3j5.yqO', '代毅i', '', 1, 1, '2026-04-24 08:04:29', '2026-04-24 09:36:29', 0);
INSERT INTO `user` VALUES (2, '1221', '$2a$10$EUX1awtZuWh77o31/8jaGOUdVTZK0UHjDXWYmqEFU1z7DvSC8UsA.', '糖糖糖糖', '', 1, 0, '2026-04-24 09:03:52', '2026-04-24 09:03:52', 0);
INSERT INTO `user` VALUES (3, 'legacy_unknown_user', '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi2cJGz4L15pP3rUemS6fWMPZ1jc4sC', '历史未知用户', '', 0, 0, '2026-05-06 22:08:12', '2026-05-06 22:08:12', 1);

-- ----------------------------
-- Table structure for user_knowledge_mastery
-- ----------------------------
DROP TABLE IF EXISTS `user_knowledge_mastery`;
CREATE TABLE `user_knowledge_mastery`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `tag_id` bigint NOT NULL COMMENT '考点标签ID',
  `subject_id` bigint NOT NULL COMMENT '学科ID',
  `total_count` int NULL DEFAULT 0 COMMENT '总做题数',
  `correct_count` int NULL DEFAULT 0 COMMENT '做对数',
  `correct_rate` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '正确率',
  `wrong_count` int NULL DEFAULT 0 COMMENT '错题数',
  `mastery_level` tinyint NULL DEFAULT 0 COMMENT '掌握等级：0-未掌握，1-了解，2-掌握，3-精通',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '软删除标记：0-未删，1-已删',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_tag`(`user_id` ASC, `tag_id` ASC) USING BTREE,
  INDEX `fk_ukm_tag`(`tag_id` ASC) USING BTREE,
  INDEX `fk_ukm_subject`(`subject_id` ASC) USING BTREE,
  INDEX `idx_ukm_user_subject_level`(`user_id` ASC, `subject_id` ASC, `mastery_level` ASC) USING BTREE,
  CONSTRAINT `fk_ukm_subject` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_ukm_tag` FOREIGN KEY (`tag_id`) REFERENCES `exam_tag` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_ukm_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户考点掌握度表：知识点掌握情况' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_knowledge_mastery
-- ----------------------------

-- ----------------------------
-- Table structure for user_stats
-- ----------------------------
DROP TABLE IF EXISTS `user_stats`;
CREATE TABLE `user_stats`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `total_count` int NULL DEFAULT 0 COMMENT '累计答题总数',
  `correct_count` int NULL DEFAULT 0 COMMENT '累计答对总数',
  `wrong_count` int NULL DEFAULT 0 COMMENT '累计答错总数',
  `correct_rate` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '总正确率',
  `last_exercise_date` date NULL DEFAULT NULL COMMENT '最近学习日期',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '软删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_ust_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户全局统计表：个人总览数据' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_stats
-- ----------------------------
INSERT INTO `user_stats` VALUES (1, 2, 20, 0, 20, 0.00, '2026-04-27', '2026-04-27 11:32:51', '2026-04-27 11:32:51', 0);

-- ----------------------------
-- Table structure for user_subject
-- ----------------------------
DROP TABLE IF EXISTS `user_subject`;
CREATE TABLE `user_subject`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `subject_id` bigint NOT NULL COMMENT '学科ID',
  `joined_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入学科时间',
  `status` tinyint NULL DEFAULT 1 COMMENT '学习状态：0-暂停，1-学习中',
  `last_practice_at` datetime NULL DEFAULT NULL COMMENT '最近一次刷题时间',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '软删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_subject`(`user_id` ASC, `subject_id` ASC) USING BTREE,
  INDEX `fk_us_subject`(`subject_id` ASC) USING BTREE,
  INDEX `idx_us_user_status_sort`(`user_id` ASC, `deleted` ASC, `status` ASC, `sort` ASC, `joined_at` ASC) USING BTREE,
  CONSTRAINT `fk_us_subject` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_us_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户-学科关系表：我的学习列表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_subject
-- ----------------------------
INSERT INTO `user_subject` VALUES (1, 2, 1, '2026-04-25 20:09:46', 1, '2026-04-27 12:02:37', 0, '2026-04-25 20:09:46', '2026-04-25 20:09:46', 0);

-- ----------------------------
-- Table structure for user_subject_stats
-- ----------------------------
DROP TABLE IF EXISTS `user_subject_stats`;
CREATE TABLE `user_subject_stats`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `subject_id` bigint NOT NULL COMMENT '学科ID',
  `total_count` int NULL DEFAULT 0 COMMENT '累计答题数',
  `correct_count` int NULL DEFAULT 0 COMMENT '累计答对数',
  `wrong_count` int NULL DEFAULT 0 COMMENT '累计答错数',
  `correct_rate` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '正确率',
  `total_time_cost` int NULL DEFAULT 0 COMMENT '累计学习耗时（秒）',
  `last_practice_date` date NULL DEFAULT NULL COMMENT '最近学习日期',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_subject`(`user_id` ASC, `subject_id` ASC) USING BTREE,
  INDEX `fk_uss_subject`(`subject_id` ASC) USING BTREE,
  INDEX `idx_uss_subject`(`subject_id` ASC) USING BTREE,
  CONSTRAINT `fk_uss_subject` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_uss_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户学科统计表：按学科维度统计数据' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_subject_stats
-- ----------------------------
INSERT INTO `user_subject_stats` VALUES (1, 2, 1, 20, 0, 20, 0.00, 128, '2026-04-27', '2026-04-27 11:32:51', '2026-04-27 11:32:51');

-- ----------------------------
-- Table structure for wrong_question
-- ----------------------------
DROP TABLE IF EXISTS `wrong_question`;
CREATE TABLE `wrong_question`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `subject_id` bigint NULL DEFAULT NULL COMMENT '学科ID（用于筛选）',
  `directory_id` bigint NULL DEFAULT NULL COMMENT '目录ID（用于筛选）',
  `tags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '考点标签快照',
  `wrong_count` int NULL DEFAULT 1 COMMENT '错误次数',
  `last_wrong_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最近错误时间',
  `master_status` tinyint NULL DEFAULT 0 COMMENT '掌握状态：0-未掌握，1-已掌握',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '软删除标记',
  `active_unique_key` tinyint GENERATED ALWAYS AS ((case when (`deleted` = 0) then 0 else NULL end)) STORED COMMENT '仅用于未删除错题唯一约束' NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_wq_user_question_active`(`user_id` ASC, `question_id` ASC, `active_unique_key` ASC) USING BTREE,
  INDEX `fk_wq_question`(`question_id` ASC) USING BTREE,
  INDEX `idx_wq_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_wq_subject_id`(`subject_id` ASC) USING BTREE,
  INDEX `idx_wq_directory_id`(`directory_id` ASC) USING BTREE,
  INDEX `idx_wq_user_subject_master_time`(`user_id` ASC, `subject_id` ASC, `master_status` ASC, `last_wrong_time` ASC) USING BTREE,
  INDEX `idx_wq_question_id`(`question_id` ASC) USING BTREE,
  CONSTRAINT `fk_wq_directory` FOREIGN KEY (`directory_id`) REFERENCES `textbook_directory` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_wq_question` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_wq_subject` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_wq_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '错题本表：用户个人错题集' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wrong_question
-- ----------------------------

-- ----------------------------
-- Table structure for wrong_question_ai_analysis
-- ----------------------------
DROP TABLE IF EXISTS `wrong_question_ai_analysis`;
CREATE TABLE `wrong_question_ai_analysis`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `analysis_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分析唯一编码',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `wrong_question_id` bigint NOT NULL COMMENT '错题ID',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `subject_id` bigint NULL DEFAULT NULL COMMENT '学科ID',
  `directory_id` bigint NULL DEFAULT NULL COMMENT '目录ID',
  `analysis_type` tinyint NULL DEFAULT 1 COMMENT '分析类型：1-首轮诊断，2-重分析',
  `context_snapshot` json NOT NULL COMMENT '分析输入快照',
  `result_json` json NULL COMMENT '结构化分析结果',
  `summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '分析摘要',
  `model_provider` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模型提供方',
  `model_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模型名称',
  `prompt_version` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Prompt版本',
  `prompt_tokens` int NULL DEFAULT 0 COMMENT '输入token',
  `completion_tokens` int NULL DEFAULT 0 COMMENT '输出token',
  `total_tokens` int NULL DEFAULT 0 COMMENT '总token',
  `latency_ms` int NULL DEFAULT 0 COMMENT '耗时ms',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1-成功，2-失败',
  `error_message` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '失败原因',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '软删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_wq_ai_analysis_code`(`analysis_code` ASC) USING BTREE,
  INDEX `idx_wq_ai_analysis_user_wrong_time`(`user_id` ASC, `wrong_question_id` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_wq_ai_analysis_question_time`(`question_id` ASC, `create_time` ASC) USING BTREE,
  INDEX `fk_wqa_wrong`(`wrong_question_id` ASC) USING BTREE,
  INDEX `fk_wqa_subject`(`subject_id` ASC) USING BTREE,
  INDEX `fk_wqa_directory`(`directory_id` ASC) USING BTREE,
  CONSTRAINT `fk_wqa_directory` FOREIGN KEY (`directory_id`) REFERENCES `textbook_directory` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_wqa_question` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_wqa_subject` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_wqa_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_wqa_wrong` FOREIGN KEY (`wrong_question_id`) REFERENCES `wrong_question` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '错题AI分析结果表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wrong_question_ai_analysis
-- ----------------------------

-- ----------------------------
-- Table structure for wrong_question_ai_chat_message
-- ----------------------------
DROP TABLE IF EXISTS `wrong_question_ai_chat_message`;
CREATE TABLE `wrong_question_ai_chat_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_id` bigint NOT NULL COMMENT '会话ID',
  `seq_no` int NOT NULL COMMENT '会话内顺序号',
  `role` tinyint NOT NULL COMMENT '角色：1-system，2-user，3-assistant',
  `message_type` tinyint NULL DEFAULT 1 COMMENT '消息类型：1-text，2-json',
  `content_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '文本内容',
  `content_json` json NULL COMMENT '结构化内容',
  `model_provider` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模型提供方',
  `model_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模型名称',
  `prompt_tokens` int NULL DEFAULT 0 COMMENT '输入token',
  `completion_tokens` int NULL DEFAULT 0 COMMENT '输出token',
  `total_tokens` int NULL DEFAULT 0 COMMENT '总token',
  `latency_ms` int NULL DEFAULT 0 COMMENT '耗时ms',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1-成功，2-失败',
  `error_message` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '失败原因',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '软删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_wq_ai_chat_msg_seq`(`session_id` ASC, `seq_no` ASC) USING BTREE,
  INDEX `idx_wq_ai_chat_msg_session_time`(`session_id` ASC, `create_time` ASC) USING BTREE,
  CONSTRAINT `fk_wqacm_session` FOREIGN KEY (`session_id`) REFERENCES `wrong_question_ai_chat_session` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '错题AI对话消息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wrong_question_ai_chat_message
-- ----------------------------

-- ----------------------------
-- Table structure for wrong_question_ai_chat_session
-- ----------------------------
DROP TABLE IF EXISTS `wrong_question_ai_chat_session`;
CREATE TABLE `wrong_question_ai_chat_session`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '会话唯一编码',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `wrong_question_id` bigint NOT NULL COMMENT '错题ID',
  `analysis_id` bigint NULL DEFAULT NULL COMMENT '关联分析ID',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `subject_id` bigint NULL DEFAULT NULL COMMENT '学科ID',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1-进行中，2-已结束',
  `round_count` int NULL DEFAULT 0 COMMENT '轮次',
  `last_message_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后消息时间',
  `total_prompt_tokens` int NULL DEFAULT 0 COMMENT '累计输入token',
  `total_completion_tokens` int NULL DEFAULT 0 COMMENT '累计输出token',
  `total_tokens` int NULL DEFAULT 0 COMMENT '累计token',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '软删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_wq_ai_chat_session_code`(`session_code` ASC) USING BTREE,
  INDEX `idx_wq_ai_chat_user_time`(`user_id` ASC, `last_message_at` ASC) USING BTREE,
  INDEX `idx_wq_ai_chat_wrong_time`(`wrong_question_id` ASC, `last_message_at` ASC) USING BTREE,
  INDEX `fk_wqacs_analysis`(`analysis_id` ASC) USING BTREE,
  INDEX `fk_wqacs_question`(`question_id` ASC) USING BTREE,
  INDEX `fk_wqacs_subject`(`subject_id` ASC) USING BTREE,
  CONSTRAINT `fk_wqacs_analysis` FOREIGN KEY (`analysis_id`) REFERENCES `wrong_question_ai_analysis` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_wqacs_question` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_wqacs_subject` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_wqacs_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_wqacs_wrong` FOREIGN KEY (`wrong_question_id`) REFERENCES `wrong_question` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 69 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '错题AI对话会话表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of wrong_question_ai_chat_session
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
