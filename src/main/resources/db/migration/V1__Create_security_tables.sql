-- Create users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    enabled BOOLEAN NOT NULL DEFAULT true,
    last_login TIMESTAMP,
    login_attempts INTEGER NOT NULL DEFAULT 0,
    account_locked BOOLEAN NOT NULL DEFAULT false,
    account_locked_until TIMESTAMP,
    password_expired BOOLEAN NOT NULL DEFAULT false,
    password_change_required BOOLEAN NOT NULL DEFAULT false,
    password_changed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    
    CONSTRAINT uk_user_username UNIQUE (username),
    CONSTRAINT uk_user_email UNIQUE (email)
);

-- Create indexes for users table
CREATE INDEX idx_user_username ON users (username);
CREATE INDEX idx_user_email ON users (email);
CREATE INDEX idx_user_enabled ON users (enabled);
CREATE INDEX idx_user_account_locked ON users (account_locked);

-- Create roles table
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);

-- Create indexes for roles table
CREATE INDEX idx_role_name ON roles (name);
CREATE INDEX idx_role_active ON roles (active);

-- Create resources table
CREATE TABLE resources (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    url VARCHAR(255) NOT NULL,
    method VARCHAR(10) NOT NULL,
    description VARCHAR(255),
    category VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    
    CONSTRAINT uk_resource_url_method UNIQUE (url, method)
);

-- Create indexes for resources table
CREATE INDEX idx_resource_url ON resources (url);
CREATE INDEX idx_resource_method ON resources (method);
CREATE INDEX idx_resource_category ON resources (category);
CREATE INDEX idx_resource_active ON resources (active);

-- Create user_roles junction table
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

-- Create role_resources junction table
CREATE TABLE role_resources (
    role_id BIGINT NOT NULL,
    resource_id BIGINT NOT NULL,
    
    PRIMARY KEY (role_id, resource_id),
    CONSTRAINT fk_role_resources_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT fk_role_resources_resource FOREIGN KEY (resource_id) REFERENCES resources (id) ON DELETE CASCADE
);

-- Insert default system roles
INSERT INTO roles (name, description, active, created_at) VALUES
('ADMIN', 'System Administrator with full access', true, CURRENT_TIMESTAMP),
('HR_MANAGER', 'HR Manager with employee management access', true, CURRENT_TIMESTAMP),
('EMPLOYEE', 'Regular employee with limited access', true, CURRENT_TIMESTAMP),
('DEPARTMENT_MANAGER', 'Department Manager with team management access', true, CURRENT_TIMESTAMP);

-- Insert default system resources
INSERT INTO resources (name, url, method, description, category, active, created_at) VALUES
-- User Management
('User List', '/api/users', 'GET', 'View user list', 'USER_MANAGEMENT', true, CURRENT_TIMESTAMP),
('User Create', '/api/users', 'POST', 'Create new user', 'USER_MANAGEMENT', true, CURRENT_TIMESTAMP),
('User Update', '/api/users/*', 'PUT', 'Update user information', 'USER_MANAGEMENT', true, CURRENT_TIMESTAMP),
('User Delete', '/api/users/*', 'DELETE', 'Delete user', 'USER_MANAGEMENT', true, CURRENT_TIMESTAMP),

-- Role Management
('Role List', '/api/roles', 'GET', 'View role list', 'ROLE_MANAGEMENT', true, CURRENT_TIMESTAMP),
('Role Create', '/api/roles', 'POST', 'Create new role', 'ROLE_MANAGEMENT', true, CURRENT_TIMESTAMP),
('Role Update', '/api/roles/*', 'PUT', 'Update role information', 'ROLE_MANAGEMENT', true, CURRENT_TIMESTAMP),
('Role Delete', '/api/roles/*', 'DELETE', 'Delete role', 'ROLE_MANAGEMENT', true, CURRENT_TIMESTAMP),

-- Employee Management
('Employee List', '/api/employees', 'GET', 'View employee list', 'EMPLOYEE_MANAGEMENT', true, CURRENT_TIMESTAMP),
('Employee Create', '/api/employees', 'POST', 'Create new employee', 'EMPLOYEE_MANAGEMENT', true, CURRENT_TIMESTAMP),
('Employee Update', '/api/employees/*', 'PUT', 'Update employee information', 'EMPLOYEE_MANAGEMENT', true, CURRENT_TIMESTAMP),
('Employee Delete', '/api/employees/*', 'DELETE', 'Delete employee', 'EMPLOYEE_MANAGEMENT', true, CURRENT_TIMESTAMP),
('Employee Import', '/api/employees/import', 'POST', 'Import employees from Excel', 'EMPLOYEE_MANAGEMENT', true, CURRENT_TIMESTAMP),
('Employee Export', '/api/employees/export', 'GET', 'Export employees to Excel', 'EMPLOYEE_MANAGEMENT', true, CURRENT_TIMESTAMP),

-- Department Management
('Department List', '/api/departments', 'GET', 'View department list', 'DEPARTMENT_MANAGEMENT', true, CURRENT_TIMESTAMP),
('Department Create', '/api/departments', 'POST', 'Create new department', 'DEPARTMENT_MANAGEMENT', true, CURRENT_TIMESTAMP),
('Department Update', '/api/departments/*', 'PUT', 'Update department information', 'DEPARTMENT_MANAGEMENT', true, CURRENT_TIMESTAMP),
('Department Delete', '/api/departments/*', 'DELETE', 'Delete department', 'DEPARTMENT_MANAGEMENT', true, CURRENT_TIMESTAMP),

-- Position Management
('Position List', '/api/positions', 'GET', 'View position list', 'POSITION_MANAGEMENT', true, CURRENT_TIMESTAMP),
('Position Create', '/api/positions', 'POST', 'Create new position', 'POSITION_MANAGEMENT', true, CURRENT_TIMESTAMP),
('Position Update', '/api/positions/*', 'PUT', 'Update position information', 'POSITION_MANAGEMENT', true, CURRENT_TIMESTAMP),
('Position Delete', '/api/positions/*', 'DELETE', 'Delete position', 'POSITION_MANAGEMENT', true, CURRENT_TIMESTAMP);

-- Assign resources to ADMIN role (full access)
INSERT INTO role_resources (role_id, resource_id)
SELECT r.id, res.id
FROM roles r
CROSS JOIN resources res
WHERE r.name = 'ADMIN';

-- Assign employee management resources to HR_MANAGER role
INSERT INTO role_resources (role_id, resource_id)
SELECT r.id, res.id
FROM roles r
CROSS JOIN resources res
WHERE r.name = 'HR_MANAGER'
AND res.category IN ('EMPLOYEE_MANAGEMENT', 'DEPARTMENT_MANAGEMENT', 'POSITION_MANAGEMENT');

-- Assign limited resources to EMPLOYEE role
INSERT INTO role_resources (role_id, resource_id)
SELECT r.id, res.id
FROM roles r
CROSS JOIN resources res
WHERE r.name = 'EMPLOYEE'
AND res.method = 'GET'
AND res.category IN ('EMPLOYEE_MANAGEMENT', 'DEPARTMENT_MANAGEMENT');

-- Assign department and employee management to DEPARTMENT_MANAGER role
INSERT INTO role_resources (role_id, resource_id)
SELECT r.id, res.id
FROM roles r
CROSS JOIN resources res
WHERE r.name = 'DEPARTMENT_MANAGER'
AND res.category IN ('EMPLOYEE_MANAGEMENT', 'DEPARTMENT_MANAGEMENT');

-- Create default admin user (password: admin123 - should be changed in production)
-- Note: This is a BCrypt hash of 'admin123'
INSERT INTO users (username, password, email, first_name, last_name, enabled, created_at)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVMFvK', 'admin@company.com', 'System', 'Administrator', true, CURRENT_TIMESTAMP);

-- Assign ADMIN role to default admin user
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
CROSS JOIN roles r
WHERE u.username = 'admin' AND r.name = 'ADMIN';