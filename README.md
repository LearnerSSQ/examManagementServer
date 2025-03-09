# 考试管理系统

一个基于 Spring Boot 的考试管理系统，用于管理教师监考安排、培训记录和考务管理等功能。

## 功能特性

### 用户角色

- **系统管理员 (ADMIN)**
  - 系统设置管理
  - 系统日志查看
  - 教师账号管理
  - 监考安排管理
  - 培训管理

- **考务管理员 (EXAM_ADMIN)**
  - 监考安排管理
  - 培训管理
  - 教师管理

- **普通教师 (TEACHER)**
  - 个人信息管理
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
- MyBatis-Plus
- MySQL
- JWT 认证

### 前端

- Thymeleaf
- Font Awesome
- Chart.js
- 现代化 UI 设计

## 开发环境要求

- JDK 17+
- MySQL 8.0+
- Gradle 7.x+

## 文件存储设计

### 存储路径结构

```bash
/static/training-materials/
    {year}/
        {month}/
            {day}/
                {creatorId}/
                    {materialId}
                        /files/
                        /thumbnails
```

### 路径生成规则

1. 根目录：`/static/training-materials`
2. 按日期组织：`/year/month/day` 便于按时间查找
3. 按创建者隔离：`/creatorId` 确保用户文件隔离
4. 按材料ID组织：`/materialId` 确保唯一性
5. 文件分类存储：
   - `/files` 存储主文件
   - `/thumbnails` 存储缩略图

### 示例路径

```bash
/static/training-materials/2025/03/03/12345/67890/files/document.pdf
```

### 安全措施

1. 文件上传限制：
   - 文件类型白名单
   - 文件大小限制
   - 病毒扫描
2. 访问控制：
   - 文件访问权限验证
   - 防止目录遍历攻击
3. 文件存储：
   - 使用唯一文件名
   - 定期清理过期文件

## 快速开始

1. **克隆项目**

   ```bash
   git clone https://github.com/LearnerSSQ/examManagementServer
   cd examManagementServer
   ```

2. **配置数据库**
   - 创建 MySQL 数据库
   - 修改 `application.yml` 中的数据库配置：

     ```yaml
     spring:
       datasource:
         url: jdbc:mysql://localhost:3306/exam_management?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
         username: your_username
         password: your_password
     ```

3. **初始化数据库**
   - 运行 `src/main/resources/db/generate_database.sql`
   - 运行 `src/main/resources/db/Initialize_data.sql`

4. **运行项目**

   ```bash
   # Windows
   gradlew.bat bootRun
   
   # Linux/MacOS
   ./gradlew bootRun
   ```

5. **访问系统（以本地数据库为例）**
   - 访问 `http://localhost:8080`
   - 默认管理员账号：<admin@example.com>
   - 默认密码：123456

## 项目结构

```bash
examManagementServer/
├── gradle/                      # Gradle wrapper 目录
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/shishaoqi/examManagementServer/
│   │   │       ├── config/        # 配置类
│   │   │       ├── controller/    # 控制器
│   │   │       ├── entity/        # 实体类
│   │   │       ├── service/       # 服务层
│   │   │       ├── repository/    # 数据访问层
│   │   │       └── security/      # 安全相关
│   │   └── resources/
│   │       ├── static/           # 静态资源
│   │       ├── templates/        # 模板文件
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

## 安全说明

- 使用 Spring Security 进行身份认证和授权
- 实现基于 JWT 的无状态认证
- 密码使用 BCrypt 加密存储
- 实现了角色基础的访问控制

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE.md](LICENSE.md) 文件了解详情
