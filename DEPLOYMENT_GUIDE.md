# Employee Management System - Deployment Guide

This guide provides comprehensive instructions for deploying the Employee Management System to different environments.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Environment Setup](#environment-setup)
3. [Local Development](#local-development)
4. [Staging Deployment](#staging-deployment)
5. [Production Deployment](#production-deployment)
6. [Docker Deployment](#docker-deployment)
7. [Monitoring and Health Checks](#monitoring-and-health-checks)
8. [Troubleshooting](#troubleshooting)

## Prerequisites

### System Requirements

- **Node.js**: 20.x or higher
- **Java**: 21 or higher
- **PostgreSQL**: 15 or higher
- **Redis**: 7 or higher
- **Docker**: 24.x or higher (for containerized deployment)
- **Docker Compose**: 2.x or higher

### Development Tools

- **Git**: For version control
- **Maven**: 3.9.x or higher (for backend)
- **npm**: 10.x or higher (for frontend)

## Environment Setup

### Environment Variables

The system uses different environment configurations for each deployment stage:

#### Backend Environment Variables

Copy and configure the appropriate environment file:

```bash
# For development
cp .env.example .env.dev

# For staging
cp .env.example .env.staging

# For production
cp .env.example .env.prod
```

Key variables to configure:

- `DATABASE_URL`: PostgreSQL connection string
- `REDIS_HOST`: Redis server host
- `JWT_SECRET`: JWT signing secret (minimum 32 characters)
- `ENCRYPTION_KEY`: Data encryption key (32 bytes)
- `EMAIL_*`: Email service configuration
- `CORS_ALLOWED_ORIGINS`: Allowed frontend origins

#### Frontend Environment Variables

Configure frontend environment files:

```bash
# For development
cp frontend/.env.example frontend/.env

# For staging
cp frontend/.env.example frontend/.env.staging

# For production
cp frontend/.env.example frontend/.env.production
```

Key variables:

- `VITE_API_BASE_URL`: Backend API URL
- `VITE_WS_URL`: WebSocket server URL
- `VITE_APP_NAME`: Application name
- `VITE_SENTRY_DSN`: Error tracking DSN (production)

## Local Development

### Backend Setup

1. **Start required services:**
   ```bash
   docker-compose -f docker-compose.dev.yml up -d
   ```

2. **Configure database:**
   ```bash
   # Run database migrations
   ./mvnw flyway:migrate -Dflyway.configFiles=src/main/resources/application-dev.properties
   ```

3. **Start backend:**
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```

### Frontend Setup

1. **Install dependencies:**
   ```bash
   cd frontend
   npm install
   ```

2. **Start development server:**
   ```bash
   npm run dev
   ```

3. **Run tests:**
   ```bash
   npm run test:all
   ```

## Staging Deployment

### Automated Deployment

Use the deployment script for staging:

```bash
cd frontend
chmod +x scripts/deploy.sh
./scripts/deploy.sh --environment staging
```

### Manual Deployment

1. **Validate environment:**
   ```bash
   cd frontend
   npm run validate:env staging
   ```

2. **Build application:**
   ```bash
   npm run build:staging
   ```

3. **Run tests:**
   ```bash
   npm run test:production
   ```

4. **Deploy to staging server:**
   ```bash
   # Upload build files to staging server
   rsync -avz dist/ user@staging-server:/var/www/html/
   ```

## Production Deployment

### Pre-deployment Checklist

- [ ] All tests passing
- [ ] Environment variables configured
- [ ] SSL certificates ready
- [ ] Database backups completed
- [ ] Monitoring systems active
- [ ] Rollback plan prepared

### Deployment Steps

1. **Validate production environment:**
   ```bash
   cd frontend
   npm run validate:env production
   ```

2. **Build optimized production bundle:**
   ```bash
   npm run build:production
   ```

3. **Run comprehensive tests:**
   ```bash
   npm run test:ci
   npm run test:e2e
   ```

4. **Deploy using script:**
   ```bash
   ./scripts/deploy.sh --environment production
   ```

### Blue-Green Deployment

For zero-downtime deployments:

1. **Deploy to green environment:**
   ```bash
   ./scripts/deploy.sh --environment production --target green
   ```

2. **Run health checks:**
   ```bash
   curl -f https://green.yourdomain.com/health
   ```

3. **Switch traffic:**
   ```bash
   # Update load balancer configuration
   # Point traffic to green environment
   ```

4. **Monitor and verify:**
   ```bash
   # Monitor logs and metrics
   # Verify all functionality
   ```

## Docker Deployment

### Full Stack Deployment

Deploy the complete system using Docker Compose:

```bash
# Production deployment
docker-compose up -d

# With monitoring
docker-compose --profile monitoring up -d

# With nginx reverse proxy
docker-compose --profile with-nginx up -d
```

### Frontend-Only Deployment

Deploy just the frontend container:

```bash
# Build frontend image
docker build -t employee-management-frontend:latest ./frontend

# Run container
docker run -d \
  --name frontend \
  -p 80:80 \
  -e VITE_API_BASE_URL=https://api.yourdomain.com/api \
  -e VITE_WS_URL=wss://api.yourdomain.com \
  employee-management-frontend:latest
```

### Environment-Specific Builds

```bash
# Staging build
docker build \
  --build-arg BUILD_ENV=staging \
  --build-arg VITE_API_BASE_URL=https://staging-api.yourdomain.com/api \
  -t employee-management-frontend:staging \
  ./frontend

# Production build
docker build \
  --build-arg BUILD_ENV=production \
  --build-arg VITE_API_BASE_URL=https://api.yourdomain.com/api \
  -t employee-management-frontend:production \
  ./frontend
```

## Monitoring and Health Checks

### Health Check Endpoints

- **Frontend**: `GET /health`
- **Backend**: `GET /actuator/health`
- **Database**: Connection check via backend health endpoint

### Monitoring Setup

1. **Prometheus metrics:**
   ```bash
   # Access Prometheus dashboard
   http://localhost:9090
   ```

2. **Grafana dashboards:**
   ```bash
   # Access Grafana
   http://localhost:3001
   # Default credentials: admin/admin
   ```

3. **Application logs:**
   ```bash
   # View backend logs
   docker logs employee-management-app

   # View frontend logs
   docker logs employee-management-frontend
   ```

### Performance Monitoring

Monitor key metrics:

- **Response times**: API endpoint performance
- **Error rates**: 4xx/5xx error percentages
- **Resource usage**: CPU, memory, disk usage
- **Database performance**: Query times, connection pool
- **Frontend metrics**: Load times, bundle sizes

## Troubleshooting

### Common Issues

#### Frontend Build Failures

```bash
# Clear npm cache
npm cache clean --force

# Remove node_modules and reinstall
rm -rf node_modules package-lock.json
npm install

# Check for TypeScript errors
npm run type-check
```

#### Backend Connection Issues

```bash
# Check database connectivity
docker exec -it employee-management-postgres psql -U employee_admin -d employee_management

# Check Redis connectivity
docker exec -it employee-management-redis redis-cli ping

# View backend logs
docker logs employee-management-app --tail 100
```

#### Docker Issues

```bash
# Clean up Docker resources
docker system prune -a

# Rebuild containers
docker-compose down
docker-compose build --no-cache
docker-compose up -d
```

### Performance Issues

#### Frontend Performance

1. **Analyze bundle size:**
   ```bash
   npm run build:analyze
   ```

2. **Check for memory leaks:**
   ```bash
   # Use browser dev tools
   # Monitor memory usage over time
   ```

3. **Optimize images and assets:**
   ```bash
   # Compress images
   # Use WebP format where possible
   # Implement lazy loading
   ```

#### Backend Performance

1. **Database optimization:**
   ```sql
   -- Check slow queries
   SELECT query, mean_time, calls 
   FROM pg_stat_statements 
   ORDER BY mean_time DESC 
   LIMIT 10;
   ```

2. **JVM tuning:**
   ```bash
   # Adjust JVM parameters in docker-compose.yml
   JAVA_OPTS: "-Xms1g -Xmx2g -XX:+UseG1GC"
   ```

### Rollback Procedures

#### Frontend Rollback

```bash
# Rollback to previous version
docker tag employee-management-frontend:previous employee-management-frontend:latest
docker-compose up -d frontend
```

#### Database Rollback

```bash
# Use Flyway to rollback migrations
./mvnw flyway:undo -Dflyway.configFiles=src/main/resources/application-prod.properties
```

### Support and Maintenance

#### Log Locations

- **Frontend logs**: `/var/log/nginx/`
- **Backend logs**: `/app/logs/`
- **Database logs**: PostgreSQL container logs
- **Redis logs**: Redis container logs

#### Backup Procedures

```bash
# Database backup
docker exec employee-management-postgres pg_dump -U employee_admin employee_management > backup.sql

# File uploads backup
docker cp employee-management-app:/app/uploads ./uploads-backup
```

#### Security Updates

1. **Regular dependency updates:**
   ```bash
   # Frontend
   npm audit fix
   npm update

   # Backend
   ./mvnw versions:use-latest-versions
   ```

2. **Security scanning:**
   ```bash
   # Frontend security scan
   npm audit

   # Docker image scanning
   docker scan employee-management-frontend:latest
   ```

## Conclusion

This deployment guide covers the essential steps for deploying the Employee Management System across different environments. Always test deployments in staging before production, maintain proper backups, and monitor system health continuously.

For additional support or questions, refer to the project documentation or contact the development team.