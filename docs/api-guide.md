# 考试监考管理系统 API 文档

## 1. 概述

### 1.1 简介
本文档详细说明了考试监考管理系统的API接口规范。系统采用RESTful API设计，提供教师管理、监考安排、培训管理等功能。

### 1.2 基础信息
- 基础URL: `/api/v1`
- 数据格式: JSON
- 字符编码: UTF-8
- 时间格式: ISO-8601 (yyyy-MM-dd'T'HH:mm:ss)

## 2. 教师管理

### 2.1 获取部门教师列表
```http
GET /teachers/department/{department}
```
获取指定部门的所有教师信息。

**响应示例：**
```json
[
  {
    "teacherId": 1,
    "name": "张三",
    "phone": "13800138000",
    "email": "zhangsan@example.com",
    "department": "计算机系",
    "title": "副教授"
  }
]
```

### 2.2 根据邮箱查询教师
```http
GET /teachers/email/{email}
```

### 2.3 根据手机号查询教师
```http
GET /teachers/phone/{phone}
```

## 3. 监考安排

### 3.1 获取教师的监考安排
```http
GET /assignments/teacher/{teacherId}
```
获取指定教师的所有监考安排。

**响应示例：**
```json
[
  {
    "assignmentId": 1,
    "teacherId": 1,
    "courseName": "高等数学",
    "examStart": "2024-02-20T09:00:00",
    "examEnd": "2024-02-20T11:00:00",
    "location": "教学楼A-101",
    "role": 0,
    "status": 1
  }
]
```

### 3.2 获取时间段内的监考安排
```http
GET /assignments/timeRange
```
**请求参数：**
- startTime: 开始时间 (ISO-8601格式)
- endTime: 结束时间 (ISO-8601格式)

### 3.3 检查时间冲突
```http
GET /assignments/conflict
```
**请求参数：**
- teacherId: 教师ID
- examStart: 考试开始时间
- examEnd: 考试结束时间

## 4. 培训管理

### 4.1 获取已发布的培训材料
```http
GET /materials/published
```
获取所有已发布状态的培训材料。

**响应示例：**
```json
[
  {
    "materialId": 1,
    "title": "监考规范培训",
    "description": "介绍监考工作的基本规范和要求",
    "type": 1,
    "requiredMinutes": 60,
    "status": 1
  }
]
```

### 4.2 获取指定类型的培训材料
```http
GET /materials/type/{type}
```
**类型说明：**
- 1: 文档
- 2: 视频
- 3: 测试

### 4.3 获取教师的培训记录
```http
GET /training-records/teacher/{teacherId}
```

### 4.4 获取培训材料的学习记录
```http
GET /training-records/material/{materialId}
```

## 5. 消息通知

### 5.1 获取未读消息
```http
GET /messages/unread/{teacherId}
```

### 5.2 获取所有消息
```http
GET /messages/teacher/{teacherId}
```

### 5.3 获取未读消息数量
```http
GET /messages/unread-count/{teacherId}
```

### 5.4 获取指定类型的消息
```http
GET /messages/type
```
**请求参数：**
- teacherId: 教师ID
- type: 消息类型 (1=系统通知, 2=监考提醒, 3=培训通知)

## 6. 监考记录

### 6.1 获取监考安排的记录
```http
GET /invigilation-records/assignment/{assignmentId}
```

### 6.2 获取签到记录
```http
GET /invigilation-records/sign-in/{assignmentId}
```

### 6.3 获取异常事件记录
```http
GET /invigilation-records/exceptions/{assignmentId}
```

## 7. 评价管理

### 7.1 获取监考安排的评价
```http
GET /evaluations/assignment/{assignmentId}
```

### 7.2 获取教师的评价
```http
GET /evaluations/teacher/{teacherId}
```

### 7.3 获取教师的平均评分
```http
GET /evaluations/average-score/{teacherId}
```

## 8. 错误码说明

### 8.1 HTTP状态码
- 200: 请求成功
- 400: 请求参数错误
- 404: 资源不存在
- 500: 服务器内部错误

### 8.2 业务状态码
- 1000: 操作成功
- 1001: 参数验证失败
- 1002: 资源不存在
- 1003: 资源已存在
- 1004: 操作失败

## 9. 注意事项

1. 时间参数统一使用ISO-8601格式
2. 分页参数统一使用page和size
3. 列表查询默认按创建时间倒序排序
4. 所有接口都支持跨域访问 