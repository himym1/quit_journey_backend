#!/bin/bash

# 服务器自动化部署脚本
# 用法: ./scripts/server-auto-deploy.sh [模式]
# 模式: init (初始化) | update (更新) | restart (重启)

set -e

# 配置变量
PROJECT_DIR="/opt/quit-journey-backend"
GIT_REPO="https://github.com/your-username/quit-journey-backend.git"  # 请替换为您的实际仓库地址
DOMAIN="chat.himym.lat"
SERVER_IP="199.68.217.152"

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

MODE=${1:-update}

echo "🚀 服务器自动化部署脚本启动..."
echo "📋 模式: $MODE"
echo "🌐 域名: $DOMAIN"
echo "📍 服务器: $SERVER_IP"

# 检查Docker环境
check_docker() {
    log_info "检查Docker环境..."
    
    if ! command -v docker &> /dev/null; then
        log_error "Docker未安装"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose未安装"
        exit 1
    fi
    
    log_success "Docker环境正常"
}

# 初始化项目
init_project() {
    log_info "初始化项目..."
    
    # 创建项目目录
    if [ ! -d "$PROJECT_DIR" ]; then
        sudo mkdir -p "$PROJECT_DIR"
        sudo chown $USER:$USER "$PROJECT_DIR"
    fi
    
    cd "$PROJECT_DIR"
    
    # 克隆或更新代码
    if [ ! -d ".git" ]; then
        log_info "克隆代码仓库..."
        git clone "$GIT_REPO" .
    else
        log_info "更新代码..."
        git pull origin main
    fi
    
    # 创建SSL证书
    create_ssl_cert
    
    # 创建必要目录
    mkdir -p logs logs/nginx backups ssl
    chmod 755 logs backups
    
    log_success "项目初始化完成"
}

# 更新项目
update_project() {
    log_info "更新项目..."
    
    if [ ! -d "$PROJECT_DIR" ]; then
        log_error "项目目录不存在，请先运行初始化: $0 init"
        exit 1
    fi
    
    cd "$PROJECT_DIR"
    
    # 拉取最新代码
    log_info "拉取最新代码..."
    git pull origin main
    
    log_success "项目更新完成"
}

# 创建SSL证书
create_ssl_cert() {
    log_info "创建SSL证书..."
    
    if [ -f "ssl/cert.pem" ] && [ -f "ssl/key.pem" ]; then
        log_info "SSL证书已存在，跳过创建"
        return
    fi
    
    mkdir -p ssl
    
    # 生成自签名证书
    openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
        -keyout ssl/key.pem \
        -out ssl/cert.pem \
        -subj "/C=CN/ST=Beijing/L=Beijing/O=Himym/CN=$DOMAIN"
    
    log_success "SSL证书创建完成"
}

# 构建和部署
deploy_services() {
    log_info "构建和部署服务..."
    
    cd "$PROJECT_DIR"
    
    # 给脚本执行权限
    chmod +x gradlew
    chmod +x scripts/*.sh
    
    # 构建应用
    log_info "构建Java应用..."
    ./gradlew clean build -x test --no-daemon
    
    # 停止旧服务
    log_info "停止旧服务..."
    if docker-compose -f docker-compose.prod.yml ps | grep -q "Up"; then
        docker-compose -f docker-compose.prod.yml down
    fi
    
    # 启动新服务
    log_info "启动新服务..."
    docker-compose -f docker-compose.prod.yml up -d --build
    
    log_success "服务部署完成"
}

# 等待服务就绪
wait_for_services() {
    log_info "等待服务就绪..."
    
    local max_attempts=30
    local attempt=1
    
    while [[ $attempt -le $max_attempts ]]; do
        if curl -f http://localhost:8080/api/v1/actuator/health &> /dev/null; then
            log_success "服务已就绪"
            return 0
        fi
        
        log_info "等待服务启动... ($attempt/$max_attempts)"
        sleep 10
        ((attempt++))
    done
    
    log_error "服务启动超时"
    return 1
}

# 显示服务状态
show_status() {
    log_info "服务状态:"
    docker-compose -f docker-compose.prod.yml ps
    
    echo ""
    log_info "服务访问地址:"
    echo "🌐 HTTP:  http://$DOMAIN/api/v1/"
    echo "🔒 HTTPS: https://$DOMAIN/api/v1/"
    echo "💓 健康检查: http://$DOMAIN/health"
    echo "📚 API文档: http://$DOMAIN/api/v1/swagger-ui.html"
    echo "🔗 直接访问: http://$SERVER_IP:8080/api/v1/"
}

# 配置防火墙
setup_firewall() {
    log_info "配置防火墙..."
    
    if command -v ufw &> /dev/null; then
        sudo ufw allow 80/tcp
        sudo ufw allow 443/tcp
        sudo ufw allow 8080/tcp
    elif command -v firewall-cmd &> /dev/null; then
        sudo firewall-cmd --permanent --add-port=80/tcp
        sudo firewall-cmd --permanent --add-port=443/tcp
        sudo firewall-cmd --permanent --add-port=8080/tcp
        sudo firewall-cmd --reload
    fi
    
    log_success "防火墙配置完成"
}

# 主函数
main() {
    log_info "=========================================="
    log_info "焕新之旅后端服务 - 服务器自动化部署"
    log_info "时间: $(date)"
    log_info "=========================================="
    
    check_docker
    
    case $MODE in
        "init")
            log_info "🔧 初始化模式"
            init_project
            deploy_services
            setup_firewall
            ;;
            
        "update")
            log_info "🔄 更新模式"
            update_project
            deploy_services
            ;;
            
        "restart")
            log_info "🔄 重启模式"
            cd "$PROJECT_DIR"
            docker-compose -f docker-compose.prod.yml restart
            ;;
            
        *)
            log_error "未知模式: $MODE"
            log_info "可用模式: init, update, restart"
            exit 1
            ;;
    esac
    
    if wait_for_services; then
        show_status
        log_success "=========================================="
        log_success "部署完成！"
        log_success "=========================================="
    else
        log_error "部署失败，请检查日志"
        docker-compose -f docker-compose.prod.yml logs
        exit 1
    fi
}

# 执行主函数
main
