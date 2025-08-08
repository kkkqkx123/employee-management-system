import { faker } from '@faker-js/faker';

export interface MockEmployee {
  id: number;
  employeeNumber: string;
  firstName: string;
  lastName: string;
  email: string;
  department: string;
  position: string;
  status: 'ACTIVE' | 'INACTIVE' | 'TERMINATED';
  dateOfBirth: string;
  hireDate: string;
  phoneNumber?: string;
  address?: string;
  salary?: number;
  managerId?: number;
}

export const createMockEmployee = (overrides: Partial<MockEmployee> = {}): MockEmployee => ({
  id: faker.number.int({ min: 1, max: 1000 }),
  employeeNumber: faker.string.alphanumeric({ length: 6, casing: 'upper' }),
  firstName: faker.person.firstName(),
  lastName: faker.person.lastName(),
  email: faker.internet.email(),
  department: faker.helpers.arrayElement(['Engineering', 'HR', 'Finance', 'Marketing', 'Sales']),
  position: faker.person.jobTitle(),
  status: faker.helpers.arrayElement(['ACTIVE', 'INACTIVE', 'TERMINATED']),
  dateOfBirth: faker.date.birthdate({ min: 22, max: 65, mode: 'age' }).toISOString().split('T')[0],
  hireDate: faker.date.past({ years: 5 }).toISOString().split('T')[0],
  phoneNumber: faker.phone.number(),
  address: faker.location.streetAddress(),
  salary: faker.number.int({ min: 40000, max: 150000 }),
  managerId: faker.helpers.maybe(() => faker.number.int({ min: 1, max: 100 })),
  ...overrides,
});

export const createMockEmployeeList = (count: number = 10): MockEmployee[] => {
  return Array.from({ length: count }, () => createMockEmployee());
};

export const createMockEmployeeWithDepartment = (department: string): MockEmployee => {
  return createMockEmployee({ department });
};

export const createMockActiveEmployee = (): MockEmployee => {
  return createMockEmployee({ status: 'ACTIVE' });
};