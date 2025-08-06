-- Create departments table with hierarchical structure
CREATE TABLE departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) NOT NULL UNIQUE,
    description VARCHAR(500),
    location VARCHAR(255),
    parent_id BIGINT,
    dep_path VARCHAR(500),
    is_parent BOOLEAN NOT NULL DEFAULT FALSE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    level INTEGER DEFAULT 0,
    sort_order INTEGER DEFAULT 0,
    manager_id BIGINT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    
    -- Self-referencing foreign key for hierarchy
    CONSTRAINT fk_department_parent FOREIGN KEY (parent_id) REFERENCES departments(id) ON DELETE RESTRICT,
    
    -- Check constraints
    CONSTRAINT chk_department_level CHECK (level >= 0),
    CONSTRAINT chk_department_sort_order CHECK (sort_order >= 0),
    CONSTRAINT chk_department_name_not_empty CHECK (LENGTH(TRIM(name)) > 0),
    CONSTRAINT chk_department_code_not_empty CHECK (LENGTH(TRIM(code)) > 0)
);

-- Create indexes for performance optimization
CREATE INDEX idx_department_name ON departments(name);
CREATE INDEX idx_department_code ON departments(code);
CREATE INDEX idx_department_parent_id ON departments(parent_id);
CREATE INDEX idx_department_dep_path ON departments(dep_path);
CREATE INDEX idx_department_enabled ON departments(enabled);
CREATE INDEX idx_department_manager_id ON departments(manager_id);
CREATE INDEX idx_department_level ON departments(level);
CREATE INDEX idx_department_sort_order ON departments(sort_order);

-- Composite indexes for common queries
CREATE INDEX idx_department_parent_sort ON departments(parent_id, sort_order);
CREATE INDEX idx_department_enabled_path ON departments(enabled, dep_path);

-- Insert default root departments
INSERT INTO departments (name, code, description, dep_path, is_parent, level, sort_order) VALUES
('Company', 'COMP', 'Root company department', '/COMP', TRUE, 0, 1),
('Human Resources', 'HR', 'Human Resources Department', '/COMP/HR', FALSE, 1, 1),
('Information Technology', 'IT', 'Information Technology Department', '/COMP/IT', FALSE, 1, 2),
('Finance', 'FIN', 'Finance Department', '/COMP/FIN', FALSE, 1, 3),
('Operations', 'OPS', 'Operations Department', '/COMP/OPS', FALSE, 1, 4);

-- Update parent relationships
UPDATE departments SET parent_id = (SELECT id FROM departments WHERE code = 'COMP') 
WHERE code IN ('HR', 'IT', 'FIN', 'OPS');

-- Update is_parent flag for company
UPDATE departments SET is_parent = TRUE WHERE code = 'COMP';

-- Create trigger to automatically update dep_path and level
CREATE OR REPLACE FUNCTION update_department_path()
RETURNS TRIGGER AS $$
DECLARE
    parent_path VARCHAR(500);
    parent_level INTEGER;
BEGIN
    IF NEW.parent_id IS NULL THEN
        -- Root department
        NEW.dep_path := '/' || NEW.code;
        NEW.level := 0;
        NEW.is_parent := CASE WHEN EXISTS(SELECT 1 FROM departments WHERE parent_id = NEW.id) THEN TRUE ELSE FALSE END;
    ELSE
        -- Child department
        SELECT dep_path, level INTO parent_path, parent_level
        FROM departments WHERE id = NEW.parent_id;
        
        IF parent_path IS NULL THEN
            RAISE EXCEPTION 'Parent department not found';
        END IF;
        
        NEW.dep_path := parent_path || '/' || NEW.code;
        NEW.level := parent_level + 1;
        
        -- Update parent's is_parent flag
        UPDATE departments SET is_parent = TRUE WHERE id = NEW.parent_id;
    END IF;
    
    NEW.updated_at := CURRENT_TIMESTAMP;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger
CREATE TRIGGER trigger_update_department_path
    BEFORE INSERT OR UPDATE ON departments
    FOR EACH ROW
    EXECUTE FUNCTION update_department_path();

-- Create function to update is_parent flag when departments are deleted
CREATE OR REPLACE FUNCTION update_parent_flag_on_delete()
RETURNS TRIGGER AS $$
BEGIN
    -- Update parent's is_parent flag if no more children exist
    IF OLD.parent_id IS NOT NULL THEN
        UPDATE departments 
        SET is_parent = CASE WHEN EXISTS(SELECT 1 FROM departments WHERE parent_id = OLD.parent_id AND id != OLD.id) THEN TRUE ELSE FALSE END
        WHERE id = OLD.parent_id;
    END IF;
    
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

-- Create delete trigger
CREATE TRIGGER trigger_update_parent_flag_on_delete
    AFTER DELETE ON departments
    FOR EACH ROW
    EXECUTE FUNCTION update_parent_flag_on_delete();

-- Add comments for documentation
COMMENT ON TABLE departments IS 'Hierarchical department structure with path-based organization';
COMMENT ON COLUMN departments.dep_path IS 'Full path from root to this department (e.g., /COMP/IT/DEV)';
COMMENT ON COLUMN departments.is_parent IS 'Flag indicating if this department has child departments';
COMMENT ON COLUMN departments.level IS 'Hierarchy level (0 for root departments)';
COMMENT ON COLUMN departments.sort_order IS 'Sort order within the same parent level';
COMMENT ON COLUMN departments.manager_id IS 'Employee ID of the department manager';