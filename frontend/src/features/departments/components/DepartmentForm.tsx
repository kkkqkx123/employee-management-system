import React from 'react';
import { Button, TextInput, Textarea, Select, Stack, Alert } from '@mantine/core';
import { useDepartmentForm } from '../hooks/useDepartmentForm';
import { useDepartments } from '../hooks/useDepartments';
import { Department } from '../types';

interface DepartmentFormProps {
  department?: Department | null;
  parentDepartment?: Department | null;
  onSuccess?: () => void;
}

export const DepartmentForm: React.FC<DepartmentFormProps> = ({
  department,
  parentDepartment,
  onSuccess,
}) => {
  const { departments } = useDepartments();
  const { form, handleSubmit, isSubmitting, error } = useDepartmentForm(
    department,
    parentDepartment,
    onSuccess
  );

  // Filter out current department and its descendants from parent options
  const getParentOptions = () => {
    if (!department) {
      return departments.map(dept => ({
        value: dept.id.toString(),
        label: dept.name,
      }));
    }

    // For editing, exclude the department itself and its descendants
    const excludeIds = new Set([department.id]);
    
    // Add descendants to exclude list
    const addDescendants = (deptId: number) => {
      departments
        .filter(d => d.parentId === deptId)
        .forEach(child => {
          excludeIds.add(child.id);
          addDescendants(child.id);
        });
    };
    
    addDescendants(department.id);

    return departments
      .filter(dept => !excludeIds.has(dept.id))
      .map(dept => ({
        value: dept.id.toString(),
        label: dept.name,
      }));
  };

  const parentOptions = getParentOptions();

  return (
    <form onSubmit={handleSubmit}>
      <Stack gap="md">
        {error && (
          <Alert color="red">
            {error.message || 'An error occurred while saving the department'}
          </Alert>
        )}

        <TextInput
          label="Department Name"
          placeholder="Enter department name"
          required
          value={form.watch('name')}
          onChange={(e) => form.setValue('name', e.target.value)}
          error={form.formState.errors.name?.message}
        />

        <Textarea
          label="Description"
          placeholder="Enter department description (optional)"
          rows={3}
          value={form.watch('description') || ''}
          onChange={(e) => form.setValue('description', e.target.value)}
          error={form.formState.errors.description?.message}
        />

        <Select
          label="Parent Department"
          placeholder={parentDepartment ? parentDepartment.name : "Select parent department (optional)"}
          data={parentOptions}
          value={form.watch('parentId')?.toString() || ''}
          onChange={(value) => {
            form.setValue('parentId', value ? parseInt(value) : undefined);
          }}
          clearable
          disabled={!!parentDepartment}
        />

        <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end' }}>
          <Button
            type="submit"
            loading={isSubmitting}
            disabled={!form.formState.isValid}
          >
            {department ? 'Update Department' : 'Create Department'}
          </Button>
        </div>
      </Stack>
    </form>
  );
};