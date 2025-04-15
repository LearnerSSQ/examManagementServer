# 考试管理系统

一个基于 Spring Boot 的考试管理系统，用于管理教师监考安排、培训记录和考务管理等功能。

## 功能特性

### 用户角色

- **通用功能**
  - 个人信息管理

- **系统管理员 (ADMIN)**
  - 系统设置管理
  - 系统日志查看
  - 教师账号管理
  - 教师培训管理

- **考务管理员 (EXAM_ADMIN)**
  - 监考安排管理
  - 监考记录管理

- **普通教师 (TEACHER)**
  - 监考任务查看
  - 培训记录查看

### 主要功能

- 个人信息管理
  - 基本信息维护
  - 监考统计
  - 培训完成率
  - 工作量统计

- 监考管理
  - 监考任务分配
  - 监考记录管理
  - 评价管理
  - 异常情况处理

- 培训管理
  - 培训材料管理
  - 培训记录追踪
  - 培训完成度统计

- 系统管理
  - 用户权限管理
  - 系统日志查看
  - 数据库管理
  - 系统设置

## 技术栈

### 后端

- Spring Boot 3.1.5
- Spring Security
- MyBatis-Plus 3.5.3.1
- PageHelper 分页插件 1.4.6
- MySQL 8.0+
- JWT 认证 (jjwt 0.11.5)
- Swagger/OpenAPI 3.0 (springdoc-openapi 2.2.0)

### 前端

- Thymeleaf
- Font Awesome
- Chart.js
- 现代化 UI 设计

## 开发环境要求

- JDK 17+
- MySQL 8.0+
- Gradle 7.x+

## 项目进度

### 已完成功能

- ✅ 用户认证与授权系统
- ✅ 基于JWT的安全机制
- ✅ 教师信息管理
- ✅ 监考任务基础管理
- ✅ 培训材料管理
- ✅ 系统日志功能
- ✅ 数据库结构设计与初始化

## 文件存储设计

### 存储路径结构

```bash
/static/training-materials/
    {year}/
        {month}/
            {day}/
                {creatorId}/
                    {materialId}
                        /files
```

### 路径生成规则

1. 根目录：`/static/training-materials`
2. 按日期组织：`/year/month/day` 便于按时间查找
3. 按创建者隔离：`/creatorId` 确保用户文件隔离
4. 按材料ID组织：`/materialId` 确保唯一性
5. 文件分类存储：
   - `/files` 存储主文件

### 示例路径

```bash
/static/training-materials/2025/03/03/12345/67890/files/document.pdf
```

### 安全措施

1. 文件上传限制：
   - 文件类型白名单
   - 文件大小限制
2. 访问控制：
   - 文件访问权限验证
   - 防止目录遍历攻击
3. 文件存储：
   - 使用唯一文件名
   - 定期清理过期文件

## 项目结构

```bash
examManagementServer/
├── gradle/                      # Gradle wrapper 目录
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/shishaoqi/examManagementServer/
│   │   │       ├── common/        # 通用工具类
│   │   │       ├── config/        # 配置类
│   │   │       ├── controller/    # 控制器
│   │   │       │   └── admin/     # 管理员控制器
│   │   │       ├── dto/           # 数据传输对象
│   │   │       ├── entity/        # 实体类
│   │   │       │   ├── base/      # 基础实体
│   │   │       │   ├── invigilation/ # 监考相关实体
│   │   │       │   ├── message/   # 消息实体
│   │   │       │   ├── teacher/   # 教师实体
│   │   │       │   └── training/  # 培训相关实体
│   │   │       ├── exception/     # 异常处理
│   │   │       ├── repository/    # 数据访问层
│   │   │       ├── security/      # 安全相关
│   │   │       ├── service/       # 服务层
│   │   │       │   └── impl/      # 服务实现
│   │   │       └── util/          # 工具类
│   │   └── resources/
│   │       ├── static/           # 静态资源
│   │       ├── templates/        # 模板文件
│   │       ├── db/               # 数据库脚本
│   │       │   ├── generate_database.sql # 数据库结构
│   │       │   └── Initialize_data.sql   # 初始化数据
│   │       └── application.yml   # 配置文件
│   └── test/                     # 测试代码
├── .gitignore                    # Git 忽略文件配置
├── .gitattributes               # Git 属性配置
├── build.gradle                 # Gradle 构建配置
├── gradlew                      # Gradle wrapper 脚本（Unix）
├── gradlew.bat                  # Gradle wrapper 脚本（Windows）
├── settings.gradle              # Gradle 项目设置
└── LICENSE.md                   # MIT 许可证
```

## 特色功能

1. **现代化 UI 设计**
   - 响应式布局
   - 暗色主题
   - 流畅动画效果

2. **完善的权限管理**
   - 基于角色的访问控制
   - JWT 令牌认证
   - 安全的密码存储

3. **数据可视化**
   - 监考统计图表
   - 工作量分析
   - 培训完成度追踪

4. **系统管理功能**
   - 数据库管理界面
   - 系统日志查看
   - 配置文件管理

## 系统架构与运作方式

### 核心类说明

#### 实体类（entity）

- **Teacher**: 教师实体类，存储教师基本信息、角色（ADMIN/EXAM_ADMIN/TEACHER）和状态（ACTIVE/INACTIVE/DISABLED）
- **InvigilatorAssignment**: 监考安排实体类，关联教师和考试信息，包含考试时间、地点、监考角色和状态
- **InvigilationRecord**: 监考记录实体类，记录监考完成情况和评价信息
- **TrainingMaterial**: 培训材料实体类，包含材料标题、描述、类型（视频/文档/测验/演示文稿）和存储路径
- **TrainingRecord**: 培训记录实体类，记录教师培训完成情况和进度
- **Message**: 系统消息实体类，用于通知和提醒，包含消息类型和状态
- **Evaluation**: 评价实体类，用于记录监考评价信息和分数

#### 控制器（controller）

- **AuthController**: 处理用户认证相关请求，实现登录功能
- **ProfileController**: 处理用户个人信息管理，包括信息查看和修改
- **MyAssignmentController**: 处理教师监考任务查看和确认
- **MyTrainingController**: 处理教师培训记录查看和培训进度更新
- **MessageController**: 处理系统消息的查看和管理
- **AdminTeacherController**: 处理管理员对教师账号的管理
- **AdminInvigilationController**: 处理考务管理员对监考任务的分配和管理
- **AdminTrainingController**: 处理管理员对培训材料和记录的管理
- **SettingsController**: 处理系统设置管理
- **LogsController**: 处理系统日志查看
- **DatabaseController**: 处理数据库管理

#### 服务层（service）

- **TeacherService**: 实现教师信息管理业务逻辑，包括账号管理、权限验证和工作量统计
- **InvigilatorAssignmentService**: 实现监考安排和管理业务逻辑，包括任务分配和确认
- **InvigilationRecordService**: 实现监考记录管理业务逻辑，包括记录创建和统计分析
- **TrainingMaterialService**: 实现培训材料管理业务逻辑，包括材料上传和分类
- **TrainingRecordService**: 实现培训记录管理业务逻辑，包括进度追踪和完成度统计
- **MessageService**: 实现系统消息管理业务逻辑，包括消息发送和状态更新
- **EvaluationService**: 实现评价管理业务逻辑，包括评分和报告生成

#### 数据访问层（repository）

- **TeacherMapper**: 教师数据访问接口，基于MyBatis-Plus实现
- **InvigilatorAssignmentMapper**: 监考安排数据访问接口
- **InvigilationRecordMapper**: 监考记录数据访问接口
- **TrainingMaterialMapper**: 培训材料数据访问接口
- **TrainingRecordMapper**: 培训记录数据访问接口
- **MessageMapper**: 消息数据访问接口
- **EvaluationMapper**: 评价数据访问接口

### 系统运作流程

1. **用户认证与授权流程**
   - **登录认证阶段**
     - 用户提交登录请求到AuthController，包含用户名和密码
     - AuthenticationManager调用TeacherService进行身份验证
     - TeacherService使用BCrypt验证密码，同时检查账号状态（ACTIVE/INACTIVE/DISABLED）
     - 验证成功后，JwtUtil生成包含用户ID、角色和权限的JWT令牌，设置过期时间
     - 返回JWT令牌和用户基本信息给客户端，客户端存储在localStorage
   - **请求授权阶段**
     - JwtAuthenticationFilter拦截所有API请求，从Authorization头提取JWT令牌
     - JwtUtil验证令牌签名、完整性和过期时间
     - TeacherUserDetailsService根据令牌中的用户ID加载完整的用户信息和权限
     - SecurityContextHolder保存认证信息用于后续授权决策
     - 基于@PreAuthorize注解进行方法级权限控制
   - **令牌管理机制**
     - 客户端实现令牌自动刷新机制，在令牌即将过期前请求新令牌
     - 服务端维护令牌黑名单，支持令牌主动失效
     - 实现单点登录控制，限制同一账号并发登录数量
     - 记录登录日志，异常登录行为检测与通知

2. **监考任务管理流程**
   - **监考任务创建与分配**
     - 考务管理员通过AdminInvigilationController创建监考任务批次
     - 考务管理员手动生成分配方案
     - 分配结果经考务管理员审核确认
   - **监考任务通知与确认**
     - 教师通过MyAssignmentController查看任务详情并进行操作：
       - 确认接受任务
       - 超时未确认则会被取消
   - **监考执行与记录**
     - 监考当天通过系统进行消息处理
     - 监考过程中记录异常情况：
       - 考生违规行为
       - 突发事件处理
       - 设备故障报告
     - 监考完成后，InvigilationRecordService记录详细信息
   - **监考评价与反馈**
     - EvaluationService处理多维度评价：
       - 时间准时性评分
       - 职责完成度评分
       - 应急处理能力评分
       - 考生/同事反馈收集

3. **培训管理全流程**
   - **培训需求分析与计划制定**
     - 管理员通过AdminTrainingController创建培训计划
     - 培训计划审批与发布流程
   - **培训资源管理与处理**
     - TrainingMaterialService处理培训资源：
       - 文件格式转换与优化（支持多种格式转PDF）
       - 自动生成预览和缩略图（便于快速浏览）
     - 培训资源分类与标签管理：
       - 按状态/类型分类

## 数据库设计

系统包含以下主要数据表：

- **teacher**: 教师信息表
- **invigilator_assignment**: 监考安排表
- **training_material**: 培训材料表
- **training_record**: 培训记录表
- **message**: 消息表
- **invigilation_record**: 监考记录表
- **evaluation**: 评价表

## 安全说明

- 使用 Spring Security 进行身份认证和授权
- 实现基于 JWT 的无状态认证
- 密码使用 BCrypt 加密存储
- 实现了角色基础的访问控制

## 日志系统

- 使用SLF4J + Logback实现日志记录
- 日志文件位置：`logs/app.log`
- 支持不同级别的日志记录（DEBUG, INFO, WARN, ERROR）
- 包含时间戳、线程信息和日志级别
