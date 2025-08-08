// Email recipients hook

import { useState, useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { EmailApiService } from '../services/emailApi';
import type { Employee, Department } from '../../../types/entities';
import type { EmailRecipient } from '../types';

export const useEmailRecipients = () => {
  const [searchTerm, setSearchTerm] = useState('');

  // Fetch employees
  const { data: employeesResponse, isLoading: isLoadingEmployees } = useQuery({
    queryKey: ['email', 'recipients', 'employees', searchTerm],
    queryFn: async () => {
      const response = await EmailApiService.getEmployees(searchTerm || undefined);
      return response.data;
    },
    staleTime: 2 * 60 * 1000, // 2 minutes
  });

  // Fetch departments
  const { data: departments, isLoading: isLoadingDepartments } = useQuery({
    queryKey: ['email', 'recipients', 'departments'],
    queryFn: async () => {
      const response = await EmailApiService.getDepartments();
      return response.data;
    },
    staleTime: 5 * 60 * 1000, // 5 minutes
  });

  // Convert employees to recipients
  const employeeRecipients: EmailRecipient[] = useMemo(() => {
    if (!employeesResponse?.content) return [];
    
    return employeesResponse.content.map((employee: Employee) => ({
      id: `employee-${employee.id}`,
      type: 'employee' as const,
      name: `${employee.firstName} ${employee.lastName}`,
      email: employee.email,
    }));
  }, [employeesResponse]);

  // Convert departments to recipients
  const departmentRecipients: EmailRecipient[] = useMemo(() => {
    if (!departments) return [];
    
    return departments.map((department: Department) => ({
      id: `department-${department.id}`,
      type: 'department' as const,
      name: department.name,
      employeeCount: department.employeeCount,
    }));
  }, [departments]);

  // All recipients combined
  const allRecipients: EmailRecipient[] = useMemo(() => {
    return [...employeeRecipients, ...departmentRecipients];
  }, [employeeRecipients, departmentRecipients]);

  // Filtered recipients based on search
  const filteredRecipients: EmailRecipient[] = useMemo(() => {
    if (!searchTerm) return allRecipients;
    
    const lowerSearchTerm = searchTerm.toLowerCase();
    return allRecipients.filter(recipient =>
      recipient.name.toLowerCase().includes(lowerSearchTerm) ||
      recipient.email?.toLowerCase().includes(lowerSearchTerm)
    );
  }, [allRecipients, searchTerm]);

  // Get department employees
  const getDepartmentEmployees = async (departmentId: number): Promise<Employee[]> => {
    const response = await EmailApiService.getDepartmentEmployees(departmentId);
    return response.data;
  };

  // Calculate total recipient count
  const calculateRecipientCount = async (recipients: EmailRecipient[]): Promise<number> => {
    let totalCount = 0;
    
    for (const recipient of recipients) {
      if (recipient.type === 'employee') {
        totalCount += 1;
      } else if (recipient.type === 'department') {
        // For departments, we need to fetch the actual employee count
        const departmentId = parseInt(recipient.id.replace('department-', ''));
        try {
          const employees = await getDepartmentEmployees(departmentId);
          totalCount += employees.length;
        } catch (error) {
          // Fallback to the stored employee count
          totalCount += recipient.employeeCount || 0;
        }
      }
    }
    
    return totalCount;
  };

  return {
    // Search
    searchTerm,
    setSearchTerm,
    
    // Recipients
    employeeRecipients,
    departmentRecipients,
    allRecipients,
    filteredRecipients,
    
    // Loading states
    isLoading: isLoadingEmployees || isLoadingDepartments,
    isLoadingEmployees,
    isLoadingDepartments,
    
    // Utilities
    getDepartmentEmployees,
    calculateRecipientCount,
  };
};