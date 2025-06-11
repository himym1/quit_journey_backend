# 🚀 Apifox集成指南

本文档详细介绍如何将焕新之旅API与Apifox集成，实现高效的API开发、测试和文档管理。

## 📋 目录

- [快速开始](#快速开始)
- [环境配置](#环境配置)
- [自动同步设置](#自动同步设置)
- [测试用例管理](#测试用例管理)
- [Mock服务配置](#mock服务配置)
- [团队协作](#团队协作)
- [最佳实践](#最佳实践)

## 🚀 快速开始

### 1. 安装Apifox

访问 [Apifox官网](https://apifox.com) 下载并安装客户端。

### 2. 创建项目

1. 打开Apifox客户端
2. 点击 "新建项目"
3. 项目名称：`焕新之旅 - 戒烟辅助应用 API`
4. 选择团队（如果有）

### 3. 导入API文档

#### 方式一：URL导入（推荐）
```
1. 点击 "导入数据" -> "OpenAPI"
2. 选择 "URL导入"
3. 输入URL: http://localhost:8080/api/v1/api-docs
4. 点击 "开始导入"
```

#### 方式二：文件导入
```bash
# 1. 先运行同步脚本下载文档
./scripts/apifox-sync.sh

# 2. 在Apifox中选择文件导入
# 文件路径: ./docs/api/openapi.json
```

## 🔧 环境配置

### 环境列表

| 环境名称 | 基础URL | 用途 |
|---------|---------|------|
| 本地开发 | `http://localhost:8080/api/v1` | 本地开发调试 |
| 局域网测试 | `http://192.168.1.100:8080/api/v1` | 团队内部测试 |
| 预发布环境 | `https://staging-api.quitjourney.com/api/v1` | 预发布测试 |
| 生产环境 | `https://api.quitjourney.com/api/v1` | 正式环境 |

### 全局变量配置

```json
{
  "accessToken": "",
  "refreshToken": "",
  "userId": "",
  "apiVersion": "v1",
  "baseUrl": "{{host}}/api/{{apiVersion}}"
}
```

### 认证配置

1. 在项目设置中选择 "认证"
2. 类型：`Bearer Token`
3. Token值：`{{accessToken}}`
4. 描述：`JWT认证令牌`

## 🔄 自动同步设置

### 配置自动同步

1. 进入项目设置 -> 数据同步
2. 启用 "自动同步"
3. 同步URL：`http://localhost:8080/api/v1/api-docs`
4. 同步频率：每日或手动
5. 保存设置

### 同步脚本使用

```bash
# 手动同步API文档
./scripts/apifox-sync.sh

# 检查API服务状态
curl -s http://localhost:8080/api/v1/actuator/health
```

## 🧪 测试用例管理

### 创建测试集合

1. **认证测试**
   - 用户注册
   - 用户登录
   - 令牌刷新
   - 令牌验证

2. **用户管理测试**
   - 获取用户资料
   - 更新用户资料
   - 用户统计数据

3. **打卡功能测试**
   - 创建打卡记录
   - 获取打卡历史
   - 打卡统计分析

### 测试数据准备

```json
{
  "testUser": {
    "username": "test_user_001",
    "email": "test@quitjourney.com",
    "password": "Test123456!",
    "quitDate": "2024-01-01T00:00:00Z"
  }
}
```

## 🎭 Mock服务配置

### 启用Mock服务

1. 在项目设置中启用 "Mock服务"
2. 配置Mock规则
3. 设置响应延迟：100-500ms
4. 启用智能Mock

### Mock数据示例

```json
{
  "success": true,
  "data": {
    "id": "@integer(1, 1000)",
    "username": "@name",
    "email": "@email",
    "quitDate": "@datetime",
    "daysQuit": "@integer(1, 365)"
  },
  "meta": {
    "timestamp": "@datetime",
    "requestId": "@guid",
    "version": "1.0"
  }
}
```

## 👥 团队协作

### 邀请团队成员

1. 进入项目设置 -> 成员管理
2. 点击 "邀请成员"
3. 输入邮箱地址
4. 设置权限级别

### 权限管理

- **管理员**：完全权限
- **编辑者**：可编辑API和测试用例
- **查看者**：只读权限

### 协作功能

- **评论系统**：在API接口上添加评论
- **变更通知**：API变更时自动通知团队
- **版本管理**：API文档版本控制

## 💡 最佳实践

### 1. 命名规范

- **接口命名**：使用中文描述，如 "用户登录"、"获取打卡记录"
- **参数命名**：保持与代码一致
- **示例命名**：使用有意义的示例名称

### 2. 文档维护

- 定期同步API文档
- 及时更新接口描述
- 添加详细的请求/响应示例

### 3. 测试策略

- 为每个接口编写测试用例
- 使用环境变量管理不同环境
- 定期执行回归测试

### 4. Mock使用

- 前端开发时使用Mock服务
- 配置真实的Mock数据
- 模拟各种异常情况

## 🔍 故障排除

### 常见问题

1. **导入失败**
   - 检查API服务是否启动
   - 确认OpenAPI文档格式正确
   - 检查网络连接

2. **认证失败**
   - 确认Token格式正确
   - 检查Token是否过期
   - 验证权限设置

3. **同步问题**
   - 检查同步URL是否正确
   - 确认API文档可访问
   - 查看同步日志

### 联系支持

- **项目问题**：support@quitjourney.com
- **Apifox支持**：https://apifox.com/help

---

**最后更新**：2024年12月  
**维护者**：Quit Journey开发团队
