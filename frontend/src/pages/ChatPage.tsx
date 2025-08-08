import { Container } from '@mantine/core';
import { ChatInterface } from '../features/chat';

const ChatPage = () => {
  return (
    <Container size="xl" p={0}>
      <ChatInterface />
    </Container>
  );
};

export default ChatPage;