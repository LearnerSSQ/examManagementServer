# 考试监考管理系统后端服务

本项目是一个基于Spring Boot的考试监考管理系统后端服务，提供完整的考试监考管理功能，包括教师管理、监考安排、培训管理、评价系统等模块。作为毕业设计作品，本项目展示了完整的后端开发流程和最佳实践。

## 项目概述

### 功能特点

1. **教师管理模块**
   - 教师信息的CRUD操作
   - 教师状态管理（在职/离职/禁用）
   - 按部门查询教师列表
   - 教师认证与登录
   - 登录时间记录

2. **监考安排模块**
   - 创建和更新监考安排
   - 监考时间冲突检测
   - 教师监考分配
   - 监考通知发送
   - 监考状态跟踪

3. **培训管理模块**
   - 培训材料管理
   - 培训记录跟踪
   - 学习进度统计
   - 培训有效期管理
   - 培训完成度检查

4. **评价系统模块**
   - 监考评价管理
   - 评分统计分析
   - 教师评价记录
   - 评价权限控制

5. **消息通知模块**
   - 系统通知管理
   - 消息状态跟踪
   - 未读消息提醒
   - 消息分类管理

### 技术栈

- **核心框架**：Spring Boot 2.7.17
- **数据库**：MySQL 8.0.33
- **ORM框架**：MyBatis Plus 3.5.3.1
- **项目管理**：Gradle
- **认证授权**：JWT + Spring Security
- **API文档**：OpenAPI 3.0 (Springdoc)
- **单元测试**：JUnit 5 + Mockito
- **日志框架**：SLF4J + Logback
- **其他工具**：Lombok, Jackson

## 开发环境搭建

### 前置要求

1. **JDK 17安装**
   ```bash
   # Windows
   # 1. 下载JDK 17 from https://adoptium.net/
   # 2. 配置环境变量
   setx JAVA_HOME "C:\Program Files\Java\jdk-17"
   setx Path "%Path%;%JAVA_HOME%\bin"
   
   # 验证安装
   java -version
   ```

2. **MySQL 8.0.33安装**
   ```bash
   # Windows
   # 1. 下载MySQL Installer from https://dev.mysql.com/downloads/
   # 2. 运行安装程序，选择Server Only选项
   # 3. 设置root密码
   
   # 验证安装
   mysql --version
   ```

3. **Gradle 7.x安装**
   ```bash
   # Windows
   # 1. 下载Gradle from https://gradle.org/releases/
   # 2. 解压到指定目录
   # 3. 配置环境变量
   setx GRADLE_HOME "C:\Gradle\gradle-7.x"
   setx Path "%Path%;%GRADLE_HOME%\bin"
   
   # 验证安装
   gradle -v
   ```

### 项目配置

1. **克隆项目**
   ```bash
   git clone [项目地址]
   cd examManagementServer
   ```

2. **数据库配置**
   ```sql
   -- 创建数据库
   CREATE DATABASE exam_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   
   -- 创建用户并授权
   CREATE USER 'exam_user'@'localhost' IDENTIFIED BY 'your_password';
   GRANT ALL PRIVILEGES ON exam_management.* TO 'exam_user'@'localhost';
   FLUSH PRIVILEGES;
   ```

3. **配置文件设置**
   ```yaml
   # src/main/resources/application.yml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/exam_management?serverTimezone=Asia/Shanghai
       username: exam_user
       password: your_password
       driver-class-name: com.mysql.cj.jdbc.Driver
   
     jpa:
       hibernate:
         ddl-auto: update
       show-sql: true
   
   server:
     port: 8080
     servlet:
       context-path: /api
   
   jwt:
     secret: your_jwt_secret
     expiration: 86400000  # 24小时
   ```

## 项目结构详解

```
src/
├── main/
│   ├── java/com/shishaoqi/examManagementServer/
│   │   ├── config/                 # 配置类
│   │   │   ├── SwaggerConfig.java  # API文档配置
│   │   │   ├── SecurityConfig.java # 安全配置
│   │   │   └── WebConfig.java      # Web配置
│   │   │
│   │   ├── controller/            # 控制器层
│   │   │   ├── TeacherController.java
│   │   │   ├── AssignmentController.java
│   │   │   └── ...
│   │   │
│   │   ├── service/              # 服务层
│   │   │   ├── impl/            # 服务实现
│   │   │   └── interfaces/      # 服务接口
│   │   │
│   │   ├── repository/          # 数据访问层
│   │   │   ├── TeacherMapper.java
│   │   │   └── ...
│   │   │
│   │   ├── entity/             # 实体类
│   │   │   ├── Teacher.java
│   │   │   └── ...
│   │   │
│   │   ├── dto/                # 数据传输对象
│   │   │   ├── request/       # 请求DTO
│   │   │   └── response/      # 响应DTO
│   │   │
│   │   ├── common/            # 公共组件
│   │   │   ├── Result.java    # 统一响应封装
│   │   │   └── Constants.java # 常量定义
│   │   │
│   │   └── exception/         # 异常处理
│   │       ├── GlobalExceptionHandler.java
│   │       └── BusinessException.java
│   │
│   └── resources/
│       ├── application.yml    # 主配置文件
│       ├── application-dev.yml  # 开发环境配置
│       └── application-prod.yml # 生产环境配置
│
└── test/
    └── java/com/shishaoqi/examManagementServer/
        └── service/           # 服务层测试
            ├── TeacherServiceTest.java
            └── ...
```

## 开发流程指南

### 1. 创建新功能

1. **定义实体类**
   ```java
   @Data
   @TableName("table_name")
   public class YourEntity {
       @TableId(type = IdType.AUTO)
       private Long id;
       
       @TableField("field_name")
       private String fieldName;
       
       // ... 其他字段
   }
   ```

2. **创建Mapper接口**
   ```java
   @Mapper
   public interface YourEntityMapper extends BaseMapper<YourEntity> {
       // 自定义查询方法
       @Select("SELECT * FROM table_name WHERE condition = #{value}")
       List<YourEntity> customQuery(@Param("value") String value);
   }
   ```

3. **实现Service层**
   ```java
   @Service
   public class YourServiceImpl extends ServiceImpl<YourEntityMapper, YourEntity> 
           implements YourService {
       
       @Override
       public YourEntity customMethod(String param) {
           // 实现业务逻辑
       }
   }
   ```

4. **编写Controller**
   ```java
   @RestController
   @RequestMapping("/api/your-entity")
   public class YourController {
       @Autowired
       private YourService yourService;
       
       @GetMapping("/{id}")
       public Result<YourEntity> getById(@PathVariable Long id) {
           // 实现控制器逻辑
       }
   }
   ```

### 2. 单元测试编写

1. **服务层测试**
   ```java
   @SpringBootTest
   class YourServiceTest {
       @MockBean
       private YourEntityMapper mapper;
       
       @Autowired
       private YourService service;
       
       @Test
       @DisplayName("测试场景描述")
       void testMethod() {
           // 准备测试数据
           // 执行测试
           // 验证结果
       }
   }
   ```

2. **控制器层测试**
   ```java
   @WebMvcTest(YourController.class)
   class YourControllerTest {
       @Autowired
       private MockMvc mockMvc;
       
       @MockBean
       private YourService service;
       
       @Test
       void testEndpoint() {
           // 模拟请求和验证
       }
   }
   ```

## API文档说明

### 访问方式

1. **Swagger UI**
   - 开发环境：http://localhost:8080/swagger-ui.html
   - 包含所有API端点的交互式文档
   - 支持在线测试API

2. **OpenAPI规范文档**
   - 地址：http://localhost:8080/v3/api-docs
   - 可导入到Postman等工具

### API认证

1. **获取Token**
   ```http
   POST /api/auth/login
   Content-Type: application/json
   
   {
     "username": "your_username",
     "password": "your_password"
   }
   ```

2. **使用Token**
   ```http
   GET /api/your-endpoint
   Authorization: Bearer your_token_here
   ```

## 部署指南

### 1. 开发环境

```bash
# 运行测试
./gradlew test

# 本地运行
./gradlew bootRun
```

### 2. 生产环境

1. **构建项目**
   ```bash
   ./gradlew build
   ```

2. **配置生产环境**
   ```yaml
   # application-prod.yml
   spring:
     datasource:
       url: jdbc:mysql://production-db:3306/exam_management
       username: prod_user
       password: prod_password
   ```

3. **运行应用**
   ```bash
   java -jar -Dspring.profiles.active=prod build/libs/examManagementServer-1.0.0.jar
   ```

### 3. Docker部署

1. **创建Dockerfile**
   ```dockerfile
   FROM adoptopenjdk/openjdk17:latest
   COPY build/libs/examManagementServer-1.0.0.jar app.jar
   ENTRYPOINT ["java","-jar","/app.jar"]
   ```

2. **构建和运行容器**
   ```bash
   docker build -t exam-management-server .
   docker run -p 8080:8080 exam-management-server
   ```

## 安全配置

### 1. JWT配置
```java
@Configuration
public class JwtConfig {
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private Long expiration;
    
    // JWT配置方法
}
```

### 2. 跨域配置
```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("http://localhost:3000")
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        .allowCredentials(true);
            }
        };
    }
}
```

## 错误处理

### 1. 错误码定义
```java
public enum ErrorCode {
    SUCCESS(200, "操作成功"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    // ... 其他错误码
}
```

### 2. 全局异常处理
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        // 处理业务异常
    }
    
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        // 处理其他异常
    }
}
```

## 性能优化

1. **数据库优化**
   - 合理使用索引
   - 优化SQL查询
   - 使用数据库连接池

2. **缓存策略**
   - 使用Redis缓存热点数据
   - 实现本地缓存
   - 缓存预热机制

3. **代码优化**
   - 使用线程池处理异步任务
   - 批量处理数据
   - 避免N+1查询问题

## 监控和日志

### 1. 日志配置
```xml
<!-- logback-spring.xml -->
<configuration>
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <!-- 日志配置 -->
    </appender>
    
    <root level="INFO">
        <appender-ref ref="FILE" />
    </root>
</configuration>
```

### 2. 性能监控
- 使用Spring Boot Actuator
- 集成Prometheus和Grafana
- 自定义健康检查端点

## 开发规范

1. **代码规范**
   - 遵循阿里巴巴Java开发手册
   - 使用统一的代码格式化工具
   - 编写清晰的注释和文档

2. **Git提交规范**
   ```
   feat: 新功能
   fix: 修复bug
   docs: 文档更新
   style: 代码格式调整
   refactor: 重构
   test: 测试相关
   chore: 构建过程或辅助工具的变动
   ```

3. **API设计规范**
   - 遵循RESTful设计原则
   - 使用统一的响应格式
   - 版本控制策略

## 常见问题解决

1. **数据库连接问题**
   ```
   问题：无法连接到数据库
   解决：
   1. 检查数据库服务是否运行
   2. 验证连接字符串
   3. 确认用户权限
   ```

2. **内存溢出**
   ```
   问题：JVM堆内存溢出
   解决：
   1. 调整JVM参数
   2. 检查内存泄漏
   3. 优化大对象处理
   ```

## 联系与支持

- **作者**：LearnerSSQ
- **邮箱**：18906671456@189.cn
- **项目地址**：https://github.com/LearnerSSQ/examManagementServer

## 许可证

本项目采用 MIT 许可证，详情请参见 [LICENSE](LICENSE) 文件。
