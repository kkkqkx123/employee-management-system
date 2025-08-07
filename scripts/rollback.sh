#!/bin/bash

# Employee Management System Rollback Script
# Usage: ./rollback.sh [environment] [backup_version]
# Example: ./rollback.sh prod backup-20241207-143000

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
APP_NAME="employee-management-system"

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
    echo "Usage: $0 [environment] [backup_version]"
    echo "Environments: dev, staging, prod"
    echo "Backup Version: Specific backup to rollback to (optional)"
    echo ""
    echo "Examples:"
    echo "  $0 prod"
    echo "  $0 staging backup-20241207-143000"
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

# Function to list available backups
list_backups() {
    local env=$1
    
    log_info "Available backups for $env environment:"
    
    case $env in
        prod)
            # List Docker image backups
            docker images "$APP_NAME" --format "table {{.Tag}}\t{{.CreatedAt}}\t{{.Size}}" | grep backup || {
                log_warning "No Docker image backups found"
                return 1
            }
            ;;
        dev|staging)
            # List JAR file backups
            local backup_dir="$PROJECT_ROOT/backups"
            if [[ -d "$backup_dir" ]]; then
                ls -la "$backup_dir"/*.jar 2>/dev/null || {
                    log_warning "No JAR file backups found"
                    return 1
                }
            else
                log_warning "Backup directory not found: $backup_dir"
                return 1
            fi
            ;;
    esac
}

# Function to create emergency backup before rollback
create_emergency_backup() {
    local env=$1
    
    log_info "Creating emergency backup before rollback..."
    
    case $env in
        prod)
            if docker ps -q -f name="$APP_NAME" | grep -q .; then
                local backup_tag="emergency-backup-$(date +%Y%m%d-%H%M%S)"
                docker commit "$APP_NAME" "$APP_NAME:$backup_tag"
                log_success "Emergency backup created: $APP_NAME:$backup_tag"
            else
                log_warning "No running container found to backup"
            fi
            ;;
        dev|staging)
            local backup_dir="$PROJECT_ROOT/backups"
            mkdir -p "$backup_dir"
            
            if [[ -f "$PROJECT_ROOT/target/employee-management-system.jar" ]]; then
                local backup_file="$backup_dir/employee-management-system-emergency-$(date +%Y%m%d-%H%M%S).jar"
                cp "$PROJECT_ROOT/target/employee-management-system.jar" "$backup_file"
                log_success "Emergency backup created: $backup_file"
            else
                log_warning "No current JAR file found to backup"
            fi
            ;;
    esac
}

# Function to rollback database migrations
rollback_database() {
    local env=$1
    local target_version=$2
    
    log_warning "Database rollback requested for $env environment"
    log_warning "This operation can be destructive. Please ensure you have a database backup."
    
    read -p "Do you want to proceed with database rollback? (yes/no): " confirm
    if [[ "$confirm" != "yes" ]]; then
        log_info "Database rollback cancelled by user"
        return 0
    fi
    
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
    
    if [[ -n "$target_version" ]]; then
        # Rollback to specific version
        mvn flyway:undo \
            -Dflyway.url="$FLYWAY_URL" \
            -Dflyway.user="$FLYWAY_USER" \
            -Dflyway.password="$FLYWAY_PASSWORD" \
            -Dflyway.target="$target_version"
    else
        # Rollback last migration
        mvn flyway:undo \
            -Dflyway.url="$FLYWAY_URL" \
            -Dflyway.user="$FLYWAY_USER" \
            -Dflyway.password="$FLYWAY_PASSWORD"
    fi
    
    if [[ $? -eq 0 ]]; then
        log_success "Database rollback completed"
    else
        log_error "Database rollback failed"
        exit 1
    fi
}

# Function to rollback development environment
rollback_dev() {
    local backup_version=$1
    
    log_info "Rolling back development environment..."
    
    # Stop current application
    pkill -f "employee-management-system" || true
    sleep 5
    
    if [[ -n "$backup_version" ]]; then
        # Restore from specific backup
        local backup_file="$PROJECT_ROOT/backups/$backup_version"
        if [[ -f "$backup_file" ]]; then
            cp "$backup_file" "$PROJECT_ROOT/target/employee-management-system.jar"
            log_success "Restored from backup: $backup_version"
        else
            log_error "Backup file not found: $backup_file"
            exit 1
        fi
    else
        # Use the most recent backup
        local backup_dir="$PROJECT_ROOT/backups"
        local latest_backup=$(ls -t "$backup_dir"/*.jar 2>/dev/null | head -n1)
        if [[ -n "$latest_backup" ]]; then
            cp "$latest_backup" "$PROJECT_ROOT/target/employee-management-system.jar"
            log_success "Restored from latest backup: $(basename "$latest_backup")"
        else
            log_error "No backup files found"
            exit 1
        fi
    fi
    
    # Start application
    cd "$PROJECT_ROOT"
    nohup java -jar target/employee-management-system.jar \
        --spring.profiles.active=dev \
        > logs/application.log 2>&1 &
    
    sleep 10
    
    # Health check
    if curl -f http://localhost:8080/actuator/health >/dev/null 2>&1; then
        log_success "Development rollback completed successfully"
    else
        log_error "Development rollback failed - health check failed"
        exit 1
    fi
}

# Function to rollback staging environment
rollback_staging() {
    local backup_version=$1
    
    log_info "Rolling back staging environment..."
    
    # Similar to dev rollback
    pkill -f "employee-management-system" || true
    sleep 5
    
    if [[ -n "$backup_version" ]]; then
        local backup_file="$PROJECT_ROOT/backups/$backup_version"
        if [[ -f "$backup_file" ]]; then
            cp "$backup_file" "$PROJECT_ROOT/target/employee-management-system.jar"
            log_success "Restored from backup: $backup_version"
        else
            log_error "Backup file not found: $backup_file"
            exit 1
        fi
    else
        local backup_dir="$PROJECT_ROOT/backups"
        local latest_backup=$(ls -t "$backup_dir"/*.jar 2>/dev/null | head -n1)
        if [[ -n "$latest_backup" ]]; then
            cp "$latest_backup" "$PROJECT_ROOT/target/employee-management-system.jar"
            log_success "Restored from latest backup: $(basename "$latest_backup")"
        else
            log_error "No backup files found"
            exit 1
        fi
    fi
    
    cd "$PROJECT_ROOT"
    nohup java -jar target/employee-management-system.jar \
        --spring.profiles.active=staging \
        > logs/application-staging.log 2>&1 &
    
    sleep 15
    
    if curl -f http://localhost:8080/actuator/health >/dev/null 2>&1; then
        log_success "Staging rollback completed successfully"
    else
        log_error "Staging rollback failed - health check failed"
        exit 1
    fi
}

# Function to rollback production environment
rollback_prod() {
    local backup_version=$1
    
    log_info "Rolling back production environment..."
    
    cd "$PROJECT_ROOT"
    
    if [[ -n "$backup_version" ]]; then
        # Rollback to specific backup
        if docker images "$APP_NAME:$backup_version" --format "{{.Repository}}:{{.Tag}}" | grep -q "$APP_NAME:$backup_version"; then
            log_info "Rolling back to specific backup: $backup_version"
            
            # Stop current containers
            docker-compose -f docker-compose.yml down
            
            # Tag the backup as latest
            docker tag "$APP_NAME:$backup_version" "$APP_NAME:latest"
            
            # Start with the backup
            docker-compose -f docker-compose.yml up -d
        else
            log_error "Backup image not found: $APP_NAME:$backup_version"
            exit 1
        fi
    else
        # Rollback to the most recent backup
        local latest_backup=$(docker images "$APP_NAME" --format "{{.Tag}}" | grep backup | head -n1)
        if [[ -n "$latest_backup" ]]; then
            log_info "Rolling back to latest backup: $latest_backup"
            
            docker-compose -f docker-compose.yml down
            docker tag "$APP_NAME:$latest_backup" "$APP_NAME:latest"
            docker-compose -f docker-compose.yml up -d
        else
            log_error "No backup images found"
            exit 1
        fi
    fi
    
    # Wait for services to be ready
    log_info "Waiting for services to be ready..."
    sleep 30
    
    # Health check
    local max_attempts=12
    local attempt=1
    
    while [[ $attempt -le $max_attempts ]]; do
        if curl -f http://localhost:8080/actuator/health >/dev/null 2>&1; then
            log_success "Production rollback completed successfully"
            return 0
        fi
        
        log_info "Health check attempt $attempt/$max_attempts failed, retrying in 10 seconds..."
        sleep 10
        ((attempt++))
    done
    
    log_error "Production rollback failed - health check failed after $max_attempts attempts"
    exit 1
}

# Function to verify rollback
verify_rollback() {
    local env=$1
    
    log_info "Verifying rollback for $env environment..."
    
    local base_url
    case $env in
        dev|staging)
            base_url="http://localhost:8080"
            ;;
        prod)
            base_url="${PRODUCTION_URL:-http://localhost:8080}"
            ;;
    esac
    
    # Test critical endpoints
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
    
    log_success "Rollback verification passed"
}

# Function to send rollback notification
send_notification() {
    local env=$1
    local backup_version=$2
    local status=$3
    
    if [[ -n "$SLACK_WEBHOOK_URL" ]]; then
        local color
        case $status in
            success) color="warning" ;;
            failure) color="danger" ;;
            *) color="warning" ;;
        esac
        
        curl -X POST -H 'Content-type: application/json' \
            --data "{
                \"attachments\": [{
                    \"color\": \"$color\",
                    \"title\": \"Rollback $status\",
                    \"fields\": [
                        {\"title\": \"Environment\", \"value\": \"$env\", \"short\": true},
                        {\"title\": \"Backup Version\", \"value\": \"${backup_version:-latest}\", \"short\": true},
                        {\"title\": \"Time\", \"value\": \"$(date)\", \"short\": false}
                    ]
                }]
            }" \
            "$SLACK_WEBHOOK_URL"
    fi
}

# Main rollback function
main() {
    local env=${1:-}
    local backup_version=${2:-}
    
    if [[ -z "$env" ]]; then
        usage
    fi
    
    validate_environment "$env"
    
    log_warning "Starting rollback for $env environment"
    log_info "Backup version: ${backup_version:-latest available}"
    log_info "Timestamp: $(date)"
    
    # Confirmation prompt for production
    if [[ "$env" == "prod" ]]; then
        log_warning "You are about to rollback the PRODUCTION environment!"
        read -p "Are you sure you want to proceed? (yes/no): " confirm
        if [[ "$confirm" != "yes" ]]; then
            log_info "Rollback cancelled by user"
            exit 0
        fi
    fi
    
    # Load environment variables
    load_environment "$env"
    
    # List available backups if no specific version provided
    if [[ -z "$backup_version" ]]; then
        list_backups "$env"
        echo ""
        read -p "Enter backup version to rollback to (or press Enter for latest): " backup_version
    fi
    
    # Create emergency backup
    create_emergency_backup "$env"
    
    # Perform rollback based on environment
    case $env in
        dev)
            rollback_dev "$backup_version"
            ;;
        staging)
            rollback_staging "$backup_version"
            ;;
        prod)
            rollback_prod "$backup_version"
            ;;
    esac
    
    # Verify rollback
    if verify_rollback "$env"; then
        log_success "Rollback to $env environment completed successfully!"
        send_notification "$env" "$backup_version" "success"
        
        # Ask about database rollback
        echo ""
        read -p "Do you need to rollback database migrations as well? (yes/no): " db_rollback
        if [[ "$db_rollback" == "yes" ]]; then
            rollback_database "$env"
        fi
    else
        log_error "Rollback verification failed"
        send_notification "$env" "$backup_version" "failure"
        exit 1
    fi
}

# Run main function with all arguments
main "$@"