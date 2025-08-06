-- Create employees table
CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    employee_number VARCHAR(20) NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    mobile_phone VARCHAR(20),
    address VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    zip_code VARCHAR(20),
    country VARCHAR(100),
    date_of_birth_encrypted TEXT, -- Encrypted field
    gender VARCHAR(20),
    marital_status VARCHAR(20),
    nationality VARCHAR(50),
    department_id BIGINT NOT NULL,
    position_id BIGINT,
    manager_id BIGINT,
    hire_date DATE NOT NULL,
    termination_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    employment_type VARCHAR(20) NOT NULL DEFAULT 'FULL_TIME',
    pay_type VARCHAR(10) NOT NULL DEFAULT 'SALARY',
    salary DECIMAL(12,2),
    hourly_rate DECIMAL(8,2),
    bank_account_encrypted TEXT, -- Encrypted field
    tax_id_encrypted TEXT, -- Encrypted field
    enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    
    -- Foreign key constraints
    CONSTRAINT fk_employee_department FOREIGN KEY (department_id) REFERENCES departments(id),
    CONSTRAINT fk_employee_manager FOREIGN KEY (manager_id) REFERENCES employees(id),
    
    -- Check constraints
    CONSTRAINT chk_employee_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'TERMINATED', 'ON_LEAVE', 'PROBATION', 'SUSPENDED')),
    CONSTRAINT chk_employment_type CHECK (employment_type IN ('FULL_TIME', 'PART_TIME', 'CONTRACT', 'TEMPORARY', 'INTERN', 'CONSULTANT')),
    CONSTRAINT chk_pay_type CHECK (pay_type IN ('SALARY', 'HOURLY')),
    CONSTRAINT chk_gender CHECK (gender IN ('MALE', 'FEMALE', 'OTHER', 'PREFER_NOT_TO_SAY')),
    CONSTRAINT chk_marital_status CHECK (marital_status IN ('SINGLE', 'MARRIED', 'DIVORCED', 'WIDOWED', 'SEPARATED', 'DOMESTIC_PARTNERSHIP')),
    CONSTRAINT chk_salary_positive CHECK (salary IS NULL OR salary >= 0),
    CONSTRAINT chk_hourly_rate_positive CHECK (hourly_rate IS NULL OR hourly_rate >= 0),
    CONSTRAINT chk_hire_date_not_future CHECK (hire_date <= CURRENT_DATE),
    CONSTRAINT chk_termination_after_hire CHECK (termination_date IS NULL OR termination_date >= hire_date)
);

-- Create indexes for performance
CREATE INDEX idx_employee_number ON employees(employee_number);
CREATE INDEX idx_employee_email ON employees(email);
CREATE INDEX idx_employee_department_id ON employees(department_id);
CREATE INDEX idx_employee_position_id ON employees(position_id);
CREATE INDEX idx_employee_status ON employees(status);
CREATE INDEX idx_employee_last_name ON employees(last_name);
CREATE INDEX idx_employee_hire_date ON employees(hire_date);
CREATE INDEX idx_employee_manager_id ON employees(manager_id);

-- Add comments
COMMENT ON TABLE employees IS 'Employee information with comprehensive fields and audit trail';
COMMENT ON COLUMN employees.employee_number IS 'Unique employee identifier';
COMMENT ON COLUMN employees.date_of_birth_encrypted IS 'Encrypted date of birth for privacy';
COMMENT ON COLUMN employees.bank_account_encrypted IS 'Encrypted bank account information';
COMMENT ON COLUMN employees.tax_id_encrypted IS 'Encrypted tax identification number';
COMMENT ON COLUMN employees.enabled IS 'Soft delete flag - false means employee is disabled';