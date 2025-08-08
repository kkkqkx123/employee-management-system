import { faker } from '@faker-js/faker';

export interface MockDepartment {
  id: number;
  name: string;
  description: string;
  parentId: number | null;
  children: MockDepartment[];
  employeeCount?: number;
  managerId?: number;
  budget?: number;
}

export const createMockDepartment = (overrides: Partial<MockDepartment> = {}): MockDepartment => ({
  id: faker.number.int({ min: 1, max: 1000 }),
  name: faker.helpers.arrayElement([
    'Engineering',
    'Human Resources',
    'Finance',
    'Marketing',
    'Sales',
    'Operations',
    'Customer Support',
    'Legal',
    'IT',
    'Research & Development'
  ]),
  description: faker.lorem.sentence(),
  parentId: faker.helpers.maybe(() => faker.number.int({ min: 1, max: 100 }), { probability: 0.5 }) ?? null,
  children: [],
  employeeCount: faker.number.int({ min: 1, max: 50 }),
  managerId: faker.helpers.maybe(() => faker.number.int({ min: 1, max: 100 })),
  budget: faker.number.int({ min: 100000, max: 5000000 }),
  ...overrides,
});

export const createMockDepartmentTree = (): MockDepartment[] => {
  const rootDepartments = [
    createMockDepartment({ 
      id: 1, 
      name: 'Engineering', 
      parentId: null,
      children: [
        createMockDepartment({ id: 2, name: 'Frontend', parentId: 1, children: [] }),
        createMockDepartment({ id: 3, name: 'Backend', parentId: 1, children: [] }),
        createMockDepartment({ id: 4, name: 'DevOps', parentId: 1, children: [] }),
      ]
    }),
    createMockDepartment({ 
      id: 5, 
      name: 'Human Resources', 
      parentId: null,
      children: [
        createMockDepartment({ id: 6, name: 'Recruitment', parentId: 5, children: [] }),
        createMockDepartment({ id: 7, name: 'Training', parentId: 5, children: [] }),
      ]
    }),
    createMockDepartment({ 
      id: 8, 
      name: 'Finance', 
      parentId: null,
      children: [
        createMockDepartment({ id: 9, name: 'Accounting', parentId: 8, children: [] }),
        createMockDepartment({ id: 10, name: 'Payroll', parentId: 8, children: [] }),
      ]
    }),
  ];

  return rootDepartments;
};

export const createMockRootDepartment = (): MockDepartment => {
  return createMockDepartment({ parentId: null });
};

export const createMockChildDepartment = (parentId: number): MockDepartment => {
  return createMockDepartment({ parentId });
};