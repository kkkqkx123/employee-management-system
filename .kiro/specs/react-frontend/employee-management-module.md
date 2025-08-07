# Employee Management Module Implementation Guide

## Overview
This document provides detailed implementation specifications for the employee management module, including employee list, forms, import/export functionality, and search capabilities.

## File Structure
```
src/features/employees/
├── components/
│   ├── EmployeeList.tsx
│   ├── EmployeeForm.tsx
│   ├── EmployeeDetail.tsx
│   ├── EmployeeImport.tsx
│   ├── EmployeeExport.tsx
│   ├── EmployeeSearch.tsx
│   └── EmployeeCard.tsx
├── hooks/
│   ├── useEmployees.ts
│   ├── useEmployeeForm.ts
│   ├── useEmployeeImport.ts
│   └── useEmployeeSearch.ts
├── services/
│   └── employeeService.ts
├── types/
│   └── employee.types.ts
├── utils/
│   ├── employeeValidation.ts
│   └── employeeExport.ts
└── index.ts
```

## Type Definitions

### employee.types.ts
```typescript
export interface Employee {
  id: number;
  employeeNumber: string;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  department: Department;
  position: Position;
  hireDate: string;
  salary?: number;
  status: EmployeeStatus;
  profilePicture?: string;
  dateOfBirth?: string;
  address?: Address;
  emergencyContact?: EmergencyContact;
  createdAt: string;
  updatedAt: string;
}

export interface Department {
  id: number;
  name: string;
  description?: string;
  parentId?: number;
}

export interface Position {
  id: number;
  title: string;
  department: Department;
  level: string;
  description?: string;
}

export interface Address {
  street: string;
  city: string;
  state: string;
  zipCode: string;
  country: string;
}

export interface EmergencyContact {
  name: string;
  relationship: string;
  phone: string;
  email?: string;
}

export enum EmployeeStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  TERMINATED = 'TERMINATED',
  ON_LEAVE = 'ON_LEAVE'
}

export interface EmployeeCreateRequest {
  employeeNumber: string;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  departmentId: number;
  positionId: number;
  hireDate: string;
  salary?: number;
  status: EmployeeStatus;
  dateOfBirth?: string;
  address?: Address;
  emergencyContact?: EmergencyContact;
}

export interface EmployeeUpdateRequest extends Partial<EmployeeCreateRequest> {
  id: number;
}

export interface EmployeeSearchCriteria {
  searchTerm?: string;
  departmentId?: number;
  positionId?: number;
  status?: EmployeeStatus;
  hireDate?: {
    from?: string;
    to?: string;
  };
}

export interface EmployeeImportResult {
  success: boolean;
  totalRows: number;
  successCount: number;
  errorCount: number;
  errors: EmployeeImportError[];
}

export interface EmployeeImportError {
  row: number;
  field: string;
  message: string;
  value: any;
}
```## Se
rvices

### employeeService.ts
```typescript
import { apiClient } from '../../services/api';
import { 
  Employee, 
  EmployeeCreateRequest, 
  EmployeeUpdateRequest, 
  EmployeeSearchCriteria,
  EmployeeImportResult 
} from '../types/employee.types';
import { PaginatedResponse } from '../../types/api.types';

export const employeeService = {
  async getEmployees(
    page: number = 0,
    size: number = 20,
    sort?: string
  ): Promise<PaginatedResponse<Employee>> {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      ...(sort && { sort })
    });
    
    const response = await apiClient.get<PaginatedResponse<Employee>>(
      `/employees?${params}`
    );
    return response.data;
  },

  async getEmployee(id: number): Promise<Employee> {
    const response = await apiClient.get<Employee>(`/employees/${id}`);
    return response.data;
  },

  async createEmployee(employee: EmployeeCreateRequest): Promise<Employee> {
    const response = await apiClient.post<Employee>('/employees', employee);
    return response.data;
  },

  async updateEmployee(employee: EmployeeUpdateRequest): Promise<Employee> {
    const response = await apiClient.put<Employee>(
      `/employees/${employee.id}`, 
      employee
    );
    return response.data;
  },

  async deleteEmployee(id: number): Promise<void> {
    await apiClient.delete(`/employees/${id}`);
  },

  async searchEmployees(
    criteria: EmployeeSearchCriteria,
    page: number = 0,
    size: number = 20
  ): Promise<PaginatedResponse<Employee>> {
    const response = await apiClient.post<PaginatedResponse<Employee>>(
      `/employees/search/advanced?page=${page}&size=${size}`,
      criteria
    );
    return response.data;
  },

  async importEmployees(file: File): Promise<EmployeeImportResult> {
    const formData = new FormData();
    formData.append('file', file);
    
    const response = await apiClient.post<EmployeeImportResult>(
      '/employees/import',
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    );
    return response.data;
  },

  async exportEmployees(criteria?: EmployeeSearchCriteria): Promise<Blob> {
    const response = await apiClient.post('/employees/export', criteria, {
      responseType: 'blob',
    });
    return response.data;
  },

  async uploadProfilePicture(employeeId: number, file: File): Promise<string> {
    const formData = new FormData();
    formData.append('file', file);
    
    const response = await apiClient.post<{ url: string }>(
      `/employees/${employeeId}/profile-picture`,
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    );
    return response.data.url;
  }
};
```

## Custom Hooks

### useEmployees.ts
```typescript
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { employeeService } from '../services/employeeService';
import { Employee, EmployeeSearchCriteria } from '../types/employee.types';
import { useState } from 'react';

export const useEmployees = (
  page: number = 0,
  size: number = 20,
  sort?: string
) => {
  const queryClient = useQueryClient();

  const employeesQuery = useQuery({
    queryKey: ['employees', page, size, sort],
    queryFn: () => employeeService.getEmployees(page, size, sort),
    keepPreviousData: true,
  });

  const deleteEmployeeMutation = useMutation({
    mutationFn: employeeService.deleteEmployee,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['employees'] });
    },
  });

  return {
    employees: employeesQuery.data?.content || [],
    totalElements: employeesQuery.data?.totalElements || 0,
    totalPages: employeesQuery.data?.totalPages || 0,
    isLoading: employeesQuery.isLoading,
    error: employeesQuery.error,
    deleteEmployee: deleteEmployeeMutation.mutate,
    isDeleting: deleteEmployeeMutation.isPending,
    refetch: employeesQuery.refetch,
  };
};

export const useEmployee = (id: number) => {
  return useQuery({
    queryKey: ['employee', id],
    queryFn: () => employeeService.getEmployee(id),
    enabled: !!id,
  });
};

export const useEmployeeSearch = () => {
  const [searchCriteria, setSearchCriteria] = useState<EmployeeSearchCriteria>({});
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);

  const searchQuery = useQuery({
    queryKey: ['employees', 'search', searchCriteria, page, size],
    queryFn: () => employeeService.searchEmployees(searchCriteria, page, size),
    enabled: Object.keys(searchCriteria).length > 0,
    keepPreviousData: true,
  });

  const search = (criteria: EmployeeSearchCriteria) => {
    setSearchCriteria(criteria);
    setPage(0);
  };

  const clearSearch = () => {
    setSearchCriteria({});
    setPage(0);
  };

  return {
    employees: searchQuery.data?.content || [],
    totalElements: searchQuery.data?.totalElements || 0,
    totalPages: searchQuery.data?.totalPages || 0,
    isLoading: searchQuery.isLoading,
    error: searchQuery.error,
    searchCriteria,
    page,
    size,
    search,
    clearSearch,
    setPage,
    setSize,
  };
};
```#
## useEmployeeForm.ts
```typescript
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { employeeService } from '../services/employeeService';
import { Employee, EmployeeCreateRequest, EmployeeUpdateRequest } from '../types/employee.types';

const employeeSchema = z.object({
  employeeNumber: z.string().min(1, 'Employee number is required'),
  firstName: z.string().min(1, 'First name is required'),
  lastName: z.string().min(1, 'Last name is required'),
  email: z.string().email('Invalid email format'),
  phone: z.string().optional(),
  departmentId: z.number().min(1, 'Department is required'),
  positionId: z.number().min(1, 'Position is required'),
  hireDate: z.string().min(1, 'Hire date is required'),
  salary: z.number().optional(),
  status: z.enum(['ACTIVE', 'INACTIVE', 'TERMINATED', 'ON_LEAVE']),
  dateOfBirth: z.string().optional(),
  address: z.object({
    street: z.string().optional(),
    city: z.string().optional(),
    state: z.string().optional(),
    zipCode: z.string().optional(),
    country: z.string().optional(),
  }).optional(),
  emergencyContact: z.object({
    name: z.string().optional(),
    relationship: z.string().optional(),
    phone: z.string().optional(),
    email: z.string().email().optional(),
  }).optional(),
});

type EmployeeFormData = z.infer<typeof employeeSchema>;

export const useEmployeeForm = (employee?: Employee) => {
  const queryClient = useQueryClient();
  const isEditing = !!employee;

  const form = useForm<EmployeeFormData>({
    resolver: zodResolver(employeeSchema),
    defaultValues: employee ? {
      employeeNumber: employee.employeeNumber,
      firstName: employee.firstName,
      lastName: employee.lastName,
      email: employee.email,
      phone: employee.phone || '',
      departmentId: employee.department.id,
      positionId: employee.position.id,
      hireDate: employee.hireDate,
      salary: employee.salary,
      status: employee.status,
      dateOfBirth: employee.dateOfBirth || '',
      address: employee.address,
      emergencyContact: employee.emergencyContact,
    } : {
      status: 'ACTIVE',
    },
  });

  const createMutation = useMutation({
    mutationFn: (data: EmployeeCreateRequest) => employeeService.createEmployee(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['employees'] });
    },
  });

  const updateMutation = useMutation({
    mutationFn: (data: EmployeeUpdateRequest) => employeeService.updateEmployee(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['employees'] });
      queryClient.invalidateQueries({ queryKey: ['employee', employee?.id] });
    },
  });

  const handleSubmit = form.handleSubmit((data) => {
    if (isEditing) {
      updateMutation.mutate({ ...data, id: employee.id });
    } else {
      createMutation.mutate(data);
    }
  });

  const mutation = isEditing ? updateMutation : createMutation;

  return {
    form,
    handleSubmit,
    isSubmitting: mutation.isPending,
    error: mutation.error,
    isSuccess: mutation.isSuccess,
    isEditing,
  };
};
```

## Components

### EmployeeList.tsx
```typescript
import React, { useState } from 'react';
import { Button, Modal, Alert } from '@mantine/core';
import { DataTable } from '../../components/ui/DataTable';
import { LoadingSpinner } from '../../components/ui/LoadingSpinner';
import { useEmployees } from '../hooks/useEmployees';
import { Employee } from '../types/employee.types';
import { EmployeeForm } from './EmployeeForm';
import { EmployeeDetail } from './EmployeeDetail';
import { EmployeeSearch } from './EmployeeSearch';

export const EmployeeList: React.FC = () => {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sort, setSort] = useState<string>();
  const [selectedEmployee, setSelectedEmployee] = useState<Employee | null>(null);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [employeeToDelete, setEmployeeToDelete] = useState<Employee | null>(null);

  const {
    employees,
    totalElements,
    totalPages,
    isLoading,
    error,
    deleteEmployee,
    isDeleting,
  } = useEmployees(page, pageSize, sort);

  const columns = [
    {
      key: 'employeeNumber',
      title: 'Employee #',
      dataIndex: 'employeeNumber',
      sortable: true,
      width: 120,
    },
    {
      key: 'name',
      title: 'Name',
      render: (_, record: Employee) => `${record.firstName} ${record.lastName}`,
      sortable: true,
    },
    {
      key: 'email',
      title: 'Email',
      dataIndex: 'email',
      sortable: true,
    },
    {
      key: 'department',
      title: 'Department',
      render: (_, record: Employee) => record.department.name,
      sortable: true,
    },
    {
      key: 'position',
      title: 'Position',
      render: (_, record: Employee) => record.position.title,
      sortable: true,
    },
    {
      key: 'status',
      title: 'Status',
      dataIndex: 'status',
      render: (status: string) => (
        <span className={`status-badge status-${status.toLowerCase()}`}>
          {status}
        </span>
      ),
      sortable: true,
      width: 100,
    },
    {
      key: 'actions',
      title: 'Actions',
      render: (_, record: Employee) => (
        <div className="action-buttons">
          <Button
            size="sm"
            variant="outline"
            onClick={() => handleViewEmployee(record)}
          >
            View
          </Button>
          <Button
            size="sm"
            variant="outline"
            onClick={() => handleEditEmployee(record)}
          >
            Edit
          </Button>
          <Button
            size="sm"
            variant="outline"
            color="red"
            onClick={() => setEmployeeToDelete(record)}
          >
            Delete
          </Button>
        </div>
      ),
      width: 200,
    },
  ];

  const handleViewEmployee = (employee: Employee) => {
    setSelectedEmployee(employee);
    setShowDetailModal(true);
  };

  const handleEditEmployee = (employee: Employee) => {
    setSelectedEmployee(employee);
    setShowCreateModal(true);
  };

  const handleDeleteEmployee = () => {
    if (employeeToDelete) {
      deleteEmployee(employeeToDelete.id);
      setEmployeeToDelete(null);
    }
  };

  const handlePageChange = (newPage: number, newPageSize: number) => {
    setPage(newPage);
    setPageSize(newPageSize);
  };

  const handleSort = (sortField: string, sortOrder: 'asc' | 'desc') => {
    setSort(`${sortField},${sortOrder}`);
  };

  if (error) {
    return (
      <Alert color="red">
        Error loading employees: {error.message}
      </Alert>
    );
  }

  return (
    <div className="employee-list">
      <div className="employee-list-header">
        <h1>Employees</h1>
        <Button onClick={() => setShowCreateModal(true)}>
          Add Employee
        </Button>
      </div>

      <EmployeeSearch />

      {isLoading ? (
        <LoadingSpinner size="lg" />
      ) : (
        <DataTable
          data={employees}
          columns={columns}
          loading={isLoading}
          pagination={{
            current: page + 1,
            pageSize,
            total: totalElements,
            onChange: handlePageChange,
            showSizeChanger: true,
            showTotal: (total, range) => 
              `${range[0]}-${range[1]} of ${total} employees`,
          }}
          onSort={handleSort}
        />
      )}

      {/* Create/Edit Modal */}
      <Modal
        isOpen={showCreateModal}
        onClose={() => {
          setShowCreateModal(false);
          setSelectedEmployee(null);
        }}
        title={selectedEmployee ? 'Edit Employee' : 'Add Employee'}
        size="lg"
      >
        <EmployeeForm
          employee={selectedEmployee}
          onSuccess={() => {
            setShowCreateModal(false);
            setSelectedEmployee(null);
          }}
        />
      </Modal>

      {/* Detail Modal */}
      <Modal
        isOpen={showDetailModal}
        onClose={() => {
          setShowDetailModal(false);
          setSelectedEmployee(null);
        }}
        title="Employee Details"
        size="lg"
      >
        {selectedEmployee && (
          <EmployeeDetail employee={selectedEmployee} />
        )}
      </Modal>

      {/* Delete Confirmation Modal */}
      <Modal
        isOpen={!!employeeToDelete}
        onClose={() => setEmployeeToDelete(null)}
        title="Confirm Delete"
        size="sm"
      >
        <div>
          <p>
            Are you sure you want to delete employee{' '}
            <strong>
              {employeeToDelete?.firstName} {employeeToDelete?.lastName}
            </strong>
            ? This action cannot be undone.
          </p>
          <div className="modal-actions">
            <Button
              variant="outline"
              onClick={() => setEmployeeToDelete(null)}
            >
              Cancel
            </Button>
            <Button
              color="red"
              loading={isDeleting}
              onClick={handleDeleteEmployee}
            >
              Delete
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
};
```