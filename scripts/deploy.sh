#!/bin/bash

# Employee Management System Deployment Script
# Usage: ./deploy.sh [environment] [version]
# Example: ./deploy.sh prod 1.0.0

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
APP_NAME="employee-management-system"
DEFAULT_VERSION="latest"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging functions
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

# Function to display usage
usage() {
    echo "Usage: $0 [environment] [version]"
    echo "Environments: dev, staging, prod"
    echo "Version: Application version (default: latest)"
    echo ""
    echo "Examples:"
    echo "  $0 dev"
    echo "  $0 staging 1.0.0"
    echo "  $0 prod 1.2.3"
    exit 1
}

# Function to validate environment
validate_environment() {
    local env=$1
    case $env in
        dev|staging|prod)
            return 0
            ;;
        *)
            log_error "Invalid environment: $env"
            usage
            ;;
    esac
}

# Function to check prerequisites
check_prerequisites() {
    local env=$1
    
    log_info "Checking prerequisites for $env environment..."
    
    # Check if required tools are installed
    command -v java >/dev/null 2>&1 || { log_error "Java is required but not installed."; exit 1; }
    command -v mvn >/dev/null 2>&1 || { log_error "Maven is required but not installed."; exit 1; }
    
    if [[ "$env" == "prod" ]]; then
        command -v docker >/dev/null 2>&1 || { log_error "Docker is required for production deployment."; exit 1; }
        command -v docker-compose >/dev/null 2>&1 || { log_error "Docker Compose is required for production deployment."; exit 1; }
    fi
    
    # Check if environment file exists
    local env_file="$PROJECT_ROOT/.env.$env"
    if [[ ! -f "$env_file" ]]; then
        log_error "Environment file not found: $env_file"
        exit 1
    fi
    
    log_success "Prerequisites check passed"
}

# Function to load environment variables
load_environment() {
    local env=$1
    local env_file="$PROJECT_ROOT/.env.$env"
    
    log_info "Loading environment variables from $env_file"
    
    if [[ -f "$env_file" ]]; then
        export $(grep -v '^#' "$env_file" | xargs)
        log_success "Environment variables loaded"
    else
        log_error "Environment file not found: $env_file"
        exit 1
    fi
}

# Function to run database migrations
run_migrations() {
    local env=$1
    
    log_info "Running database migrations for $env environment..."
    
    cd "$PROJECT_ROOT"
    
    # Set Flyway properties based on environment
    case $env in
        dev)
            FLYWAY_URL="${DATABASE_URL:-jdbc:postgresql://localhost:5432/employee_management}"
            FLYWAY_USER="${DATABASE_USERNAME:-employee_admin}"
            FLYWAY_PASSWORD="${DATABASE_PASSWORD:-dev_password}"
            ;;
        staging)
            FLYWAY_URL="${DATABASE_URL:-jdbc:postgresql://localhost:5432/employee_management_staging}"
            FLYWAY_USER="${DATABASE_USERNAME:-employee_admin}"
            FLYWAY_PASSWORD="${DATABASE_PASSWORD}"
            ;;
        prod)
            FLYWAY_URL="${DATABASE_URL}"
            FLYWAY_USER="${DATABASE_USERNAME}"
            FLYWAY_PASSWORD="${DATABASE_PASSWORD}"
            ;;
    esac
    
    # Run migrations
    mvn flyway:migrate \
        -Dflyway.url="$FLYWAY_URL" \
        -Dflyway.user="$FLYWAY_USER" \
        -Dflyway.password="$FLYWAY_PASSWORD" \
        -Dflyway.locations="classpath:db/migration"
    
    if [[ $? -eq 0 ]]; then
        log_success "Database migrations completed successfully"
    else
        log_error "Database migrations failed"
        exit 1
    fi
}

# Function to build application
build_application() {
    local env=$1
    local version=$2
    
    log_info "Building application for $env environment (version: $version)..."
    
    cd "$PROJECT_ROOT"
    
    # Clean and build
    case $env in
        dev)
            mvn clean package -Pdev -DskipTests=false
            ;;
        staging)
            mvn clean package -Pstaging -DskipTests=false
            ;;
        prod)
            mvn clean package -Pprod -DskipTests=true
            ;;
    esac
    
    if [[ $? -eq 0 ]]; then
        log_success "Application build completed successfully"
    else
        log_error "Application build failed"
        exit 1
    fi
}

# Function to deploy to development
deploy_dev() {
    local version=$1
    
    log_info "Deploying to development environment..."
    
    # Stop existing application if running
    pkill -f "employee-management-system" || true
    
    # Start application
    cd "$PROJECT_ROOT"
    nohup java -jar target/employee-management-system.jar \
        --spring.profiles.active=dev \
        > logs/application.log 2>&1 &
    
    # Wait for application to start
    sleep 10
    
    # Health check
    if curl -f http://localhost:8080/actuator/health >/dev/null 2>&1; then
        log_success "Development deployment completed successfully"
    else
        log_error "Development deployment failed - health check failed"
        exit 1
    fi
}

# Function to deploy to staging
deploy_staging() {
    local version=$1
    
    log_info "Deploying to staging environment..."
    
    # Similar to dev but with staging profile
    pkill -f "employee-management-system" || true
    
    cd "$PROJECT_ROOT"
    nohup java -jar target/employee-management-system.jar \
        --spring.profiles.active=staging \
        > logs/application-staging.log 2>&1 &
    
    sleep 15
    
    if curl -f http://localhost:8080/actuator/health >/dev/null 2>&1; then
        log_success "Staging deployment completed successfully"
    else
        log_error "Staging deployment failed - health check failed"
        exit 1
    fi
}

# Function to deploy to production
deploy_prod() {
    local version=$1
    
    log_info "Deploying to production environment..."
    
    cd "$PROJECT_ROOT"
    
    # Build Docker image
    docker build -t "$APP_NAME:$version" .
    docker tag "$APP_NAME:$version" "$APP_NAME:latest"
    
    # Create backup of current deployment
    if docker ps -q -f name="$APP_NAME" | grep -q .; then
        log_info "Creating backup of current deployment..."
        docker commit "$APP_NAME" "$APP_NAME:backup-$(date +%Y%m%d-%H%M%S)"
    fi
    
    # Deploy with Docker Compose
    export APP_VERSION="$version"
    docker-compose -f docker-compose.yml down
    docker-compose -f docker-compose.yml up -d
    
    # Wait for services to be ready
    log_info "Waiting for services to be ready..."
    sleep 30
    
    # Health check
    local max_attempts=12
    local attempt=1
    
    while [[ $attempt -le $max_attempts ]]; do
        if curl -f http://localhost:8080/actuator/health >/dev/null 2>&1; then
            log_success "Production deployment completed successfully"
            return 0
        fi
        
        log_info "Health check attempt $attempt/$max_attempts failed, retrying in 10 seconds..."
        sleep 10
        ((attempt++))
    done
    
    log_error "Production deployment failed - health check failed after $max_attempts attempts"
    
    # Rollback on failure
    log_warning "Initiating rollback..."
    "$SCRIPT_DIR/rollback.sh" prod
    exit 1
}

# Function to run post-deployment tests
run_post_deployment_tests() {
    local env=$1
    
    log_info "Running post-deployment tests for $env environment..."
    
    # Basic health checks
    local base_url
    case $env in
        dev|staging)
            base_url="http://localhost:8080"
            ;;
        prod)
            base_url="${PRODUCTION_URL:-http://localhost:8080}"
            ;;
    esac
    
    # Test endpoints
    local endpoints=(
        "/actuator/health"
        "/actuator/info"
        "/api/auth/health"
    )
    
    for endpoint in "${endpoints[@]}"; do
        if curl -f "$base_url$endpoint" >/dev/null 2>&1; then
            log_success "✓ $endpoint is responding"
        else
            log_error "✗ $endpoint is not responding"
            return 1
        fi
    done
    
    log_success "Post-deployment tests passed"
}

# Function to send deployment notification
send_notification() {
    local env=$1
    local version=$2
    local status=$3
    
    if [[ -n "$SLACK_WEBHOOK_URL" ]]; then
        local color
        case $status in
            success) color="good" ;;
            failure) color="danger" ;;
            *) color="warning" ;;
        esac
        
        curl -X POST -H 'Content-type: application/json' \
            --data "{
                \"attachments\": [{
                    \"color\": \"$color\",
                    \"title\": \"Deployment $status\",
                    \"fields\": [
                        {\"title\": \"Environment\", \"value\": \"$env\", \"short\": true},
                        {\"title\": \"Version\", \"value\": \"$version\", \"short\": true},
                        {\"title\": \"Time\", \"value\": \"$(date)\", \"short\": false}
                    ]
                }]
            }" \
            "$SLACK_WEBHOOK_URL"
    fi
}

# Main deployment function
main() {
    local env=${1:-}
    local version=${2:-$DEFAULT_VERSION}
    
    if [[ -z "$env" ]]; then
        usage
    fi
    
    validate_environment "$env"
    
    log_info "Starting deployment to $env environment (version: $version)"
    log_info "Timestamp: $(date)"
    
    # Create logs directory if it doesn't exist
    mkdir -p "$PROJECT_ROOT/logs"
    
    # Load environment variables
    load_environment "$env"
    
    # Check prerequisites
    check_prerequisites "$env"
    
    # Run database migrations
    run_migrations "$env"
    
    # Build application
    build_application "$env" "$version"
    
    # Deploy based on environment
    case $env in
        dev)
            deploy_dev "$version"
            ;;
        staging)
            deploy_staging "$version"
            ;;
        prod)
            deploy_prod "$version"
            ;;
    esac
    
    # Run post-deployment tests
    if run_post_deployment_tests "$env"; then
        log_success "Deployment to $env environment completed successfully!"
        send_notification "$env" "$version" "success"
    else
        log_error "Post-deployment tests failed"
        send_notification "$env" "$version" "failure"
        exit 1
    fi
}

# Run main function with all arguments
main "$@"