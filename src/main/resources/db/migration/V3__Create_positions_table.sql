-- Create positions table for job position management
CREATE TABLE positions (
    id BIGSERIAL PRIMARY KEY,
    job_title VARCHAR(100) NOT NULL,
    professional_title VARCHAR(100),
    code VARCHAR(20) NOT NULL UNIQUE,
    description TEXT,
    requirements TEXT,
    responsibilities TEXT,
    category VARCHAR(20) NOT NULL DEFAULT 'TECHNICAL',
    salary_grade VARCHAR(10),
    department_id BIGINT NOT NULL,
    level VARCHAR(20) NOT NULL DEFAULT 'JUNIOR',
    enabled BOOLEAN NOT NULL DEFAULT true,
    min_salary DECIMAL(12,2),
    max_salary DECIMAL(12,2),
    required_skills TEXT,
    required_education VARCHAR(500),
    required_experience INTEGER,
    benefits TEXT,
    work_location VARCHAR(255),
    employment_type VARCHAR(20) NOT NULL DEFAULT 'FULL_TIME',
    is_managerial BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    
    -- Foreign key constraint to departments table
    CONSTRAINT fk_position_department FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE RESTRICT
);

-- Create indexes for performance optimization
CREATE INDEX idx_position_job_title ON positions(job_title);
CREATE INDEX idx_position_code ON positions(code);
CREATE INDEX idx_position_department_id ON positions(department_id);
CREATE INDEX idx_position_level ON positions(level);
CREATE INDEX idx_position_enabled ON positions(enabled);
CREATE INDEX idx_position_category ON positions(category);

-- Add check constraints for enum values
ALTER TABLE positions ADD CONSTRAINT chk_position_category 
    CHECK (category IN ('TECHNICAL', 'MANAGEMENT', 'ADMINISTRATIVE', 'SALES', 'HR', 'FINANCE', 'MARKETING', 'OPERATIONS', 'SUPPORT', 'OTHER'));

ALTER TABLE positions ADD CONSTRAINT chk_position_level 
    CHECK (level IN ('JUNIOR', 'MID', 'SENIOR', 'LEAD', 'MANAGER', 'DIRECTOR', 'VP', 'EXECUTIVE'));

ALTER TABLE positions ADD CONSTRAINT chk_employment_type 
    CHECK (employment_type IN ('FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERNSHIP', 'TEMPORARY'));

-- Add check constraint for salary range
ALTER TABLE positions ADD CONSTRAINT chk_salary_range 
    CHECK (min_salary IS NULL OR max_salary IS NULL OR min_salary <= max_salary);

-- Add comments for documentation
COMMENT ON TABLE positions IS 'Job positions and titles within the organization';
COMMENT ON COLUMN positions.job_title IS 'The official job title for the position';
COMMENT ON COLUMN positions.professional_title IS 'Professional or industry-standard title';
COMMENT ON COLUMN positions.code IS 'Unique position code for identification';
COMMENT ON COLUMN positions.department_id IS 'Department where this position belongs';
COMMENT ON COLUMN positions.level IS 'Hierarchical level of the position';
COMMENT ON COLUMN positions.category IS 'Functional category of the position';
COMMENT ON COLUMN positions.employment_type IS 'Type of employment (full-time, part-time, etc.)';
COMMENT ON COLUMN positions.is_managerial IS 'Whether this position has management responsibilities';