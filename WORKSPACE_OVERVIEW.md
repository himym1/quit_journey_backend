# 工作区项目概览 - 焕新之旅戒烟应用

> **AI记忆提示**: 此工作区包含两个独立但相关的项目：Flutter移动端应用 + Spring Boot后端服务

## 📋 工作区概述

**项目名称**: 焕新之旅 (Quit Journey) - 戒烟辅助应用  
**工作区路径**: `/Users/wangjianguo/IdeaProjects/quit-journey-backend`
**项目类型**: 全栈移动应用 (前后端分离架构)  
**开发状态**: MVP功能基本完成，后端集成进行中

---

## 📱 项目1: Flutter前端应用

### 基本信息
- **项目类型**: Flutter移动应用 (iOS/Android)
- **包名**: `quitting_smoking`
- **版本**: 1.0.0+1
- **主要文件**: `pubspec.yaml`, `lib/main.dart`

### 技术栈
- **框架**: Flutter 3.x + Dart 3.7.0+
- **状态管理**: Riverpod 2.5.1
- **路由**: GoRouter 15.1.2
- **本地存储**: Hive + SharedPreferences
- **网络请求**: Dio 5.4.0 + Retrofit 4.0.3
- **UI组件**: Material Design + Lottie动画
- **国际化**: 中文/英文双语支持

### 核心功能模块
- ✅ **用户认证**: 登录/注册/初始设置
- ✅ **进度追踪**: 戒烟时长、节省金额、少吸烟支数
- ✅ **健康效益**: 分阶段健康恢复里程碑展示
- ✅ **烟瘾管理**: "我想吸烟"应急按钮、应对策略、深呼吸练习
- ✅ **每日打卡**: 本地存储、即时反馈
- ✅ **成就系统**: 12种预设成就、自动解锁、徽章墙
- ✅ **数据统计**: 月度日历视图、趋势图表
- ✅ **主题系统**: 明亮/暗黑/跟随系统
- 🔄 **云端同步**: 与后端API集成中

### 项目结构
```
lib/
├── main.dart                 # 应用入口
├── app_widget.dart          # 应用根组件
├── core/                    # 核心工具、配置、主题
├── data/                    # 数据层 (本地/远程数据源)
├── domain/                  # 领域层 (实体、仓库接口、用例)
└── presentation/            # 表现层 (页面、组件、状态管理)
```

### 快速启动
```bash
# 安装依赖
flutter pub get

# 代码生成
flutter packages pub run build_runner build

# 运行应用 (调试模式)
flutter run

# 运行应用 (发布模式)
flutter run --release
```

---

## 🚀 项目2: Spring Boot后端服务

### 基本信息
- **项目类型**: Spring Boot REST API服务
- **语言**: Kotlin
- **版本**: Spring Boot 3.2.x
- **主要文件**: `build.gradle.kts`, `src/main/kotlin/`

### 技术栈
- **框架**: Spring Boot 3.2.x + Kotlin 1.9.x
- **安全**: Spring Security 6.x + JWT认证
- **数据库**: PostgreSQL 15+ (主库) + Redis 7+ (缓存)
- **ORM**: Spring Data JPA + Hibernate
- **构建**: Gradle Kotlin DSL
- **容器化**: Docker + Docker Compose
- **API文档**: SpringDoc OpenAPI 3.0

### 核心API模块
- **认证服务**: 用户注册/登录/令牌刷新
- **用户管理**: 用户资料、统计数据
- **每日打卡**: 打卡记录CRUD、统计分析
- **吸烟记录**: 吸烟数据记录和管理
- **成就系统**: 成就解锁和进度追踪
- **数据同步**: 多设备数据同步和冲突解决

### 项目结构
```
src/main/kotlin/com/quitjourney/
├── QuitJourneyApplication.kt    # 应用启动类
├── config/                      # 配置类 (安全、缓存、API文档)
├── controller/                  # REST控制器
├── service/                     # 业务逻辑层
├── repository/                  # 数据访问层
├── entity/                      # JPA实体类
├── dto/                         # 数据传输对象
└── security/                    # 安全相关类
```

### 数据库表结构
- `users` - 用户基本信息
- `user_profiles` - 用户详细资料
- `daily_checkins` - 每日打卡记录
- `smoking_records` - 吸烟记录
- `achievements` - 成就定义
- `user_achievements` - 用户成就记录

### 快速启动
```bash
# 使用Docker Compose启动所有服务 (推荐)
docker-compose up -d

# 本地开发启动 (需要先启动数据库)
docker-compose up -d db redis
./gradlew bootRun --args='--spring.profiles.active=dev'

# 构建JAR包
./gradlew build
```

### 服务端点
- **API文档**: http://localhost:8080/api/v1/swagger-ui/index.html
- **健康检查**: http://localhost:8080/api/v1/actuator/health
- **API根路径**: http://localhost:8080/api/v1/

---

## 🔗 项目关系

### 架构概述
```
Flutter App (移动端)
    ↕ HTTP/HTTPS
Spring Boot API (后端)
    ↕ JDBC
PostgreSQL (数据库) + Redis (缓存)
```

### 数据流
1. **用户操作** → Flutter UI
2. **状态管理** → Riverpod Providers
3. **本地存储** → Hive数据库 (离线支持)
4. **网络请求** → Dio + Retrofit → Spring Boot API
5. **数据持久化** → PostgreSQL数据库
6. **缓存优化** → Redis缓存

### 开发模式
- **前端**: 本地开发，使用Hive本地存储
- **后端**: Docker容器化开发环境
- **集成**: 前端通过API与后端通信，支持离线模式

---

## 🛠️ 开发环境要求

### Flutter前端
- Flutter SDK 3.x+
- Dart SDK 3.7.0+
- Android Studio / VS Code
- iOS开发需要Xcode (macOS)

### Spring Boot后端
- JDK 17+
- Docker & Docker Compose
- IntelliJ IDEA (推荐)

### 数据库
- PostgreSQL 15+ (可用Docker)
- Redis 7+ (可用Docker)

---

## 📊 项目状态

### 已完成功能
- ✅ Flutter前端MVP功能 (90%完成)
- ✅ Spring Boot后端API框架
- ✅ 数据库设计和迁移脚本
- ✅ Docker容器化部署

### 进行中
- 🔄 前后端API集成
- 🔄 数据同步机制
- 🔄 用户认证流程

### 待开发
- ⏳ 生产环境部署
- ⏳ 应用商店发布
- ⏳ 高级功能 (社区、AI建议等)

---

## 🚀 快速命令速查

### Flutter开发
```bash
flutter pub get                 # 安装依赖
flutter run                     # 运行应用
flutter build apk              # 构建Android APK
flutter test                   # 运行测试
flutter analyze                # 代码分析
```

### Spring Boot开发
```bash
docker-compose up -d            # 启动所有服务
./gradlew bootRun              # 运行后端服务
./gradlew test                 # 运行测试
./gradlew build                # 构建项目
```

### 数据库管理
```bash
docker-compose logs db         # 查看数据库日志
./gradlew flywayMigrate       # 执行数据库迁移
./gradlew flywayInfo          # 查看迁移状态
```

---

**最后更新**: 2024年12月
**维护者**: Quit Journey开发团队
