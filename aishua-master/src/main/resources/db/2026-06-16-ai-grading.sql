-- AI 主观题评分迁移脚本
-- 适用于已有 MySQL 库。执行前建议先备份 aishua 数据库。

CREATE TABLE IF NOT EXISTS `ai_grading_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `biz_type` varchar(20) NOT NULL COMMENT '业务类型：PRACTICE/EXAM',
  `biz_record_id` bigint NOT NULL COMMENT '业务记录ID：练习记录ID或考试题目记录ID',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/PROCESSING/SUCCESS/FAILED',
  `trigger_source` varchar(20) NOT NULL DEFAULT 'SUBMIT' COMMENT '触发来源：SUBMIT/MANUAL_RETRY',
  `request_payload_json` longtext NULL COMMENT '评分请求快照',
  `response_payload_json` longtext NULL COMMENT '评分响应快照',
  `error_message` varchar(1000) NULL COMMENT '失败原因',
  `retry_count` int NULL DEFAULT 0 COMMENT '自动重试次数',
  `max_retry_count` int NULL DEFAULT 0 COMMENT '最大自动重试次数',
  `locked_at` datetime NULL COMMENT '任务锁定时间',
  `started_at` datetime NULL COMMENT '开始评分时间',
  `finished_at` datetime NULL COMMENT '完成评分时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '软删除标记',
  PRIMARY KEY (`id`),
  INDEX `idx_ai_grading_status_id` (`status`, `id`),
  INDEX `idx_ai_grading_biz` (`biz_type`, `biz_record_id`),
  INDEX `idx_ai_grading_user_time` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI 主观题评分任务表';

ALTER TABLE `exercise_record`
  ADD COLUMN `ai_grading_status` varchar(20) NULL COMMENT 'AI评分状态' AFTER `is_correct`,
  ADD COLUMN `ai_grading_confidence` double NULL COMMENT 'AI评分置信度' AFTER `ai_grading_status`,
  ADD COLUMN `ai_grading_feedback` varchar(1000) NULL COMMENT 'AI评分反馈' AFTER `ai_grading_confidence`,
  ADD COLUMN `ai_grading_detail_json` longtext NULL COMMENT 'AI评分详情JSON' AFTER `ai_grading_feedback`,
  ADD COLUMN `ai_grading_error_message` varchar(1000) NULL COMMENT 'AI评分失败原因' AFTER `ai_grading_detail_json`,
  ADD COLUMN `ai_graded_at` datetime NULL COMMENT 'AI评分完成时间' AFTER `ai_grading_error_message`;

ALTER TABLE `practice_session`
  ADD COLUMN `grading_status` varchar(20) NULL DEFAULT 'NOT_REQUIRED' COMMENT '主观题评分汇总状态' AFTER `total_time_cost`,
  ADD COLUMN `pending_subjective_count` int NULL DEFAULT 0 COMMENT '待评分主观题数' AFTER `grading_status`,
  ADD COLUMN `failed_subjective_count` int NULL DEFAULT 0 COMMENT '评分失败主观题数' AFTER `pending_subjective_count`;

ALTER TABLE `exam_record_question`
  ADD COLUMN `ai_grading_status` varchar(20) NULL COMMENT 'AI评分状态' AFTER `is_correct`,
  ADD COLUMN `ai_grading_confidence` double NULL COMMENT 'AI评分置信度' AFTER `ai_grading_status`,
  ADD COLUMN `ai_grading_feedback` varchar(1000) NULL COMMENT 'AI评分反馈' AFTER `ai_grading_confidence`,
  ADD COLUMN `ai_grading_detail_json` longtext NULL COMMENT 'AI评分详情JSON' AFTER `ai_grading_feedback`,
  ADD COLUMN `ai_grading_error_message` varchar(1000) NULL COMMENT 'AI评分失败原因' AFTER `ai_grading_detail_json`,
  ADD COLUMN `ai_graded_at` datetime NULL COMMENT 'AI评分完成时间' AFTER `ai_grading_error_message`,
  ADD COLUMN `full_score` double NULL COMMENT '本题满分快照' AFTER `ai_graded_at`,
  ADD COLUMN `awarded_score` double NULL COMMENT '本题实际得分' AFTER `full_score`;

ALTER TABLE `exam_record`
  ADD COLUMN `objective_score` double NULL DEFAULT 0 COMMENT '客观题得分' AFTER `score`,
  ADD COLUMN `subjective_score` double NULL DEFAULT 0 COMMENT '主观题得分' AFTER `objective_score`,
  ADD COLUMN `grading_status` varchar(20) NULL DEFAULT 'NOT_REQUIRED' COMMENT '主观题评分汇总状态' AFTER `subjective_score`,
  ADD COLUMN `pending_subjective_count` int NULL DEFAULT 0 COMMENT '待评分主观题数' AFTER `grading_status`,
  ADD COLUMN `failed_subjective_count` int NULL DEFAULT 0 COMMENT '评分失败主观题数' AFTER `pending_subjective_count`;
