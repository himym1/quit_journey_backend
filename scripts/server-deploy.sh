#!/bin/bash

# 服务器部署脚本 - 适用于 chat.himym.lat
# 使用方法: ./scripts/server-deploy.sh

set -e

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

# 检查Docker环境
check_docker() {
    log_info "检查Docker环境..."
    
    if ! command -v docker &> /dev/null; then
        log_error "Docker未安装，请先安装Docker"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose未安装，请先安装Docker Compose"
        exit 1
    fi
    
    if ! docker info &> /dev/null; then
        log_error "Docker服务未运行，请启动Docker服务"
        exit 1
    fi
    
    log_success "Docker环境检查完成"
}

# 创建SSL证书（自签名）
create_ssl() {
    log_info "创建SSL证书..."
    
    if [[ ! -d "ssl" ]]; then
        mkdir -p ssl
    fi
    
    if [[ -f "ssl/cert.pem" && -f "ssl/key.pem" ]]; then
        log_info "SSL证书已存在，跳过创建"
        return
    fi
    
    # 生成自签名证书
    openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
        -keyout ssl/key.pem \
        -out ssl/cert.pem \
        -subj "/C=CN/ST=Beijing/L=Beijing/O=Himym/CN=chat.himym.lat"
    
    log_success "SSL证书创建完成"
}

# 创建必要目录
create_directories() {
    log_info "创建必要目录..."
    
    mkdir -p logs logs/nginx backups
    chmod 755 logs backups
    
    log_success "目录创建完成"
}

# 构建应用
build_app() {
    log_info "构建应用..."
    
    # 清理旧的构建
    ./gradlew clean
    
    # 构建应用
    ./gradlew build -x test
    
    log_success "应用构建完成"
}

# 停止旧服务
stop_services() {
    log_info "停止旧服务..."
    
    if docker-compose -f docker-compose.prod.yml ps | grep -q "Up"; then
        docker-compose -f docker-compose.prod.yml down
        log_success "旧服务已停止"
    else
        log_info "没有运行中的服务"
    fi
}

# 启动服务
start_services() {
    log_info "启动服务..."
    
    # 构建并启动服务
    docker-compose -f docker-compose.prod.yml up -d --build
    
    log_success "服务启动完成"
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
    log_info "服务日志 (最近10行):"
    docker-compose -f docker-compose.prod.yml logs --tail=10 api
}

# 配置防火墙
setup_firewall() {
    log_info "配置防火墙..."
    
    # 检查并开放端口
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

# 主部署流程
main() {
    log_info "=========================================="
    log_info "焕新之旅后端服务 - 服务器部署"
    log_info "域名: chat.himym.lat"
    log_info "时间: $(date)"
    log_info "=========================================="
    
    check_docker
    create_directories
    create_ssl
    build_app
    stop_services
    start_services
    
    if wait_for_services; then
        setup_firewall
        show_status
        log_success "=========================================="
        log_success "部署完成！"
        log_success "HTTP访问: http://chat.himym.lat/api/v1"
        log_success "HTTPS访问: https://chat.himym.lat/api/v1"
        log_success "健康检查: http://chat.himym.lat/health"
        log_success "API文档: http://chat.himym.lat/api/v1/swagger-ui.html"
        log_success "直接访问: http://199.68.217.152:8080/api/v1"
        log_success "=========================================="
    else
        log_error "部署失败，请检查日志"
        docker-compose -f docker-compose.prod.yml logs
        exit 1
    fi
}

# 执行主函数
main
