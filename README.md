# 考试管理系统服务端

这是一个基于Spring Boot的考试管理系统后端服务，提供完整的考试监考管理功能。

## 技术栈

- **Java 21**
- **Spring Boot 2.7.17**
- **MySQL 8.0**
- **MyBatis Plus 3.5.3.1**
- **Gradle**
- **JWT认证**
- **OpenAPI 3.0 (Springdoc)**

## 项目结构

```
src/main/java/com/shishaoqi/examManagementServer/
├── ExamManagementServerApplication.java  # 应用程序入口
├── config/                              # 配置类
├── controller/                          # 控制器层
├── entity/                             # 实体类
├── repository/                          # 数据访问层
└── service/                            # 业务逻辑层
    └── impl/                           # 业务逻辑实现
```

## 主要功能模块

### 1. 监考安排管理
- 创建和更新监考安排
- 查询教师的监考安排
- 按时间范围查询监考安排
- 更新监考安排状态
- 取消监考安排

### 2. 培训材料管理
- 发布培训材料
- 按类型查询培训材料
- 更新培训材料状态
- 设置考试通过分数

### 3. 监考评价管理
- 创建和更新监考评价
- 查询监考安排的评价
- 查询教师的评价记录
- 计算教师的平均评分

### 4. 消息通知管理
- 发送系统通知
- 查询未读消息
- 标记消息已读
- 按类型查询消息

### 5. 监考记录管理
- 记录监考签到情况
- 记录异常事件
- 查询监考记录
- 统计异常事件

## API文档

### 访问方式

API文档提供两种访问方式：

1. **Swagger UI界面**
   - 地址：http://localhost:8080/swagger-ui.html
   - 提供交互式API文档
   - 支持在线测试API

2. **OpenAPI规范文档**
   - 地址：http://localhost:8080/v3/api-docs
   - 提供标准的OpenAPI 3.0文档
   - 可导入到其他工具中使用

### API分组

- `/api/assignments/**` - 监考安排相关接口
- `/api/training-materials/**` - 培训材料相关接口
- `/api/evaluations/**` - 监考评价相关接口
- `/api/messages/**` - 消息通知相关接口
- `/api/invigilation-records/**` - 监考记录相关接口

## 快速开始

### 环境要求

- JDK 21
- MySQL 8.0+
- Gradle 7.x+

### 数据库配置

1. 创建MySQL数据库：
```sql
CREATE DATABASE exam_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 在`application.yml`中配置数据库连接：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/exam_management
    username: your_username
    password: your_password
```

### 构建和运行

1. 克隆项目：
```bash
git clone [项目地址]
```

2. 进入项目目录：
```bash
cd examManagementServer
```

3. 构建项目：
```bash
./gradlew build
```

4. 运行项目：
```bash
./gradlew bootRun
```

服务将在 http://localhost:8080 启动

## 配置说明

主要配置文件位于 `src/main/resources/application.yml`，包含：

- 服务器配置
- 数据库连接配置
- MyBatis Plus配置
- JWT配置
- Swagger文档配置

## 开发指南

1. 遵循标准的Java代码规范
2. 使用Lombok简化代码
3. 使用MyBatis Plus进行数据库操作
4. 实现RESTful API规范
5. 使用Swagger注解文档化API

## 安全说明

1. 所有API都需要JWT认证
2. 密码需要加密存储
3. 使用HTTPS进行通信
4. 实现了角色权限控制

## 贡献指南

1. Fork 本仓库
2. 创建新的特性分支
3. 提交更改
4. 创建Pull Request

## 许可证

[MIT License](LICENSE)
