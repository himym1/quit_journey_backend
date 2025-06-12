#!/bin/bash

# 本地打包并部署到服务器脚本
# 用法: ./scripts/deploy-to-server.sh

set -e

# 配置变量
SERVER_IP="199.68.217.152"
SERVER_USER="root"
SERVER_PATH="/opt/quit-journey-backend"
DOMAIN="chat.himym.lat"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

echo "🚀 本地打包并部署到服务器..."
echo "🌐 目标服务器: $SERVER_IP"
echo "📁 部署路径: $SERVER_PATH"

# 检查本地环境
check_local_env() {
    log_info "检查本地环境..."
    
    if [ ! -f "gradlew" ]; then
        log_error "当前目录不是项目根目录"
        exit 1
    fi
    
    if ! command -v ssh &> /dev/null; then
        log_error "SSH命令不可用"
        exit 1
    fi
    
    if ! command -v scp &> /dev/null; then
        log_error "SCP命令不可用"
        exit 1
    fi
    
    log_success "本地环境检查完成"
}

# 本地构建
build_local() {
    log_info "本地构建应用..."
    
    # 清理并构建
    ./gradlew clean build -x test --no-daemon
    
    log_success "本地构建完成"
}

# 创建部署包
create_package() {
    log_info "创建部署包..."
    
    # 创建临时目录
    TEMP_DIR=$(mktemp -d)
    PACKAGE_DIR="$TEMP_DIR/quit-journey-backend"
    
    mkdir -p "$PACKAGE_DIR"
    
    # 复制必要文件
    cp -r build/ "$PACKAGE_DIR/"
    cp -r src/ "$PACKAGE_DIR/"
    cp -r gradle/ "$PACKAGE_DIR/"
    cp -r scripts/ "$PACKAGE_DIR/"
    cp -r nginx/ "$PACKAGE_DIR/"
    cp gradlew "$PACKAGE_DIR/"
    cp gradlew.bat "$PACKAGE_DIR/"
    cp build.gradle.kts "$PACKAGE_DIR/"
    cp settings.gradle.kts "$PACKAGE_DIR/"
    cp docker-compose.prod.yml "$PACKAGE_DIR/"
    cp Dockerfile "$PACKAGE_DIR/"
    cp init-db.sql "$PACKAGE_DIR/"
    cp redis.conf "$PACKAGE_DIR/"
    
    # 如果存在其他配置文件
    [ -f "DEPLOYMENT.md" ] && cp DEPLOYMENT.md "$PACKAGE_DIR/"
    
    # 创建tar包
    cd "$TEMP_DIR"
    tar -czf quit-journey-backend.tar.gz quit-journey-backend/
    
    echo "$TEMP_DIR/quit-journey-backend.tar.gz"
}

# 上传到服务器
upload_to_server() {
    local package_file=$1
    
    log_info "上传到服务器..."
    
    # 上传包文件
    scp "$package_file" "$SERVER_USER@$SERVER_IP:/tmp/"
    
    log_success "文件上传完成"
}

# 在服务器上部署
deploy_on_server() {
    log_info "在服务器上部署..."
    
    ssh "$SERVER_USER@$SERVER_IP" << 'EOF'
        set -e
        
        # 颜色定义
        RED='\033[0;31m'
        GREEN='\033[0;32m'
        YELLOW='\033[1;33m'
        BLUE='\033[0;34m'
        NC='\033[0m'
        
        log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
        log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
        log_error() { echo -e "${RED}[ERROR]${NC} $1"; }
        
        SERVER_PATH="/opt/quit-journey-backend"
        
        log_info "服务器端部署开始..."
        
        # 停止旧服务
        if [ -d "$SERVER_PATH" ]; then
            log_info "停止旧服务..."
            cd "$SERVER_PATH"
            if [ -f "docker-compose.prod.yml" ]; then
                docker-compose -f docker-compose.prod.yml down || true
            fi
        fi
        
        # 备份旧版本
        if [ -d "$SERVER_PATH" ]; then
            log_info "备份旧版本..."
            sudo mv "$SERVER_PATH" "$SERVER_PATH.backup.$(date +%Y%m%d_%H%M%S)" || true
        fi
        
        # 创建新目录
        sudo mkdir -p "$SERVER_PATH"
        sudo chown $USER:$USER "$SERVER_PATH"
        
        # 解压新版本
        log_info "解压新版本..."
        cd /tmp
        tar -xzf quit-journey-backend.tar.gz
        mv quit-journey-backend/* "$SERVER_PATH/"
        rm -rf quit-journey-backend quit-journey-backend.tar.gz
        
        # 进入项目目录
        cd "$SERVER_PATH"
        
        # 创建必要目录
        mkdir -p logs logs/nginx backups ssl
        chmod 755 logs backups
        
        # 创建SSL证书
        if [ ! -f "ssl/cert.pem" ] || [ ! -f "ssl/key.pem" ]; then
            log_info "创建SSL证书..."
            openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
                -keyout ssl/key.pem \
                -out ssl/cert.pem \
                -subj "/C=CN/ST=Beijing/L=Beijing/O=Himym/CN=chat.himym.lat"
        fi
        
        # 给脚本执行权限
        chmod +x gradlew
        chmod +x scripts/*.sh
        
        # 启动服务
        log_info "启动服务..."
        docker-compose -f docker-compose.prod.yml up -d --build
        
        # 等待服务启动
        log_info "等待服务启动..."
        sleep 30
        
        # 检查服务状态
        if curl -f http://localhost:8080/api/v1/actuator/health &> /dev/null; then
            log_success "服务启动成功！"
            
            echo ""
            log_info "服务访问地址:"
            echo "🌐 HTTP:  http://chat.himym.lat/api/v1/"
            echo "🔒 HTTPS: https://chat.himym.lat/api/v1/"
            echo "💓 健康检查: http://chat.himym.lat/health"
            echo "📚 API文档: http://chat.himym.lat/api/v1/swagger-ui.html"
            echo "🔗 直接访问: http://199.68.217.152:8080/api/v1/"
            
            echo ""
            log_info "服务状态:"
            docker-compose -f docker-compose.prod.yml ps
        else
            log_error "服务启动失败"
            docker-compose -f docker-compose.prod.yml logs
            exit 1
        fi
EOF
    
    log_success "服务器部署完成"
}

# 清理临时文件
cleanup() {
    if [ -n "$TEMP_DIR" ] && [ -d "$TEMP_DIR" ]; then
        rm -rf "$TEMP_DIR"
        log_info "清理临时文件完成"
    fi
}

# 主函数
main() {
    log_info "=========================================="
    log_info "焕新之旅后端服务 - 自动化部署"
    log_info "时间: $(date)"
    log_info "=========================================="
    
    # 设置清理陷阱
    trap cleanup EXIT
    
    check_local_env
    build_local
    
    PACKAGE_FILE=$(create_package)
    log_success "部署包创建完成: $PACKAGE_FILE"
    
    upload_to_server "$PACKAGE_FILE"
    deploy_on_server
    
    log_success "=========================================="
    log_success "部署完成！"
    log_success "您的API服务现在可以通过以下地址访问："
    log_success "http://chat.himym.lat/api/v1/"
    log_success "http://199.68.217.152:8080/api/v1/"
    log_success "=========================================="
}

# 执行主函数
main
