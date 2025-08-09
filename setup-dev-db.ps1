# setup-dev-db.ps1 - Environment Variables for Hybrid Development
$env:DB_HOST="localhost"
$env:DB_PORT="5432"
$env:DB_NAME="employee_management"
$env:DB_USERNAME="employee_admin_new"
$env:DB_PASSWORD="admin123"
$env:REDIS_HOST="localhost"
$env:REDIS_PORT="6379"
$env:REDIS_DB="1"
$env:JWT_SECRET="dev-jwt-secret-key-for-testing-only-change-in-production"
$env:ENCRYPTION_KEY="dev-encryption-key-for-testing-only-change-in-production"

$env:DB_HOST
$env:DB_PORT
$env:DB_NAME
$env:DB_USERNAME
$env:DB_PASSWORD
$env:REDIS_HOST
$env:REDIS_PORT