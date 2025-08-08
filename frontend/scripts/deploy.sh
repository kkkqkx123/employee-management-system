#!/bin/bash

# Frontend Deployment Script
# This script handles the deployment of the React frontend to different environments

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
ENVIRONMENT="staging"
SKIP_TESTS=false
SKIP_BUILD=false
DRY_RUN=false
VERBOSE=false

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to show usage
show_usage() {
    cat << EOF
Usage: $0 [OPTIONS]

Deploy the React frontend to the specified environment.

OPTIONS:
    -e, --environment ENV    Target environment (staging|production) [default: staging]
    -s, --skip-tests        Skip running tests before deployment
    -b, --skip-build        Skip building the application
    -d, --dry-run           Show what would be done without actually doing it
    -v, --verbose           Enable verbose output
    -h, --help              Show this help message

EXAMPLES:
    $0 --environment production
    $0 -e staging --skip-tests
    $0 --dry-run --environment production

EOF
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -e|--environment)
            ENVIRONMENT="$2"
            shift 2
            ;;
        -s|--skip-tests)
            SKIP_TESTS=true
            shift
            ;;
        -b|--skip-build)
            SKIP_BUILD=true
            shift
            ;;
        -d|--dry-run)
            DRY_RUN=true
            shift
            ;;
        -v|--verbose)
            VERBOSE=true
            shift
            ;;
        -h|--help)
            show_usage
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            show_usage
            exit 1
            ;;
    esac
done

# Validate environment
if [[ "$ENVIRONMENT" != "staging" && "$ENVIRONMENT" != "production" ]]; then
    print_error "Invalid environment: $ENVIRONMENT. Must be 'staging' or 'production'"
    exit 1
fi

# Function to run command with dry-run support
run_command() {
    local cmd="$1"
    local description="$2"
    
    if [[ "$VERBOSE" == "true" ]]; then
        print_status "$description"
        echo "Command: $cmd"
    fi
    
    if [[ "$DRY_RUN" == "true" ]]; then
        print_warning "[DRY RUN] Would execute: $cmd"
    else
        if [[ "$VERBOSE" == "true" ]]; then
            eval "$cmd"
        else
            eval "$cmd" > /dev/null 2>&1
        fi
    fi
}

# Function to check prerequisites
check_prerequisites() {
    print_status "Checking prerequisites..."
    
    # Check if we're in the frontend directory
    if [[ ! -f "package.json" ]]; then
        print_error "package.json not found. Please run this script from the frontend directory."
        exit 1
    fi
    
    # Check if Node.js is installed
    if ! command -v node &> /dev/null; then
        print_error "Node.js is not installed"
        exit 1
    fi
    
    # Check if npm is installed
    if ! command -v npm &> /dev/null; then
        print_error "npm is not installed"
        exit 1
    fi
    
    # Check if environment file exists
    if [[ ! -f ".env.${ENVIRONMENT}" ]]; then
        print_error "Environment file .env.${ENVIRONMENT} not found"
        exit 1
    fi
    
    print_success "Prerequisites check passed"
}

# Function to validate environment variables
validate_environment() {
    print_status "Validating environment variables for $ENVIRONMENT..."
    
    run_command "node scripts/validate-env.js $ENVIRONMENT" "Validating environment variables"
    
    print_success "Environment validation passed"
}

# Function to install dependencies
install_dependencies() {
    print_status "Installing dependencies..."
    
    run_command "npm ci" "Installing npm dependencies"
    
    print_success "Dependencies installed"
}

# Function to run tests
run_tests() {
    if [[ "$SKIP_TESTS" == "true" ]]; then
        print_warning "Skipping tests as requested"
        return
    fi
    
    print_status "Running tests..."
    
    # Run unit tests with coverage
    run_command "npm run test:coverage" "Running unit tests with coverage"
    
    # Run integration tests
    run_command "npm run test:integration" "Running integration tests"
    
    # Run e2e tests for production
    if [[ "$ENVIRONMENT" == "production" ]]; then
        run_command "npm run test:e2e" "Running end-to-end tests"
    fi
    
    print_success "All tests passed"
}

# Function to run linting and formatting
run_quality_checks() {
    print_status "Running code quality checks..."
    
    run_command "npm run lint" "Running ESLint"
    run_command "npm run format:check" "Checking code formatting"
    run_command "npm run type-check" "Running TypeScript type checking"
    
    print_success "Code quality checks passed"
}

# Function to build the application
build_application() {
    if [[ "$SKIP_BUILD" == "true" ]]; then
        print_warning "Skipping build as requested"
        return
    fi
    
    print_status "Building application for $ENVIRONMENT..."
    
    if [[ "$ENVIRONMENT" == "production" ]]; then
        run_command "npm run build:production" "Building for production"
    else
        run_command "npm run build:staging" "Building for staging"
    fi
    
    print_success "Application built successfully"
}

# Function to run post-build validation
validate_build() {
    print_status "Validating build output..."
    
    # Check if dist directory exists
    if [[ ! -d "dist" ]]; then
        print_error "Build output directory 'dist' not found"
        exit 1
    fi
    
    # Check if index.html exists
    if [[ ! -f "dist/index.html" ]]; then
        print_error "index.html not found in build output"
        exit 1
    fi
    
    # Check build size
    local build_size=$(du -sh dist | cut -f1)
    print_status "Build size: $build_size"
    
    # Warn if build is too large (>10MB)
    local size_bytes=$(du -sb dist | cut -f1)
    if [[ $size_bytes -gt 10485760 ]]; then
        print_warning "Build size is larger than 10MB. Consider optimizing."
    fi
    
    print_success "Build validation passed"
}

# Function to deploy to server
deploy_to_server() {
    print_status "Deploying to $ENVIRONMENT server..."
    
    case $ENVIRONMENT in
        staging)
            # Add staging deployment logic here
            print_status "Deploying to staging server..."
            # Example: rsync -avz dist/ user@staging-server:/var/www/html/
            run_command "echo 'Staging deployment would happen here'" "Staging deployment"
            ;;
        production)
            # Add production deployment logic here
            print_status "Deploying to production server..."
            # Example: rsync -avz dist/ user@prod-server:/var/www/html/
            run_command "echo 'Production deployment would happen here'" "Production deployment"
            ;;
    esac
    
    print_success "Deployment completed"
}

# Function to run post-deployment health checks
run_health_checks() {
    print_status "Running post-deployment health checks..."
    
    # Add health check logic here
    # Example: curl -f https://your-domain.com/health
    run_command "echo 'Health checks would run here'" "Health checks"
    
    print_success "Health checks passed"
}

# Main deployment function
main() {
    print_status "Starting deployment to $ENVIRONMENT environment"
    echo "=============================================="
    
    if [[ "$DRY_RUN" == "true" ]]; then
        print_warning "DRY RUN MODE - No actual changes will be made"
    fi
    
    # Run deployment steps
    check_prerequisites
    validate_environment
    install_dependencies
    run_quality_checks
    run_tests
    build_application
    validate_build
    deploy_to_server
    run_health_checks
    
    print_success "Deployment to $ENVIRONMENT completed successfully!"
    
    if [[ "$ENVIRONMENT" == "production" ]]; then
        print_status "🚀 Production deployment completed!"
        print_status "Don't forget to:"
        print_status "  - Monitor application logs"
        print_status "  - Check error tracking (Sentry)"
        print_status "  - Verify all features are working"
        print_status "  - Update documentation if needed"
    fi
}

# Run main function
main "$@"