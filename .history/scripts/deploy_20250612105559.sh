#!/bin/bash

# 焕新之旅后端服务部署脚本
# 使用方法: ./scripts/deploy.sh [环境]
# 环境选项: dev, prod (默认: prod)

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查参数
ENVIRONMENT=${1:-prod}
if [[ "$ENVIRONMENT" != "dev" && "$ENVIRONMENT" != "prod" ]]; then
    log_error "无效的环境参数: $ENVIRONMENT"
    log_info "使用方法: $0 [dev|prod]"
    exit 1
fi

log_info "开始部署到 $ENVIRONMENT 环境..."

# 检查必要的文件
check_files() {
    log_info "检查必要文件..."

    local files=(
        "docker-compose.${ENVIRONMENT}.yml"
        "Dockerfile"
        ".env"
    )

    for file in "${files[@]}"; do
        if [[ ! -f "$file" ]]; then
            log_error "缺少必要文件: $file"
            if [[ "$file" == ".env" ]]; then
                log_info "请复制 .env.example 为 .env 并配置相应的值"
                if [[ -f ".env.example" ]]; then
                    log_info "发现 .env.example 文件，是否自动复制？(y/n)"
                    read -r response
                    if [[ "$response" == "y" || "$response" == "Y" ]]; then
                        cp .env.example .env
                        log_warning "已复制 .env.example 为 .env，请编辑 .env 文件并填入正确的配置值"
                        log_info "编辑完成后请重新运行部署脚本"
                        exit 0
                    fi
                fi
            fi
            exit 1
        fi
    done

    # 检查环境变量文件的关键配置
    if [[ -f ".env" ]]; then
        log_info "检查环境变量配置..."
        local required_vars=("DB_PASSWORD" "REDIS_PASSWORD" "JWT_SECRET")
        for var in "${required_vars[@]}"; do
            if ! grep -q "^${var}=" .env || grep -q "^${var}=.*your.*" .env; then
                log_error "请在 .env 文件中正确配置 $var"
                exit 1
            fi
        done
    fi

    log_success "文件检查完成"
}

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
    
    if ! docker info &> /dev/null; then
        log_error "Docker服务未运行"
        exit 1
    fi
    
    log_success "Docker环境检查完成"
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
    
    if docker-compose -f "docker-compose.${ENVIRONMENT}.yml" ps | grep -q "Up"; then
        docker-compose -f "docker-compose.${ENVIRONMENT}.yml" down
        log_success "旧服务已停止"
    else
        log_info "没有运行中的服务"
    fi
}

# 启动服务
start_services() {
    log_info "启动服务..."
    
    # 拉取最新镜像
    docker-compose -f "docker-compose.${ENVIRONMENT}.yml" pull
    
    # 构建并启动服务
    docker-compose -f "docker-compose.${ENVIRONMENT}.yml" up -d --build
    
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
    docker-compose -f "docker-compose.${ENVIRONMENT}.yml" ps
    
    echo ""
    log_info "服务日志 (最近10行):"
    docker-compose -f "docker-compose.${ENVIRONMENT}.yml" logs --tail=10 api
}

# 清理函数
cleanup() {
    if [[ $? -ne 0 ]]; then
        log_error "部署失败，正在清理..."
        docker-compose -f "docker-compose.${ENVIRONMENT}.yml" down
    fi
}

# 设置清理陷阱
trap cleanup EXIT

# 主部署流程
main() {
    log_info "=========================================="
    log_info "焕新之旅后端服务部署脚本"
    log_info "环境: $ENVIRONMENT"
    log_info "时间: $(date)"
    log_info "=========================================="
    
    check_files
    check_docker
    build_app
    stop_services
    start_services
    
    if wait_for_services; then
        show_status
        log_success "=========================================="
        log_success "部署完成！"
        log_success "API地址: http://localhost:8080/api/v1"
        log_success "健康检查: http://localhost:8080/api/v1/actuator/health"
        if [[ "$ENVIRONMENT" == "dev" ]]; then
            log_success "API文档: http://localhost:8080/api/v1/swagger-ui.html"
        fi
        log_success "=========================================="
    else
        log_error "部署失败，请检查日志"
        exit 1
    fi
}

# 执行主函数
main
