#!/bin/bash

# Docker 快速更新脚本
# 用法: ./scripts/docker-update.sh [模式]
# 模式: fast (快速) | full (完整) | dev (开发)

set -e

MODE=${1:-fast}

echo "🐳 Docker 更新脚本启动..."
echo "📋 模式: $MODE"

case $MODE in
    "fast")
        echo "⚡ 快速更新模式"
        
        echo "0️⃣ 重新编译JAR文件..."
        ./gradlew assemble --no-daemon
        
        echo "1️⃣ 停止 API 容器..."
        docker-compose stop api
        docker-compose rm -f api
        
        echo "2️⃣ 重新构建 API 镜像..."
        docker-compose build api
        
        echo "3️⃣ 启动 API 服务..."
        docker-compose up -d api
        
        echo "4️⃣ 等待服务启动..."
        sleep 15
        ;;
        
    "full")
        echo "🔄 完整重构建模式"
        echo "1️⃣ 停止所有容器..."
        docker-compose down
        
        echo "2️⃣ 重新构建所有镜像..."
        docker-compose build --no-cache
        
        echo "3️⃣ 启动所有服务..."
        docker-compose up -d
        
        echo "4️⃣ 等待服务启动..."
        sleep 30
        ;;
        
    "dev")
        echo "💻 开发模式（本地运行）"
        echo "1️⃣ 停止 Docker 服务..."
        docker-compose down
        
        echo "2️⃣ 启动数据库和 Redis..."
        docker-compose up -d db redis
        
        echo "3️⃣ 等待数据库启动..."
        sleep 10
        
        echo "4️⃣ 本地启动 API..."
        echo "💡 请在另一个终端运行: SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun"
        echo "🔗 API 将在 http://localhost:8080 启动"
        exit 0
        ;;
        
    *)
        echo "❌ 未知模式: $MODE"
        echo "💡 可用模式: fast, full, dev"
        exit 1
        ;;
esac

# 验证服务状态
echo "🔍 检查服务状态..."
docker-compose ps

echo "📊 检查 API 接口..."
if curl -s --fail "http://localhost/api/v1/api-docs" > /dev/null; then
    echo "✅ API 服务正常运行"
    
    # 检查是否有测试接口
    if curl -s "http://localhost/api/v1/api-docs" | grep -q "测试接口"; then
        echo "✅ 测试接口已包含"
    else
        echo "⚠️ 测试接口未找到（可能还在构建中）"
    fi
    
    # 运行同步测试
    echo "🔄 执行 Apifox 同步测试..."
    if [ -f "./scripts/test-sync.sh" ]; then
        ./scripts/test-sync.sh
    fi
    
else
    echo "❌ API 服务启动失败"
    echo "📋 查看日志:"
    docker-compose logs api --tail=20
fi

echo "🎉 更新完成！" 