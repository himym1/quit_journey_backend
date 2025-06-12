#!/bin/bash

# 焕新之旅后端服务 - 快速部署脚本
# 适用于首次部署到新服务器

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

# 检查是否为root用户
check_root() {
    if [[ $EUID -eq 0 ]]; then
        log_error "请不要使用root用户运行此脚本"
        log_info "建议创建普通用户并添加到docker组"
        exit 1
    fi
}

# 检查系统要求
check_system() {
    log_info "检查系统要求..."
    
    # 检查内存
    local mem_gb=$(free -g | awk '/^Mem:/{print $2}')
    if [[ $mem_gb -lt 2 ]]; then
        log_warning "系统内存少于2GB，可能影响性能"
    fi
    
    # 检查磁盘空间
    local disk_gb=$(df -BG . | awk 'NR==2{print $4}' | sed 's/G//')
    if [[ $disk_gb -lt 20 ]]; then
        log_error "可用磁盘空间少于20GB"
        exit 1
    fi
    
    log_success "系统要求检查完成"
}

# 安装Docker
install_docker() {
    if command -v docker &> /dev/null; then
        log_info "Docker已安装，跳过安装步骤"
        return
    fi
    
    log_info "安装Docker..."
    
    # 检测操作系统
    if [[ -f /etc/debian_version ]]; then
        # Debian/Ubuntu
        sudo apt update
        sudo apt install -y docker.io docker-compose
    elif [[ -f /etc/redhat-release ]]; then
        # CentOS/RHEL
        sudo yum install -y docker docker-compose
    else
        log_error "不支持的操作系统"
        exit 1
    fi
    
    # 启动Docker服务
    sudo systemctl start docker
    sudo systemctl enable docker
    
    # 添加用户到docker组
    sudo usermod -aG docker $USER
    
    log_success "Docker安装完成"
    log_warning "请重新登录以使docker组权限生效，然后重新运行此脚本"
    exit 0
}

# 配置防火墙
setup_firewall() {
    log_info "配置防火墙..."
    
    if command -v ufw &> /dev/null; then
        # Ubuntu/Debian
        sudo ufw allow 80/tcp
        sudo ufw allow 443/tcp
        sudo ufw --force enable
    elif command -v firewall-cmd &> /dev/null; then
        # CentOS/RHEL
        sudo firewall-cmd --permanent --add-port=80/tcp
        sudo firewall-cmd --permanent --add-port=443/tcp
        sudo firewall-cmd --reload
    fi
    
    log_success "防火墙配置完成"
}

# 生成SSL证书
generate_ssl() {
    log_info "生成SSL证书..."
    
    if [[ ! -d "ssl" ]]; then
        mkdir -p ssl
    fi
    
    if [[ -f "ssl/cert.pem" && -f "ssl/key.pem" ]]; then
        log_info "SSL证书已存在，跳过生成"
        return
    fi
    
    # 获取域名
    read -p "请输入您的域名（如：api.example.com，留空使用自签名证书）: " domain
    
    if [[ -z "$domain" ]]; then
        # 生成自签名证书
        log_info "生成自签名证书..."
        openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
            -keyout ssl/key.pem \
            -out ssl/cert.pem \
            -subj "/C=CN/ST=State/L=City/O=Organization/CN=localhost"
    else
        # 尝试使用Let's Encrypt
        if command -v certbot &> /dev/null; then
            log_info "使用Let's Encrypt获取证书..."
            sudo certbot certonly --standalone -d "$domain"
            sudo cp "/etc/letsencrypt/live/$domain/fullchain.pem" ssl/cert.pem
            sudo cp "/etc/letsencrypt/live/$domain/privkey.pem" ssl/key.pem
            sudo chown $USER:$USER ssl/*.pem
        else
            log_warning "certbot未安装，生成自签名证书..."
            openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
                -keyout ssl/key.pem \
                -out ssl/cert.pem \
                -subj "/C=CN/ST=State/L=City/O=Organization/CN=$domain"
        fi
    fi
    
    log_success "SSL证书生成完成"
}

# 配置环境变量
setup_env() {
    log_info "配置环境变量..."
    
    if [[ -f ".env" ]]; then
        log_info ".env文件已存在，跳过配置"
        return
    fi
    
    if [[ ! -f ".env.example" ]]; then
        log_error "缺少.env.example文件"
        exit 1
    fi
    
    cp .env.example .env
    
    # 生成随机密码
    local db_password=$(openssl rand -base64 32 | tr -d "=+/" | cut -c1-25)
    local redis_password=$(openssl rand -base64 32 | tr -d "=+/" | cut -c1-25)
    local jwt_secret=$(openssl rand -base64 64 | tr -d "=+/" | cut -c1-50)
    
    # 替换配置
    sed -i "s/your_secure_database_password_here/$db_password/" .env
    sed -i "s/your_secure_redis_password_here/$redis_password/" .env
    sed -i "s/your_very_secure_jwt_secret_key_at_least_32_characters_long/$jwt_secret/" .env
    
    log_success "环境变量配置完成"
    log_info "已生成随机密码，请查看.env文件"
}

# 创建必要目录
create_directories() {
    log_info "创建必要目录..."
    
    mkdir -p logs logs/nginx backups
    chmod 755 logs backups
    
    log_success "目录创建完成"
}

# 主函数
main() {
    log_info "=========================================="
    log_info "焕新之旅后端服务 - 快速部署脚本"
    log_info "=========================================="
    
    check_root
    check_system
    install_docker
    setup_firewall
    create_directories
    generate_ssl
    setup_env
    
    log_success "=========================================="
    log_success "环境准备完成！"
    log_info "接下来请运行: ./scripts/deploy.sh prod"
    log_success "=========================================="
}

main "$@"
