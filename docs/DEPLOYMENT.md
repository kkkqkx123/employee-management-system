# Employee Management System - Production Deployment Guide

## Overview

This guide provides comprehensive instructions for deploying the Employee Management System to production environments. The system supports multiple deployment strategies including traditional JAR deployment, Docker containerization, and cloud-native deployments.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Environment Setup](#environment-setup)
3. [Database Setup](#database-setup)
4. [Application Configuration](#application-configuration)
5. [Deployment Methods](#deployment-methods)
6. [Security Hardening](#security-hardening)
7. [Monitoring and Logging](#monitoring-and-logging)
8. [Backup and Recovery](#backup-and-recovery)
9. [Troubleshooting](#troubleshooting)

## Prerequisites

### System Requirements

**Minimum Hardware Requirements:**
- CPU: 4 cores (8 cores recommended for production)
- RAM: 8GB (16GB recommended for production)
- Storage: 100GB SSD (500GB recommended for production)
- Network: 1Gbps connection

**Software Requirements:**
- Java 21 or higher
- PostgreSQL 15.x
- Redis 7.x
- Docker and Docker Compose (for containerized deployment)
- Nginx (for reverse proxy setup)

### Network Requirements

**Ports:**
- 8080: Application server (internal)
- 80/443: HTTP/HTTPS (external)
- 5432: PostgreSQL (internal)
- 6379: Redis (internal)
- 9090: Prometheus (monitoring)
- 3001: Grafana (monitoring)

**Firewall Configuration:**
```bash
# Allow HTTP and HTTPS
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp

# Allow SSH (adjust port as needed)
sudo ufw allow 22/tcp

# Internal services (restrict to internal network)
sudo ufw allow from 10.0.0.0/8 to any port 5432
sudo ufw allow from 10.0.0.0/8 to any port 6379
sudo ufw allow from 10.0.0.0/8 to any port 8080
```

## Environment Setup

### 1. Create Application User

```bash
# Create dedicated user for the application
sudo useradd -r -m -s /bin/bash employee-mgmt
sudo usermod -aG docker employee-mgmt

# Create application directories
sudo mkdir -p /opt/employee-management
sudo mkdir -p /var/log/employee-management
sudo mkdir -p /var/lib/employee-management/{uploads,temp}

# Set ownership
sudo chown -R employee-mgmt:employee-mgmt /opt/employee-management
sudo chown -R employee-mgmt:employee-mgmt /var/log/employee-management
sudo chown -R employee-mgmt:employee-mgmt /var/lib/employee-management
```

### 2. Environment Variables

Copy the production environment template:
```bash
cp .env.example .env.prod
```

Edit `.env.prod` with production values:
```bash
sudo nano .env.prod
```

**Critical Variables to Update:**
- `DATABASE_PASSWORD`: Strong database password
- `JWT_SECRET`: 256-bit secret key
- `ENCRYPTION_KEY`: 256-bit encryption key
- `REDIS_PASSWORD`: Redis authentication password
- `EMAIL_PASSWORD`: SMTP password
- `CORS_ALLOWED_ORIGINS`: Production domain
- `SSL_KEYSTORE_PASSWORD`: SSL certificate password

### 3. SSL Certificate Setup

**Option A: Let's Encrypt (Recommended)**
```bash
# Install Certbot
sudo apt update
sudo apt install certbot python3-certbot-nginx

# Obtain certificate
sudo certbot --nginx -d your-domain.com -d www.your-domain.com

# Auto-renewal
sudo crontab -e
# Add: 0 12 * * * /usr/bin/certbot renew --quiet
```

**Option B: Custom Certificate**
```bash
# Create SSL directory
sudo mkdir -p /etc/ssl/employee-management

# Copy your certificates
sudo cp your-cert.pem /etc/ssl/employee-management/cert.pem
sudo cp your-key.pem /etc/ssl/employee-management/key.pem

# Set permissions
sudo chmod 600 /etc/ssl/employee-management/key.pem
sudo chmod 644 /etc/ssl/employee-management/cert.pem
```##
 Database Setup

### 1. PostgreSQL Installation and Configuration

**Install PostgreSQL:**
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install postgresql postgresql-contrib

# CentOS/RHEL
sudo yum install postgresql-server postgresql-contrib
sudo postgresql-setup initdb
```

**Configure PostgreSQL:**
```bash
# Switch to postgres user
sudo -u postgres psql

-- Create database and user
CREATE DATABASE employee_management;
CREATE USER employee_admin WITH ENCRYPTED PASSWORD 'your_secure_password';
GRANT ALL PRIVILEGES ON DATABASE employee_management TO employee_admin;
ALTER USER employee_admin CREATEDB;

-- Exit psql
\q
```

**Configure PostgreSQL for Production:**
```bash
# Edit postgresql.conf
sudo nano /etc/postgresql/15/main/postgresql.conf

# Key settings for production:
max_connections = 200
shared_buffers = 256MB
effective_cache_size = 1GB
work_mem = 4MB
maintenance_work_mem = 64MB
checkpoint_completion_target = 0.9
wal_buffers = 16MB
default_statistics_target = 100
```

**Configure Authentication:**
```bash
# Edit pg_hba.conf
sudo nano /etc/postgresql/15/main/pg_hba.conf

# Add line for application access:
host    employee_management    employee_admin    127.0.0.1/32    md5
```

**Restart PostgreSQL:**
```bash
sudo systemctl restart postgresql
sudo systemctl enable postgresql
```

### 2. Redis Installation and Configuration

**Install Redis:**
```bash
# Ubuntu/Debian
sudo apt install redis-server

# CentOS/RHEL
sudo yum install redis
```

**Configure Redis for Production:**
```bash
# Edit redis.conf
sudo nano /etc/redis/redis.conf

# Key settings:
bind 127.0.0.1
port 6379
requirepass your_redis_password
maxmemory 512mb
maxmemory-policy allkeys-lru
save 900 1
save 300 10
save 60 10000
```

**Start Redis:**
```bash
sudo systemctl start redis
sudo systemctl enable redis
```

### 3. Database Migration

**Run Initial Migration:**
```bash
# Navigate to application directory
cd /opt/employee-management

# Run database initialization
psql -h localhost -U employee_admin -d employee_management -f scripts/init-db.sql

# Run Flyway migrations
./mvnw flyway:migrate -Pprod \
  -Dflyway.url=jdbc:postgresql://localhost:5432/employee_management \
  -Dflyway.user=employee_admin \
  -Dflyway.password=your_secure_password
```

## Application Configuration

### 1. Build Configuration

**Production Build:**
```bash
# Clean and build for production
./mvnw clean package -Pprod -DskipTests

# Verify build
ls -la target/employee-management-system.jar
```

### 2. JVM Configuration

Create JVM options file:
```bash
# Create JVM options file
sudo nano /opt/employee-management/jvm.options

# Add JVM options:
-Xms1g
-Xmx2g
-XX:+UseG1GC
-XX:+UseContainerSupport
-XX:MaxRAMPercentage=75.0
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/var/log/employee-management/
-XX:+UseStringDeduplication
-XX:+OptimizeStringConcat
-Djava.security.egd=file:/dev/./urandom
-Dspring.profiles.active=prod
```

### 3. Systemd Service Configuration

Create systemd service:
```bash
sudo nano /etc/systemd/system/employee-management.service
```

Service file content:
```ini
[Unit]
Description=Employee Management System
After=network.target postgresql.service redis.service
Requires=postgresql.service redis.service

[Service]
Type=simple
User=employee-mgmt
Group=employee-mgmt
WorkingDirectory=/opt/employee-management
ExecStart=/usr/bin/java @/opt/employee-management/jvm.options -jar /opt/employee-management/employee-management-system.jar
ExecStop=/bin/kill -TERM $MAINPID
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=employee-management

# Environment
Environment=SPRING_PROFILES_ACTIVE=prod
EnvironmentFile=/opt/employee-management/.env.prod

# Security
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=strict
ProtectHome=true
ReadWritePaths=/var/log/employee-management /var/lib/employee-management

# Resource limits
LimitNOFILE=65536
LimitNPROC=4096

[Install]
WantedBy=multi-user.target
```

**Enable and start service:**
```bash
sudo systemctl daemon-reload
sudo systemctl enable employee-management
sudo systemctl start employee-management

# Check status
sudo systemctl status employee-management
```##
 Deployment Methods

### Method 1: Traditional JAR Deployment

**1. Prepare Application:**
```bash
# Copy JAR file
sudo cp target/employee-management-system.jar /opt/employee-management/

# Copy configuration files
sudo cp .env.prod /opt/employee-management/
sudo cp -r scripts/ /opt/employee-management/

# Set permissions
sudo chown employee-mgmt:employee-mgmt /opt/employee-management/*
```

**2. Deploy using script:**
```bash
# Make deployment script executable
chmod +x scripts/deploy.sh

# Deploy to production
./scripts/deploy.sh prod 1.0.0
```

### Method 2: Docker Deployment

**1. Build Docker Image:**
```bash
# Build application image
docker build -t employee-management-system:1.0.0 .
docker tag employee-management-system:1.0.0 employee-management-system:latest
```

**2. Deploy with Docker Compose:**
```bash
# Copy environment file
cp .env.prod .env

# Deploy services
docker-compose up -d

# Check status
docker-compose ps
docker-compose logs -f app
```

**3. Docker Swarm Deployment (Multi-node):**
```bash
# Initialize swarm
docker swarm init

# Deploy stack
docker stack deploy -c docker-compose.yml employee-management

# Check services
docker service ls
docker service logs employee-management_app
```

### Method 3: Kubernetes Deployment

**1. Create Kubernetes Manifests:**

Create `k8s/namespace.yaml`:
```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: employee-management
```

Create `k8s/configmap.yaml`:
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
  namespace: employee-management
data:
  SPRING_PROFILES_ACTIVE: "prod"
  DATABASE_URL: "jdbc:postgresql://postgres-service:5432/employee_management"
  REDIS_HOST: "redis-service"
```

Create `k8s/secret.yaml`:
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: app-secrets
  namespace: employee-management
type: Opaque
data:
  DATABASE_PASSWORD: <base64-encoded-password>
  JWT_SECRET: <base64-encoded-jwt-secret>
  ENCRYPTION_KEY: <base64-encoded-encryption-key>
```

**2. Deploy to Kubernetes:**
```bash
# Apply manifests
kubectl apply -f k8s/

# Check deployment
kubectl get pods -n employee-management
kubectl logs -f deployment/employee-management-app -n employee-management
```

## Security Hardening

### 1. Application Security

**Enable Security Headers:**
```properties
# In application-prod.properties
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.same-site=strict
```

**Configure Rate Limiting:**
```bash
# Nginx rate limiting (already configured in nginx.conf)
# Application-level rate limiting is configured in properties
```

### 2. System Security

**Firewall Configuration:**
```bash
# Configure UFW
sudo ufw --force reset
sudo ufw default deny incoming
sudo ufw default allow outgoing

# Allow necessary ports
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 80/tcp    # HTTP
sudo ufw allow 443/tcp   # HTTPS

# Enable firewall
sudo ufw --force enable
```

**Fail2Ban Configuration:**
```bash
# Install Fail2Ban
sudo apt install fail2ban

# Configure for SSH and Nginx
sudo nano /etc/fail2ban/jail.local

[DEFAULT]
bantime = 3600
findtime = 600
maxretry = 3

[sshd]
enabled = true

[nginx-http-auth]
enabled = true

[nginx-limit-req]
enabled = true
filter = nginx-limit-req
logpath = /var/log/nginx/error.log
```

### 3. Database Security

**PostgreSQL Security:**
```bash
# Restrict connections
sudo nano /etc/postgresql/15/main/pg_hba.conf

# Use SSL connections only
hostssl employee_management employee_admin 127.0.0.1/32 md5

# Configure SSL
ssl = on
ssl_cert_file = '/etc/ssl/certs/server.crt'
ssl_key_file = '/etc/ssl/private/server.key'
```

**Redis Security:**
```bash
# Configure Redis security
sudo nano /etc/redis/redis.conf

# Disable dangerous commands
rename-command FLUSHDB ""
rename-command FLUSHALL ""
rename-command DEBUG ""
rename-command CONFIG "CONFIG_b835729b"
```

## Monitoring and Logging

### 1. Application Monitoring

**Prometheus Configuration:**
```bash
# Copy Prometheus config
sudo cp config/prometheus.yml /etc/prometheus/

# Start Prometheus
docker run -d \
  --name prometheus \
  -p 9090:9090 \
  -v /etc/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml \
  prom/prometheus
```

**Grafana Setup:**
```bash
# Start Grafana
docker run -d \
  --name grafana \
  -p 3001:3000 \
  -e "GF_SECURITY_ADMIN_PASSWORD=secure_password" \
  grafana/grafana

# Import dashboards
# Access http://your-server:3001
# Import dashboard ID: 4701 (JVM dashboard)
```

### 2. Log Management

**Centralized Logging with ELK Stack:**
```bash
# Start Elasticsearch
docker run -d \
  --name elasticsearch \
  -p 9200:9200 \
  -e "discovery.type=single-node" \
  elasticsearch:7.17.0

# Start Logstash
docker run -d \
  --name logstash \
  -p 5044:5044 \
  -v /path/to/logstash.conf:/usr/share/logstash/pipeline/logstash.conf \
  logstash:7.17.0

# Start Kibana
docker run -d \
  --name kibana \
  -p 5601:5601 \
  -e "ELASTICSEARCH_HOSTS=http://elasticsearch:9200" \
  kibana:7.17.0
```

### 3. Health Checks

**Application Health Check:**
```bash
# Create health check script
sudo nano /opt/employee-management/health-check.sh

#!/bin/bash
response=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health)
if [ $response -eq 200 ]; then
    echo "Application is healthy"
    exit 0
else
    echo "Application is unhealthy (HTTP $response)"
    exit 1
fi

# Make executable
sudo chmod +x /opt/employee-management/health-check.sh

# Add to cron for monitoring
echo "*/5 * * * * /opt/employee-management/health-check.sh" | sudo crontab -u employee-mgmt -
```##
 Backup and Recovery

### 1. Database Backup

**Automated PostgreSQL Backup:**
```bash
# Create backup script
sudo nano /opt/employee-management/backup-db.sh

#!/bin/bash
BACKUP_DIR="/var/backups/employee-management"
DATE=$(date +%Y%m%d_%H%M%S)
DB_NAME="employee_management"
DB_USER="employee_admin"

# Create backup directory
mkdir -p $BACKUP_DIR

# Create backup
pg_dump -h localhost -U $DB_USER -d $DB_NAME | gzip > $BACKUP_DIR/db_backup_$DATE.sql.gz

# Keep only last 30 days of backups
find $BACKUP_DIR -name "db_backup_*.sql.gz" -mtime +30 -delete

echo "Database backup completed: db_backup_$DATE.sql.gz"

# Make executable
sudo chmod +x /opt/employee-management/backup-db.sh

# Schedule daily backups
echo "0 2 * * * /opt/employee-management/backup-db.sh" | sudo crontab -u employee-mgmt -
```

**Database Restore:**
```bash
# Restore from backup
gunzip -c /var/backups/employee-management/db_backup_YYYYMMDD_HHMMSS.sql.gz | \
psql -h localhost -U employee_admin -d employee_management
```

### 2. Application Backup

**File System Backup:**
```bash
# Create application backup script
sudo nano /opt/employee-management/backup-app.sh

#!/bin/bash
BACKUP_DIR="/var/backups/employee-management"
DATE=$(date +%Y%m%d_%H%M%S)
APP_DIR="/opt/employee-management"
DATA_DIR="/var/lib/employee-management"

# Create backup
tar -czf $BACKUP_DIR/app_backup_$DATE.tar.gz \
  $APP_DIR \
  $DATA_DIR \
  /etc/systemd/system/employee-management.service

# Keep only last 7 days of app backups
find $BACKUP_DIR -name "app_backup_*.tar.gz" -mtime +7 -delete

echo "Application backup completed: app_backup_$DATE.tar.gz"
```

### 3. Disaster Recovery Plan

**Recovery Procedures:**

1. **Database Recovery:**
```bash
# Stop application
sudo systemctl stop employee-management

# Restore database
gunzip -c backup_file.sql.gz | psql -h localhost -U employee_admin -d employee_management

# Start application
sudo systemctl start employee-management
```

2. **Full System Recovery:**
```bash
# Restore application files
tar -xzf app_backup_YYYYMMDD_HHMMSS.tar.gz -C /

# Restore database
gunzip -c db_backup_YYYYMMDD_HHMMSS.sql.gz | psql -h localhost -U employee_admin -d employee_management

# Restart services
sudo systemctl daemon-reload
sudo systemctl start postgresql redis employee-management
```

## Troubleshooting

### Common Issues

**1. Application Won't Start:**
```bash
# Check logs
sudo journalctl -u employee-management -f

# Check Java process
ps aux | grep java

# Check port availability
sudo netstat -tlnp | grep 8080

# Check database connectivity
psql -h localhost -U employee_admin -d employee_management -c "SELECT 1;"
```

**2. Database Connection Issues:**
```bash
# Check PostgreSQL status
sudo systemctl status postgresql

# Check connections
sudo -u postgres psql -c "SELECT * FROM pg_stat_activity;"

# Check configuration
sudo nano /etc/postgresql/15/main/postgresql.conf
sudo nano /etc/postgresql/15/main/pg_hba.conf
```

**3. Redis Connection Issues:**
```bash
# Check Redis status
sudo systemctl status redis

# Test Redis connection
redis-cli ping

# Check Redis logs
sudo tail -f /var/log/redis/redis-server.log
```

**4. Performance Issues:**
```bash
# Check system resources
htop
df -h
free -h

# Check application metrics
curl http://localhost:8080/actuator/metrics

# Check database performance
sudo -u postgres psql -d employee_management -c "SELECT * FROM pg_stat_statements ORDER BY total_time DESC LIMIT 10;"
```

### Log Analysis

**Application Logs:**
```bash
# View application logs
sudo tail -f /var/log/employee-management/employee-management.log

# Search for errors
sudo grep -i error /var/log/employee-management/employee-management.log

# View security audit logs
sudo tail -f /var/log/employee-management/security-audit.log
```

**System Logs:**
```bash
# View system logs
sudo journalctl -u employee-management -f

# View nginx logs
sudo tail -f /var/log/nginx/access.log
sudo tail -f /var/log/nginx/error.log
```

## Maintenance

### Regular Maintenance Tasks

**Daily:**
- Monitor application health
- Check disk space
- Review error logs

**Weekly:**
- Update system packages
- Review security logs
- Check backup integrity

**Monthly:**
- Update application dependencies
- Review performance metrics
- Conduct security audit

**Quarterly:**
- Update SSL certificates
- Review and update documentation
- Conduct disaster recovery test

### Update Procedures

**Application Updates:**
```bash
# Create backup before update
./scripts/backup-app.sh

# Deploy new version
./scripts/deploy.sh prod 1.1.0

# Verify deployment
curl -f http://localhost:8080/actuator/health

# Rollback if needed
./scripts/rollback.sh prod
```

**System Updates:**
```bash
# Update system packages
sudo apt update && sudo apt upgrade

# Restart services if needed
sudo systemctl restart employee-management
```

## Support and Documentation

### Additional Resources

- [Configuration Guide](CONFIGURATION.md)
- [API Documentation](API_DOCUMENTATION.md)
- [Architecture Overview](ARCHITECTURE.md)
- [Troubleshooting Guide](TROUBLESHOOTING.md)

### Support Contacts

- **Technical Support:** support@company.com
- **Security Issues:** security@company.com
- **Emergency Contact:** +1-XXX-XXX-XXXX

---

**Note:** This deployment guide should be customized for your specific environment and requirements. Always test deployment procedures in a staging environment before applying to production.