-- Employee Management System Database Initialization Script
-- This script sets up the initial database structure and default data

-- Create database if it doesn't exist (for development)
-- Note: This is typically handled by the deployment process

-- Set timezone
SET timezone = 'UTC';

-- Create extensions if they don't exist
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Create schemas for better organization
CREATE SCHEMA IF NOT EXISTS audit;
CREATE SCHEMA IF NOT EXISTS config;

-- Grant permissions to application user
GRANT USAGE ON SCHEMA public TO employee_admin;
GRANT USAGE ON SCHEMA audit TO employee_admin;
GRANT USAGE ON SCHEMA config TO employee_admin;

GRANT CREATE ON SCHEMA public TO employee_admin;
GRANT CREATE ON SCHEMA audit TO employee_admin;
GRANT CREATE ON SCHEMA config TO employee_admin;

-- Create audit table for tracking all changes
CREATE TABLE IF NOT EXISTS audit.audit_log (
    id BIGSERIAL PRIMARY KEY,
    table_name VARCHAR(255) NOT NULL,
    operation VARCHAR(10) NOT NULL,
    old_values JSONB,
    new_values JSONB,
    user_id BIGINT,
    username VARCHAR(255),
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    ip_address INET,
    user_agent TEXT
);

-- Create configuration table for system settings
CREATE TABLE IF NOT EXISTS config.system_config (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(255) UNIQUE NOT NULL,
    config_value TEXT,
    description TEXT,
    is_encrypted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Insert default system configurations
INSERT INTO config.system_config (config_key, config_value, description) VALUES
('system.name', 'Employee Management System', 'System name displayed in UI'),
('system.version', '1.0.0', 'Current system version'),
('system.maintenance.enabled', 'false', 'Enable maintenance mode'),
('email.enabled', 'true', 'Enable email functionality'),
('notification.cleanup.days', '90', 'Days to keep notifications'),
('file.max.size', '10485760', 'Maximum file upload size in bytes'),
('session.timeout', '1800', 'Session timeout in seconds'),
('password.min.length', '8', 'Minimum password length'),
('password.require.special', 'true', 'Require special characters in password'),
('audit.retention.days', '365', 'Days to keep audit logs')
ON CONFLICT (config_key) DO NOTHING;

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_audit_log_table_name ON audit.audit_log(table_name);
CREATE INDEX IF NOT EXISTS idx_audit_log_timestamp ON audit.audit_log(timestamp);
CREATE INDEX IF NOT EXISTS idx_audit_log_user_id ON audit.audit_log(user_id);
CREATE INDEX IF NOT EXISTS idx_system_config_key ON config.system_config(config_key);

-- Create function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create trigger for system_config table
DROP TRIGGER IF EXISTS update_system_config_updated_at ON config.system_config;
CREATE TRIGGER update_system_config_updated_at
    BEFORE UPDATE ON config.system_config
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Create function for audit logging
CREATE OR REPLACE FUNCTION audit_trigger_function()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'DELETE' THEN
        INSERT INTO audit.audit_log (
            table_name, 
            operation, 
            old_values, 
            user_id, 
            username
        ) VALUES (
            TG_TABLE_NAME, 
            TG_OP, 
            row_to_json(OLD), 
            COALESCE(current_setting('app.current_user_id', true)::BIGINT, 0),
            COALESCE(current_setting('app.current_username', true), 'system')
        );
        RETURN OLD;
    ELSIF TG_OP = 'UPDATE' THEN
        INSERT INTO audit.audit_log (
            table_name, 
            operation, 
            old_values, 
            new_values, 
            user_id, 
            username
        ) VALUES (
            TG_TABLE_NAME, 
            TG_OP, 
            row_to_json(OLD), 
            row_to_json(NEW), 
            COALESCE(current_setting('app.current_user_id', true)::BIGINT, 0),
            COALESCE(current_setting('app.current_username', true), 'system')
        );
        RETURN NEW;
    ELSIF TG_OP = 'INSERT' THEN
        INSERT INTO audit.audit_log (
            table_name, 
            operation, 
            new_values, 
            user_id, 
            username
        ) VALUES (
            TG_TABLE_NAME, 
            TG_OP, 
            row_to_json(NEW), 
            COALESCE(current_setting('app.current_user_id', true)::BIGINT, 0),
            COALESCE(current_setting('app.current_username', true), 'system')
        );
        RETURN NEW;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Create cleanup function for old audit logs
CREATE OR REPLACE FUNCTION cleanup_audit_logs()
RETURNS INTEGER AS $$
DECLARE
    retention_days INTEGER;
    deleted_count INTEGER;
BEGIN
    -- Get retention period from configuration
    SELECT config_value::INTEGER INTO retention_days 
    FROM config.system_config 
    WHERE config_key = 'audit.retention.days';
    
    -- Default to 365 days if not configured
    IF retention_days IS NULL THEN
        retention_days := 365;
    END IF;
    
    -- Delete old audit logs
    DELETE FROM audit.audit_log 
    WHERE timestamp < CURRENT_TIMESTAMP - INTERVAL '1 day' * retention_days;
    
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

-- Create cleanup function for old notifications
CREATE OR REPLACE FUNCTION cleanup_old_notifications()
RETURNS INTEGER AS $$
DECLARE
    retention_days INTEGER;
    deleted_count INTEGER;
BEGIN
    -- Get retention period from configuration
    SELECT config_value::INTEGER INTO retention_days 
    FROM config.system_config 
    WHERE config_key = 'notification.cleanup.days';
    
    -- Default to 90 days if not configured
    IF retention_days IS NULL THEN
        retention_days := 90;
    END IF;
    
    -- Delete old read notifications
    DELETE FROM notifications 
    WHERE is_read = true 
    AND created_at < CURRENT_TIMESTAMP - INTERVAL '1 day' * retention_days;
    
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

-- Grant necessary permissions
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO employee_admin;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA audit TO employee_admin;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA config TO employee_admin;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO employee_admin;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA audit TO employee_admin;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA config TO employee_admin;

-- Set default privileges for future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO employee_admin;
ALTER DEFAULT PRIVILEGES IN SCHEMA audit GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO employee_admin;
ALTER DEFAULT PRIVILEGES IN SCHEMA config GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO employee_admin;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE, SELECT ON SEQUENCES TO employee_admin;
ALTER DEFAULT PRIVILEGES IN SCHEMA audit GRANT USAGE, SELECT ON SEQUENCES TO employee_admin;
ALTER DEFAULT PRIVILEGES IN SCHEMA config GRANT USAGE, SELECT ON SEQUENCES TO employee_admin;

-- Log initialization completion
INSERT INTO audit.audit_log (
    table_name, 
    operation, 
    new_values, 
    username
) VALUES (
    'system', 
    'INIT', 
    '{"message": "Database initialization completed", "timestamp": "' || CURRENT_TIMESTAMP || '"}',
    'system'
);

COMMIT;