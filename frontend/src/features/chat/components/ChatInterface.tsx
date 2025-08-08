import { useState } from 'react';
import { Grid, Paper, Box, Title, Text, Center } from '@mantine/core';
import { IconMessageCircle } from '@tabler/icons-react';
import { ConversationList } from './ConversationList';
import { MessageList } from './MessageList';
import { MessageInput } from './MessageInput';
import { useChat } from '../hooks/useChat';
import { useChatStore } from '../stores/chatStore';
import { ConnectionIndicator } from '../../../components/ui/ConnectionIndicator';
import classes from './ChatInterface.module.css';

export const ChatInterface = () => {
  const { conversations, isLoading, error } = useChat();
  const { activeConversation, setActiveConversation } = useChatStore();
  const [searchQuery, setSearchQuery] = useState('');

  const filteredConversations = conversations.filter(conv => {
    if (!searchQuery) return true;
    const searchLower = searchQuery.toLowerCase();
    return (
      conv.name?.toLowerCase().includes(searchLower) ||
      conv.participants.some(p => 
        p.firstName.toLowerCase().includes(searchLower) ||
        p.lastName.toLowerCase().includes(searchLower) ||
        p.username.toLowerCase().includes(searchLower)
      )
    );
  });

  const activeConv = conversations.find(conv => conv.id === activeConversation);

  return (
    <Box className={classes.chatContainer}>
      <ConnectionIndicator />
      
      <Grid h="100%" gutter={0}>
        {/* Conversation List */}
        <Grid.Col span={4} className={classes.conversationPanel}>
          <Paper h="100%" radius={0} withBorder>
            <ConversationList
              conversations={filteredConversations}
              activeConversationId={activeConversation}
              onConversationSelect={setActiveConversation}
              searchQuery={searchQuery}
              onSearchChange={setSearchQuery}
              isLoading={isLoading}
              error={error}
            />
          </Paper>
        </Grid.Col>

        {/* Chat Area */}
        <Grid.Col span={8} className={classes.chatPanel}>
          <Paper h="100%" radius={0} withBorder>
            {activeConversation && activeConv ? (
              <Box className={classes.chatArea}>
                {/* Chat Header */}
                <Box className={classes.chatHeader}>
                  <Title order={4}>
                    {activeConv.name || 
                     activeConv.participants
                       .map(p => `${p.firstName} ${p.lastName}`)
                       .join(', ')
                    }
                  </Title>
                  <Text size="sm" c="dimmed">
                    {activeConv.participants.length} participant{activeConv.participants.length !== 1 ? 's' : ''}
                  </Text>
                </Box>

                {/* Messages */}
                <Box className={classes.messagesContainer}>
                  <MessageList conversationId={activeConversation} />
                </Box>

                {/* Message Input */}
                <Box className={classes.messageInputContainer}>
                  <MessageInput conversationId={activeConversation} />
                </Box>
              </Box>
            ) : (
              <Center h="100%">
                <Box ta="center">
                  <IconMessageCircle size={64} stroke={1} color="var(--mantine-color-gray-4)" />
                  <Title order={3} mt="md" c="dimmed">
                    Select a conversation
                  </Title>
                  <Text c="dimmed">
                    Choose a conversation from the list to start chatting
                  </Text>
                </Box>
              </Center>
            )}
          </Paper>
        </Grid.Col>
      </Grid>
    </Box>
  );
};