import { useState, useRef, useCallback } from 'react';
import { Box, TextInput, ActionIcon, Group, FileInput, Tooltip } from '@mantine/core';
import { IconSend, IconPaperclip, IconMoodSmile } from '@tabler/icons-react';
import { useMessages } from '../hooks/useMessages';
import { useChatWebSocket } from '../../../hooks/useWebSocket';
import { useAuth } from '../../../stores/authStore';
import classes from './MessageInput.module.css';

interface MessageInputProps {
  conversationId: string;
}

export const MessageInput = ({ conversationId }: MessageInputProps) => {
  const [message, setMessage] = useState('');
  const [attachments, setAttachments] = useState<File[]>([]);
  const [isTyping, setIsTyping] = useState(false);
  const { user } = useAuth();
  const { sendMessage, isSending } = useMessages(conversationId);
  const webSocket = useChatWebSocket();
  const typingTimeoutRef = useRef<NodeJS.Timeout>();

  const handleTyping = useCallback((value: string) => {
    setMessage(value);

    if (!isTyping && value.length > 0) {
      setIsTyping(true);
      webSocket.sendTyping(conversationId, true);
    }

    // Clear existing timeout
    if (typingTimeoutRef.current) {
      clearTimeout(typingTimeoutRef.current);
    }

    // Set new timeout to stop typing indicator
    typingTimeoutRef.current = setTimeout(() => {
      if (isTyping) {
        setIsTyping(false);
        webSocket.sendTyping(conversationId, false);
      }
    }, 1000);
  }, [conversationId, isTyping, webSocket]);

  const handleSend = useCallback(() => {
    if ((!message.trim() && attachments.length === 0) || isSending) {
      return;
    }

    // Stop typing indicator
    if (isTyping) {
      setIsTyping(false);
      webSocket.sendTyping(conversationId, false);
      if (typingTimeoutRef.current) {
        clearTimeout(typingTimeoutRef.current);
      }
    }

    // Send message
    sendMessage(message, attachments);
    
    // Clear input
    setMessage('');
    setAttachments([]);
  }, [message, attachments, isSending, isTyping, conversationId, sendMessage, webSocket]);

  const handleKeyPress = (event: React.KeyboardEvent) => {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      handleSend();
    }
  };

  const handleFileSelect = (files: File[]) => {
    setAttachments(prev => [...prev, ...files]);
  };

  const removeAttachment = (index: number) => {
    setAttachments(prev => prev.filter((_, i) => i !== index));
  };

  return (
    <Box className={classes.container}>
      {/* Attachments Preview */}
      {attachments.length > 0 && (
        <Box className={classes.attachments}>
          {attachments.map((file, index) => (
            <Box key={index} className={classes.attachment}>
              <span>{file.name}</span>
              <ActionIcon
                size="xs"
                variant="subtle"
                color="red"
                onClick={() => removeAttachment(index)}
              >
                ×
              </ActionIcon>
            </Box>
          ))}
        </Box>
      )}

      {/* Message Input */}
      <Group gap="sm" align="flex-end" wrap="nowrap">
        <FileInput
          multiple
          accept="image/*,.pdf,.doc,.docx,.txt"
          onChange={handleFileSelect}
          style={{ display: 'none' }}
          id="file-input"
        />
        
        <Tooltip label="Attach files">
          <ActionIcon
            variant="subtle"
            color="gray"
            size="lg"
            component="label"
            htmlFor="file-input"
          >
            <IconPaperclip size={20} />
          </ActionIcon>
        </Tooltip>

        <TextInput
          placeholder="Type a message..."
          value={message}
          onChange={(e) => handleTyping(e.currentTarget.value)}
          onKeyPress={handleKeyPress}
          disabled={isSending}
          style={{ flex: 1 }}
          size="md"
          radius="xl"
          classNames={{
            input: classes.messageInput
          }}
        />

        <Tooltip label="Send emoji">
          <ActionIcon
            variant="subtle"
            color="gray"
            size="lg"
            onClick={() => {
              // TODO: Implement emoji picker
              console.log('Open emoji picker');
            }}
          >
            <IconMoodSmile size={20} />
          </ActionIcon>
        </Tooltip>

        <ActionIcon
          variant="filled"
          color="blue"
          size="lg"
          onClick={handleSend}
          disabled={(!message.trim() && attachments.length === 0) || isSending}
          loading={isSending}
        >
          <IconSend size={20} />
        </ActionIcon>
      </Group>
    </Box>
  );
};