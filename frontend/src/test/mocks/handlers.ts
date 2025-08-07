import { http, HttpResponse } from 'msw';

const API_BASE_URL = '/api';

export const handlers = [
  // Auth endpoints
  http.post(`${API_BASE_URL}/auth/login`, () => {
    return HttpResponse.json({
      success: true,
      message: 'Login successful',
      data: {
        token: 'mock-jwt-token',
        tokenType: 'Bearer',
        expiresIn: 3600,
        user: {
          id: 1,
          username: 'testuser',
          email: 'test@example.com',
          firstName: 'Test',
          lastName: 'User',
          enabled: true,
          roles: ['EMPLOYEE'],
        },
        permissions: ['USER_READ', 'EMPLOYEE_READ'],
        loginTime: new Date().toISOString(),
      },
    });
  }),

  http.post(`${API_BASE_URL}/auth/logout`, () => {
    return HttpResponse.json({
      success: true,
      message: 'Logout successful',
    });
  }),

  // Employee endpoints
  http.get(`${API_BASE_URL}/employees`, () => {
    return HttpResponse.json({
      success: true,
      message: 'Employees retrieved successfully',
      data: {
        content: [
          {
            id: 1,
            employeeNumber: 'EMP001',
            firstName: 'John',
            lastName: 'Doe',
            email: 'john.doe@company.com',
            department: 'Engineering',
            position: 'Software Developer',
            status: 'ACTIVE',
          },
        ],
        totalElements: 1,
        totalPages: 1,
        size: 20,
        number: 0,
      },
    });
  }),

  // Department endpoints
  http.get(`${API_BASE_URL}/departments/tree`, () => {
    return HttpResponse.json({
      success: true,
      message: 'Department tree retrieved successfully',
      data: [
        {
          id: 1,
          name: 'Engineering',
          description: 'Software Engineering Department',
          parentId: null,
          children: [
            {
              id: 2,
              name: 'Frontend',
              description: 'Frontend Development Team',
              parentId: 1,
              children: [],
            },
          ],
        },
      ],
    });
  }),
];