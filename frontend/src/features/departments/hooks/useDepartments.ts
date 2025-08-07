import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { departmentService } from '../services/departmentService';
import { Department, DepartmentMoveRequest } from '../types';

export const useDepartments = () => {
  const queryClient = useQueryClient();

  const departmentsQuery = useQuery({
    queryKey: ['departments'],
    queryFn: departmentService.getDepartments,
  });

  const departmentTreeQuery = useQuery({
    queryKey: ['departments', 'tree'],
    queryFn: departmentService.getDepartmentTree,
  });

  const deleteDepartmentMutation = useMutation({
    mutationFn: departmentService.deleteDepartment,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['departments'] });
    },
  });

  const moveDepartmentMutation = useMutation({
    mutationFn: (moveRequest: DepartmentMoveRequest) => 
      departmentService.moveDepartment(moveRequest),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['departments'] });
    },
  });

  return {
    departments: departmentsQuery.data || [],
    departmentTree: departmentTreeQuery.data || [],
    isLoading: departmentsQuery.isLoading || departmentTreeQuery.isLoading,
    error: departmentsQuery.error || departmentTreeQuery.error,
    deleteDepartment: deleteDepartmentMutation.mutate,
    isDeleting: deleteDepartmentMutation.isPending,
    moveDepartment: moveDepartmentMutation.mutate,
    isMoving: moveDepartmentMutation.isPending,
    refetch: () => {
      departmentsQuery.refetch();
      departmentTreeQuery.refetch();
    },
  };
};

export const useDepartment = (id: number) => {
  return useQuery({
    queryKey: ['department', id],
    queryFn: () => departmentService.getDepartment(id),
    enabled: !!id,
  });
};