-- Employee Management System - Development Database Setup
-- Run these commands in PostgreSQL as postgres user

-- Create database
CREATE DATABASE employee_management;

-- Create user with password
CREATE USER employee_admin WITH PASSWORD 'dev_password123';

-- Grant all privileges to the user
GRANT ALL PRIVILEGES ON DATABASE employee_management TO employee_admin;

-- Connect to the specific database and grant schema privileges
\c employee_management;

-- Grant privileges on schema
GRANT ALL ON SCHEMA public TO employee_admin;

-- Grant privileges on all tables (will be applied to future tables too)
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO employee_admin;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO employee_admin;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON FUNCTIONS TO employee_admin;

-- Verify setup
SELECT 
    datname as database_name,
    rolname as owner
FROM pg_database 
WHERE datname = 'employee_management';

SELECT 
    rolname as username,
    rolsuper as is_superuser
FROM pg_roles 
WHERE rolname = 'employee_admin';