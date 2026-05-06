/*
  AI刷题系统数据库修复脚本 v2

  适用场景：
  - 在已经执行过 src/main/resources/db/aishua.sql 的数据库上执行。
  - 目标是补齐当前库设计中的完整性约束、常用索引、软删除一致性字段，并尽量保留历史数据。

  执行前建议：
  - 先备份数据库：mysqldump -u root -p aishua > aishua_backup_before_v2.sql
  - 建议在业务低峰或本地测试库先执行。
*/

SET NAMES utf8mb4;
SET SQL_SAFE_UPDATES = 0;

CREATE DATABASE IF NOT EXISTS `aishua`
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_general_ci;

USE `aishua`;

-- ============================================================
-- 1. 幂等DDL工具过程
-- ============================================================
DROP PROCEDURE IF EXISTS add_column_if_not_exists;
DROP PROCEDURE IF EXISTS modify_column_if_not_nullable;
DROP PROCEDURE IF EXISTS add_index_if_not_exists;
DROP PROCEDURE IF EXISTS drop_index_if_exists;
DROP PROCEDURE IF EXISTS add_fk_if_not_exists;

DELIMITER $$

CREATE PROCEDURE add_column_if_not_exists(
    IN p_table_name VARCHAR(64),
    IN p_column_name VARCHAR(64),
    IN p_column_definition TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = p_table_name
          AND column_name = p_column_name
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table_name, '` ADD COLUMN ', p_column_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

CREATE PROCEDURE modify_column_if_not_nullable(
    IN p_table_name VARCHAR(64),
    IN p_column_name VARCHAR(64),
    IN p_column_definition TEXT
)
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = p_table_name
          AND column_name = p_column_name
          AND is_nullable = 'NO'
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table_name, '` MODIFY COLUMN ', p_column_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

CREATE PROCEDURE add_index_if_not_exists(
    IN p_table_name VARCHAR(64),
    IN p_index_name VARCHAR(64),
    IN p_index_definition TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = p_table_name
          AND index_name = p_index_name
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table_name, '` ', p_index_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

CREATE PROCEDURE drop_index_if_exists(
    IN p_table_name VARCHAR(64),
    IN p_index_name VARCHAR(64)
)
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = p_table_name
          AND index_name = p_index_name
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table_name, '` DROP INDEX `', p_index_name, '`');
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

CREATE PROCEDURE add_fk_if_not_exists(
    IN p_table_name VARCHAR(64),
    IN p_constraint_name VARCHAR(64),
    IN p_constraint_definition TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_schema = DATABASE()
          AND table_name = p_table_name
          AND constraint_name = p_constraint_name
          AND constraint_type = 'FOREIGN KEY'
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table_name, '` ADD CONSTRAINT ', p_constraint_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

DELIMITER ;

-- ============================================================
-- 2. 结构补强：软删除一致性、审计字段、历史记录可保留
-- ============================================================
CALL add_column_if_not_exists(
    'exam_tag',
    'deleted',
    '`deleted` tinyint DEFAULT 0 COMMENT ''软删除标记：0-未删，1-已删'' AFTER `update_time`'
);

CALL add_column_if_not_exists(
    'user_knowledge_mastery',
    'create_time',
    '`create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间'' AFTER `mastery_level`'
);

CALL add_column_if_not_exists(
    'user_knowledge_mastery',
    'deleted',
    '`deleted` tinyint DEFAULT 0 COMMENT ''软删除标记：0-未删，1-已删'' AFTER `update_time`'
);

-- practice_session 是历史流水，物理删除 subject 时不应级联删除历史会话，因此允许 subject_id 置空。
CALL modify_column_if_not_nullable(
    'practice_session',
    'subject_id',
    '`subject_id` bigint DEFAULT NULL COMMENT ''学科ID，学科被物理删除时保留历史会话并置空'''
);

-- wrong_question 使用逻辑删除时，原 uk_user_question 会阻止同一题再次进入错题本。
-- 增加“仅约束未删除行”的生成列，允许历史已删除记录保留。
CALL add_column_if_not_exists(
    'wrong_question',
    'active_unique_key',
    '`active_unique_key` tinyint GENERATED ALWAYS AS (CASE WHEN `deleted` = 0 THEN 0 ELSE NULL END) STORED COMMENT ''仅用于未删除错题唯一约束'' AFTER `deleted`'
);

-- ============================================================
-- 3. 数据修复：先消除会导致新增外键失败的历史脏数据
-- ============================================================

-- 3.0 处理 0、负数、NULL 这类不可能满足外键的非法引用。
INSERT INTO `subject` (`name`, `code`, `description`, `is_enabled`, `deleted`)
SELECT '历史未知学科',
       'LEGACY_UNKNOWN_SUBJECT',
       '迁移脚本自动创建的兜底学科，用于承接非法 subject_id。',
       0,
       1
WHERE NOT EXISTS (
    SELECT 1 FROM `subject` WHERE `code` = 'LEGACY_UNKNOWN_SUBJECT'
);

SET @legacy_subject_id = (
    SELECT id FROM `subject` WHERE `code` = 'LEGACY_UNKNOWN_SUBJECT' LIMIT 1
);

INSERT INTO `user` (`phone`, `password`, `nickname`, `status`, `is_admin`, `deleted`)
SELECT 'legacy_unknown_user',
       '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi2cJGz4L15pP3rUemS6fWMPZ1jc4sC',
       '历史未知用户',
       0,
       0,
       1
WHERE NOT EXISTS (
    SELECT 1 FROM `user` WHERE `phone` = 'legacy_unknown_user'
);

SET @legacy_user_id = (
    SELECT id FROM `user` WHERE `phone` = 'legacy_unknown_user' LIMIT 1
);

INSERT INTO `question` (`title`, `type`, `answer`, `analysis`, `deleted`)
SELECT '历史未知题目',
       5,
       '',
       '迁移脚本自动创建的兜底题目，用于承接非法 question_id。',
       1
WHERE NOT EXISTS (
    SELECT 1 FROM `question` WHERE `title` = '历史未知题目' AND `deleted` = 1
);

SET @legacy_question_id = (
    SELECT id FROM `question` WHERE `title` = '历史未知题目' AND `deleted` = 1 LIMIT 1
);

DELETE FROM `user_subject` WHERE user_id IS NULL OR user_id <= 0 OR subject_id IS NULL OR subject_id <= 0;
UPDATE `practice_session` SET user_id = @legacy_user_id WHERE user_id IS NULL OR user_id <= 0;
UPDATE `exercise_record` SET user_id = @legacy_user_id WHERE user_id IS NULL OR user_id <= 0;
DELETE FROM `wrong_question` WHERE user_id IS NULL OR user_id <= 0 OR question_id IS NULL OR question_id <= 0;
DELETE FROM `user_stats` WHERE user_id IS NULL OR user_id <= 0;
DELETE FROM `user_subject_stats` WHERE user_id IS NULL OR user_id <= 0 OR subject_id IS NULL OR subject_id <= 0;
DELETE FROM `daily_stats` WHERE user_id IS NULL OR user_id <= 0;
DELETE FROM `user_knowledge_mastery` WHERE user_id IS NULL OR user_id <= 0 OR tag_id IS NULL OR tag_id <= 0;
UPDATE `ai_generated_question` SET user_id = @legacy_user_id WHERE user_id IS NULL OR user_id <= 0;
UPDATE `exam_record` SET user_id = @legacy_user_id WHERE user_id IS NULL OR user_id <= 0;

UPDATE `textbook_directory` SET subject_id = @legacy_subject_id WHERE subject_id IS NULL OR subject_id <= 0;
UPDATE `exam_tag` SET subject_id = @legacy_subject_id WHERE subject_id IS NULL OR subject_id <= 0;
UPDATE `question` SET subject_id = NULL WHERE subject_id IS NOT NULL AND subject_id <= 0;
UPDATE `practice_session` SET subject_id = NULL WHERE subject_id IS NOT NULL AND subject_id <= 0;
UPDATE `wrong_question` SET subject_id = NULL WHERE subject_id IS NOT NULL AND subject_id <= 0;
UPDATE `user_subject_stats` SET subject_id = @legacy_subject_id WHERE subject_id IS NULL OR subject_id <= 0;
UPDATE `user_knowledge_mastery` SET subject_id = @legacy_subject_id WHERE subject_id IS NULL OR subject_id <= 0;
UPDATE `ai_generated_question` SET subject_id = NULL WHERE subject_id IS NOT NULL AND subject_id <= 0;
UPDATE `exam_paper` SET subject_id = @legacy_subject_id WHERE subject_id IS NULL OR subject_id <= 0;
UPDATE `exam_record` SET subject_id = NULL WHERE subject_id IS NOT NULL AND subject_id <= 0;

DELETE FROM `question_tag_relation` WHERE question_id IS NULL OR question_id <= 0 OR tag_id IS NULL OR tag_id <= 0;
UPDATE `exercise_record` SET question_id = @legacy_question_id WHERE question_id IS NULL OR question_id <= 0;
DELETE FROM `exam_paper_question` WHERE paper_id IS NULL OR paper_id <= 0 OR question_id IS NULL OR question_id <= 0;
UPDATE `exam_record_question` SET question_id = @legacy_question_id WHERE question_id IS NULL OR question_id <= 0;

DELETE FROM `exam_record_question` WHERE exam_record_id IS NULL OR exam_record_id <= 0;

-- 3.1 自动补齐历史缺失用户。此类用户禁用且软删除，只为保留历史数据引用。
INSERT INTO `user` (`id`, `phone`, `password`, `nickname`, `status`, `is_admin`, `deleted`)
SELECT refs.user_id,
       CONCAT('legacy_user_', refs.user_id, '_', SUBSTRING(REPLACE(UUID(), '-', ''), 1, 8)),
       '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi2cJGz4L15pP3rUemS6fWMPZ1jc4sC',
       CONCAT('历史缺失用户#', refs.user_id),
       0,
       0,
       1
FROM (
    SELECT user_id FROM user_subject
    UNION SELECT user_id FROM practice_session
    UNION SELECT user_id FROM exercise_record
    UNION SELECT user_id FROM wrong_question
    UNION SELECT user_id FROM user_stats
    UNION SELECT user_id FROM user_subject_stats
    UNION SELECT user_id FROM daily_stats
    UNION SELECT user_id FROM user_knowledge_mastery
    UNION SELECT user_id FROM ai_generated_question
    UNION SELECT user_id FROM exam_record
) refs
LEFT JOIN `user` u ON u.id = refs.user_id
WHERE refs.user_id IS NOT NULL
  AND refs.user_id > 0
  AND u.id IS NULL;

-- 3.2 自动补齐历史缺失学科。此类学科禁用且软删除，只为保留历史数据引用。
INSERT INTO `subject` (`id`, `name`, `code`, `description`, `is_enabled`, `deleted`)
SELECT refs.subject_id,
       CONCAT('历史缺失学科#', refs.subject_id),
       CONCAT('LEGACY_', refs.subject_id, '_', SUBSTRING(REPLACE(UUID(), '-', ''), 1, 8)),
       '迁移脚本自动补齐的历史占位学科，用于修复外键引用。',
       0,
       1
FROM (
    SELECT subject_id FROM user_subject
    UNION SELECT subject_id FROM textbook_directory
    UNION SELECT subject_id FROM exam_tag
    UNION SELECT subject_id FROM question
    UNION SELECT subject_id FROM practice_session
    UNION SELECT subject_id FROM wrong_question
    UNION SELECT subject_id FROM user_subject_stats
    UNION SELECT subject_id FROM user_knowledge_mastery
    UNION SELECT subject_id FROM ai_generated_question
    UNION SELECT subject_id FROM exam_paper
    UNION SELECT subject_id FROM exam_record
) refs
LEFT JOIN `subject` s ON s.id = refs.subject_id
WHERE refs.subject_id IS NOT NULL
  AND refs.subject_id > 0
  AND s.id IS NULL;

-- 3.3 自动补齐历史缺失题目。占位题目软删除，只为保留作答、错题、考试明细引用。
INSERT INTO `question` (`id`, `title`, `type`, `answer`, `analysis`, `deleted`)
SELECT refs.question_id,
       CONCAT('历史缺失题目#', refs.question_id),
       5,
       '',
       '迁移脚本自动补齐的历史占位题目，用于修复外键引用。',
       1
FROM (
    SELECT question_id FROM question_tag_relation
    UNION SELECT question_id FROM exercise_record
    UNION SELECT question_id FROM wrong_question
    UNION SELECT question_id FROM exam_paper_question
    UNION SELECT question_id FROM exam_record_question
) refs
LEFT JOIN `question` q ON q.id = refs.question_id
WHERE refs.question_id IS NOT NULL
  AND refs.question_id > 0
  AND q.id IS NULL;

-- 3.4 修复目录、学科、会话等可置空引用。
UPDATE `question` q
LEFT JOIN `textbook_directory` d ON d.id = q.directory_id
SET q.directory_id = NULL
WHERE q.directory_id IS NOT NULL
  AND d.id IS NULL;

UPDATE `question` q
JOIN `textbook_directory` d ON d.id = q.directory_id
SET q.directory_id = NULL
WHERE q.directory_id IS NOT NULL
  AND q.subject_id IS NOT NULL
  AND d.subject_id <> q.subject_id;

UPDATE `exercise_record` er
LEFT JOIN `practice_session` ps ON ps.id = er.session_ref_id
SET er.session_ref_id = NULL
WHERE er.session_ref_id IS NOT NULL
  AND ps.id IS NULL;

UPDATE `wrong_question` w
JOIN `question` q ON q.id = w.question_id
SET w.subject_id = COALESCE(w.subject_id, q.subject_id),
    w.directory_id = COALESCE(w.directory_id, q.directory_id)
WHERE w.deleted = 0;

UPDATE `wrong_question` w
LEFT JOIN `subject` s ON s.id = w.subject_id
SET w.subject_id = NULL
WHERE w.subject_id IS NOT NULL
  AND s.id IS NULL;

UPDATE `wrong_question` w
LEFT JOIN `textbook_directory` d ON d.id = w.directory_id
SET w.directory_id = NULL
WHERE w.directory_id IS NOT NULL
  AND d.id IS NULL;

UPDATE `wrong_question` w
JOIN `textbook_directory` d ON d.id = w.directory_id
SET w.directory_id = NULL
WHERE w.directory_id IS NOT NULL
  AND w.subject_id IS NOT NULL
  AND d.subject_id <> w.subject_id;

UPDATE `ai_generated_question` agq
LEFT JOIN `subject` s ON s.id = agq.subject_id
SET agq.subject_id = NULL
WHERE agq.subject_id IS NOT NULL
  AND s.id IS NULL;

UPDATE `ai_generated_question` agq
LEFT JOIN `textbook_directory` d ON d.id = agq.directory_id
SET agq.directory_id = NULL
WHERE agq.directory_id IS NOT NULL
  AND d.id IS NULL;

UPDATE `ai_generated_question` agq
JOIN `textbook_directory` d ON d.id = agq.directory_id
SET agq.directory_id = NULL
WHERE agq.directory_id IS NOT NULL
  AND agq.subject_id IS NOT NULL
  AND d.subject_id <> agq.subject_id;

UPDATE `exam_record` er
LEFT JOIN `subject` s ON s.id = er.subject_id
SET er.subject_id = NULL
WHERE er.subject_id IS NOT NULL
  AND s.id IS NULL;

-- 3.5 关系表没有业务主体时，删除无意义的孤儿关系行。
DELETE qtr
FROM `question_tag_relation` qtr
LEFT JOIN `exam_tag` t ON t.id = qtr.tag_id
WHERE t.id IS NULL;

DELETE ukm
FROM `user_knowledge_mastery` ukm
LEFT JOIN `exam_tag` t ON t.id = ukm.tag_id
WHERE t.id IS NULL;

DELETE epq
FROM `exam_paper_question` epq
LEFT JOIN `exam_paper` ep ON ep.id = epq.paper_id
WHERE ep.id IS NULL;

DELETE erq
FROM `exam_record_question` erq
LEFT JOIN `exam_record` er ON er.id = erq.exam_record_id
WHERE er.id IS NULL;

-- 3.6 如果历史库中已经存在重复的未删除错题，只保留最新一条为未删除，避免新增唯一约束失败。
UPDATE `wrong_question` w
JOIN (
    SELECT user_id, question_id, MAX(id) AS keep_id
    FROM `wrong_question`
    WHERE deleted = 0
    GROUP BY user_id, question_id
    HAVING COUNT(*) > 1
) dup ON dup.user_id = w.user_id
     AND dup.question_id = w.question_id
     AND dup.keep_id <> w.id
SET w.deleted = 1;

-- ============================================================
-- 3.7 新增：错题AI分析与追问表
-- ============================================================
CREATE TABLE IF NOT EXISTS `wrong_question_ai_analysis` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `analysis_code` varchar(64) NOT NULL COMMENT '分析唯一编码',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `wrong_question_id` bigint NOT NULL COMMENT '错题ID',
    `question_id` bigint NOT NULL COMMENT '题目ID',
    `subject_id` bigint DEFAULT NULL COMMENT '学科ID',
    `directory_id` bigint DEFAULT NULL COMMENT '目录ID',
    `analysis_type` tinyint DEFAULT 1 COMMENT '分析类型：1-首轮诊断，2-重分析',
    `context_snapshot` json NOT NULL COMMENT '分析输入快照',
    `result_json` json DEFAULT NULL COMMENT '结构化分析结果',
    `summary` text COMMENT '分析摘要',
    `model_provider` varchar(32) DEFAULT NULL COMMENT '模型提供方',
    `model_name` varchar(64) DEFAULT NULL COMMENT '模型名称',
    `prompt_version` varchar(32) DEFAULT NULL COMMENT 'Prompt版本',
    `prompt_tokens` int DEFAULT 0 COMMENT '输入token',
    `completion_tokens` int DEFAULT 0 COMMENT '输出token',
    `total_tokens` int DEFAULT 0 COMMENT '总token',
    `latency_ms` int DEFAULT 0 COMMENT '耗时ms',
    `status` tinyint DEFAULT 1 COMMENT '状态：1-成功，2-失败',
    `error_message` varchar(1000) DEFAULT NULL COMMENT '失败原因',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint DEFAULT 0 COMMENT '软删除标记',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_wq_ai_analysis_code` (`analysis_code`),
    KEY `idx_wq_ai_analysis_user_wrong_time` (`user_id`, `wrong_question_id`, `create_time`),
    KEY `idx_wq_ai_analysis_question_time` (`question_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='错题AI分析结果表';

CREATE TABLE IF NOT EXISTS `wrong_question_ai_chat_session` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `session_code` varchar(64) NOT NULL COMMENT '会话唯一编码',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `wrong_question_id` bigint NOT NULL COMMENT '错题ID',
    `analysis_id` bigint DEFAULT NULL COMMENT '关联分析ID',
    `question_id` bigint NOT NULL COMMENT '题目ID',
    `subject_id` bigint DEFAULT NULL COMMENT '学科ID',
    `status` tinyint DEFAULT 1 COMMENT '状态：1-进行中，2-已结束',
    `round_count` int DEFAULT 0 COMMENT '轮次',
    `last_message_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '最后消息时间',
    `total_prompt_tokens` int DEFAULT 0 COMMENT '累计输入token',
    `total_completion_tokens` int DEFAULT 0 COMMENT '累计输出token',
    `total_tokens` int DEFAULT 0 COMMENT '累计token',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint DEFAULT 0 COMMENT '软删除标记',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_wq_ai_chat_session_code` (`session_code`),
    KEY `idx_wq_ai_chat_user_time` (`user_id`, `last_message_at`),
    KEY `idx_wq_ai_chat_wrong_time` (`wrong_question_id`, `last_message_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='错题AI对话会话表';

CREATE TABLE IF NOT EXISTS `wrong_question_ai_chat_message` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `session_id` bigint NOT NULL COMMENT '会话ID',
    `seq_no` int NOT NULL COMMENT '会话内顺序号',
    `role` tinyint NOT NULL COMMENT '角色：1-system，2-user，3-assistant',
    `message_type` tinyint DEFAULT 1 COMMENT '消息类型：1-text，2-json',
    `content_text` text COMMENT '文本内容',
    `content_json` json DEFAULT NULL COMMENT '结构化内容',
    `model_provider` varchar(32) DEFAULT NULL COMMENT '模型提供方',
    `model_name` varchar(64) DEFAULT NULL COMMENT '模型名称',
    `prompt_tokens` int DEFAULT 0 COMMENT '输入token',
    `completion_tokens` int DEFAULT 0 COMMENT '输出token',
    `total_tokens` int DEFAULT 0 COMMENT '总token',
    `latency_ms` int DEFAULT 0 COMMENT '耗时ms',
    `status` tinyint DEFAULT 1 COMMENT '状态：1-成功，2-失败',
    `error_message` varchar(1000) DEFAULT NULL COMMENT '失败原因',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint DEFAULT 0 COMMENT '软删除标记',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_wq_ai_chat_msg_seq` (`session_id`, `seq_no`),
    KEY `idx_wq_ai_chat_msg_session_time` (`session_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='错题AI对话消息表';

-- ============================================================
-- 4. 常用查询索引补齐
-- ============================================================
CALL add_index_if_not_exists('subject', 'idx_subject_enabled_sort', 'ADD INDEX `idx_subject_enabled_sort` (`is_enabled`, `deleted`, `sort`, `id`)');

CALL add_index_if_not_exists('user_subject', 'idx_us_user_status_sort', 'ADD INDEX `idx_us_user_status_sort` (`user_id`, `deleted`, `status`, `sort`, `joined_at`)');

CALL add_index_if_not_exists('textbook_directory', 'idx_td_subject_deleted_parent', 'ADD INDEX `idx_td_subject_deleted_parent` (`subject_id`, `deleted`, `parent_id`, `sort`, `id`)');

CALL add_index_if_not_exists('exam_tag', 'idx_tag_subject_deleted', 'ADD INDEX `idx_tag_subject_deleted` (`subject_id`, `deleted`, `id`)');

CALL add_index_if_not_exists('question', 'idx_q_filter', 'ADD INDEX `idx_q_filter` (`subject_id`, `directory_id`, `difficulty`, `type`, `deleted`, `update_time`)');

CALL add_index_if_not_exists('practice_session', 'idx_ps_subject_time', 'ADD INDEX `idx_ps_subject_time` (`subject_id`, `started_at`)');
CALL add_index_if_not_exists('practice_session', 'idx_ps_user_subject_status', 'ADD INDEX `idx_ps_user_subject_status` (`user_id`, `subject_id`, `status`, `started_at`)');

CALL add_index_if_not_exists('exercise_record', 'idx_er_session_question', 'ADD INDEX `idx_er_session_question` (`session_ref_id`, `question_id`)');
CALL add_index_if_not_exists('exercise_record', 'idx_er_user_question_time', 'ADD INDEX `idx_er_user_question_time` (`user_id`, `question_id`, `exercise_time`)');

CALL add_index_if_not_exists('wrong_question', 'idx_wq_user_id', 'ADD INDEX `idx_wq_user_id` (`user_id`)');
CALL add_index_if_not_exists('wrong_question', 'idx_wq_question_id', 'ADD INDEX `idx_wq_question_id` (`question_id`)');
CALL add_index_if_not_exists('wrong_question', 'idx_wq_subject_id', 'ADD INDEX `idx_wq_subject_id` (`subject_id`)');
CALL add_index_if_not_exists('wrong_question', 'idx_wq_directory_id', 'ADD INDEX `idx_wq_directory_id` (`directory_id`)');
CALL add_index_if_not_exists('wrong_question', 'idx_wq_user_subject_master_time', 'ADD INDEX `idx_wq_user_subject_master_time` (`user_id`, `subject_id`, `master_status`, `last_wrong_time`)');

CALL add_index_if_not_exists('daily_stats', 'idx_ds_date', 'ADD INDEX `idx_ds_date` (`stat_date`)');

CALL add_index_if_not_exists('user_subject_stats', 'idx_uss_subject', 'ADD INDEX `idx_uss_subject` (`subject_id`)');

CALL add_index_if_not_exists('user_knowledge_mastery', 'idx_ukm_user_subject_level', 'ADD INDEX `idx_ukm_user_subject_level` (`user_id`, `subject_id`, `mastery_level`)');

CALL add_index_if_not_exists('ai_generated_question', 'idx_agq_subject_directory', 'ADD INDEX `idx_agq_subject_directory` (`subject_id`, `directory_id`, `difficulty`)');
CALL add_index_if_not_exists('ai_generated_question', 'idx_agq_user_practiced', 'ADD INDEX `idx_agq_user_practiced` (`user_id`, `is_practiced`, `generate_time`)');

CALL add_index_if_not_exists('wrong_question_ai_analysis', 'uk_wq_ai_analysis_code', 'ADD UNIQUE KEY `uk_wq_ai_analysis_code` (`analysis_code`)');
CALL add_index_if_not_exists('wrong_question_ai_analysis', 'idx_wq_ai_analysis_user_wrong_time', 'ADD INDEX `idx_wq_ai_analysis_user_wrong_time` (`user_id`, `wrong_question_id`, `create_time`)');
CALL add_index_if_not_exists('wrong_question_ai_analysis', 'idx_wq_ai_analysis_question_time', 'ADD INDEX `idx_wq_ai_analysis_question_time` (`question_id`, `create_time`)');

CALL add_index_if_not_exists('wrong_question_ai_chat_session', 'uk_wq_ai_chat_session_code', 'ADD UNIQUE KEY `uk_wq_ai_chat_session_code` (`session_code`)');
CALL add_index_if_not_exists('wrong_question_ai_chat_session', 'idx_wq_ai_chat_user_time', 'ADD INDEX `idx_wq_ai_chat_user_time` (`user_id`, `last_message_at`)');
CALL add_index_if_not_exists('wrong_question_ai_chat_session', 'idx_wq_ai_chat_wrong_time', 'ADD INDEX `idx_wq_ai_chat_wrong_time` (`wrong_question_id`, `last_message_at`)');

CALL add_index_if_not_exists('wrong_question_ai_chat_message', 'uk_wq_ai_chat_msg_seq', 'ADD UNIQUE KEY `uk_wq_ai_chat_msg_seq` (`session_id`, `seq_no`)');
CALL add_index_if_not_exists('wrong_question_ai_chat_message', 'idx_wq_ai_chat_msg_session_time', 'ADD INDEX `idx_wq_ai_chat_msg_session_time` (`session_id`, `create_time`)');

CALL add_index_if_not_exists('exam_paper', 'idx_ep_subject_status', 'ADD INDEX `idx_ep_subject_status` (`subject_id`, `status`, `deleted`)');

CALL add_index_if_not_exists('exam_record', 'idx_exam_record_user_time', 'ADD INDEX `idx_exam_record_user_time` (`user_id`, `start_time`)');
CALL add_index_if_not_exists('exam_record', 'idx_exam_record_subject_time', 'ADD INDEX `idx_exam_record_subject_time` (`subject_id`, `start_time`)');

CALL add_index_if_not_exists('exam_record_question', 'idx_erq_record_question', 'ADD INDEX `idx_erq_record_question` (`exam_record_id`, `question_id`)');

-- ============================================================
-- 5. 修复错题本软删除唯一性
-- ============================================================
CALL drop_index_if_exists('wrong_question', 'uk_user_question');
CALL add_index_if_not_exists(
    'wrong_question',
    'uk_wq_user_question_active',
    'ADD UNIQUE KEY `uk_wq_user_question_active` (`user_id`, `question_id`, `active_unique_key`)'
);

-- ============================================================
-- 6. 外键补齐
-- ============================================================
CALL add_fk_if_not_exists(
    'textbook_directory',
    'fk_td_subject',
    '`fk_td_subject` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`id`) ON DELETE CASCADE ON UPDATE CASCADE'
);

CALL add_fk_if_not_exists(
    'exam_tag',
    'fk_tag_subject',
    '`fk_tag_subject` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`id`) ON DELETE CASCADE ON UPDATE CASCADE'
);

CALL add_fk_if_not_exists(
    'practice_session',
    'fk_ps_user',
    '`fk_ps_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE'
);

CALL add_fk_if_not_exists(
    'practice_session',
    'fk_ps_subject',
    '`fk_ps_subject` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`id`) ON DELETE SET NULL ON UPDATE CASCADE'
);

CALL add_fk_if_not_exists(
    'exercise_record',
    'fk_er_session',
    '`fk_er_session` FOREIGN KEY (`session_ref_id`) REFERENCES `practice_session` (`id`) ON DELETE SET NULL ON UPDATE CASCADE'
);

CALL add_fk_if_not_exists(
    'wrong_question',
    'fk_wq_subject',
    '`fk_wq_subject` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`id`) ON DELETE SET NULL ON UPDATE CASCADE'
);

CALL add_fk_if_not_exists(
    'wrong_question',
    'fk_wq_directory',
    '`fk_wq_directory` FOREIGN KEY (`directory_id`) REFERENCES `textbook_directory` (`id`) ON DELETE SET NULL ON UPDATE CASCADE'
);

CALL add_fk_if_not_exists(
    'ai_generated_question',
    'fk_agq_subject',
    '`fk_agq_subject` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`id`) ON DELETE SET NULL ON UPDATE CASCADE'
);

CALL add_fk_if_not_exists(
    'ai_generated_question',
    'fk_agq_directory',
    '`fk_agq_directory` FOREIGN KEY (`directory_id`) REFERENCES `textbook_directory` (`id`) ON DELETE SET NULL ON UPDATE CASCADE'
);

CALL add_fk_if_not_exists(
    'wrong_question_ai_analysis',
    'fk_wqa_user',
    '`fk_wqa_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE'
);

CALL add_fk_if_not_exists(
    'wrong_question_ai_analysis',
    'fk_wqa_wrong',
    '`fk_wqa_wrong` FOREIGN KEY (`wrong_question_id`) REFERENCES `wrong_question` (`id`) ON DELETE CASCADE ON UPDATE CASCADE'
);

CALL add_fk_if_not_exists(
    'wrong_question_ai_analysis',
    'fk_wqa_question',
    '`fk_wqa_question` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE CASCADE ON UPDATE CASCADE'
);

CALL add_fk_if_not_exists(
    'wrong_question_ai_analysis',
    'fk_wqa_subject',
    '`fk_wqa_subject` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`id`) ON DELETE SET NULL ON UPDATE CASCADE'
);

CALL add_fk_if_not_exists(
    'wrong_question_ai_analysis',
    'fk_wqa_directory',
    '`fk_wqa_directory` FOREIGN KEY (`directory_id`) REFERENCES `textbook_directory` (`id`) ON DELETE SET NULL ON UPDATE CASCADE'
);

CALL add_fk_if_not_exists(
    'wrong_question_ai_chat_session',
    'fk_wqacs_user',
    '`fk_wqacs_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE'
);

CALL add_fk_if_not_exists(
    'wrong_question_ai_chat_session',
    'fk_wqacs_wrong',
    '`fk_wqacs_wrong` FOREIGN KEY (`wrong_question_id`) REFERENCES `wrong_question` (`id`) ON DELETE CASCADE ON UPDATE CASCADE'
);

CALL add_fk_if_not_exists(
    'wrong_question_ai_chat_session',
    'fk_wqacs_analysis',
    '`fk_wqacs_analysis` FOREIGN KEY (`analysis_id`) REFERENCES `wrong_question_ai_analysis` (`id`) ON DELETE SET NULL ON UPDATE CASCADE'
);

CALL add_fk_if_not_exists(
    'wrong_question_ai_chat_session',
    'fk_wqacs_question',
    '`fk_wqacs_question` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE CASCADE ON UPDATE CASCADE'
);

CALL add_fk_if_not_exists(
    'wrong_question_ai_chat_session',
    'fk_wqacs_subject',
    '`fk_wqacs_subject` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`id`) ON DELETE SET NULL ON UPDATE CASCADE'
);

CALL add_fk_if_not_exists(
    'wrong_question_ai_chat_message',
    'fk_wqacm_session',
    '`fk_wqacm_session` FOREIGN KEY (`session_id`) REFERENCES `wrong_question_ai_chat_session` (`id`) ON DELETE CASCADE ON UPDATE CASCADE'
);

CALL add_fk_if_not_exists(
    'exam_record',
    'fk_exam_record_subject',
    '`fk_exam_record_subject` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`id`) ON DELETE SET NULL ON UPDATE CASCADE'
);

-- ============================================================
-- 7. 统计值纠偏
-- ============================================================
UPDATE `subject` s
LEFT JOIN (
    SELECT subject_id, COUNT(*) AS question_count
    FROM `question`
    WHERE deleted = 0
      AND subject_id IS NOT NULL
    GROUP BY subject_id
) q ON q.subject_id = s.id
SET s.question_count = COALESCE(q.question_count, 0);

UPDATE `question`
SET correct_rate = 0.00
WHERE correct_rate IS NULL
   OR correct_rate < 0
   OR correct_rate > 100;

UPDATE `question`
SET do_count = 0
WHERE do_count IS NULL
   OR do_count < 0;

UPDATE `wrong_question`
SET wrong_count = 1
WHERE wrong_count IS NULL
   OR wrong_count < 1;

UPDATE `user_stats`
SET total_count = COALESCE(total_count, 0),
    correct_count = COALESCE(correct_count, 0),
    wrong_count = COALESCE(wrong_count, 0),
    correct_rate = CASE
        WHEN COALESCE(total_count, 0) <= 0 THEN 0.00
        ELSE ROUND(COALESCE(correct_count, 0) * 100 / NULLIF(COALESCE(total_count, 0), 0), 2)
    END;

UPDATE `user_subject_stats`
SET total_count = COALESCE(total_count, 0),
    correct_count = COALESCE(correct_count, 0),
    wrong_count = COALESCE(wrong_count, 0),
    total_time_cost = COALESCE(total_time_cost, 0),
    correct_rate = CASE
        WHEN COALESCE(total_count, 0) <= 0 THEN 0.00
        ELSE ROUND(COALESCE(correct_count, 0) * 100 / NULLIF(COALESCE(total_count, 0), 0), 2)
    END;

UPDATE `user_knowledge_mastery`
SET total_count = COALESCE(total_count, 0),
    correct_count = COALESCE(correct_count, 0),
    wrong_count = COALESCE(wrong_count, 0),
    correct_rate = CASE
        WHEN COALESCE(total_count, 0) <= 0 THEN 0.00
        ELSE ROUND(COALESCE(correct_count, 0) * 100 / NULLIF(COALESCE(total_count, 0), 0), 2)
    END,
    mastery_level = CASE
        WHEN COALESCE(total_count, 0) >= 10
            AND ROUND(COALESCE(correct_count, 0) * 100 / NULLIF(COALESCE(total_count, 0), 0), 2) >= 90 THEN 3
        WHEN COALESCE(total_count, 0) >= 5
            AND ROUND(COALESCE(correct_count, 0) * 100 / NULLIF(COALESCE(total_count, 0), 0), 2) >= 75 THEN 2
        WHEN COALESCE(total_count, 0) >= 1
            AND ROUND(COALESCE(correct_count, 0) * 100 / NULLIF(COALESCE(total_count, 0), 0), 2) >= 50 THEN 1
        ELSE 0
    END;

-- ============================================================
-- 8. 清理临时过程并输出检查项
-- ============================================================
DROP PROCEDURE IF EXISTS add_column_if_not_exists;
DROP PROCEDURE IF EXISTS modify_column_if_not_nullable;
DROP PROCEDURE IF EXISTS add_index_if_not_exists;
DROP PROCEDURE IF EXISTS drop_index_if_exists;
DROP PROCEDURE IF EXISTS add_fk_if_not_exists;

SELECT 'textbook_directory_missing_subject' AS check_item, COUNT(*) AS remain_count
FROM textbook_directory td
LEFT JOIN subject s ON s.id = td.subject_id
WHERE s.id IS NULL
UNION ALL
SELECT 'exam_tag_missing_subject', COUNT(*)
FROM exam_tag t
LEFT JOIN subject s ON s.id = t.subject_id
WHERE s.id IS NULL
UNION ALL
SELECT 'practice_session_missing_user', COUNT(*)
FROM practice_session ps
LEFT JOIN `user` u ON u.id = ps.user_id
WHERE u.id IS NULL
UNION ALL
SELECT 'exercise_record_missing_session', COUNT(*)
FROM exercise_record er
LEFT JOIN practice_session ps ON ps.id = er.session_ref_id
WHERE er.session_ref_id IS NOT NULL
  AND ps.id IS NULL
UNION ALL
SELECT 'wrong_question_missing_question', COUNT(*)
FROM wrong_question w
LEFT JOIN question q ON q.id = w.question_id
WHERE q.id IS NULL
UNION ALL
SELECT 'ai_generated_question_bad_subject', COUNT(*)
FROM ai_generated_question agq
LEFT JOIN subject s ON s.id = agq.subject_id
WHERE agq.subject_id IS NOT NULL
  AND s.id IS NULL
UNION ALL
SELECT 'exam_record_bad_subject', COUNT(*)
FROM exam_record er
LEFT JOIN subject s ON s.id = er.subject_id
WHERE er.subject_id IS NOT NULL
  AND s.id IS NULL;
