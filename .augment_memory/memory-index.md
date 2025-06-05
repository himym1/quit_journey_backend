# 记忆索引和元数据

## 记忆系统概览

**创建时间**: 2025-01-27
**最后更新**: 2025-01-27
**项目类型**: Kotlin + Spring Boot 后端服务
**记忆版本**: 1.0

## 长期记忆 (core/)

### architecture.md
- **内容**: 项目架构设计和技术栈说明
- **关键词**: Kotlin, Spring Boot, PostgreSQL, Redis, JWT, Docker
- **重要性**: ⭐⭐⭐⭐⭐
- **最后更新**: 2025-01-27

### best-practices.md
- **内容**: 开发最佳实践和编码规范
- **关键词**: 代码规范, 安全实践, 性能优化, 测试策略
- **重要性**: ⭐⭐⭐⭐
- **最后更新**: 2025-01-27

## 工作记忆

### activeContext.md
- **内容**: 当前项目状态和开发上下文
- **关键词**: 项目状态, 完成度, 下一步计划, 关键决策
- **重要性**: ⭐⭐⭐⭐⭐
- **最后更新**: 2025-01-27

## 短期记忆 (task-logs/)

### 任务日志
- **目录**: task-logs/
- **状态**: 待创建
- **用途**: 记录具体任务的执行过程和结果

## 核心概念索引

### 技术栈
- **主要语言**: Kotlin 1.9.22
- **框架**: Spring Boot 3.2.2
- **数据库**: PostgreSQL + Redis
- **安全**: Spring Security + JWT
- **构建工具**: Gradle Kotlin DSL
- **容器化**: Docker + Docker Compose

### 项目模块
1. **用户认证模块**: JWT认证, 用户注册/登录
2. **用户管理模块**: 资料管理, 统计数据
3. **每日打卡模块**: 打卡记录, 连续天数计算
4. **吸烟记录模块**: 记录管理, 统计分析
5. **成就系统模块**: 成就解锁, 进度追踪
6. **数据同步模块**: 多设备同步, 冲突解决

### 数据模型
- **User**: 用户基本信息和认证
- **UserProfile**: 用户详细资料和戒烟设置
- **DailyCheckIn**: 每日打卡记录
- **SmokingRecord**: 吸烟记录和触发因素
- **Achievement**: 成就定义和多语言支持
- **UserAchievement**: 用户成就解锁记录
- **SyncStatus**: 设备同步状态
- **DataChangeLog**: 数据变更追踪

### API端点
- **认证API**: /auth/* (注册, 登录, 刷新令牌)
- **用户API**: /users/* (资料, 设置, 统计)
- **打卡API**: /checkins/* (CRUD, 统计)
- **吸烟记录API**: /smoking-records/* (待实现)
- **成就API**: /achievements/* (待实现)

## 重要决策记录

### 技术选择理由
1. **Kotlin**: 现代化语言, 与Flutter Dart相似, 类型安全
2. **Spring Boot**: 企业级框架, 生态成熟, 开发效率高
3. **PostgreSQL**: 关系型数据库, JSON支持, 性能优秀
4. **JWT**: 无状态认证, 分布式友好, 移动端适配

### 架构决策
1. **分层架构**: Controller-Service-Repository-Entity
2. **UUID主键**: 分布式环境支持
3. **多环境配置**: dev/docker/production分离
4. **Docker化**: 容器化部署, 环境一致性

### 安全设计
1. **JWT双令牌**: 访问令牌 + 刷新令牌
2. **密码加密**: BCrypt哈希存储
3. **权限控制**: Spring Security RBAC
4. **数据保护**: 敏感信息加密传输

## 项目状态追踪

### 完成度统计
- **数据层**: 100% (8个实体, 6个Repository, 2个迁移脚本)
- **业务层**: 90% (5个核心Service实现)
- **控制层**: 80% (3个主要Controller)
- **安全配置**: 100% (JWT + Spring Security)
- **部署配置**: 100% (Docker + 多环境)

### 待办事项
1. 补充SmokingRecordController和AchievementController
2. 完善数据同步API
3. 编写单元测试和集成测试
4. Flutter客户端HTTP集成
5. 生产环境部署优化

## 关联项目

### Flutter前端项目
- **位置**: /Volumes/Android/FlutterProject/quitting_smoking/
- **关系**: 客户端应用, 调用本后端API
- **集成状态**: 待集成
- **技术栈**: Flutter + Dart + Riverpod

### 文档资源
- **技术方案**: doc/服务端技术方案设计.md
- **API设计**: doc/API详细设计文档.md
- **项目说明**: README.md

## 搜索标签

### 技术标签
#kotlin #springboot #postgresql #redis #jwt #docker #gradle #jpa #hibernate

### 功能标签
#authentication #user-management #checkin #smoking-record #achievement #sync #api

### 状态标签
#completed #in-progress #todo #testing #deployment

## 记忆维护

### 更新频率
- **activeContext.md**: 每次重要变更后更新
- **architecture.md**: 架构变更时更新
- **best-practices.md**: 发现新实践时更新
- **memory-index.md**: 添加新记忆时更新

### 清理策略
- 保留所有长期记忆
- 短期记忆按时间清理 (保留30天)
- 工作记忆实时更新
- 定期备份重要记忆

### 版本控制
- 重要变更创建备份
- 记录变更原因和时间
- 保持记忆一致性
- 定期验证记忆完整性
