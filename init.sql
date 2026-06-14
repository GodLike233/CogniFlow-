-- ============================================================
-- CogniFlow 个性化学习路径导航系统 - MySQL 完整建库建表脚本
-- ============================================================
CREATE DATABASE IF NOT EXISTS cogniflow CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cogniflow;

-- ===== 1. 用户表（Admin/Student 单表继承） =====
CREATE TABLE IF NOT EXISTS users (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    role     VARCHAR(31)  NOT NULL,
    username VARCHAR(50)  NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email    VARCHAR(100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===== 2. 课程表 =====
CREATE TABLE IF NOT EXISTS courses (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    category    VARCHAR(50),
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===== 3. 知识点表（自关联树形结构） =====
CREATE TABLE IF NOT EXISTS knowledge_points (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    description TEXT,
    course_id   BIGINT,
    parent_id   BIGINT,
    order_index INT,
    difficulty  VARCHAR(20),
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_kp_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE SET NULL,
    CONSTRAINT fk_kp_parent FOREIGN KEY (parent_id) REFERENCES knowledge_points(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===== 4. 学习记录表 =====
CREATE TABLE IF NOT EXISTS study_records (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id            BIGINT,
    knowledge_point_id BIGINT,
    status             VARCHAR(20),
    score              DOUBLE,
    time_spent         INT,
    created_at         DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sr_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_sr_kp   FOREIGN KEY (knowledge_point_id) REFERENCES knowledge_points(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===== 5. 学习计划表 =====
CREATE TABLE IF NOT EXISTS study_plans (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT       NOT NULL,
    title         VARCHAR(200) NOT NULL,
    plan_type     VARCHAR(50),
    content       TEXT,
    total_days    INT,
    progress_days INT DEFAULT 0,
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sp_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===== 6. 聊天消息表 =====
CREATE TABLE IF NOT EXISTS chat_messages (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT,
    role       VARCHAR(50),
    content    TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cm_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===== 7. 题库表 =====
CREATE TABLE IF NOT EXISTS questions (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id          BIGINT,
    type               VARCHAR(20)  NOT NULL,
    title              TEXT         NOT NULL,
    option_a           TEXT,
    option_b           TEXT,
    option_c           TEXT,
    option_d           TEXT,
    answer             VARCHAR(255) NOT NULL,
    analysis           TEXT,
    score              INT DEFAULT 5,
    difficulty         VARCHAR(20),
    created_at         DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_q_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===== 8. 考试记录表 =====
CREATE TABLE IF NOT EXISTS exam_records (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT,
    course_id      BIGINT,
    total_score    INT,
    user_score     INT,
    correct_count  INT,
    question_count INT,
    question_ids   TEXT,
    status         VARCHAR(20),
    start_time     DATETIME,
    end_time       DATETIME,
    created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_er_user   FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_er_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===== 9. 考试答题记录表 =====
CREATE TABLE IF NOT EXISTS exam_answers (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_record_id  BIGINT NOT NULL,
    question_id     BIGINT NOT NULL,
    user_answer     VARCHAR(255),
    is_correct      TINYINT(1) DEFAULT 0,
    score           INT DEFAULT 0,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ea_record   FOREIGN KEY (exam_record_id) REFERENCES exam_records(id) ON DELETE CASCADE,
    CONSTRAINT fk_ea_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===== 10. 公告表 =====
CREATE TABLE IF NOT EXISTS announcements (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    title        VARCHAR(200) NOT NULL,
    content      TEXT         NOT NULL,
    pinned       TINYINT(1) DEFAULT 0,
    publisher_id BIGINT,
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ann_publisher FOREIGN KEY (publisher_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===== 11. 课程申请表 =====
CREATE TABLE IF NOT EXISTS course_requests (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    topic       VARCHAR(200) NOT NULL,
    description TEXT,
    status      VARCHAR(20) DEFAULT 'PENDING',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cr_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===== 12. 系统日志表 =====
CREATE TABLE IF NOT EXISTS system_logs (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT,
    action     VARCHAR(100) NOT NULL,
    detail     TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_log_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===== 种子数据 =====
INSERT INTO announcements (title, content, pinned) VALUES
('欢迎使用 CogniFlow 学习平台', '本平台集成 AI 智能学习路径规划、课程管理、在线考试等功能', 1),
('考试功能已上线', '支持选择题、判断题的在线考试，自动判分并生成成绩报告', 1);
