-- Create notifications table (single-table model as per database-design.md)
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('SYSTEM', 'ANNOUNCEMENT', 'CHAT_MESSAGE', 'EMAIL', 'TASK_ASSIGNMENT', 'PAYROLL', 'EMPLOYEE_UPDATE', 'DEPARTMENT_UPDATE')),
    priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL' CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT')),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sender_id BIGINT,
    reference_id BIGINT,
    reference_type VARCHAR(50),
    action_url VARCHAR(500),
    metadata TEXT,
    expires_at TIMESTAMP WITH TIME ZONE
);

-- Create indexes for notifications
CREATE INDEX idx_notification_user_id ON notifications(user_id);
CREATE INDEX idx_notification_type ON notifications(type);
CREATE INDEX idx_notification_is_read ON notifications(is_read);
CREATE INDEX idx_notification_created_at ON notifications(created_at);
CREATE INDEX idx_notification_user_read ON notifications(user_id, is_read);
CREATE INDEX idx_notification_sender_id ON notifications(sender_id);
CREATE INDEX idx_notification_reference ON notifications(reference_type, reference_id);
CREATE INDEX idx_notification_expires_at ON notifications(expires_at);

-- Create announcements table
CREATE TABLE announcements (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    author_id BIGINT NOT NULL,
    target_audience VARCHAR(50) CHECK (target_audience IN ('ALL', 'DEPARTMENT', 'ROLE')),
    department_id BIGINT,
    role_name VARCHAR(100),
    publish_date DATE,
    expiry_date DATE,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL' CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);

-- Create indexes for announcements
CREATE INDEX idx_announcement_author_id ON announcements(author_id);
CREATE INDEX idx_announcement_target_audience ON announcements(target_audience);
CREATE INDEX idx_announcement_department_id ON announcements(department_id);
CREATE INDEX idx_announcement_published ON announcements(published);
CREATE INDEX idx_announcement_publish_date ON announcements(publish_date);
CREATE INDEX idx_announcement_expiry_date ON announcements(expiry_date);
CREATE INDEX idx_announcement_priority ON announcements(priority);

-- Create function to automatically create notifications for published announcements
CREATE OR REPLACE FUNCTION create_announcement_notifications()
RETURNS TRIGGER AS $$
DECLARE
    target_user_id BIGINT;
BEGIN
    -- Only create notifications when announcement is published
    IF NEW.published = TRUE AND (OLD IS NULL OR OLD.published = FALSE) THEN
        -- Create notifications based on target audience
        IF NEW.target_audience = 'ALL' THEN
            -- Create notifications for all active employees
            INSERT INTO notifications (user_id, title, content, type, priority, sender_id, reference_id, reference_type, action_url)
            SELECT 
                e.id,
                'New Announcement: ' || NEW.title,
                NEW.content,
                'ANNOUNCEMENT',
                NEW.priority::VARCHAR,
                NEW.author_id,
                NEW.id,
                'ANNOUNCEMENT',
                '/announcements/' || NEW.id
            FROM employees e 
            WHERE e.status = 'ACTIVE';
            
        ELSIF NEW.target_audience = 'DEPARTMENT' AND NEW.department_id IS NOT NULL THEN
            -- Create notifications for employees in specific department
            INSERT INTO notifications (user_id, title, content, type, priority, sender_id, reference_id, reference_type, action_url)
            SELECT 
                e.id,
                'New Announcement: ' || NEW.title,
                NEW.content,
                'ANNOUNCEMENT',
                NEW.priority::VARCHAR,
                NEW.author_id,
                NEW.id,
                'ANNOUNCEMENT',
                '/announcements/' || NEW.id
            FROM employees e 
            WHERE e.department_id = NEW.department_id AND e.status = 'ACTIVE';
            
        ELSIF NEW.target_audience = 'ROLE' AND NEW.role_name IS NOT NULL THEN
            -- Create notifications for users with specific role
            INSERT INTO notifications (user_id, title, content, type, priority, sender_id, reference_id, reference_type, action_url)
            SELECT 
                u.id,
                'New Announcement: ' || NEW.title,
                NEW.content,
                'ANNOUNCEMENT',
                NEW.priority::VARCHAR,
                NEW.author_id,
                NEW.id,
                'ANNOUNCEMENT',
                '/announcements/' || NEW.id
            FROM users u 
            JOIN user_roles ur ON u.id = ur.user_id
            JOIN roles r ON ur.role_id = r.id
            WHERE r.name = NEW.role_name AND u.enabled = TRUE;
        END IF;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger for announcement notifications
CREATE TRIGGER trigger_announcement_notifications
    AFTER INSERT OR UPDATE ON announcements
    FOR EACH ROW
    EXECUTE FUNCTION create_announcement_notifications();

-- Create trigger to automatically update updated_at for announcements
CREATE TRIGGER update_announcements_updated_at 
    BEFORE UPDATE ON announcements 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Create function to clean up expired notifications
CREATE OR REPLACE FUNCTION cleanup_expired_notifications()
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM notifications 
    WHERE expires_at IS NOT NULL AND expires_at < CURRENT_TIMESTAMP;
    
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

-- Create function to mark notification as read
CREATE OR REPLACE FUNCTION mark_notification_read(notification_id BIGINT, user_id BIGINT)
RETURNS BOOLEAN AS $$
DECLARE
    updated_count INTEGER;
BEGIN
    UPDATE notifications 
    SET is_read = TRUE, read_at = CURRENT_TIMESTAMP
    WHERE id = notification_id AND user_id = user_id AND is_read = FALSE;
    
    GET DIAGNOSTICS updated_count = ROW_COUNT;
    RETURN updated_count > 0;
END;
$$ LANGUAGE plpgsql;