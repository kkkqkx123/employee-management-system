import { faker } from '@faker-js/faker';

export interface MockNotification {
  id: number;
  title: string;
  message: string;
  type: 'INFO' | 'SUCCESS' | 'WARNING' | 'ERROR';
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
  read: boolean;
  userId: number;
  createdAt: string;
  readAt?: string;
  actionUrl?: string;
  metadata?: Record<string, any>;
}

export const createMockNotification = (overrides: Partial<MockNotification> = {}): MockNotification => ({
  id: faker.number.int({ min: 1, max: 1000 }),
  title: faker.lorem.sentence({ min: 3, max: 6 }),
  message: faker.lorem.paragraph(),
  type: faker.helpers.arrayElement(['INFO', 'SUCCESS', 'WARNING', 'ERROR']),
  priority: faker.helpers.arrayElement(['LOW', 'MEDIUM', 'HIGH', 'URGENT']),
  read: faker.datatype.boolean(),
  userId: faker.number.int({ min: 1, max: 100 }),
  createdAt: faker.date.recent().toISOString(),
  readAt: faker.helpers.maybe(() => faker.date.recent().toISOString()),
  actionUrl: faker.helpers.maybe(() => faker.internet.url()),
  metadata: faker.helpers.maybe(() => ({
    source: faker.helpers.arrayElement(['system', 'user', 'external']),
    category: faker.helpers.arrayElement(['employee', 'department', 'payroll', 'chat']),
  })),
  ...overrides,
});

export const createMockUnreadNotification = (): MockNotification => {
  return createMockNotification({ read: false, readAt: undefined });
};

export const createMockReadNotification = (): MockNotification => {
  return createMockNotification({ read: true, readAt: faker.date.recent().toISOString() });
};

export const createMockUrgentNotification = (): MockNotification => {
  return createMockNotification({ 
    priority: 'URGENT', 
    type: 'ERROR',
    title: 'Urgent: System Alert',
    read: false 
  });
};

export const createMockNotificationList = (count: number = 10): MockNotification[] => {
  return Array.from({ length: count }, () => createMockNotification());
};