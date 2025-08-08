#!/bin/sh

# Docker entrypoint script for React frontend
# This script handles environment variable substitution and nginx startup

set -e

echo "Starting frontend container..."

# Function to replace environment variables in files
replace_env_vars() {
    local file="$1"
    echo "Processing environment variables in $file"
    
    # Replace environment variables in the format __ENV_VAR__
    if [ -f "$file" ]; then
        # Create a temporary file for processing
        temp_file=$(mktemp)
        cp "$file" "$temp_file"
        
        # Replace common environment variables
        sed -i "s|__VITE_API_BASE_URL__|${VITE_API_BASE_URL:-/api}|g" "$temp_file"
        sed -i "s|__VITE_WS_URL__|${VITE_WS_URL:-ws://localhost:8080}|g" "$temp_file"
        sed -i "s|__VITE_APP_NAME__|${VITE_APP_NAME:-Employee Management System}|g" "$temp_file"
        sed -i "s|__VITE_APP_VERSION__|${VITE_APP_VERSION:-1.0.0}|g" "$temp_file"
        
        # Move processed file back
        mv "$temp_file" "$file"
        echo "Environment variables processed in $file"
    else
        echo "Warning: File $file not found"
    fi
}

# Process environment variables in built files
echo "Processing environment variables in built files..."

# Find and process JavaScript files that might contain environment variables
find /usr/share/nginx/html -name "*.js" -type f | while read -r file; do
    if grep -q "__VITE_" "$file" 2>/dev/null; then
        replace_env_vars "$file"
    fi
done

# Process index.html
if [ -f "/usr/share/nginx/html/index.html" ]; then
    replace_env_vars "/usr/share/nginx/html/index.html"
fi

# Create runtime configuration file
cat > /usr/share/nginx/html/config.js << EOF
window.__RUNTIME_CONFIG__ = {
    API_BASE_URL: '${VITE_API_BASE_URL:-/api}',
    WS_URL: '${VITE_WS_URL:-ws://localhost:8080}',
    APP_NAME: '${VITE_APP_NAME:-Employee Management System}',
    APP_VERSION: '${VITE_APP_VERSION:-1.0.0}',
    ENVIRONMENT: '${NODE_ENV:-production}'
};
EOF

echo "Runtime configuration created"

# Validate nginx configuration
echo "Validating nginx configuration..."
nginx -t

# Create necessary directories
mkdir -p /var/cache/nginx/client_temp
mkdir -p /var/cache/nginx/proxy_temp
mkdir -p /var/cache/nginx/fastcgi_temp
mkdir -p /var/cache/nginx/uwsgi_temp
mkdir -p /var/cache/nginx/scgi_temp

# Set permissions
chown -R nginx:nginx /var/cache/nginx
chown -R nginx:nginx /usr/share/nginx/html

echo "Frontend container initialization complete"

# Execute the main command
exec "$@"