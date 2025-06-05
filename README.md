# 焕新之旅 - 戒烟辅助应用后端服务

基于 Kotlin + Spring Boot 的现代化戒烟辅助应用后端API服务。

## 🚀 项目特性

- **现代化技术栈**: Kotlin + Spring Boot 3.x + PostgreSQL + Redis
- **安全认证**: JWT + Spring Security 6.x
- **数据同步**: 支持多设备数据同步和冲突解决
- **成就系统**: 完整的成就解锁和进度追踪
- **API文档**: 自动生成的OpenAPI 3.0文档
- **容器化**: Docker + Docker Compose 一键部署
- **监控健康**: Spring Boot Actuator 健康检查

## 📋 技术栈

### 核心框架
- **Kotlin 1.9.x** - 现代化JVM语言
- **Spring Boot 3.2.x** - 企业级应用框架
- **Spring Security 6.x** - 安全认证框架
- **Spring Data JPA** - 数据访问层

### 数据存储
- **PostgreSQL 15+** - 主数据库
- **Redis 7+** - 缓存和会话存储
- **Flyway** - 数据库版本管理

### 工具和库
- **Gradle Kotlin DSL** - 构建工具
- **Jackson** - JSON序列化
- **JJWT** - JWT令牌处理
- **SpringDoc OpenAPI** - API文档生成

## 🏗️ 项目结构

```
quit-journey-backend/
├── src/main/kotlin/com/quitjourney/
│   ├── QuitJourneyApplication.kt          # 应用启动类
│   ├── config/                            # 配置类
│   │   ├── SecurityConfig.kt              # 安全配置
│   │   ├── OpenApiConfig.kt               # API文档配置
│   │   └── CacheConfig.kt                 # 缓存配置
│   ├── controller/                        # REST控制器
│   │   ├── AuthController.kt              # 认证API
│   │   ├── UserController.kt              # 用户管理API
│   │   └── CheckInController.kt           # 打卡API
│   ├── service/                           # 业务逻辑层
│   │   ├── AuthService.kt                 # 认证服务
│   │   ├── UserService.kt                 # 用户服务
│   │   ├── DailyCheckInService.kt         # 打卡服务
│   │   ├── SmokingRecordService.kt        # 吸烟记录服务
│   │   └── AchievementService.kt          # 成就服务
│   ├── repository/                        # 数据访问层
│   ├── entity/                            # JPA实体类
│   ├── dto/                               # 数据传输对象
│   └── security/                          # 安全相关类
├── src/main/resources/
│   ├── application.yml                    # 应用配置
│   ├── application-dev.yml               # 开发环境配置
│   ├── application-docker.yml            # Docker环境配置
│   └── db/migration/                      # 数据库迁移脚本
├── docker-compose.yml                     # Docker编排文件
├── Dockerfile                            # Docker镜像构建
└── README.md                             # 项目说明
```

## 🚀 快速开始

### 环境要求

- **JDK 17+**
- **Docker & Docker Compose**
- **PostgreSQL 15+** (可选，可使用Docker)
- **Redis 7+** (可选，可使用Docker)

### 1. 克隆项目

```bash
git clone <repository-url>
cd quit-journey-backend
```

### 2. 使用Docker Compose启动

```bash
# 启动所有服务（推荐）
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f api
```

### 3. 本地开发启动

```bash
# 启动数据库服务
docker-compose up -d db redis

# 运行应用
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### 4. 验证服务

- **API文档**: http://localhost:8080/api/v1/swagger-ui.html
- **健康检查**: http://localhost:8080/api/v1/actuator/health
- **API根路径**: http://localhost:8080/api/v1/

## 📚 API文档

启动服务后，访问 [Swagger UI](http://localhost:8080/api/v1/swagger-ui.html) 查看完整的API文档。

### 主要API端点

#### 认证相关
- `POST /auth/register` - 用户注册
- `POST /auth/login` - 用户登录
- `POST /auth/refresh` - 刷新令牌

#### 用户管理
- `GET /users/profile` - 获取用户资料
- `PUT /users/profile` - 更新用户资料
- `GET /users/stats` - 获取用户统计

#### 每日打卡
- `GET /checkins` - 获取打卡记录
- `POST /checkins` - 创建打卡
- `GET /checkins/stats` - 打卡统计

## 🔧 配置说明

### 环境变量

| 变量名 | 描述 | 默认值 |
|--------|------|--------|
| `SPRING_PROFILES_ACTIVE` | 激活的配置文件 | `dev` |
| `SPRING_DATASOURCE_URL` | 数据库连接URL | `jdbc:postgresql://localhost:5432/quitjourney` |
| `SPRING_DATASOURCE_USERNAME` | 数据库用户名 | `quitjourney_user` |
| `SPRING_DATASOURCE_PASSWORD` | 数据库密码 | `quitjourney_pass` |
| `SPRING_REDIS_HOST` | Redis主机 | `localhost` |
| `SPRING_REDIS_PORT` | Redis端口 | `6379` |
| `JWT_SECRET` | JWT密钥 | `dev-secret-key` |

### 配置文件

- `application.yml` - 基础配置
- `application-dev.yml` - 开发环境配置
- `application-docker.yml` - Docker环境配置

## 🗄️ 数据库

### 数据库迁移

使用Flyway进行数据库版本管理：

```bash
# 查看迁移状态
./gradlew flywayInfo

# 执行迁移
./gradlew flywayMigrate

# 清理数据库（谨慎使用）
./gradlew flywayClean
```

### 主要数据表

- `users` - 用户基本信息
- `user_profiles` - 用户详细资料
- `daily_checkins` - 每日打卡记录
- `smoking_records` - 吸烟记录
- `achievements` - 成就定义
- `user_achievements` - 用户成就记录

## 🧪 测试

```bash
# 运行所有测试
./gradlew test

# 运行特定测试
./gradlew test --tests "com.quitjourney.service.*"

# 生成测试报告
./gradlew test jacocoTestReport
```

## 📦 构建和部署

### 构建JAR包

```bash
./gradlew build
```

### 构建Docker镜像

```bash
docker build -t quit-journey-backend .
```

### 生产环境部署

1. 修改生产环境配置
2. 构建Docker镜像
3. 使用Docker Compose或Kubernetes部署

## 🔍 监控和日志

### 健康检查

- **应用健康**: `/actuator/health`
- **数据库连接**: `/actuator/health/db`
- **Redis连接**: `/actuator/health/redis`

### 日志配置

日志级别可通过配置文件调整：

```yaml
logging:
  level:
    com.quitjourney: DEBUG
    org.springframework.security: INFO
```

## 🤝 开发指南

### 代码规范

- 使用Kotlin官方代码风格
- 遵循Spring Boot最佳实践
- 编写单元测试和集成测试
- 添加适当的API文档注释

### 提交规范

```
feat: 添加新功能
fix: 修复bug
docs: 更新文档
style: 代码格式调整
refactor: 代码重构
test: 添加测试
chore: 构建过程或辅助工具的变动
```

## 📄 许可证

MIT License - 详见 [LICENSE](LICENSE) 文件

## 📞 联系方式

- **项目维护**: Quit Journey Team
- **邮箱**: support@quitjourney.com
- **文档**: https://docs.quitjourney.com
