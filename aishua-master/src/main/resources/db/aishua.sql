SET NAMES utf8mb4;
-- ----------------------------
-- 0. 创建并选择数据库
-- ----------------------------
CREATE DATABASE IF NOT EXISTS `aishua`
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_general_ci;

USE `aishua`;

SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 删除旧表
-- ----------------------------
DROP TABLE IF EXISTS exam_record_question;
DROP TABLE IF EXISTS exam_record;
DROP TABLE IF EXISTS user_subject_stats;
DROP TABLE IF EXISTS user_knowledge_mastery;
DROP TABLE IF EXISTS question_tag_relation;
DROP TABLE IF EXISTS wrong_question;
DROP TABLE IF EXISTS exercise_record;
DROP TABLE IF EXISTS practice_session;
DROP TABLE IF EXISTS ai_generated_question;
DROP TABLE IF EXISTS daily_stats;
DROP TABLE IF EXISTS user_stats;
DROP TABLE IF EXISTS question;
DROP TABLE IF EXISTS exam_tag;
DROP TABLE IF EXISTS textbook_directory;
DROP TABLE IF EXISTS user_subject;
DROP TABLE IF EXISTS subject;
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS exam_paper_question;
DROP TABLE IF EXISTS exam_paper;

-- ----------------------------
-- 2. 用户与学科
-- ----------------------------
-- 用户表
CREATE TABLE `user` (
                      id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                      phone varchar(50) NOT NULL COMMENT '手机号（登录账号）',
                      password varchar(100) NOT NULL COMMENT '密码（加密存储）',
                      nickname varchar(50) DEFAULT '' COMMENT '用户昵称',
                      avatar varchar(255) DEFAULT '' COMMENT '头像URL',
                      status tinyint DEFAULT 1 COMMENT '账号状态：0-禁用，1-启用',
                      is_admin tinyint DEFAULT 0 COMMENT '是否管理员：0-否，1-是',
                      create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                      update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                      deleted tinyint DEFAULT 0 COMMENT '软删除标记：0-未删，1-已删',
                      PRIMARY KEY (id),
                      UNIQUE KEY uk_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表：存储学员和管理员的基础信息';

-- 学科表
CREATE TABLE subject (
                         id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                         name varchar(100) NOT NULL COMMENT '学科名称（如：高中数学）',
                         code varchar(50) NOT NULL COMMENT '学科唯一编码',
                         description text COMMENT '学科描述',
                         icon varchar(255) DEFAULT NULL COMMENT '学科图标URL',
                         question_count int DEFAULT 0 COMMENT '题目总数统计',
                         sort int DEFAULT 0 COMMENT '排序权重',
                         is_enabled tinyint DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
                         create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                         deleted tinyint DEFAULT 0 COMMENT '软删除标记',
                         PRIMARY KEY (id),
                         UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学科表：管理所有学科分类';

-- 用户-学科关系表
CREATE TABLE user_subject (
                              id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                              user_id bigint NOT NULL COMMENT '用户ID',
                              subject_id bigint NOT NULL COMMENT '学科ID',
                              joined_at datetime DEFAULT CURRENT_TIMESTAMP COMMENT '加入学科时间',
                              status tinyint DEFAULT 1 COMMENT '学习状态：0-暂停，1-学习中',
                              last_practice_at datetime DEFAULT NULL COMMENT '最近一次刷题时间',
                              sort int DEFAULT 0 COMMENT '排序',
                              create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                              deleted tinyint DEFAULT 0 COMMENT '软删除标记',
                              PRIMARY KEY (id),
                              UNIQUE KEY uk_user_subject (user_id, subject_id),
                              CONSTRAINT fk_us_user FOREIGN KEY (user_id) REFERENCES `user` (id) ON DELETE CASCADE,
                              CONSTRAINT fk_us_subject FOREIGN KEY (subject_id) REFERENCES subject (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-学科关系表：我的学习列表';

-- ----------------------------
-- 3. 教材目录 + 考点标签
-- ----------------------------
-- 教材目录表
CREATE TABLE textbook_directory (
                                    id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                    name varchar(100) NOT NULL COMMENT '目录名称',
                                    subject_id bigint NOT NULL COMMENT '所属学科ID',
                                    parent_id bigint DEFAULT 0 COMMENT '父目录ID',
                                    sort int DEFAULT 0 COMMENT '排序',
                                    create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                    deleted tinyint DEFAULT 0 COMMENT '软删除标记',
                                    PRIMARY KEY (id),
                                    KEY idx_subject_parent (subject_id, parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教材目录表：题目的物理位置（章节树形结构）';

-- 考点标签表
CREATE TABLE exam_tag (
                          id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                          name varchar(100) NOT NULL COMMENT '考点名称',
                          subject_id bigint NOT NULL COMMENT '所属学科ID',
                          tag varchar(255) DEFAULT NULL COMMENT '备注描述',
                          create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          PRIMARY KEY (id),
                          UNIQUE KEY uk_subject_name (subject_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考点标签表：题目的逻辑知识点属性';

-- ----------------------------
-- 4. 题目核心
-- ----------------------------
-- 题目表
CREATE TABLE question (
                          id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                          title varchar(500) NOT NULL COMMENT '题干',
                          content text COMMENT '题目详细内容',
                          type tinyint NOT NULL COMMENT '题型：1-单选，2-多选，3-判断，4-填空，5-简答',
                          options json COMMENT '选项JSON',
                          answer text NOT NULL COMMENT '标准答案',
                          analysis text COMMENT '题目解析',
                          image_urls varchar(2000) DEFAULT NULL COMMENT '题目图片URL',
                          image_desc text COMMENT '图片描述',
                          directory_id bigint DEFAULT NULL COMMENT '所属教材目录ID',
                          subject_id bigint DEFAULT NULL COMMENT '所属学科ID',
                          difficulty tinyint DEFAULT 1 COMMENT '难度：1-简单，2-中等，3-困难',
                          correct_rate decimal(5,2) DEFAULT 0.00 COMMENT '全站正确率',
                          do_count int DEFAULT 0 COMMENT '做题次数',
                          create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          deleted tinyint DEFAULT 0 COMMENT '软删除标记',
                          PRIMARY KEY (id),
                          KEY idx_directory_id (directory_id),
                          KEY idx_subject_id (subject_id),
                          CONSTRAINT fk_q_directory FOREIGN KEY (directory_id) REFERENCES textbook_directory (id) ON DELETE SET NULL,
                          CONSTRAINT fk_q_subject FOREIGN KEY (subject_id) REFERENCES subject (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目表：官方题库核心表';

-- 题目-考点关联表
CREATE TABLE question_tag_relation (
                                       id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                       question_id bigint NOT NULL COMMENT '题目ID',
                                       tag_id bigint NOT NULL COMMENT '考点标签ID',
                                       PRIMARY KEY (id),
                                       UNIQUE KEY uk_q_t (question_id, tag_id),
                                       CONSTRAINT fk_qtr_question FOREIGN KEY (question_id) REFERENCES question (id) ON DELETE CASCADE,
                                       CONSTRAINT fk_qtr_tag FOREIGN KEY (tag_id) REFERENCES exam_tag (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目-考点关联表：多对多关系中间表';

-- ----------------------------
-- 5. 练习模块（已删冗余字段）
-- ----------------------------
-- 刷题会话表
CREATE TABLE practice_session (
                                  id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                  session_code varchar(64) NOT NULL COMMENT '会话唯一编码',
                                  user_id bigint NOT NULL COMMENT '用户ID',
                                  subject_id bigint NOT NULL COMMENT '学科ID',
                                  practice_mode tinyint NOT NULL COMMENT '练习模式：1-顺序，2-随机，3-错题重练，4-AI强化',
                                  question_count int DEFAULT 0 COMMENT '计划题目数',
                                  answered_count int DEFAULT 0 COMMENT '已答数',
                                  correct_count int DEFAULT 0 COMMENT '答对数',
                                  wrong_count int DEFAULT 0 COMMENT '答错数',
                                  total_time_cost int DEFAULT 0 COMMENT '总耗时（秒）',
                                  status tinyint DEFAULT 1 COMMENT '状态：1-进行中，2-已完成',
                                  started_at datetime DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
                                  ended_at datetime DEFAULT NULL COMMENT '结束时间',
                                  create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                  PRIMARY KEY (id),
                                  UNIQUE KEY uk_session_code (session_code),
                                  KEY idx_user_time (user_id, started_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='刷题会话表：记录一次完整刷题过程';

-- 练习记录表（已删除所有冗余字段）
CREATE TABLE exercise_record (
                                 id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                 user_id bigint NOT NULL COMMENT '用户ID',
                                 question_id bigint NOT NULL COMMENT '题目ID',
                                 session_ref_id bigint DEFAULT NULL COMMENT '所属刷题会话ID',
                                 exercise_mode tinyint DEFAULT 1 COMMENT '练习模式',
                                 user_answer text COMMENT '用户答案',
                                 is_correct tinyint DEFAULT 0 COMMENT '是否正确：0-错误，1-正确',
                                 time_cost int DEFAULT 0 COMMENT '本题耗时（秒）',
                                 exercise_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '作答时间',
                                 create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 deleted tinyint DEFAULT 0 COMMENT '软删除标记',
                                 PRIMARY KEY (id),
                                 KEY idx_user_time (user_id, exercise_time),
                                 KEY idx_question (question_id),
                                 CONSTRAINT fk_exercise_record_user FOREIGN KEY (user_id) REFERENCES `user` (id) ON DELETE CASCADE,
                                 CONSTRAINT fk_exercise_record_question FOREIGN KEY (question_id) REFERENCES question (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='练习记录表：用户每道题的作答流水日志';

-- 错题本表（学科ID、目录ID 保留，用于分类筛选）
CREATE TABLE wrong_question (
                                id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                user_id bigint NOT NULL COMMENT '用户ID',
                                question_id bigint NOT NULL COMMENT '题目ID',
                                subject_id bigint DEFAULT NULL COMMENT '学科ID（用于筛选）',
                                directory_id bigint DEFAULT NULL COMMENT '目录ID（用于筛选）',
                                tags varchar(500) DEFAULT NULL COMMENT '考点标签快照',
                                wrong_count int DEFAULT 1 COMMENT '错误次数',
                                last_wrong_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '最近错误时间',
                                master_status tinyint DEFAULT 0 COMMENT '掌握状态：0-未掌握，1-已掌握',
                                create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                deleted tinyint DEFAULT 0 COMMENT '软删除标记',
                                PRIMARY KEY (id),
                                UNIQUE KEY uk_user_question (user_id, question_id),
                                CONSTRAINT fk_wq_user FOREIGN KEY (user_id) REFERENCES `user` (id) ON DELETE CASCADE,
                                CONSTRAINT fk_wq_question FOREIGN KEY (question_id) REFERENCES question (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='错题本表：用户个人错题集';

-- ----------------------------
-- 6. 统计模块（已按要求删除字段）
-- ----------------------------
-- 用户全局统计表（删除4个字段）
CREATE TABLE user_stats (
                            id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                            user_id bigint NOT NULL COMMENT '用户ID',
                            total_count int DEFAULT 0 COMMENT '累计答题总数',
                            correct_count int DEFAULT 0 COMMENT '累计答对总数',
                            wrong_count int DEFAULT 0 COMMENT '累计答错总数',
                            correct_rate decimal(5,2) DEFAULT 0.00 COMMENT '总正确率',
                            last_exercise_date date DEFAULT NULL COMMENT '最近学习日期',
                            create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            deleted tinyint DEFAULT 0 COMMENT '软删除标记',
                            PRIMARY KEY (id),
                            UNIQUE KEY uk_user_id (user_id),
                            CONSTRAINT fk_ust_user FOREIGN KEY (user_id) REFERENCES `user` (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户全局统计表：个人总览数据';

-- 用户学科统计表（删除2个字段）
CREATE TABLE user_subject_stats (
                                    id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                    user_id bigint NOT NULL COMMENT '用户ID',
                                    subject_id bigint NOT NULL COMMENT '学科ID',
                                    total_count int DEFAULT 0 COMMENT '累计答题数',
                                    correct_count int DEFAULT 0 COMMENT '累计答对数',
                                    wrong_count int DEFAULT 0 COMMENT '累计答错数',
                                    correct_rate decimal(5,2) DEFAULT 0.00 COMMENT '正确率',
                                    total_time_cost int DEFAULT 0 COMMENT '累计学习耗时（秒）',
                                    last_practice_date date DEFAULT NULL COMMENT '最近学习日期',
                                    create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                    PRIMARY KEY (id),
                                    UNIQUE KEY uk_user_subject (user_id, subject_id),
                                    CONSTRAINT fk_uss_user FOREIGN KEY (user_id) REFERENCES `user` (id) ON DELETE CASCADE,
                                    CONSTRAINT fk_uss_subject FOREIGN KEY (subject_id) REFERENCES subject (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户学科统计表：按学科维度统计数据';

-- 每日统计表
CREATE TABLE daily_stats (
                             id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                             user_id bigint NOT NULL COMMENT '用户ID',
                             stat_date date NOT NULL COMMENT '统计日期',
                             do_count int DEFAULT 0 COMMENT '当日做题数',
                             correct_count int DEFAULT 0 COMMENT '当日正确数',
                             time_cost int DEFAULT 0 COMMENT '当日学习耗时（秒）',
                             create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             deleted tinyint DEFAULT 0 COMMENT '软删除标记',
                             PRIMARY KEY (id),
                             UNIQUE KEY uk_user_date (user_id, stat_date),
                             CONSTRAINT fk_ds_user FOREIGN KEY (user_id) REFERENCES `user` (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日统计表：用户每日学习记录（打卡日历）';

-- 用户考点掌握度表
CREATE TABLE user_knowledge_mastery (
                                        id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                        user_id bigint NOT NULL COMMENT '用户ID',
                                        tag_id bigint NOT NULL COMMENT '考点标签ID',
                                        subject_id bigint NOT NULL COMMENT '学科ID',
                                        total_count int DEFAULT 0 COMMENT '总做题数',
                                        correct_count int DEFAULT 0 COMMENT '做对数',
                                        correct_rate decimal(5,2) DEFAULT 0.00 COMMENT '正确率',
                                        wrong_count int DEFAULT 0 COMMENT '错题数',
                                        mastery_level tinyint DEFAULT 0 COMMENT '掌握等级：0-未掌握，1-了解，2-掌握，3-精通',
                                        update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                        PRIMARY KEY (id),
                                        UNIQUE KEY uk_user_tag (user_id, tag_id),
                                        CONSTRAINT fk_ukm_user FOREIGN KEY (user_id) REFERENCES `user` (id) ON DELETE CASCADE,
                                        CONSTRAINT fk_ukm_tag FOREIGN KEY (tag_id) REFERENCES exam_tag (id) ON DELETE CASCADE,
                                        CONSTRAINT fk_ukm_subject FOREIGN KEY (subject_id) REFERENCES subject (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户考点掌握度表：知识点掌握情况';

-- ----------------------------
-- 7. AI生成题目
-- ----------------------------
CREATE TABLE ai_generated_question (
                                       id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                       user_id bigint NOT NULL COMMENT '用户ID',
                                       title varchar(500) NOT NULL COMMENT '题干',
                                       content text COMMENT '题目内容',
                                       type tinyint NOT NULL COMMENT '题型',
                                       directory_id bigint DEFAULT NULL COMMENT '目录ID',
                                       subject_id bigint DEFAULT NULL COMMENT '学科ID',
                                       difficulty tinyint DEFAULT 1 COMMENT '难度',
                                       options json COMMENT '选项JSON',
                                       answer text NOT NULL COMMENT '答案',
                                       analysis text COMMENT '解析',
                                       tags varchar(500) DEFAULT NULL COMMENT '考点标签',
                                       generate_reason text COMMENT '生成原因',
                                       generate_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
                                       is_practiced tinyint DEFAULT 0 COMMENT '是否已练习',
                                       create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                       deleted tinyint DEFAULT 0 COMMENT '软删除标记',
                                       PRIMARY KEY (id),
                                       KEY idx_user_id (user_id),
                                       CONSTRAINT fk_agq_user FOREIGN KEY (user_id) REFERENCES `user` (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI生成题目表：个性化强化练习题';

-- ----------------------------
-- 8. 考试模块（两张表不重复，都保留）
-- ----------------------------
-- 试卷表
CREATE TABLE exam_paper (
                            id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                            paper_name varchar(100) NOT NULL COMMENT '试卷名称',
                            subject_id bigint NOT NULL COMMENT '学科ID',
                            total_questions int DEFAULT 0 COMMENT '总题数',
                            total_score int DEFAULT 100 COMMENT '总分',
                            duration int DEFAULT 120 COMMENT '考试时长（分钟）',
                            status tinyint DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
                            create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            deleted tinyint DEFAULT 0 COMMENT '软删除标记',
                            PRIMARY KEY (id),
                            KEY idx_subject_id (subject_id),
                            CONSTRAINT fk_ep_subject FOREIGN KEY (subject_id) REFERENCES subject (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷表：考试试卷模板';

-- 试卷题目关联表（试卷固定题目）
CREATE TABLE exam_paper_question (
                                     id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                     paper_id bigint NOT NULL COMMENT '试卷ID',
                                     question_id bigint NOT NULL COMMENT '题目ID',
                                     sort int DEFAULT 0 COMMENT '排序',
                                     score int DEFAULT 0 COMMENT '分值',
                                     create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                     deleted tinyint DEFAULT 0 COMMENT '软删除标记',
                                     PRIMARY KEY (id),
                                     UNIQUE KEY uk_paper_question (paper_id, question_id),
                                     CONSTRAINT fk_epq_paper FOREIGN KEY (paper_id) REFERENCES exam_paper (id) ON DELETE CASCADE,
                                     CONSTRAINT fk_epq_question FOREIGN KEY (question_id) REFERENCES question (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷题目关联表：试卷包含的题目模板';

-- 考试记录表
CREATE TABLE exam_record (
                             id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                             user_id bigint NOT NULL COMMENT '用户ID',
                             subject_id bigint DEFAULT NULL COMMENT '学科ID',
                             exam_name varchar(100) NOT NULL COMMENT '考试名称',
                             exam_mode tinyint NOT NULL COMMENT '考试模式',
                             total_questions int NOT NULL COMMENT '总题数',
                             correct_questions int COMMENT '正确题数',
                             score double COMMENT '得分',
                             duration int COMMENT '实际用时（分钟）',
                             start_time datetime NOT NULL COMMENT '开始时间',
                             end_time datetime COMMENT '结束时间',
                             status tinyint DEFAULT 1 COMMENT '状态：1-进行中，2-已完成',
                             create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             deleted tinyint DEFAULT 0 COMMENT '软删除标记',
                             PRIMARY KEY (id),
                             KEY idx_user_id (user_id),
                             CONSTRAINT fk_exam_record_user FOREIGN KEY (user_id) REFERENCES `user` (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试记录表：用户一次考试的总记录';

-- 考试记录题目表（用户答题记录）
CREATE TABLE exam_record_question (
                                      id bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                      exam_record_id bigint NOT NULL COMMENT '考试记录ID',
                                      question_id bigint NOT NULL COMMENT '题目ID',
                                      user_answer text COMMENT '用户答案',
                                      is_correct tinyint DEFAULT 0 COMMENT '是否正确',
                                      answer_time int DEFAULT 0 COMMENT '答题时长（秒）',
                                      create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                      deleted tinyint DEFAULT 0 COMMENT '软删除标记',
                                      PRIMARY KEY (id),
                                      KEY idx_record_id (exam_record_id),
                                      CONSTRAINT fk_erq_record FOREIGN KEY (exam_record_id) REFERENCES exam_record (id) ON DELETE CASCADE,
                                      CONSTRAINT fk_erq_question FOREIGN KEY (question_id) REFERENCES question (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试记录题目表：用户每道题的作答详情';

SET FOREIGN_KEY_CHECKS = 1;
