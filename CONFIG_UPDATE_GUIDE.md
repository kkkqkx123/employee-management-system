# Employee Management System - 配置更新指南

## 概述
根据 `advice.md` 的建议，已对配置文件进行了全面优化，主要改进包括：

## 主要改进点

### 1. 环境变量支持 ✅
- **数据库配置**：所有数据库连接参数都支持环境变量注入
- **Redis配置**：主机、端口、密码等参数可动态配置
- **安全配置**：JWT密钥和加密密钥通过环境变量设置
- **邮件配置**：SMTP服务器配置支持环境变量
- **文件存储**：上传目录和临时目录可配置

### 2. 生产环境安全性 ✅
- 移除硬编码的敏感信息
- 提供环境变量默认值，便于开发环境使用
- 创建 `.env.hybrid.example` 模板文件，方便生产环境配置

### 3. 健康检查扩展 ✅
- 添加 Redis 健康检查指示器：`management.health.redis.enabled=true`
- 添加数据库健康检查指示器：`management.health.db.enabled=true`

### 4. 缓存配置优化 ✅
- 添加 Redis 缓存过期时间：`spring.cache.redis.time-to-live=3600s`
- 优化 JPA 批量操作：
  - 批处理大小增加到 50
  - 启用批量插入排序
  - 启用批量更新排序

### 5. 环境隔离 ✅
- 在 `application.properties` 中添加 hybrid 环境激活注释
- 提供 Docker Compose 开发环境配置

## 文件变更说明

### 修改的文件
1. `application-hybrid.properties` - 全面优化配置
2. `application.properties` - 添加环境变量支持
3. `DemoApplication.java` - 无需修改（已符合要求）
4. `ServletInitializer.java` - 无需修改（已符合要求）

### 新增的文件
1. `.env.hybrid.example` - 环境变量模板
2. `docker-compose.dev.yml` - 开发环境服务配置
3. `CONFIG_UPDATE_GUIDE.md` - 本指南文档

## 使用指南

### 开发环境启动

#### 方式1：使用 Docker Compose（推荐）
```bash
# 启动 Redis 和 MailHog
docker-compose -f docker-compose.dev.yml up -d

# 启动应用（使用 hybrid 配置）
./mvnw spring-boot:run -Dspring.profiles.active=hybrid
```

#### 方式2：使用环境变量文件
```bash
# 复制环境变量模板
cp .env.hybrid.example .env.hybrid

# 编辑环境变量（根据需要修改）
nano .env.hybrid

# 启动应用时加载环境变量
source .env.hybrid && ./mvnw spring-boot:run -Dspring.profiles.active=hybrid
```

### 生产环境配置

1. **创建生产环境变量文件**
   ```bash
   cp .env.hybrid.example .env.production
   ```

2. **修改关键配置**
   - 设置强密码：`DB_PASSWORD`, `JWT_SECRET`, `ENCRYPTION_KEY`
   - 配置真实SMTP：修改 `MAIL_*` 相关配置
   - 设置正确的文件存储路径：使用绝对路径或云存储

3. **启动应用**
   ```bash
   source .env.production && java -jar target/employee-management-*.jar --spring.profiles.active=hybrid
   ```

## 环境变量参考

### 数据库配置
| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| DB_HOST | 172.30.204.139 | PostgreSQL 主机地址 |
| DB_PORT | 5432 | PostgreSQL 端口 |
| DB_NAME | employee_management | 数据库名 |
| DB_USERNAME | employee_admin | 数据库用户名 |
| DB_PASSWORD | dev_password | 数据库密码 |

### Redis 配置
| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| REDIS_HOST | 172.17.0.3 | Redis 主机地址 |
| REDIS_PORT | 6379 | Redis 端口 |
| REDIS_DB | 1 | Redis 数据库索引 |
| REDIS_PASSWORD | - | Redis 密码（可选） |

### 安全配置
| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| JWT_SECRET | developmentSecretKeyNotForProduction | JWT 签名密钥 |
| ENCRYPTION_KEY | developmentEncryptionKeyNotForProduction | 数据加密密钥 |

### 邮件配置
| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| MAIL_HOST | localhost | SMTP 服务器地址 |
| MAIL_PORT | 1025 | SMTP 端口 |
| MAIL_USERNAME | - | SMTP 用户名 |
| MAIL_PASSWORD | - | SMTP 密码 |
| MAIL_SMTP_AUTH | false | 是否启用SMTP认证 |
| MAIL_SMTP_STARTTLS | false | 是否启用STARTTLS |

### 文件存储配置
| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| FILE_UPLOAD_DIR | /app/uploads/dev | 文件上传目录 |
| FILE_TEMP_DIR | /app/temp/dev | 临时文件目录 |

## 验证配置

启动应用后，可以通过以下方式验证配置是否正确：

1. **健康检查端点**
   ```
   GET http://localhost:8080/api/actuator/health
   ```

2. **环境信息端点**
   ```
   GET http://localhost:8080/api/actuator/env
   ```

3. **数据库连接测试**
   ```
   GET http://localhost:8080/api/actuator/health/db
   ```

4. **Redis连接测试**
   ```
   GET http://localhost:8080/api/actuator/health/redis
   ```

## 注意事项

1. **生产环境安全**
   - 务必更改所有默认密码和密钥
   - 使用 HTTPS 协议
   - 配置防火墙规则

2. **WSL IP 变化**
   - 如果 WSL IP 经常变化，建议使用 `host.docker.internal` 或配置固定 IP

3. **文件权限**
   - 确保文件存储目录有正确的读写权限
   - 生产环境建议使用云存储服务

4. **日志级别**
   - 生产环境建议降低日志级别为 INFO 或 WARN
   - 避免记录敏感信息