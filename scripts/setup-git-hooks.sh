#!/bin/bash

# Git Hook 自动同步设置脚本
# 使用方法: ./scripts/setup-git-hooks.sh

set -e

echo "🔧 设置 Git Hook 自动同步..."

# 检查是否在 Git 仓库中
if [ ! -d ".git" ]; then
    echo "❌ 当前目录不是 Git 仓库"
    exit 1
fi

# 创建 Git Hooks 目录（如果不存在）
mkdir -p .git/hooks

# 创建 post-commit hook
cat > .git/hooks/post-commit << 'EOF'
#!/bin/bash

# Git Post-Commit Hook - 自动同步 Apifox
# 在每次提交后自动生成 API 文档

echo "🔄 Post-commit: 开始同步 API 文档..."

# 检查是否有 Docker 服务在运行
if docker-compose ps | grep -q "Up"; then
    echo "📋 检测到 Docker 服务运行中，开始同步..."
    
    # 快速更新 Docker 容器
    ./scripts/docker-update.sh fast
    
    # 生成 Apifox 文档
    ./scripts/sync-apifox-simple.sh
    
    echo "✅ API 文档同步完成！"
else
    echo "⚠️  Docker 服务未运行，跳过同步"
    echo "💡 手动启动: ./scripts/docker-update.sh fast"
fi

echo "🎉 Post-commit hook 执行完成"
EOF

# 给 hook 添加执行权限
chmod +x .git/hooks/post-commit

# 创建 pre-push hook（可选）
cat > .git/hooks/pre-push << 'EOF'
#!/bin/bash

# Git Pre-Push Hook - 推送前同步检查
# 确保 API 文档是最新的

echo "🚀 Pre-push: 检查 API 文档状态..."

# 检查 API 服务是否可用
if curl -s --fail "http://localhost:8080/api/v1/api-docs" > /dev/null 2>&1; then
    echo "✅ API 服务正常，文档可同步"
    
    # 生成最新文档备份
    ./scripts/sync-apifox-simple.sh
    
    echo "📤 推送前文档同步完成"
else
    echo "⚠️  API 服务不可用，请检查："
    echo "   1. Docker 容器是否运行"
    echo "   2. API 服务是否启动"
    echo ""
    echo "🔧 快速修复命令:"
    echo "   ./scripts/docker-update.sh fast"
    echo ""
    
    read -p "是否继续推送？(y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "❌ 推送取消"
        exit 1
    fi
fi

echo "🎉 Pre-push hook 执行完成"
EOF

# 给 pre-push hook 添加执行权限
chmod +x .git/hooks/pre-push

echo ""
echo "✅ Git Hook 自动同步设置完成！"
echo ""
echo "📋 已创建的 Hooks："
echo "   - post-commit: 提交后自动同步文档"
echo "   - pre-push: 推送前检查并同步文档"
echo ""
echo "🧪 测试 Hooks："
echo "   git add . && git commit -m 'test: 测试自动同步'"
echo ""
echo "🔧 手动触发："
echo "   .git/hooks/post-commit  # 测试 post-commit hook"
echo ""
echo "⚠️  注意：Hooks 只在本地生效，团队成员需要各自运行此脚本" 