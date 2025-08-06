-- Create chat_rooms table
CREATE TABLE chat_rooms (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100),
    type VARCHAR(20) NOT NULL CHECK (type IN ('DIRECT', 'GROUP', 'CHANNEL')),
    description VARCHAR(500),
    avatar_url VARCHAR(255),
    created_by BIGINT NOT NULL,
    is_private BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_message_at TIMESTAMP WITH TIME ZONE,
    last_message_id BIGINT
);

-- Create indexes for chat_rooms
CREATE INDEX idx_chatroom_type ON chat_rooms(type);
CREATE INDEX idx_chatroom_created_by ON chat_rooms(created_by);
CREATE INDEX idx_chatroom_active ON chat_rooms(is_active);
CREATE INDEX idx_chatroom_last_message_at ON chat_rooms(last_message_at);

-- Create chat_participants table
CREATE TABLE chat_participants (
    id BIGSERIAL PRIMARY KEY,
    room_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('OWNER', 'ADMIN', 'MEMBER')),
    joined_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_read_at TIMESTAMP WITH TIME ZONE,
    last_read_message_id BIGINT,
    is_muted BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    left_at TIMESTAMP WITH TIME ZONE,
    
    CONSTRAINT fk_participant_room FOREIGN KEY (room_id) REFERENCES chat_rooms(id) ON DELETE CASCADE,
    CONSTRAINT uk_participant_room_user UNIQUE (room_id, user_id)
);

-- Create indexes for chat_participants
CREATE INDEX idx_participant_room_id ON chat_participants(room_id);
CREATE INDEX idx_participant_user_id ON chat_participants(user_id);
CREATE INDEX idx_participant_active ON chat_participants(is_active);
CREATE INDEX idx_participant_role ON chat_participants(role);

-- Create chat_messages table
CREATE TABLE chat_messages (
    id BIGSERIAL PRIMARY KEY,
    room_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content TEXT,
    message_type VARCHAR(20) DEFAULT 'TEXT' CHECK (message_type IN ('TEXT', 'IMAGE', 'FILE', 'SYSTEM')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_edited BOOLEAN NOT NULL DEFAULT FALSE,
    edited_at TIMESTAMP WITH TIME ZONE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    
    CONSTRAINT fk_message_room FOREIGN KEY (room_id) REFERENCES chat_rooms(id) ON DELETE CASCADE
);

-- Create indexes for chat_messages
CREATE INDEX idx_chatmessage_room_id ON chat_messages(room_id);
CREATE INDEX idx_chatmessage_sender_id ON chat_messages(sender_id);
CREATE INDEX idx_chatmessage_created_at ON chat_messages(created_at);
CREATE INDEX idx_chatmessage_deleted ON chat_messages(is_deleted);
CREATE INDEX idx_chatmessage_room_created ON chat_messages(room_id, created_at);

-- Create composite index for efficient message queries
CREATE INDEX idx_chatmessage_room_active ON chat_messages(room_id, is_deleted, created_at);

-- Add foreign key constraint for last_message_id in chat_rooms
ALTER TABLE chat_rooms 
ADD CONSTRAINT fk_room_last_message 
FOREIGN KEY (last_message_id) REFERENCES chat_messages(id) ON DELETE SET NULL;

-- Create function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create trigger to automatically update updated_at for chat_rooms
CREATE TRIGGER update_chat_rooms_updated_at 
    BEFORE UPDATE ON chat_rooms 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();