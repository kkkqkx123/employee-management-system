-- Create payroll management tables
-- This migration creates tables for payroll ledgers, periods, salary components, and audit trails

-- Create payroll_periods table
CREATE TABLE payroll_periods (
    id BIGSERIAL PRIMARY KEY,
    period_name VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    period_type VARCHAR(20) NOT NULL CHECK (period_type IN ('MONTHLY', 'BI_WEEKLY', 'WEEKLY', 'CUSTOM')),
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'PROCESSING', 'CLOSED', 'CANCELLED')),
    pay_date DATE,
    description VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_payroll_period_dates CHECK (end_date >= start_date),
    CONSTRAINT chk_payroll_period_pay_date CHECK (pay_date IS NULL OR pay_date >= end_date)
);

-- Create salary_components table
CREATE TABLE salary_components (
    id BIGSERIAL PRIMARY KEY,
    component_name VARCHAR(100) NOT NULL,
    component_type VARCHAR(50) NOT NULL CHECK (component_type IN ('EARNING', 'DEDUCTION', 'TAX')),
    amount DECIMAL(15,2) NOT NULL DEFAULT 0.00 CHECK (amount >= 0),
    percentage DECIMAL(5,2) CHECK (percentage >= 0 AND percentage <= 100),
    is_taxable BOOLEAN NOT NULL DEFAULT false,
    is_mandatory BOOLEAN NOT NULL DEFAULT false,
    calculation_order INTEGER NOT NULL DEFAULT 0,
    description VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT uk_salary_component_name UNIQUE (component_name),
    CONSTRAINT chk_salary_component_amount_or_percentage CHECK (
        (amount > 0 AND percentage IS NULL) OR 
        (amount = 0 AND percentage IS NOT NULL AND percentage > 0)
    )
);

-- Create payroll_ledgers table
CREATE TABLE payroll_ledgers (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    payroll_period_id BIGINT NOT NULL,
    base_salary DECIMAL(15,2) NOT NULL CHECK (base_salary >= 0),
    gross_pay DECIMAL(15,2) DEFAULT 0.00 CHECK (gross_pay >= 0),
    total_deductions DECIMAL(15,2) DEFAULT 0.00 CHECK (total_deductions >= 0),
    total_taxes DECIMAL(15,2) DEFAULT 0.00 CHECK (total_taxes >= 0),
    net_pay DECIMAL(15,2) DEFAULT 0.00 CHECK (net_pay >= 0),
    overtime_hours DECIMAL(8,2) DEFAULT 0.00 CHECK (overtime_hours >= 0),
    overtime_pay DECIMAL(15,2) DEFAULT 0.00 CHECK (overtime_pay >= 0),
    bonus_amount DECIMAL(15,2) DEFAULT 0.00 CHECK (bonus_amount >= 0),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'CALCULATED', 'APPROVED', 'PAID', 'REJECTED', 'CANCELLED')),
    payment_method VARCHAR(20) CHECK (payment_method IN ('BANK_TRANSFER', 'CHECK', 'CASH', 'OTHER')),
    pay_date DATE,
    payment_reference VARCHAR(100),
    notes VARCHAR(1000),
    approved_by BIGINT,
    approved_at TIMESTAMP,
    paid_by BIGINT,
    paid_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    
    -- Foreign key constraints
    CONSTRAINT fk_payroll_ledger_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    CONSTRAINT fk_payroll_ledger_period FOREIGN KEY (payroll_period_id) REFERENCES payroll_periods(id) ON DELETE CASCADE,
    CONSTRAINT fk_payroll_ledger_approved_by FOREIGN KEY (approved_by) REFERENCES users(id),
    CONSTRAINT fk_payroll_ledger_paid_by FOREIGN KEY (paid_by) REFERENCES users(id),
    CONSTRAINT fk_payroll_ledger_created_by FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT fk_payroll_ledger_updated_by FOREIGN KEY (updated_by) REFERENCES users(id),
    
    -- Business constraints
    CONSTRAINT uk_payroll_ledger_employee_period UNIQUE (employee_id, payroll_period_id),
    CONSTRAINT chk_payroll_ledger_net_pay CHECK (net_pay = gross_pay - total_deductions - total_taxes),
    CONSTRAINT chk_payroll_ledger_approval CHECK (
        (status != 'APPROVED' AND approved_by IS NULL AND approved_at IS NULL) OR
        (status = 'APPROVED' AND approved_by IS NOT NULL AND approved_at IS NOT NULL)
    ),
    CONSTRAINT chk_payroll_ledger_payment CHECK (
        (status != 'PAID' AND paid_by IS NULL AND paid_at IS NULL AND payment_reference IS NULL) OR
        (status = 'PAID' AND paid_by IS NOT NULL AND paid_at IS NOT NULL)
    )
);

-- Create payroll_ledger_components table for detailed salary breakdowns
CREATE TABLE payroll_ledger_components (
    id BIGSERIAL PRIMARY KEY,
    payroll_ledger_id BIGINT NOT NULL,
    salary_component_id BIGINT NOT NULL,
    amount DECIMAL(15,2) NOT NULL CHECK (amount >= 0),
    calculated_amount DECIMAL(15,2) NOT NULL CHECK (calculated_amount >= 0),
    percentage_applied DECIMAL(5,2) CHECK (percentage_applied >= 0 AND percentage_applied <= 100),
    is_override BOOLEAN NOT NULL DEFAULT false,
    override_reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_payroll_component_ledger FOREIGN KEY (payroll_ledger_id) REFERENCES payroll_ledgers(id) ON DELETE CASCADE,
    CONSTRAINT fk_payroll_component_salary FOREIGN KEY (salary_component_id) REFERENCES salary_components(id) ON DELETE CASCADE,
    
    -- Unique constraint
    CONSTRAINT uk_payroll_component_ledger_salary UNIQUE (payroll_ledger_id, salary_component_id)
);

-- Create payroll_audits table for change tracking
CREATE TABLE payroll_audits (
    id BIGSERIAL PRIMARY KEY,
    payroll_ledger_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL CHECK (action IN ('CREATED', 'CALCULATED', 'APPROVED', 'PAID', 'REJECTED', 'CANCELLED', 'UPDATED')),
    old_status VARCHAR(20),
    new_status VARCHAR(20),
    changes TEXT, -- JSON string of changes
    reason VARCHAR(500),
    performed_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_payroll_audit_ledger FOREIGN KEY (payroll_ledger_id) REFERENCES payroll_ledgers(id) ON DELETE CASCADE,
    CONSTRAINT fk_payroll_audit_performed_by FOREIGN KEY (performed_by) REFERENCES users(id)
);

-- Create indexes for performance optimization
CREATE INDEX idx_payroll_periods_status ON payroll_periods(status);
CREATE INDEX idx_payroll_periods_dates ON payroll_periods(start_date, end_date);
CREATE INDEX idx_payroll_periods_active ON payroll_periods(is_active);

CREATE INDEX idx_salary_components_type ON salary_components(component_type);
CREATE INDEX idx_salary_components_active ON salary_components(is_active);
CREATE INDEX idx_salary_components_order ON salary_components(calculation_order);

CREATE INDEX idx_payroll_ledger_employee_id ON payroll_ledgers(employee_id);
CREATE INDEX idx_payroll_ledger_period_id ON payroll_ledgers(payroll_period_id);
CREATE INDEX idx_payroll_ledger_status ON payroll_ledgers(status);
CREATE INDEX idx_payroll_ledger_pay_date ON payroll_ledgers(pay_date);
CREATE INDEX idx_payroll_ledger_created_at ON payroll_ledgers(created_at);

CREATE INDEX idx_payroll_component_ledger_id ON payroll_ledger_components(payroll_ledger_id);
CREATE INDEX idx_payroll_component_salary_id ON payroll_ledger_components(salary_component_id);

CREATE INDEX idx_payroll_audit_ledger_id ON payroll_audits(payroll_ledger_id);
CREATE INDEX idx_payroll_audit_action ON payroll_audits(action);
CREATE INDEX idx_payroll_audit_created_at ON payroll_audits(created_at);
CREATE INDEX idx_payroll_audit_performed_by ON payroll_audits(performed_by);

-- Insert default salary components
INSERT INTO salary_components (component_name, component_type, amount, is_taxable, is_mandatory, calculation_order, description) VALUES
('Basic Salary', 'EARNING', 0.00, true, true, 1, 'Base salary component'),
('House Rent Allowance', 'EARNING', 0.00, true, false, 2, 'Housing allowance'),
('Transport Allowance', 'EARNING', 0.00, false, false, 3, 'Transportation allowance'),
('Medical Allowance', 'EARNING', 0.00, false, false, 4, 'Medical benefits allowance'),
('Performance Bonus', 'EARNING', 0.00, true, false, 5, 'Performance-based bonus'),
('Overtime Pay', 'EARNING', 0.00, true, false, 6, 'Overtime compensation'),

('Income Tax', 'TAX', 0.00, false, true, 10, 'Federal income tax'),
('Social Security Tax', 'TAX', 0.00, false, true, 11, 'Social security contribution'),
('Medicare Tax', 'TAX', 0.00, false, true, 12, 'Medicare contribution'),
('State Tax', 'TAX', 0.00, false, false, 13, 'State income tax'),

('Health Insurance', 'DEDUCTION', 0.00, false, false, 20, 'Health insurance premium'),
('Life Insurance', 'DEDUCTION', 0.00, false, false, 21, 'Life insurance premium'),
('Retirement Fund', 'DEDUCTION', 0.00, false, false, 22, 'Retirement savings contribution'),
('Union Dues', 'DEDUCTION', 0.00, false, false, 23, 'Union membership dues'),
('Loan Repayment', 'DEDUCTION', 0.00, false, false, 24, 'Employee loan repayment');

-- Add comments for documentation
COMMENT ON TABLE payroll_periods IS 'Payroll periods for organizing payroll processing cycles';
COMMENT ON TABLE salary_components IS 'Configurable salary components for earnings, deductions, and taxes';
COMMENT ON TABLE payroll_ledgers IS 'Individual employee payroll records for each pay period';
COMMENT ON TABLE payroll_ledger_components IS 'Detailed breakdown of salary components for each payroll ledger';
COMMENT ON TABLE payroll_audits IS 'Audit trail for all payroll-related changes and actions';

COMMENT ON COLUMN payroll_ledgers.net_pay IS 'Calculated as gross_pay - total_deductions - total_taxes';
COMMENT ON COLUMN payroll_audits.changes IS 'JSON string containing detailed change information';
COMMENT ON COLUMN salary_components.calculation_order IS 'Order in which components are calculated (lower numbers first)';