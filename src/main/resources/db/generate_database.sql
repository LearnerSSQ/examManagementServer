-- 删除已存在的数据库（如果需要重新创建）
DROP DATABASE IF EXISTS exam_management;

-- 创建数据库
CREATE DATABASE exam_management 
  DEFAULT CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE exam_management;

-- ----------------------------
-- 教师表
-- ----------------------------
CREATE TABLE teacher (
    teacher_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100) UNIQUE,
    department VARCHAR(50),
    title VARCHAR(50),
    role VARCHAR(20) NOT NULL DEFAULT 'TEACHER' COMMENT 'ADMIN=系统管理员, EXAM_ADMIN=考务管理员, TEACHER=普通教师',
    status VARCHAR(20) NOT NULL DEFAULT 'INACTIVE' COMMENT 'INACTIVE=未激活, ACTIVE=已激活, DISABLED=已停用',
    last_login DATETIME DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- 监考安排表
-- ----------------------------
CREATE TABLE invigilator_assignment (
    assignment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    teacher_id INT NOT NULL,
    course_name VARCHAR(100) NOT NULL,
    exam_start DATETIME NOT NULL COMMENT '考试开始时间',
    exam_end DATETIME NOT NULL COMMENT '考试结束时间',
    location VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL COMMENT 'CHIEF=主监考, ASSISTANT=副监考',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING=待确认, CONFIRMED=已确认, COMPLETED=已完成, CANCELLED=已取消',
    assign_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    confirm_time DATETIME,
    FOREIGN KEY (teacher_id) REFERENCES teacher(teacher_id) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE INDEX idx_teacher_time (teacher_id, exam_start)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- 培训材料表
-- ----------------------------
CREATE TABLE training_material (
    material_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    content TEXT NOT NULL COMMENT '文档类型存储路径',
    type VARCHAR(20) NOT NULL COMMENT 'DOCUMENT=文档, VIDEO=视频, QUIZ=测试, PRESENTATION=演示文稿',
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT=草稿, PUBLISHED=已发布, ARCHIVED=已归档, DELETED=已删除',
    creator_id INT NOT NULL,
    duration INT NOT NULL COMMENT '预计学习时长（分钟）',
    is_required BOOLEAN NOT NULL DEFAULT FALSE,
    tags VARCHAR(255),
    pass_score INT COMMENT '通过分数（仅适用于测验类型）',
    expire_date DATETIME COMMENT '过期时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (creator_id) REFERENCES teacher(teacher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- 培训记录表
-- ----------------------------
CREATE TABLE training_record (
    record_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    material_id BIGINT NOT NULL,
    teacher_id INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED' COMMENT 'NOT_STARTED=未开始, IN_PROGRESS=进行中, COMPLETED=已完成, EXPIRED=已过期',
    progress INT NOT NULL DEFAULT 0 COMMENT '学习进度（百分比）',
    start_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    complete_time DATETIME,
    last_access DATETIME,
    remarks TEXT,
    FOREIGN KEY (material_id) REFERENCES training_material(material_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES teacher(teacher_id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_material_teacher (material_id, teacher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- 消息表
-- ----------------------------
CREATE TABLE message (
    message_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    type VARCHAR(20) NOT NULL COMMENT 'SYSTEM=系统通知, ASSIGNMENT=监考提醒, TRAINING=培训通知, NOTIFICATION=一般通知',
    status VARCHAR(20) NOT NULL DEFAULT 'UNREAD' COMMENT 'UNREAD=未读, READ=已读',
    receiver_id INT NOT NULL,
    sender_id INT NOT NULL DEFAULT 0,
    send_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    read_time DATETIME,
    reference_id BIGINT,
    require_confirm BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (receiver_id) REFERENCES teacher(teacher_id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_message_type_status (type, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- 监考记录表
-- ----------------------------
CREATE TABLE invigilation_record (
    record_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    assignment_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL COMMENT 'SIGN_IN=签到, INCIDENT=异常事件, VIOLATION=违规记录, NOTE=备注',
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT=草稿, SUBMITTED=已提交, APPROVED=已审核, REJECTED=已驳回',
    content TEXT NOT NULL,
    creator_id INT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    submit_time DATETIME,
    reviewer_id INT,
    review_time DATETIME,
    review_comment TEXT,
    attachments JSON,
    FOREIGN KEY (assignment_id) REFERENCES invigilator_assignment(assignment_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (creator_id) REFERENCES teacher(teacher_id),
    FOREIGN KEY (reviewer_id) REFERENCES teacher(teacher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- 评价表
-- ----------------------------
CREATE TABLE evaluation (
    evaluation_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    assignment_id BIGINT NOT NULL,
    evaluator_id INT NOT NULL,
    score DECIMAL(5,2) NOT NULL COMMENT '评分（0-100）',
    comment TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (assignment_id) REFERENCES invigilator_assignment(assignment_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (evaluator_id) REFERENCES teacher(teacher_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- 其他索引优化
-- ----------------------------
CREATE INDEX idx_teacher_department ON teacher(department);
CREATE INDEX idx_teacher_status ON teacher(status);
CREATE INDEX idx_assignment_date_range ON invigilator_assignment(exam_start, exam_end);