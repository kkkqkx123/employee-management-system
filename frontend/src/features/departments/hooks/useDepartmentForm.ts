import React from 'react';
import { useForm } from 'react-hook-form';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { departmentService } from '../services/departmentService';
import { Department, DepartmentCreateRequest, DepartmentUpdateRequest } from '../types';

interface DepartmentFormData {
  name: string;
  description?: string;
  parentId?: number;
}

export const useDepartmentForm = (
  department?: Department | null,
  parentDepartment?: Department | null,
  onSuccess?: () => void
) => {
  const queryClient = useQueryClient();

  const form = useForm<DepartmentFormData>({
    defaultValues: {
      name: department?.name || '',
      description: department?.description || '',
      parentId: department?.parentId || parentDepartment?.id || undefined,
    },
    mode: 'onChange',
  });

  // Add validation
  React.useEffect(() => {
    form.register('name', {
      required: 'Department name is required',
      minLength: {
        value: 2,
        message: 'Department name must be at least 2 characters',
      },
    });
  }, [form]);

  const createMutation = useMutation({
    mutationFn: (data: DepartmentCreateRequest) => departmentService.createDepartment(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['departments'] });
      onSuccess?.();
    },
  });

  const updateMutation = useMutation({
    mutationFn: (data: DepartmentUpdateRequest) => departmentService.updateDepartment(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['departments'] });
      onSuccess?.();
    },
  });

  const handleSubmit = form.handleSubmit((data) => {
    if (department) {
      updateMutation.mutate({
        id: department.id,
        ...data,
      });
    } else {
      createMutation.mutate(data);
    }
  });

  return {
    form,
    handleSubmit,
    isSubmitting: createMutation.isPending || updateMutation.isPending,
    error: createMutation.error || updateMutation.error,
  };
};