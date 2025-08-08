import { Container } from '@mantine/core';
import { ChatInterface } from '../features/chat';

export const ChatPage = () => {
  return (
    <Container size="xl" p={0}>
      <ChatInterface />
    </Container>
  );
};