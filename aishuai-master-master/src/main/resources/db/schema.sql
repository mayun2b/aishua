-- 用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号（唯一）',
    `password` VARCHAR(64) NOT NULL COMMENT '密码（MD5加密）',
    `nickname` VARCHAR(100) COMMENT '昵称',
    `avatar` VARCHAR(500) COMMENT '头像URL',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `is_admin` TINYINT DEFAULT 0 COMMENT '管理员标识：0-普通用户，1-管理员',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`),
    INDEX `idx_status` (`status`),
    INDEX `idx_is_admin` (`is_admin`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 学科表
CREATE TABLE IF NOT EXISTS `subject` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `name` VARCHAR(100) NOT NULL COMMENT '学科名称',
    `code` VARCHAR(50) NOT NULL COMMENT '学科代码',
    `description` TEXT COMMENT '学科描述',
    `sort` INT DEFAULT 0 COMMENT '排序值（越小越靠前）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    INDEX `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学科表';

-- 题目分类表
CREATE TABLE IF NOT EXISTS `question_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父分类 ID（0 表示一级分类）',
    `subject_id` BIGINT COMMENT '学科 ID',
    `sort` INT DEFAULT 0 COMMENT '排序值（越小越靠前）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_parent_id` (`parent_id`),
    INDEX `idx_subject_id` (`subject_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目分类表';

-- 题目表
CREATE TABLE IF NOT EXISTS `question` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `title` VARCHAR(500) NOT NULL COMMENT '题目标题/题干',
    `content` TEXT COMMENT '题目内容（支持富文本、公式等）',
    `type` TINYINT NOT NULL COMMENT '题型：1-单选，2-多选，3-判断，4-填空，5-简答',
    `category_id` BIGINT COMMENT '分类 ID',
    `subject_id` BIGINT COMMENT '学科 ID',
    `difficulty` TINYINT DEFAULT 1 COMMENT '难度等级：1-简单，2-中等，3-困难',
    `options` JSON COMMENT '选择题选项（JSON 格式）',
    `answer` TEXT NOT NULL COMMENT '正确答案',
    `analysis` TEXT COMMENT '答案解析',
    `tags` VARCHAR(500) COMMENT '知识点标签（逗号分隔）',
    `correct_rate` DECIMAL(5,2) DEFAULT 0 COMMENT '正确率（百分比）',
    `do_count` INT DEFAULT 0 COMMENT '做题次数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_category_id` (`category_id`),
    INDEX `idx_subject_id` (`subject_id`),
    INDEX `idx_type` (`type`),
    INDEX `idx_difficulty` (`difficulty`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目表';

-- 练习记录表
CREATE TABLE IF NOT EXISTS `exercise_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `user_id` BIGINT NOT NULL COMMENT '用户 ID',
    `question_id` BIGINT NOT NULL COMMENT '题目 ID',
    `user_answer` TEXT COMMENT '用户答案',
    `is_correct` TINYINT DEFAULT 0 COMMENT '是否正确：0-错误，1-正确',
    `time_cost` INT DEFAULT 0 COMMENT '耗时（秒）',
    `exercise_mode` TINYINT DEFAULT 1 COMMENT '练习模式：1-顺序，2-随机，3-错题，4-限时',
    `exercise_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '练习时间',
    `session_id` VARCHAR(64) COMMENT '练习会话 ID（用于批量答题）',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_question_id` (`question_id`),
    INDEX `idx_exercise_time` (`exercise_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='练习记录表';

-- 错题表
CREATE TABLE IF NOT EXISTS `wrong_question` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `user_id` BIGINT NOT NULL COMMENT '用户 ID',
    `question_id` BIGINT NOT NULL COMMENT '题目 ID',
    `wrong_count` INT DEFAULT 1 COMMENT '错误次数',
    `last_wrong_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '最后错误时间',
    `master_status` TINYINT DEFAULT 0 COMMENT '掌握状态：0-未掌握，1-已掌握',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_question` (`user_id`, `question_id`),
    INDEX `idx_master_status` (`master_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='错题表';

-- 用户统计表
CREATE TABLE IF NOT EXISTS `user_stats` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `user_id` BIGINT NOT NULL COMMENT '用户 ID',
    `total_count` INT DEFAULT 0 COMMENT '总做题数',
    `correct_count` INT DEFAULT 0 COMMENT '正确数',
    `wrong_count` INT DEFAULT 0 COMMENT '错误数',
    `continuous_days` INT DEFAULT 0 COMMENT '连续天数',
    `max_continuous_days` INT DEFAULT 0 COMMENT '最大连续天数',
    `last_exercise_date` DATE COMMENT '最后练习日期',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户统计表';

-- 考试记录表
CREATE TABLE IF NOT EXISTS `exam_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `user_id` BIGINT NOT NULL COMMENT '用户 ID',
    `exam_name` VARCHAR(100) NOT NULL COMMENT '考试名称',
    `exam_mode` TINYINT NOT NULL COMMENT '考试模式：1-基础，2-中级，3-高级',
    `total_questions` INT NOT NULL COMMENT '总题数',
    `correct_questions` INT NOT NULL COMMENT '正确题数',
    `score` DOUBLE NOT NULL COMMENT '得分',
    `duration` INT NOT NULL COMMENT '考试时长（分钟）',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME NOT NULL COMMENT '结束时间',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1-已完成，2-已取消',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_start_time` (`start_time`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试记录表';

-- 考试记录题目表
CREATE TABLE IF NOT EXISTS `exam_record_question` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `exam_record_id` BIGINT NOT NULL COMMENT '考试记录 ID',
    `question_id` BIGINT NOT NULL COMMENT '题目 ID',
    `user_answer` TEXT COMMENT '用户答案',
    `is_correct` TINYINT DEFAULT 0 COMMENT '是否正确：0-错误，1-正确',
    `answer_time` INT DEFAULT 0 COMMENT '答题时长（秒）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_exam_record_id` (`exam_record_id`),
    INDEX `idx_question_id` (`question_id`),
    FOREIGN KEY (`exam_record_id`) REFERENCES `exam_record` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试记录题目表';

-- 初始化数据 - 学科
INSERT INTO `subject` (`name`, `code`, `description`, `sort`) VALUES
('数学', 'MATH', '数学学科，包括高等数学、线性代数等', 1),
('英语', 'ENGLISH', '英语学科，包括词汇、语法等', 2),
('计算机', 'COMPUTER', '计算机学科，包括数据结构、计算机网络等', 3),
('物理', 'PHYSICS', '物理学科，包括力学、电磁学等', 4),
('化学', 'CHEMISTRY', '化学学科，包括无机化学、有机化学等', 5);

-- 初始化数据 - 题目分类
INSERT INTO `question_category` (`name`, `parent_id`, `subject_id`, `sort`) VALUES
('数学', 0, 1, 1),
('英语', 0, 2, 2),
('计算机基础', 0, 3, 3),
('高等数学', 1, 1, 1),
('线性代数', 1, 1, 2),
('词汇', 2, 2, 1),
('语法', 2, 2, 2),
('数据结构', 3, 3, 1),
('计算机网络', 3, 3, 2);