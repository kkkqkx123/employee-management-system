import { faker } from '@faker-js/faker';

export interface MockUser {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  enabled: boolean;
  roles: string[];
  permissions: string[];
  lastLoginAt?: string;
  createdAt: string;
  updatedAt: string;
}

export const createMockUser = (overrides: Partial<MockUser> = {}): MockUser => ({
  id: faker.number.int({ min: 1, max: 1000 }),
  username: faker.internet.userName(),
  email: faker.internet.email(),
  firstName: faker.person.firstName(),
  lastName: faker.person.lastName(),
  enabled: faker.datatype.boolean(),
  roles: faker.helpers.arrayElements(['ADMIN', 'MANAGER', 'EMPLOYEE', 'HR'], { min: 1, max: 2 }),
  permissions: faker.helpers.arrayElements([
    'USER_READ', 'USER_WRITE', 'USER_DELETE',
    'EMPLOYEE_READ', 'EMPLOYEE_WRITE', 'EMPLOYEE_DELETE',
    'DEPARTMENT_READ', 'DEPARTMENT_WRITE', 'DEPARTMENT_DELETE',
    'PAYROLL_READ', 'PAYROLL_WRITE',
    'CHAT_READ', 'CHAT_WRITE',
    'NOTIFICATION_READ', 'NOTIFICATION_WRITE'
  ], { min: 2, max: 6 }),
  lastLoginAt: faker.helpers.maybe(() => faker.date.recent().toISOString()),
  createdAt: faker.date.past().toISOString(),
  updatedAt: faker.date.recent().toISOString(),
  ...overrides,
});

export const createMockAdminUser = (): MockUser => {
  return createMockUser({
    roles: ['ADMIN'],
    permissions: [
      'USER_READ', 'USER_WRITE', 'USER_DELETE',
      'EMPLOYEE_READ', 'EMPLOYEE_WRITE', 'EMPLOYEE_DELETE',
      'DEPARTMENT_READ', 'DEPARTMENT_WRITE', 'DEPARTMENT_DELETE',
      'PAYROLL_READ', 'PAYROLL_WRITE',
      'CHAT_READ', 'CHAT_WRITE',
      'NOTIFICATION_READ', 'NOTIFICATION_WRITE'
    ],
    enabled: true,
  });
};

export const createMockEmployeeUser = (): MockUser => {
  return createMockUser({
    roles: ['EMPLOYEE'],
    permissions: ['USER_READ', 'EMPLOYEE_READ', 'CHAT_READ', 'NOTIFICATION_READ'],
    enabled: true,
  });
};

export const createMockManagerUser = (): MockUser => {
  return createMockUser({
    roles: ['MANAGER'],
    permissions: [
      'USER_READ', 'EMPLOYEE_READ', 'EMPLOYEE_WRITE',
      'DEPARTMENT_READ', 'CHAT_READ', 'CHAT_WRITE',
      'NOTIFICATION_READ', 'NOTIFICATION_WRITE'
    ],
    enabled: true,
  });
};