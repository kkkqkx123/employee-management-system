# 开发环境启动指南

## 重要提示

**对于混合开发环境（Hybrid），请使用专门的启动指南：**
👉 **[HYBRID_STARTUP_GUIDE.md](./HYBRID_STARTUP_GUIDE.md)**

该指南包含了完整的环境变量设置和启动步骤。

## 快速启动（推荐方法）

### 混合环境启动（PostgreSQL + Redis in Docker）

```powershell
# 1. 启动Docker服务
docker-compose up -d postgres redis

# 2. 设置环境变量
.\setup-dev-db.ps1

# 3. 启动应用
mvn spring-boot:run -Dspring-boot.run.profiles=hybrid
```

### 纯开发环境启动

```bash
# 启动所有服务
docker-compose -f docker-compose.dev.yml up -d

# 启动应用
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## 问题解决

### 常见启动错误

#### 1. 密码认证失败
**错误信息：** `FATAL: password authentication failed for user "employee_admin"`

**解决方案：**
```powershell
# 确保运行了环境变量设置脚本
.\setup-dev-db.ps1

# 验证环境变量
echo $env:DB_PASSWORD
```

#### 2. 连接被拒绝
**错误信息：** `Connection refused`

**解决方案：**
```bash
# 检查Docker容器状态
docker ps

# 启动数据库服务
docker-compose up -d postgres redis
```

### 环境变量设置（混合模式）

如果使用混合环境(hybrid)，必须设置以下环境变量：

#### Windows (PowerShell) - 使用脚本
```powershell
# 推荐：使用提供的脚本
.\setup-dev-db.ps1

# 手动设置（不推荐）
$env:DB_HOST="localhost"
$env:DB_PORT="5432"
$env:DB_NAME="employee_management"
$env:DB_USERNAME="employee_admin"
$env:DB_PASSWORD="admin123"
$env:REDIS_HOST="localhost"
$env:REDIS_PORT="6379"
```

#### Linux/Mac
```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=employee_management
export DB_USERNAME=employee_admin
export DB_PASSWORD=admin123
export REDIS_HOST=localhost
export REDIS_PORT=6379
```

### 5. 验证启动

启动成功后，可以访问以下地址：

- **应用主页**: http://localhost:8080
- **Swagger文档**: http://localhost:8080/swagger-ui.html
- **Actuator健康检查**: http://localhost:8080/actuator/health

### 6. 常见问题

#### Q: 端口冲突
如果8080端口被占用，可以在application.properties中修改：
```properties
server.port=8081
```

#### Q: 数据库连接失败
检查PostgreSQL是否运行：
```bash
# Windows
netstat -an | findstr :5432

# Linux/Mac
netstat -an | grep :5432
```

#### Q: Redis连接失败
检查Redis是否运行：
```bash
# 使用docker-compose启动的Redis
docker-compose -f docker-compose.dev.yml logs redis
```

## 快速启动检查清单

- [ ] PostgreSQL已安装并运行
- [ ] Redis已安装并运行（或使用docker-compose）
- [ ] 数据库和用户已创建（运行init-db.sql）
- [ ] 应用配置文件已检查
- [ ] 端口未被占用

## 一键启动命令

```bash
# 完整的启动流程
cd d:\项目\Spring\employee-management-system

# 1. 启动依赖服务
docker-compose -f docker-compose.dev.yml up -d

# 2. 等待服务启动完成
sleep 10

# 3. 启动应用
mvn spring-boot:run -DskipTests
```