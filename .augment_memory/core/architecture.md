# 项目架构设计

## 项目概述

**项目名称**：Quit Journey Backend（焕新之旅后端服务）
**技术栈**：Kotlin + Spring Boot
**版本**：1.0.0
**创建时间**：2025-01-27

## 核心架构

### 技术架构
- **编程语言**：Kotlin 1.9.x
- **框架**：Spring Boot 3.2.x
- **安全认证**：Spring Security 6.x + JWT
- **数据库**：PostgreSQL 15+ (主数据库) + Redis 7+ (缓存)
- **ORM**：Spring Data JPA + Hibernate
- **构建工具**：Gradle Kotlin DSL
- **容器化**：Docker + Docker Compose

### 项目结构
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

### 架构模式
- **分层架构**：Controller → Service → Repository → Entity
- **依赖注入**：Spring IoC容器管理
- **面向切面编程**：Spring AOP用于横切关注点
- **RESTful API**：标准REST接口设计

## 核心功能模块

### 1. 用户认证模块
- JWT令牌认证
- 用户注册/登录
- 令牌刷新机制
- 密码重置功能

### 2. 用户管理模块
- 用户资料管理
- 戒烟设置配置
- 用户统计数据
- 账户设置管理

### 3. 每日打卡模块
- 打卡记录管理
- 连续天数计算
- 打卡统计分析
- 成就触发机制

### 4. 吸烟记录模块
- 吸烟记录管理
- 触发因素分析
- 统计报表生成
- 趋势分析

### 5. 成就系统模块
- 成就定义管理
- 成就解锁逻辑
- 进度追踪
- 积分系统

### 6. 数据同步模块
- 多设备数据同步
- 冲突解决机制
- 数据变更追踪
- 同步状态管理

## 数据库设计

### 核心表结构
- **users**: 用户基本信息
- **user_profiles**: 用户详细资料
- **daily_checkins**: 每日打卡记录
- **smoking_records**: 吸烟记录
- **achievements**: 成就定义
- **user_achievements**: 用户成就记录
- **sync_status**: 同步状态
- **data_change_logs**: 数据变更日志

### 数据关系
- 用户与资料：一对一关系
- 用户与打卡：一对多关系
- 用户与吸烟记录：一对多关系
- 用户与成就：多对多关系

## 安全设计

### 认证机制
- JWT Bearer Token认证
- 访问令牌 + 刷新令牌双令牌机制
- 令牌过期和刷新策略

### 授权控制
- 基于角色的访问控制(RBAC)
- API端点权限控制
- 数据访问权限验证

### 数据安全
- 密码BCrypt加密存储
- 敏感数据传输加密
- SQL注入防护
- XSS攻击防护

## 部署架构

### 开发环境
- 本地PostgreSQL + Redis
- Spring Boot DevTools热重载
- 详细日志输出

### Docker环境
- 容器化部署
- Docker Compose编排
- 服务间网络隔离
- 数据卷持久化

### 生产环境
- Kubernetes集群部署
- 负载均衡和高可用
- 监控和日志收集
- 自动扩缩容
