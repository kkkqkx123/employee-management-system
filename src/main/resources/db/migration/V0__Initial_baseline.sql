-- Initial baseline migration for Employee Management System
-- This migration establishes the baseline for the database schema
-- Version: V0__Initial_baseline.sql
-- Description: Creates the initial database structure baseline

-- Enable UUID extension for PostgreSQL
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create schema for application tables (optional, using public schema by default)
-- CREATE SCHEMA IF NOT EXISTS employee_management;

-- Create audit trigger function for automatic timestamp updates
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create function for generating employee numbers
CREATE OR REPLACE FUNCTION generate_employee_number()
RETURNS TEXT AS $$
DECLARE
    next_number INTEGER;
    employee_number TEXT;
BEGIN
    -- Get the next sequence number
    SELECT COALESCE(MAX(CAST(SUBSTRING(employee_number FROM 4) AS INTEGER)), 0) + 1
    INTO next_number
    FROM employees
    WHERE employee_number ~ '^EMP[0-9]{6}$';
    
    -- Format as EMP followed by 6 digits
    employee_number := 'EMP' || LPAD(next_number::TEXT, 6, '0');
    
    RETURN employee_number;
END;
$$ LANGUAGE plpgsql;

-- Create function for generating department codes
CREATE OR REPLACE FUNCTION generate_department_code()
RETURNS TEXT AS $$
DECLARE
    next_number INTEGER;
    department_code TEXT;
BEGIN
    -- Get the next sequence number
    SELECT COALESCE(MAX(CAST(SUBSTRING(code FROM 5) AS INTEGER)), 0) + 1
    INTO next_number
    FROM departments
    WHERE code ~ '^DEPT[0-9]{4}$';
    
    -- Format as DEPT followed by 4 digits
    department_code := 'DEPT' || LPAD(next_number::TEXT, 4, '0');
    
    RETURN department_code;
END;
$$ LANGUAGE plpgsql;

-- Create function for generating position codes
CREATE OR REPLACE FUNCTION generate_position_code()
RETURNS TEXT AS $$
DECLARE
    next_number INTEGER;
    position_code TEXT;
BEGIN
    -- Get the next sequence number
    SELECT COALESCE(MAX(CAST(SUBSTRING(code FROM 4) AS INTEGER)), 0) + 1
    INTO next_number
    FROM positions
    WHERE code ~ '^POS[0-9]{4}$';
    
    -- Format as POS followed by 4 digits
    position_code := 'POS' || LPAD(next_number::TEXT, 4, '0');
    
    RETURN position_code;
END;
$$ LANGUAGE plpgsql;

-- Create indexes for common query patterns (will be used by future migrations)
-- These are placeholder comments for the actual table creation in subsequent migrations

-- Baseline migration completed
-- Next migrations will create the actual table structures:
-- V1__Create_security_tables.sql - User, Role, Resource tables
-- V2__Create_departments_table.sql - Department hierarchy
-- V3__Create_positions_table.sql - Position management
-- V4__Create_employees_table.sql - Employee data
-- V5__Create_email_tables.sql - Email templates and logs
-- V6__Create_notification_tables.sql - Notification system
-- V7__Create_payroll_tables.sql - Payroll management

COMMENT ON EXTENSION "uuid-ossp" IS 'UUID generation functions for primary keys';
COMMENT ON FUNCTION update_updated_at_column() IS 'Trigger function to automatically update updated_at timestamps';
COMMENT ON FUNCTION generate_employee_number() IS 'Generates sequential employee numbers in format EMP######';
COMMENT ON FUNCTION generate_department_code() IS 'Generates sequential department codes in format DEPT####';
COMMENT ON FUNCTION generate_position_code() IS 'Generates sequential position codes in format POS####';