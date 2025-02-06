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
    teacher_id INT PRIMARY KEY AUTO_INCREMENT,  -- 改为自增整数类型
    name VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL COMMENT '存储BCrypt加密后的哈希值',
    phone VARCHAR(20),
    email VARCHAR(100) UNIQUE,  -- 添加唯一约束
    department VARCHAR(50),
    title VARCHAR(50),
    status TINYINT DEFAULT 0 COMMENT '0=未激活, 1=已激活, 2=已停用',
    last_login DATETIME DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- 监考安排表
-- ----------------------------
CREATE TABLE invigilator_assignment (
    assignment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    teacher_id INT NOT NULL,  -- 同步为INT类型
    course_name VARCHAR(100) NOT NULL,
    exam_start DATETIME NOT NULL COMMENT '考试开始时间（包含日期和时间）',  -- 合并日期和时间
    exam_end DATETIME NOT NULL COMMENT '考试结束时间',
    location VARCHAR(100) NOT NULL,
    role INT NOT NULL COMMENT '0=主监考, 1=副监考',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0=未确认, 1=已确认, 2=已取消',
    assign_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    confirm_time DATETIME,
    FOREIGN KEY (teacher_id) REFERENCES teacher(teacher_id) ON DELETE CASCADE ON UPDATE CASCADE,
    -- 避免同一教师在相同时间段重复分配
    UNIQUE INDEX idx_teacher_time (teacher_id, exam_start)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- 培训材料表
-- ----------------------------
CREATE TABLE training_material (
    material_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    content TEXT NOT NULL,
    type INT NOT NULL COMMENT '1=文档, 2=视频, 3=测试',
    required_minutes INT NOT NULL COMMENT '预计学习时长（分钟）',  -- 重命名并添加注释
    exam_questions TEXT,
    pass_score INT NOT NULL CHECK (pass_score BETWEEN 0 AND 100),  -- 添加约束
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0=草稿, 1=发布, 2=下架',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- 培训记录表
-- ----------------------------
CREATE TABLE training_record (
    record_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    material_id BIGINT NOT NULL,
    teacher_id INT NOT NULL,  -- 同步为INT类型
    study_time INT NOT NULL DEFAULT 0 COMMENT '实际学习时长（分钟）',
    exam_score INT DEFAULT NULL COMMENT '未考试时为NULL',  -- 允许NULL
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0=未开始, 1=进行中, 2=已完成',
    start_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    complete_time DATETIME,
    FOREIGN KEY (material_id) REFERENCES training_material(material_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES teacher(teacher_id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_material_teacher (material_id, teacher_id)  -- 联合索引
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- 消息表
-- ----------------------------
CREATE TABLE message (
    message_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    teacher_id INT NOT NULL,  -- 同步为INT类型
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    type INT NOT NULL COMMENT '1=系统通知, 2=监考提醒, 3=培训通知',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0=未读, 1=已读',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    read_time DATETIME,
    FOREIGN KEY (teacher_id) REFERENCES teacher(teacher_id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_message_type_status (type, status)  -- 联合索引
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- 监考记录表
-- ----------------------------
CREATE TABLE invigilation_record (
    record_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    assignment_id BIGINT NOT NULL,
    type INT NOT NULL COMMENT '1=签到, 2=异常事件, 3=备注',
    description TEXT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,  -- 添加默认时间
    FOREIGN KEY (assignment_id) REFERENCES invigilator_assignment(assignment_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- 评价表
-- ----------------------------
CREATE TABLE evaluation (
    evaluation_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    assignment_id BIGINT NOT NULL,
    evaluator_id INT NOT NULL,  -- 同步为INT类型
    score DECIMAL(5,2) NOT NULL COMMENT '支持小数点评分（如90.5）',  -- 改用DECIMAL
    comment TEXT DEFAULT NULL,  -- 允许NULL
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (assignment_id) REFERENCES invigilator_assignment(assignment_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (evaluator_id) REFERENCES teacher(teacher_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- 其他索引优化
-- ----------------------------
-- 教师表按部门和状态查询
CREATE INDEX idx_teacher_department ON teacher(department);
CREATE INDEX idx_teacher_status ON teacher(status);

-- 监考安排表按日期范围查询
CREATE INDEX idx_assignment_date_range ON invigilator_assignment(exam_start, exam_end);