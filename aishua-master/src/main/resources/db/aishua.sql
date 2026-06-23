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

 Date: 23/06/2026 16:51:49
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
-- Table structure for ai_grading_task
-- ----------------------------
DROP TABLE IF EXISTS `ai_grading_task`;
CREATE TABLE `ai_grading_task`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `biz_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务类型：PRACTICE/EXAM',
  `biz_record_id` bigint NOT NULL COMMENT '业务记录ID：练习记录ID或考试题目记录ID',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/PROCESSING/SUCCESS/FAILED',
  `trigger_source` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'SUBMIT' COMMENT '触发来源：SUBMIT/MANUAL_RETRY',
  `request_payload_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '评分请求快照',
  `response_payload_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '评分响应快照',
  `error_message` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '失败原因',
  `retry_count` int NULL DEFAULT 0 COMMENT '自动重试次数',
  `max_retry_count` int NULL DEFAULT 0 COMMENT '最大自动重试次数',
  `locked_at` datetime NULL DEFAULT NULL COMMENT '任务锁定时间',
  `started_at` datetime NULL DEFAULT NULL COMMENT '开始评分时间',
  `finished_at` datetime NULL DEFAULT NULL COMMENT '完成评分时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '软删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_ai_grading_status_id`(`status` ASC, `id` ASC) USING BTREE,
  INDEX `idx_ai_grading_biz`(`biz_type` ASC, `biz_record_id` ASC) USING BTREE,
  INDEX `idx_ai_grading_user_time`(`user_id` ASC, `create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI 主观题评分任务表' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '每日统计表：用户每日学习记录（打卡日历）' ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 497 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '教材目录-考点关联表：用于章节与知识点的多对多映射' ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '试卷表：考试试卷模板' ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 38 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '试卷题目关联表：试卷包含的题目模板' ROW_FORMAT = DYNAMIC;

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
  `objective_score` double NULL DEFAULT 0 COMMENT '客观题得分',
  `subjective_score` double NULL DEFAULT 0 COMMENT '主观题得分',
  `grading_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'NOT_REQUIRED' COMMENT '主观题评分汇总状态',
  `pending_subjective_count` int NULL DEFAULT 0 COMMENT '待评分主观题数',
  `failed_subjective_count` int NULL DEFAULT 0 COMMENT '评分失败主观题数',
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
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '考试记录表：用户一次考试的总记录' ROW_FORMAT = DYNAMIC;

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
  `ai_grading_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'AI评分状态',
  `ai_grading_confidence` double NULL DEFAULT NULL COMMENT 'AI评分置信度',
  `ai_grading_feedback` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'AI评分反馈',
  `ai_grading_detail_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'AI评分详情JSON',
  `ai_grading_error_message` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'AI评分失败原因',
  `ai_graded_at` datetime NULL DEFAULT NULL COMMENT 'AI评分完成时间',
  `full_score` double NULL DEFAULT NULL COMMENT '本题满分快照',
  `awarded_score` double NULL DEFAULT NULL COMMENT '本题实际得分',
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
) ENGINE = InnoDB AUTO_INCREMENT = 49 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '考试记录题目表：用户每道题的作答详情' ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 159 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '考点标签表：题目的逻辑知识点属性' ROW_FORMAT = DYNAMIC;

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
  `ai_grading_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'AI评分状态',
  `ai_grading_confidence` double NULL DEFAULT NULL COMMENT 'AI评分置信度',
  `ai_grading_feedback` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'AI评分反馈',
  `ai_grading_detail_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'AI评分详情JSON',
  `ai_grading_error_message` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'AI评分失败原因',
  `ai_graded_at` datetime NULL DEFAULT NULL COMMENT 'AI评分完成时间',
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
) ENGINE = InnoDB AUTO_INCREMENT = 166 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '练习记录表：用户每道题的作答流水日志' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for learning_analysis_knowledge_point
-- ----------------------------
DROP TABLE IF EXISTS `learning_analysis_knowledge_point`;
CREATE TABLE `learning_analysis_knowledge_point`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `report_id` bigint NOT NULL COMMENT '学情分析报告ID',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '知识点名称',
  `module_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所属模块名称',
  `reason_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '薄弱原因说明',
  `mastery_level` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'AI返回的掌握程度',
  `correct_rate` decimal(5, 2) NULL DEFAULT NULL COMMENT '正确率或准确率',
  `sample_count` int NULL DEFAULT NULL COMMENT '样本数量',
  `wrong_count` int NULL DEFAULT NULL COMMENT '错题数量',
  `priority` int NULL DEFAULT NULL COMMENT 'AI返回的优先级',
  `sort_order` int NULL DEFAULT 0 COMMENT '展示排序',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '软删除标记：0-未删，1-已删',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_lakp_report_sort`(`report_id` ASC, `sort_order` ASC) USING BTREE,
  CONSTRAINT `fk_lakp_report` FOREIGN KEY (`report_id`) REFERENCES `learning_analysis_report` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学情分析知识点表：保存报告中的薄弱知识点明细' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for learning_analysis_report
-- ----------------------------
DROP TABLE IF EXISTS `learning_analysis_report`;
CREATE TABLE `learning_analysis_report`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `report_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '唯一报告编码',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `subject_id` bigint NULL DEFAULT NULL COMMENT '学科ID',
  `subject_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学科名称快照',
  `grade` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '年级快照',
  `textbook_version` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '教材版本快照',
  `query_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户分析问题',
  `summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '分析摘要',
  `full_text` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '大模型完整输出文本',
  `result_json` json NULL COMMENT '结构化分析结果JSON',
  `raw_response_json` json NULL COMMENT 'Dify原始响应JSON',
  `data_quality_sufficient` tinyint NULL DEFAULT NULL COMMENT '数据质量是否充足：0-否，1-是',
  `missing_metrics` json NULL COMMENT '数据质量缺失指标JSON',
  `workflow_run_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Dify工作流运行ID',
  `task_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Dify任务ID',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1-成功，2-失败',
  `error_message` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误信息',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '软删除标记：0-未删，1-已删',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_learning_analysis_code`(`report_code` ASC) USING BTREE,
  INDEX `idx_la_user_subject_time`(`user_id` ASC, `subject_id` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_la_workflow_run_id`(`workflow_run_id` ASC) USING BTREE,
  INDEX `fk_la_subject`(`subject_id` ASC) USING BTREE,
  CONSTRAINT `fk_la_subject` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_la_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学情分析报告表：保存用户学情分析结果' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for practice_question_ai_chat_message
-- ----------------------------
DROP TABLE IF EXISTS `practice_question_ai_chat_message`;
CREATE TABLE `practice_question_ai_chat_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `assistant_session_id` bigint NOT NULL COMMENT 'AI助手会话ID',
  `seq_no` int NOT NULL COMMENT '单个会话内的消息序号',
  `role` tinyint NOT NULL COMMENT '消息角色：1-系统，2-用户，3-AI助手',
  `message_type` tinyint NULL DEFAULT 1 COMMENT '消息类型：1-文本，2-JSON',
  `content_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '文本内容',
  `content_json` json NULL COMMENT 'JSON内容',
  `draft_answer_snapshot` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '用户草稿答案快照',
  `draft_time_cost_snapshot` int NULL DEFAULT 0 COMMENT '用户草稿用时快照，单位：秒',
  `model_provider` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模型服务商',
  `model_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模型名称',
  `prompt_tokens` int NULL DEFAULT 0 COMMENT '提示词Token数',
  `completion_tokens` int NULL DEFAULT 0 COMMENT '生成Token数',
  `total_tokens` int NULL DEFAULT 0 COMMENT '总Token数',
  `latency_ms` int NULL DEFAULT 0 COMMENT '响应耗时，单位：毫秒',
  `status` tinyint NULL DEFAULT 1 COMMENT '消息状态：1-成功，2-失败',
  `error_message` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误原因',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除标识：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_pq_ai_chat_msg_seq`(`assistant_session_id` ASC, `seq_no` ASC) USING BTREE,
  INDEX `idx_pq_ai_chat_msg_session_time`(`assistant_session_id` ASC, `create_time` ASC) USING BTREE,
  CONSTRAINT `fk_pqacm_session` FOREIGN KEY (`assistant_session_id`) REFERENCES `practice_question_ai_chat_session` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 35 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '练习题AI助手消息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for practice_question_ai_chat_session
-- ----------------------------
DROP TABLE IF EXISTS `practice_question_ai_chat_session`;
CREATE TABLE `practice_question_ai_chat_session`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `session_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '唯一AI助手会话编码',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `practice_session_id` bigint NOT NULL COMMENT '练习会话ID',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `subject_id` bigint NULL DEFAULT NULL COMMENT '学科ID',
  `exercise_record_id` bigint NULL DEFAULT NULL COMMENT '练习会话中的答题记录ID',
  `trigger_source` tinyint NULL DEFAULT 1 COMMENT '触发来源：1-手动触发，2-自动提醒',
  `status` tinyint NULL DEFAULT 1 COMMENT '会话状态：1-进行中，2-已关闭',
  `round_count` int NULL DEFAULT 0 COMMENT 'AI助手对话轮次',
  `last_message_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最近消息时间',
  `total_prompt_tokens` int NULL DEFAULT 0 COMMENT '累计提示词Token数',
  `total_completion_tokens` int NULL DEFAULT 0 COMMENT '累计生成Token数',
  `total_tokens` int NULL DEFAULT 0 COMMENT '累计Token数',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除标识：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_pq_ai_chat_session_code`(`session_code` ASC) USING BTREE,
  INDEX `idx_pq_ai_chat_user_time`(`user_id` ASC, `last_message_at` ASC) USING BTREE,
  INDEX `idx_pq_ai_chat_session_question_time`(`practice_session_id` ASC, `question_id` ASC, `last_message_at` ASC) USING BTREE,
  INDEX `idx_pqacs_question`(`question_id` ASC) USING BTREE,
  INDEX `idx_pqacs_subject`(`subject_id` ASC) USING BTREE,
  INDEX `idx_pqacs_record`(`exercise_record_id` ASC) USING BTREE,
  CONSTRAINT `fk_pqacs_practice_session` FOREIGN KEY (`practice_session_id`) REFERENCES `practice_session` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_pqacs_question` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_pqacs_record` FOREIGN KEY (`exercise_record_id`) REFERENCES `exercise_record` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_pqacs_subject` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_pqacs_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '练习题AI助手会话表' ROW_FORMAT = DYNAMIC;

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
  `grading_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'NOT_REQUIRED' COMMENT '主观题评分汇总状态',
  `pending_subjective_count` int NULL DEFAULT 0 COMMENT '待评分主观题数',
  `failed_subjective_count` int NULL DEFAULT 0 COMMENT '评分失败主观题数',
  `draft_version` int NULL DEFAULT 0 COMMENT '草稿版本',
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
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '刷题会话表：记录一次完整刷题过程' ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 584 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '题目表：官方题库核心表' ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 737 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '题目-考点关联表：多对多关系中间表' ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学科表：管理所有学科分类' ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 193 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '教材目录表：题目的物理位置（章节树形结构）' ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统用户表：存储学员和管理员的基础信息' ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 45 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户考点掌握度表：知识点掌握情况' ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户全局统计表：个人总览数据' ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户-学科关系表：我的学习列表' ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户学科统计表：按学科维度统计数据' ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 65 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '错题本表：用户个人错题集' ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '错题AI分析结果表' ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '错题AI对话消息表' ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 70 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '错题AI对话会话表' ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
