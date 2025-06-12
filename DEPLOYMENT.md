# 焕新之旅后端服务部署指南

## 📋 部署前准备

### 1. 服务器要求

- **操作系统**: Ubuntu 20.04+ / CentOS 8+ / Debian 11+
- **内存**: 最少 2GB，推荐 4GB+
- **存储**: 最少 20GB 可用空间
- **网络**: 公网IP，开放 80、443 端口

### 2. 安装依赖

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install -y docker.io docker-compose git curl

# CentOS/RHEL
sudo yum install -y docker docker-compose git curl

# 启动Docker服务
sudo systemctl start docker
sudo systemctl enable docker

# 将当前用户添加到docker组（可选）
sudo usermod -aG docker $USER
```

## 🚀 部署步骤

### 1. 克隆项目

```bash
git clone <your-repository-url>
cd quit-journey-backend
```

### 2. 配置环境变量

```bash
# 复制环境变量模板
cp .env.example .env

# 编辑环境变量文件
nano .env
```

**重要配置项说明：**

- `DB_PASSWORD`: 数据库密码（请使用强密码）
- `REDIS_PASSWORD`: Redis密码（请使用强密码）
- `JWT_SECRET`: JWT密钥（至少32位字符的强密码）
- `DOMAIN_NAME`: 您的域名（如：api.yoursite.com）

### 3. SSL证书配置（推荐）

#### 方式一：使用Let's Encrypt（免费）

```bash
# 安装certbot
sudo apt install certbot

# 创建SSL目录
mkdir -p ssl

# 获取证书（替换your-domain.com为您的域名）
sudo certbot certonly --standalone -d your-domain.com

# 复制证书到项目目录
sudo cp /etc/letsencrypt/live/your-domain.com/fullchain.pem ssl/cert.pem
sudo cp /etc/letsencrypt/live/your-domain.com/privkey.pem ssl/key.pem
sudo chown $USER:$USER ssl/*.pem
```

#### 方式二：使用自签名证书（测试用）

```bash
mkdir -p ssl
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
    -keyout ssl/key.pem \
    -out ssl/cert.pem \
    -subj "/C=CN/ST=State/L=City/O=Organization/CN=your-domain.com"
```

### 4. 创建必要目录

```bash
# 创建日志目录
mkdir -p logs logs/nginx

# 创建备份目录
mkdir -p backups

# 设置权限
chmod 755 logs backups
```

### 5. 执行部署

```bash
# 给部署脚本执行权限
chmod +x scripts/deploy.sh

# 部署到生产环境
./scripts/deploy.sh prod
```

## 🔧 部署后配置

### 1. 验证部署

```bash
# 检查服务状态
docker-compose -f docker-compose.prod.yml ps

# 检查健康状态
curl -k https://your-domain.com/health

# 查看日志
docker-compose -f docker-compose.prod.yml logs -f api
```

### 2. 防火墙配置

```bash
# Ubuntu/Debian (ufw)
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable

# CentOS/RHEL (firewalld)
sudo firewall-cmd --permanent --add-port=80/tcp
sudo firewall-cmd --permanent --add-port=443/tcp
sudo firewall-cmd --reload
```

### 3. 设置自动备份

```bash
# 创建备份脚本
cat > scripts/backup.sh << 'EOF'
#!/bin/bash
BACKUP_DIR="/path/to/your/project/backups"
DATE=$(date +%Y%m%d_%H%M%S)

# 备份数据库
docker exec quit-journey-db-prod pg_dump -U quitjourney_user quitjourney > "$BACKUP_DIR/db_backup_$DATE.sql"

# 删除30天前的备份
find "$BACKUP_DIR" -name "db_backup_*.sql" -mtime +30 -delete

echo "备份完成: db_backup_$DATE.sql"
EOF

chmod +x scripts/backup.sh

# 添加到crontab（每天凌晨2点备份）
(crontab -l 2>/dev/null; echo "0 2 * * * /path/to/your/project/scripts/backup.sh") | crontab -
```

## 📊 监控和维护

### 1. 查看服务状态

```bash
# 查看所有容器状态
docker-compose -f docker-compose.prod.yml ps

# 查看资源使用情况
docker stats

# 查看日志
docker-compose -f docker-compose.prod.yml logs -f
```

### 2. 更新应用

```bash
# 拉取最新代码
git pull

# 重新部署
./scripts/deploy.sh prod
```

### 3. 常用维护命令

```bash
# 重启服务
docker-compose -f docker-compose.prod.yml restart

# 停止服务
docker-compose -f docker-compose.prod.yml down

# 清理未使用的镜像
docker system prune -f

# 查看数据库
docker exec -it quit-journey-db-prod psql -U quitjourney_user -d quitjourney
```

## 🔒 安全建议

1. **定期更新系统和Docker**
2. **使用强密码**
3. **定期备份数据**
4. **监控日志异常**
5. **限制SSH访问**
6. **使用HTTPS**
7. **定期更新SSL证书**

## 🆘 故障排除

### 常见问题

1. **容器启动失败**
   ```bash
   # 查看详细日志
   docker-compose -f docker-compose.prod.yml logs api
   ```

2. **数据库连接失败**
   ```bash
   # 检查数据库容器状态
   docker-compose -f docker-compose.prod.yml ps db
   ```

3. **SSL证书问题**
   ```bash
   # 检查证书文件
   ls -la ssl/
   openssl x509 -in ssl/cert.pem -text -noout
   ```

4. **端口被占用**
   ```bash
   # 查看端口占用
   sudo netstat -tlnp | grep :80
   sudo netstat -tlnp | grep :443
   ```

## 📞 技术支持

如遇到部署问题，请检查：
1. 日志文件：`logs/` 目录
2. 容器状态：`docker-compose ps`
3. 系统资源：`docker stats`

---

**注意**: 首次部署建议在测试环境验证后再部署到生产环境。
