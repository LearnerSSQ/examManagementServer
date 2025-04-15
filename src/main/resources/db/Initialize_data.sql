-- ----------------------------
-- 1. 禁用外键约束检查
-- ----------------------------
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 2. 按依赖顺序清理所有表数据（从叶子表到根表）
-- ----------------------------
TRUNCATE TABLE evaluation;
TRUNCATE TABLE invigilation_record;
TRUNCATE TABLE message;
TRUNCATE TABLE training_record;
TRUNCATE TABLE invigilator_assignment;
TRUNCATE TABLE training_material;
TRUNCATE TABLE teacher;

-- ----------------------------
-- 3. 重置自增计数器
-- ----------------------------
ALTER TABLE teacher AUTO_INCREMENT = 1;
ALTER TABLE invigilator_assignment AUTO_INCREMENT = 1;
ALTER TABLE training_material AUTO_INCREMENT = 1;
ALTER TABLE training_record AUTO_INCREMENT = 1;
ALTER TABLE message AUTO_INCREMENT = 1;
ALTER TABLE invigilation_record AUTO_INCREMENT = 1;
ALTER TABLE evaluation AUTO_INCREMENT = 1;

-- ----------------------------
-- 4. 重新启用外键约束检查
-- ----------------------------
SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- 5. 初始化管理员账号
-- ----------------------------
START TRANSACTION;

INSERT INTO teacher (name, password, email, role, status, create_time) VALUES
('系统管理员', '$2a$10$wmISVE3hfRHjmbJKVoNiWOf7LzjadJVy2SQpwADu9JVwrpfHHUwOu', 'admin@example.com', 'ADMIN', 'ACTIVE', '2023-01-01 00:00:00'),
('考务管理员', '$2a$10$wmISVE3hfRHjmbJKVoNiWOf7LzjadJVy2SQpwADu9JVwrpfHHUwOu', 'exam_admin@example.com', 'EXAM_ADMIN', 'ACTIVE', '2023-01-01 00:00:00');
-- 注：密码为BCrypt加密后的'123456'

-- ----------------------------
-- 6. 插入测试数据（按依赖顺序）
-- ----------------------------
-- 教师表（10条）
INSERT INTO teacher (name, password, phone, email, department, title, status, last_login) VALUES
('张三', '$2a$10$wmISVE3hfRHjmbJKVoNiWOf7LzjadJVy2SQpwADu9JVwrpfHHUwOu', '13800138001', 'zhangsan@univ.edu', '计算机学院', '教授', 'ACTIVE', '2023-10-01 09:00:00'),
('李四', '$2a$10$wmISVE3hfRHjmbJKVoNiWOf7LzjadJVy2SQpwADu9JVwrpfHHUwOu', '13800138002', 'lisi@univ.edu', '数学学院', '副教授', 'ACTIVE', '2023-10-02 10:00:00'),
('王五', '$2a$10$wmISVE3hfRHjmbJKVoNiWOf7LzjadJVy2SQpwADu9JVwrpfHHUwOu', '13800138003', 'wangwu@univ.edu', '物理学院', '讲师', 'ACTIVE', '2023-10-03 11:00:00'),
('赵六', '$2a$10$wmISVE3hfRHjmbJKVoNiWOf7LzjadJVy2SQpwADu9JVwrpfHHUwOu', '13800138004', 'zhaoliu@univ.edu', '化学学院', '助教', 'INACTIVE', NULL),
('陈七', '$2a$10$wmISVE3hfRHjmbJKVoNiWOf7LzjadJVy2SQpwADu9JVwrpfHHUwOu', '13800138005', 'chenqi@univ.edu', '外语学院', '教授', 'ACTIVE', '2023-10-05 13:00:00'),
('周八', '$2a$10$wmISVE3hfRHjmbJKVoNiWOf7LzjadJVy2SQpwADu9JVwrpfHHUwOu', '13800138006', 'zhouba@univ.edu', '历史学院', '副教授', 'ACTIVE', '2023-10-06 14:00:00'),
('吴九', '$2a$10$wmISVE3hfRHjmbJKVoNiWOf7LzjadJVy2SQpwADu9JVwrpfHHUwOu', '13800138007', 'wujiu@univ.edu', '法学院', '讲师', 'INACTIVE', NULL),
('郑十', '$2a$10$wmISVE3hfRHjmbJKVoNiWOf7LzjadJVy2SQpwADu9JVwrpfHHUwOu', '13800138008', 'zhengshi@univ.edu', '医学院', '教授', 'ACTIVE', '2023-10-08 16:00:00'),
('钱十一', '$2a$10$wmISVE3hfRHjmbJKVoNiWOf7LzjadJVy2SQpwADu9JVwrpfHHUwOu', '13800138009', 'qianshiyi@univ.edu', '经济学院', '副教授', 'ACTIVE', '2023-10-09 17:00:00'),
('孙十二', '$2a$10$wmISVE3hfRHjmbJKVoNiWOf7LzjadJVy2SQpwADu9JVwrpfHHUwOu', '13800138010', 'sunshier@univ.edu', '艺术学院', '讲师', 'ACTIVE', '2023-10-10 18:00:00');
-- 注：密码为BCrypt加密后的'123456'

-- 培训材料表（10条）
INSERT INTO training_material (title, description, content, type, status, creator_id, duration, is_required, tags, pass_score, expire_date) VALUES
('监考规范手册', '监考流程与注意事项', 'src/main/resources/static/training-materials/2023/01/01/1/1/files/document.pdf', 'DOCUMENT', 'PUBLISHED', 1, 30, TRUE, '监考规范,必修', NULL, NULL),
('考场应急处理视频', '突发事件处理方法', 'src/main/resources/static/training-materials/2023/01/01/1/2/files/video.mp4', 'VIDEO', 'PUBLISHED', 1, 20, TRUE, '应急处理,必修', NULL, NULL),
('监考资格考试', '在线测试题库', 'src/main/resources/static/training-materials/2023/01/01/1/3/files/quiz.json', 'QUIZ', 'PUBLISHED', 1, 15, TRUE, '考试,必修', 80, '2025-12-31'),
('诚信监考培训', '学术诚信重要性', 'src/main/resources/static/training-materials/2023/01/01/2/4/files/document.pdf', 'DOCUMENT', 'PUBLISHED', 2, 25, TRUE, '诚信,必修', NULL, NULL),
('监考设备操作指南', '考场设备使用说明', 'src/main/resources/static/training-materials/2023/01/01/2/5/files/document.pdf', 'DOCUMENT', 'PUBLISHED', 2, 40, TRUE, '设备,必修', NULL, NULL),
('监考心理调节课程', '压力管理技巧', 'src/main/resources/static/training-materials/2023/01/01/1/6/files/video.mp4', 'VIDEO', 'PUBLISHED', 1, 30, FALSE, '心理,选修', NULL, NULL),
('监考模拟测试', '模拟监考场景测试', 'src/main/resources/static/training-materials/2023/01/01/2/7/files/quiz.json', 'QUIZ', 'PUBLISHED', 2, 20, TRUE, '模拟,必修', NULL, NULL),
('监考法律法规', '监考相关法律条文', 'src/main/resources/static/training-materials/2023/01/01/1/8/files/document.pdf', 'DOCUMENT', 'PUBLISHED', 1, 35, TRUE, '法规,必修', NULL, NULL),
('监考沟通技巧', '与考生沟通方法', 'src/main/resources/static/training-materials/2023/01/01/2/9/files/video.mp4', 'VIDEO', 'PUBLISHED', 2, 25, FALSE, '沟通,选修', NULL, NULL),
('监考总结报告', '监考总结模板', 'src/main/resources/static/training-materials/2023/01/01/1/10/files/document.pdf', 'DOCUMENT', 'PUBLISHED', 1, 20, FALSE, '总结,选修', NULL, NULL);

-- 监考安排表（10条）
INSERT INTO invigilator_assignment (teacher_id, course_name, exam_start, exam_end, location, role, status, confirm_time) VALUES
(1, '数据库原理', '2023-11-01 09:00:00', '2023-11-01 11:00:00', '教学楼A101', 'CHIEF', 'CONFIRMED', '2023-10-25 10:00:00'),
(2, '高等数学', '2023-11-02 14:00:00', '2023-11-02 16:00:00', '教学楼B202', 'ASSISTANT', 'CONFIRMED', '2023-10-26 11:00:00'),
(3, '大学物理', '2023-11-03 10:00:00', '2023-11-03 12:00:00', '实验楼C305', 'CHIEF', 'PENDING', NULL),
(5, '英语六级', '2023-11-05 08:30:00', '2023-11-05 11:30:00', '外语楼D401', 'CHIEF', 'CONFIRMED', '2023-10-28 09:00:00'),
(6, '中国近代史', '2023-11-06 13:00:00', '2023-11-06 15:00:00', '历史楼E102', 'ASSISTANT', 'CONFIRMED', '2023-10-29 14:00:00'),
(8, '临床医学基础', '2023-11-08 09:00:00', '2023-11-08 12:00:00', '医学院F203', 'CHIEF', 'PENDING', NULL),
(9, '宏观经济学', '2023-11-09 14:30:00', '2023-11-09 17:00:00', '经管楼G304', 'ASSISTANT', 'CONFIRMED', '2023-10-30 16:00:00'),
(10, '油画技法', '2023-11-10 10:00:00', '2023-11-10 12:30:00', '艺术学院H105', 'CHIEF', 'CONFIRMED', '2023-10-31 10:00:00'),
(4, '有机化学', '2023-11-04 15:00:00', '2023-11-04 17:00:00', '化学楼I206', 'ASSISTANT', 'PENDING', NULL),
(7, '宪法学', '2023-11-07 09:30:00', '2023-11-07 11:30:00', '法学院J307', 'CHIEF', 'CONFIRMED', '2023-10-27 15:00:00');

-- 培训记录表（10条）
INSERT INTO training_record (material_id, teacher_id, status, progress, start_time, complete_time, last_access) VALUES
(1, 1, 'COMPLETED', 100, '2023-10-01 09:00:00', '2023-10-05 10:00:00', '2023-10-05 10:00:00'),
(3, 2, 'COMPLETED', 100, '2023-10-02 10:00:00', '2023-10-06 11:00:00', '2023-10-06 11:00:00'),
(2, 3, 'COMPLETED', 100, '2023-10-03 11:00:00', '2023-10-06 11:00:00', '2023-10-15 15:00:00'),
(5, 5, 'COMPLETED', 100, '2023-10-05 13:00:00', '2023-10-08 14:00:00', '2023-10-08 14:00:00'),
(6, 6, 'COMPLETED', 100, '2023-10-06 14:00:00', '2023-10-09 15:00:00', '2023-10-09 15:00:00'),
(7, 8, 'COMPLETED', 100, '2023-10-08 16:00:00', '2023-10-10 16:00:00', '2023-10-10 16:00:00'),
(4, 9, 'COMPLETED', 100, '2023-10-09 17:00:00', '2023-10-11 17:00:00', '2023-10-11 17:00:00'),
(8, 10, 'COMPLETED', 100, '2023-10-10 18:00:00', '2023-10-12 18:00:00', '2023-10-12 18:00:00'),
(9, 4, 'NOT_STARTED', 0, '2023-10-04 12:00:00', NULL, NULL),
(10, 7, 'COMPLETED', 100, '2023-10-07 15:00:00', '2023-10-14 09:00:00', '2023-10-14 09:00:00');

-- 消息表（10条）
INSERT INTO message (title, content, type, status, receiver_id, sender_id, send_time, read_time, reference_id, require_confirm) VALUES
('监考任务通知', '您有新的监考任务：数据库原理考试', 'ASSIGNMENT', 'READ', 1, 0, '2023-10-25 10:00:00', '2023-10-25 10:05:00', 1, TRUE),
('培训完成提醒', '您已完成监考资格考试', 'TRAINING', 'READ', 2, 0, '2023-10-06 11:00:00', '2023-10-06 11:10:00', 3, FALSE),
('系统维护通知', '系统将于本周五凌晨升级', 'SYSTEM', 'UNREAD', 3, 0, '2023-10-20 09:00:00', NULL, NULL, FALSE),
('监考确认提醒', '请确认11月5日的监考安排', 'ASSIGNMENT', 'READ', 5, 0, '2023-10-28 09:00:00', '2023-10-28 09:15:00', 4, TRUE),
('培训材料更新', '新增《监考沟通技巧》视频', 'TRAINING', 'UNREAD', 6, 0, '2023-10-25 14:00:00', NULL, 9, FALSE),
('监考任务变更', '临床医学基础考试地点调整', 'ASSIGNMENT', 'UNREAD', 8, 0, '2023-10-30 10:00:00', NULL, 6, TRUE),
('评价提交提醒', '请对11月9日的监考进行评价', 'NOTIFICATION', 'READ', 9, 0, '2023-11-01 08:00:00', '2023-11-01 08:30:00', NULL, FALSE),
('培训进度通知', '您的《监考总结报告》学习进度已达80%', 'TRAINING', 'READ', 10, 0, '2023-10-12 18:00:00', '2023-10-12 18:30:00', 10, FALSE),
('账户激活提醒', '请尽快激活您的账户', 'SYSTEM', 'UNREAD', 4, 0, '2023-10-15 09:00:00', NULL, NULL, TRUE),
('监考反馈请求', '请填写上周监考反馈表', 'ASSIGNMENT', 'READ', 7, 0, '2023-10-27 15:00:00', '2023-10-27 15:30:00', 10, TRUE);

-- 监考记录表（10条）
INSERT INTO invigilation_record (assignment_id, type, status, content, creator_id, create_time, submit_time, reviewer_id, review_time, review_comment) VALUES
(1, 'SIGN_IN', 'APPROVED', '张三按时签到', 1, '2023-11-01 08:45:00', '2023-11-01 08:46:00', 2, '2023-11-01 08:50:00', '已确认'),
(2, 'SIGN_IN', 'APPROVED', '李四签到', 2, '2023-11-02 13:50:00', '2023-11-02 13:51:00', 1, '2023-11-02 13:55:00', '已确认'),
(3, 'INCIDENT', 'SUBMITTED', '王五报告考生作弊', 3, '2023-11-03 10:30:00', '2023-11-03 10:35:00', NULL, NULL, NULL),
(5, 'SIGN_IN', 'APPROVED', '陈七签到', 5, '2023-11-05 08:20:00', '2023-11-05 08:21:00', 1, '2023-11-05 08:25:00', '已确认'),
(6, 'NOTE', 'APPROVED', '周八备注：考场设备故障已处理', 6, '2023-11-06 13:15:00', '2023-11-06 13:20:00', 2, '2023-11-06 13:25:00', '处理及时'),
(8, 'SIGN_IN', 'APPROVED', '郑十签到', 8, '2023-11-10 09:45:00', '2023-11-10 09:46:00', 1, '2023-11-10 09:50:00', '已确认'),
(4, 'VIOLATION', 'SUBMITTED', '赵六报告考生突发疾病', 4, '2023-11-04 15:10:00', '2023-11-04 15:15:00', NULL, NULL, NULL),
(7, 'SIGN_IN', 'APPROVED', '吴九签到', 7, '2023-11-08 09:00:00', '2023-11-08 09:01:00', 2, '2023-11-08 09:05:00', '已确认'),
(9, 'NOTE', 'DRAFT', '钱十一备注：考试延后10分钟', 9, '2023-11-09 14:40:00', NULL, NULL, NULL, NULL),
(10, 'SIGN_IN', 'APPROVED', '孙十二签到', 10, '2023-11-07 09:25:00', '2023-11-07 09:26:00', 1, '2023-11-07 09:30:00', '已确认');

-- 评价表（10条）
INSERT INTO evaluation (assignment_id, evaluator_id, score, comment, create_time) VALUES
(1, 2, 95.0, '监考认真负责', '2023-11-01 12:00:00'),
(2, 1, 88.5, '考场秩序良好', '2023-11-02 16:30:00'),
(3, 5, 75.0, '处理突发事件较慢', '2023-11-03 12:30:00'),
(5, 6, 92.0, '准时到岗，沟通清晰', '2023-11-05 12:00:00'),
(6, 8, 85.5, '设备故障处理及时', '2023-11-06 15:30:00'),
(8, 9, 90.0, '监考流程规范', '2023-11-10 13:00:00'),
(4, 7, 80.0, '考生突发情况处理得当', '2023-11-04 17:15:00'),
(7, 10, 87.0, '监考态度友好', '2023-11-08 12:45:00'),
(9, 3, 78.5, '考试延后未提前通知', '2023-11-09 17:30:00'),
(10, 4, 93.0, '签到流程高效', '2023-11-07 11:50:00');

-- ----------------------------
-- 7. 提交数据
-- ----------------------------
COMMIT