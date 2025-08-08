import { useEffect, useRef } from 'react';
import { ScrollArea, Box, Text, Center, Skeleton, Stack } from '@mantine/core';
import { MessageItem } from './MessageItem';
import { TypingIndicator } from './TypingIndicator';
import { useMessages } from '../hooks/useMessages';
import { useChatStore } from '../stores/chatStore';
import { useAuth } from '../../../stores/authStore';
import classes from './MessageList.module.css';

interface MessageListProps {
  conversationId: string;
}

export const MessageList = ({ conversationId }: MessageListProps) => {
  const { user } = useAuth();
  const { messages, isLoading, error } = useMessages(conversationId);
  const { typingUsers } = useChatStore();
  const scrollAreaRef = useRef<HTMLDivElement>(null);
  const viewport = useRef<HTMLDivElement>(null);

  // Auto-scroll to bottom when new messages arrive
  useEffect(() => {
    if (viewport.current) {
      viewport.current.scrollTo({
        top: viewport.current.scrollHeight,
        behavior: 'smooth'
      });
    }
  }, [messages]);

  // Show typing indicators for current conversation
  const currentTypingUsers = typingUsers.filter(
    tu => tu.conversationId === conversationId && tu.userId !== user?.id
  );

  if (isLoading) {
    return (
      <Box p="md">
        <Stack gap="md">
          {Array.from({ length: 5 }).map((_, index) => (
            <Box key={index} className={index % 2 === 0 ? classes.messageLeft : classes.messageRight}>
              <Skeleton height={20} width="60%" mb={4} />
              <Skeleton height={40} width="80%" />
            </Box>
          ))}
        </Stack>
      </Box>
    );
  }

  if (error) {
    return (
      <Center h="100%">
        <Text c="red" size="sm">
          {error}
        </Text>
      </Center>
    );
  }

  if (messages.length === 0) {
    return (
      <Center h="100%">
        <Box ta="center">
          <Text c="dimmed" size="sm">
            No messages yet
          </Text>
          <Text c="dimmed" size="xs" mt={4}>
            Start the conversation by sending a message
          </Text>
        </Box>
      </Center>
    );
  }

  return (
    <ScrollArea
      ref={scrollAreaRef}
      viewportRef={viewport}
      className={classes.scrollArea}
      scrollbarSize={6}
    >
      <Box className={classes.messagesList}>
        {messages.map((message, index) => {
          const previousMessage = index > 0 ? messages[index - 1] : null;
          const showSender = !previousMessage || 
            previousMessage.senderId !== message.senderId ||
            new Date(message.createdAt).getTime() - new Date(previousMessage.createdAt).getTime() > 300000; // 5 minutes

          return (
            <MessageItem
              key={message.id}
              message={message}
              isOwn={message.senderId === user?.id}
              showSender={showSender}
            />
          );
        })}

        {/* Typing Indicators */}
        {currentTypingUsers.map((typingUser) => (
          <TypingIndicator
            key={`${typingUser.userId}-${typingUser.conversationId}`}
            userName={typingUser.userName}
          />
        ))}
      </Box>
    </ScrollArea>
  );
};