-- Create email templates table
CREATE TABLE email_templates (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    subject VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    template_type VARCHAR(20) NOT NULL CHECK (template_type IN ('HTML', 'TEXT', 'MIXED')),
    category VARCHAR(50) CHECK (category IN ('WELCOME', 'NOTIFICATION', 'REMINDER', 'MARKETING', 'PASSWORD_RESET')),
    description VARCHAR(500),
    variables TEXT, -- JSON string of available template variables
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by BIGINT,
    updated_by BIGINT
);

-- Create indexes for email_templates
CREATE UNIQUE INDEX idx_emailtemplate_code ON email_templates(code);
CREATE INDEX idx_emailtemplate_category ON email_templates(category);
CREATE INDEX idx_emailtemplate_enabled ON email_templates(enabled);
CREATE INDEX idx_emailtemplate_created_by ON email_templates(created_by);

-- Create email logs table
CREATE TABLE email_logs (
    id BIGSERIAL PRIMARY KEY,
    to_email VARCHAR(255) NOT NULL,
    cc_emails VARCHAR(1000), -- Comma-separated CC emails
    bcc_emails VARCHAR(1000), -- Comma-separated BCC emails
    subject VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    template_code VARCHAR(50), -- Template used (if any)
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'SENT', 'FAILED', 'BOUNCED', 'DELIVERED', 'OPENED', 'CLICKED')),
    error_message VARCHAR(2000), -- Error details if failed
    retry_count INTEGER NOT NULL DEFAULT 0,
    sent_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_by BIGINT, -- User who sent the email
    message_id VARCHAR(255), -- Email provider message ID
    priority VARCHAR(20) CHECK (priority IN ('HIGH', 'NORMAL', 'LOW')) DEFAULT 'NORMAL'
);

-- Create indexes for email_logs
CREATE INDEX idx_emaillog_to_email ON email_logs(to_email);
CREATE INDEX idx_emaillog_status ON email_logs(status);
CREATE INDEX idx_emaillog_template_code ON email_logs(template_code);
CREATE INDEX idx_emaillog_sent_by ON email_logs(sent_by);
CREATE INDEX idx_emaillog_created_at ON email_logs(created_at);
CREATE INDEX idx_emaillog_sent_at ON email_logs(sent_at);
CREATE INDEX idx_emaillog_priority ON email_logs(priority);

-- Add foreign key constraint for template_code (optional, as template might be deleted)
ALTER TABLE email_logs 
ADD CONSTRAINT fk_emaillog_template 
FOREIGN KEY (template_code) REFERENCES email_templates(code) 
ON DELETE SET NULL;

-- Add foreign key constraints for user references (assuming users table exists)
-- These will be added when the users table is available
-- ALTER TABLE email_templates ADD CONSTRAINT fk_emailtemplate_created_by FOREIGN KEY (created_by) REFERENCES users(id);
-- ALTER TABLE email_templates ADD CONSTRAINT fk_emailtemplate_updated_by FOREIGN KEY (updated_by) REFERENCES users(id);
-- ALTER TABLE email_logs ADD CONSTRAINT fk_emaillog_sent_by FOREIGN KEY (sent_by) REFERENCES users(id);

-- Insert some default email templates
INSERT INTO email_templates (name, code, subject, content, template_type, category, description, variables, is_default, enabled, created_at) VALUES
('Welcome Email', 'WELCOME_USER', 'Welcome to {{companyName}}!', 
'<html><body><h1>Welcome {{firstName}}!</h1><p>We are excited to have you join {{companyName}}. Your account has been created successfully.</p><p>Best regards,<br>The {{companyName}} Team</p></body></html>', 
'HTML', 'WELCOME', 'Default welcome email template for new users', 
'["firstName", "companyName"]', TRUE, TRUE, CURRENT_TIMESTAMP),

('Password Reset', 'PASSWORD_RESET', 'Reset Your Password', 
'<html><body><h2>Password Reset Request</h2><p>Hello {{firstName}},</p><p>You have requested to reset your password. Please click the link below to reset your password:</p><p><a href="{{resetLink}}">Reset Password</a></p><p>This link will expire in {{expirationHours}} hours.</p><p>If you did not request this, please ignore this email.</p><p>Best regards,<br>The {{companyName}} Team</p></body></html>', 
'HTML', 'PASSWORD_RESET', 'Password reset email template', 
'["firstName", "resetLink", "expirationHours", "companyName"]', TRUE, TRUE, CURRENT_TIMESTAMP),

('General Notification', 'GENERAL_NOTIFICATION', '{{subject}}', 
'<html><body><h2>{{title}}</h2><p>Hello {{firstName}},</p><p>{{message}}</p><p>Best regards,<br>The {{companyName}} Team</p></body></html>', 
'HTML', 'NOTIFICATION', 'General notification email template', 
'["firstName", "title", "subject", "message", "companyName"]', TRUE, TRUE, CURRENT_TIMESTAMP);

-- Add comments to tables
COMMENT ON TABLE email_templates IS 'Email templates for system-generated emails';
COMMENT ON TABLE email_logs IS 'Log of all emails sent by the system';

COMMENT ON COLUMN email_templates.variables IS 'JSON array of available template variables';
COMMENT ON COLUMN email_templates.is_default IS 'Whether this is the default template for its category';
COMMENT ON COLUMN email_logs.retry_count IS 'Number of times email sending was retried';
COMMENT ON COLUMN email_logs.message_id IS 'Unique message ID from email provider';