import { faker } from '@faker-js/faker';

export interface MockChatMessage {
  id: number;
  content: string;
  senderId: number;
  senderName: string;
  roomId: number;
  timestamp: string;
  type: 'TEXT' | 'IMAGE' | 'FILE' | 'SYSTEM';
  edited?: boolean;
  editedAt?: string;
  replyToId?: number;
}

export interface MockChatRoom {
  id: number;
  name: string;
  type: 'DIRECT' | 'GROUP' | 'DEPARTMENT';
  participants: number[];
  lastMessage?: MockChatMessage;
  unreadCount: number;
  createdAt: string;
  updatedAt: string;
}

export const createMockChatMessage = (overrides: Partial<MockChatMessage> = {}): MockChatMessage => ({
  id: faker.number.int({ min: 1, max: 1000 }),
  content: faker.lorem.sentence(),
  senderId: faker.number.int({ min: 1, max: 100 }),
  senderName: faker.person.fullName(),
  roomId: faker.number.int({ min: 1, max: 50 }),
  timestamp: faker.date.recent().toISOString(),
  type: faker.helpers.arrayElement(['TEXT', 'IMAGE', 'FILE', 'SYSTEM']),
  edited: faker.helpers.maybe(() => faker.datatype.boolean()),
  editedAt: faker.helpers.maybe(() => faker.date.recent().toISOString()),
  replyToId: faker.helpers.maybe(() => faker.number.int({ min: 1, max: 100 })),
  ...overrides,
});

export const createMockChatRoom = (overrides: Partial<MockChatRoom> = {}): MockChatRoom => {
  const lastMessage = createMockChatMessage();
  
  return {
    id: faker.number.int({ min: 1, max: 50 }),
    name: faker.helpers.arrayElement([
      'General Discussion',
      'Engineering Team',
      'Project Alpha',
      'Random',
      'HR Announcements'
    ]),
    type: faker.helpers.arrayElement(['DIRECT', 'GROUP', 'DEPARTMENT']),
    participants: Array.from({ length: faker.number.int({ min: 2, max: 10 }) }, () => 
      faker.number.int({ min: 1, max: 100 })
    ),
    lastMessage,
    unreadCount: faker.number.int({ min: 0, max: 10 }),
    createdAt: faker.date.past().toISOString(),
    updatedAt: faker.date.recent().toISOString(),
    ...overrides,
  };
};

export const createMockDirectChatRoom = (participant1: number, participant2: number): MockChatRoom => {
  return createMockChatRoom({
    type: 'DIRECT',
    participants: [participant1, participant2],
    name: `Direct Chat`,
  });
};

export const createMockGroupChatRoom = (participants: number[]): MockChatRoom => {
  return createMockChatRoom({
    type: 'GROUP',
    participants,
    name: faker.company.buzzPhrase(),
  });
};

export const createMockChatMessageList = (roomId: number, count: number = 20): MockChatMessage[] => {
  return Array.from({ length: count }, () => createMockChatMessage({ roomId }));
};

export const createMockSystemMessage = (roomId: number, content: string): MockChatMessage => {
  return createMockChatMessage({
    roomId,
    content,
    type: 'SYSTEM',
    senderName: 'System',
    senderId: 0,
  });
};