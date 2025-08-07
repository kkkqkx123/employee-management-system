// Employee form component for creating and editing employees

import React, { useEffect } from 'react';
import {
  Stack,
  Group,
  Button,
  TextInput,
  Select,
  NumberInput,
  Textarea,
  Grid,
  Divider,
  Text,
  Alert,
  Box,
} from '@mantine/core';
import { DateInput } from '@mantine/dates';
import { useForm } from '@mantine/form';
import { notifications } from '@mantine/notifications';
import { IconAlertCircle } from '@tabler/icons-react';
import { useCreateEmployee, useUpdateEmployee } from '../hooks';
import type {
  Employee,
  EmployeeCreateRequest,
  EmployeeUpdateRequest,
  EmployeeStatus,
  EmploymentType,
  PayType,
  Gender,
  MaritalStatus,
} from '../types';

interface EmployeeFormProps {
  employee?: Employee;
  onSuccess: () => void;
  onCancel: () => void;
}

export const EmployeeForm: React.FC<EmployeeFormProps> = ({
  employee,
  onSuccess,
  onCancel,
}) => {
  const isEditing = !!employee;
  const createEmployeeMutation = useCreateEmployee();
  const updateEmployeeMutation = useUpdateEmployee();

  // Form setup
  const form = useForm<EmployeeCreateRequest | EmployeeUpdateRequest>({
    initialValues: {
      firstName: employee?.firstName || '',
      lastName: employee?.lastName || '',
      email: employee?.email || '',
      phone: employee?.phone || '',
      mobilePhone: employee?.mobilePhone || '',
      address: employee?.address || '',
      city: employee?.city || '',
      state: employee?.state || '',
      zipCode: employee?.zipCode || '',
      country: employee?.country || '',
      dateOfBirth: employee?.dateOfBirth || '',
      gender: employee?.gender || undefined,
      maritalStatus: employee?.maritalStatus || undefined,
      nationality: employee?.nationality || '',
      departmentId: employee?.departmentId || 0,
      positionId: employee?.positionId || 0,
      managerId: employee?.managerId || undefined,
      hireDate: employee?.hireDate || '',
      terminationDate: isEditing ? employee?.terminationDate || '' : undefined,
      status: employee?.status || 'ACTIVE',
      employmentType: employee?.employmentType || 'FULL_TIME',
      payType: employee?.payType || 'SALARIED',
      salary: employee?.salary || undefined,
      hourlyRate: employee?.hourlyRate || undefined,
      bankAccount: employee?.bankAccount || '',
      taxId: employee?.taxId || '',
      enabled: isEditing ? employee?.enabled ?? true : true,
    },
    validate: {
      firstName: (value) => {
        if (!value || value.trim().length < 2) {
          return 'First name must be at least 2 characters';
        }
        if (value.length > 50) {
          return 'First name must not exceed 50 characters';
        }
        return null;
      },
      lastName: (value) => {
        if (!value || value.trim().length < 2) {
          return 'Last name must be at least 2 characters';
        }
        if (value.length > 50) {
          return 'Last name must not exceed 50 characters';
        }
        return null;
      },
      email: (value) => {
        if (!value) {
          return 'Email is required';
        }
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(value)) {
          return 'Please enter a valid email address';
        }
        if (value.length > 100) {
          return 'Email must not exceed 100 characters';
        }
        return null;
      },
      phone: (value) => {
        if (value && !/^[+]?[0-9\s\-\(\)]{10,20}$/.test(value)) {
          return 'Please enter a valid phone number';
        }
        return null;
      },
      mobilePhone: (value) => {
        if (value && !/^[+]?[0-9\s\-\(\)]{10,20}$/.test(value)) {
          return 'Please enter a valid mobile phone number';
        }
        return null;
      },
      departmentId: (value) => {
        if (!value || value === 0) {
          return 'Department is required';
        }
        return null;
      },
      positionId: (value) => {
        if (!value || value === 0) {
          return 'Position is required';
        }
        return null;
      },
      hireDate: (value) => {
        if (!value) {
          return 'Hire date is required';
        }
        const hireDate = new Date(value);
        const today = new Date();
        if (hireDate > today) {
          return 'Hire date cannot be in the future';
        }
        return null;
      },
      salary: (value, values) => {
        if (values.payType === 'SALARIED' && (!value || value <= 0)) {
          return 'Salary is required for salaried employees';
        }
        if (value && value < 0) {
          return 'Salary must be non-negative';
        }
        return null;
      },
      hourlyRate: (value, values) => {
        if (values.payType === 'HOURLY' && (!value || value <= 0)) {
          return 'Hourly rate is required for hourly employees';
        }
        if (value && value < 0) {
          return 'Hourly rate must be non-negative';
        }
        return null;
      },
    },
  });

  // Handle form submission
  const handleSubmit = async (values: EmployeeCreateRequest | EmployeeUpdateRequest) => {
    try {
      if (isEditing && employee) {
        await updateEmployeeMutation.mutateAsync({
          id: employee.id,
          data: values as EmployeeUpdateRequest,
        });
        notifications.show({
          title: 'Success',
          message: 'Employee updated successfully',
          color: 'green',
        });
      } else {
        await createEmployeeMutation.mutateAsync(values as EmployeeCreateRequest);
        notifications.show({
          title: 'Success',
          message: 'Employee created successfully',
          color: 'green',
        });
      }
      onSuccess();
    } catch (error: any) {
      notifications.show({
        title: 'Error',
        message: error.message || `Failed to ${isEditing ? 'update' : 'create'} employee`,
        color: 'red',
      });
    }
  };

  // Watch pay type changes to show/hide relevant fields
  const payType = form.values.payType;

  const isLoading = createEmployeeMutation.isPending || updateEmployeeMutation.isPending;

  return (
    <Box>
      <form onSubmit={form.onSubmit(handleSubmit)}>
        <Stack gap="lg">
          {/* Personal Information */}
          <div>
            <Text fw={600} mb="md">Personal Information</Text>
            <Grid>
              <Grid.Col span={6}>
                <TextInput
                  label="First Name"
                  placeholder="Enter first name"
                  required
                  {...form.getInputProps('firstName')}
                />
              </Grid.Col>
              <Grid.Col span={6}>
                <TextInput
                  label="Last Name"
                  placeholder="Enter last name"
                  required
                  {...form.getInputProps('lastName')}
                />
              </Grid.Col>
              <Grid.Col span={6}>
                <TextInput
                  label="Email"
                  placeholder="Enter email address"
                  type="email"
                  required
                  {...form.getInputProps('email')}
                />
              </Grid.Col>
              <Grid.Col span={6}>
                <TextInput
                  label="Phone"
                  placeholder="Enter phone number"
                  {...form.getInputProps('phone')}
                />
              </Grid.Col>
              <Grid.Col span={6}>
                <TextInput
                  label="Mobile Phone"
                  placeholder="Enter mobile phone number"
                  {...form.getInputProps('mobilePhone')}
                />
              </Grid.Col>
              <Grid.Col span={6}>
                <DateInput
                  label="Date of Birth"
                  placeholder="Select date of birth"
                  value={form.values.dateOfBirth ? new Date(form.values.dateOfBirth) : null}
                  onChange={(date) => form.setFieldValue('dateOfBirth', date?.toISOString().split('T')[0] || '')}
                  maxDate={new Date()}
                />
              </Grid.Col>
              <Grid.Col span={6}>
                <Select
                  label="Gender"
                  placeholder="Select gender"
                  data={[
                    { value: 'MALE', label: 'Male' },
                    { value: 'FEMALE', label: 'Female' },
                    { value: 'OTHER', label: 'Other' },
                  ]}
                  {...form.getInputProps('gender')}
                />
              </Grid.Col>
              <Grid.Col span={6}>
                <Select
                  label="Marital Status"
                  placeholder="Select marital status"
                  data={[
                    { value: 'SINGLE', label: 'Single' },
                    { value: 'MARRIED', label: 'Married' },
                    { value: 'DIVORCED', label: 'Divorced' },
                    { value: 'WIDOWED', label: 'Widowed' },
                  ]}
                  {...form.getInputProps('maritalStatus')}
                />
              </Grid.Col>
              <Grid.Col span={12}>
                <TextInput
                  label="Nationality"
                  placeholder="Enter nationality"
                  {...form.getInputProps('nationality')}
                />
              </Grid.Col>
            </Grid>
          </div>

          <Divider />

          {/* Address Information */}
          <div>
            <Text fw={600} mb="md">Address Information</Text>
            <Grid>
              <Grid.Col span={12}>
                <Textarea
                  label="Address"
                  placeholder="Enter street address"
                  rows={2}
                  {...form.getInputProps('address')}
                />
              </Grid.Col>
              <Grid.Col span={6}>
                <TextInput
                  label="City"
                  placeholder="Enter city"
                  {...form.getInputProps('city')}
                />
              </Grid.Col>
              <Grid.Col span={6}>
                <TextInput
                  label="State/Province"
                  placeholder="Enter state or province"
                  {...form.getInputProps('state')}
                />
              </Grid.Col>
              <Grid.Col span={6}>
                <TextInput
                  label="ZIP/Postal Code"
                  placeholder="Enter ZIP or postal code"
                  {...form.getInputProps('zipCode')}
                />
              </Grid.Col>
              <Grid.Col span={6}>
                <TextInput
                  label="Country"
                  placeholder="Enter country"
                  {...form.getInputProps('country')}
                />
              </Grid.Col>
            </Grid>
          </div>

          <Divider />

          {/* Employment Information */}
          <div>
            <Text fw={600} mb="md">Employment Information</Text>
            <Grid>
              <Grid.Col span={6}>
                <Select
                  label="Department"
                  placeholder="Select department"
                  required
                  data={[
                    // TODO: Load from departments API
                    { value: '1', label: 'Human Resources' },
                    { value: '2', label: 'Engineering' },
                    { value: '3', label: 'Sales' },
                    { value: '4', label: 'Marketing' },
                  ]}
                  value={form.values.departmentId?.toString()}
                  onChange={(value) => form.setFieldValue('departmentId', value ? parseInt(value) : 0)}
                  error={form.errors.departmentId}
                />
              </Grid.Col>
              <Grid.Col span={6}>
                <Select
                  label="Position"
                  placeholder="Select position"
                  required
                  data={[
                    // TODO: Load from positions API
                    { value: '1', label: 'Software Engineer' },
                    { value: '2', label: 'Senior Software Engineer' },
                    { value: '3', label: 'HR Manager' },
                    { value: '4', label: 'Sales Representative' },
                  ]}
                  value={form.values.positionId?.toString()}
                  onChange={(value) => form.setFieldValue('positionId', value ? parseInt(value) : 0)}
                  error={form.errors.positionId}
                />
              </Grid.Col>
              <Grid.Col span={6}>
                <Select
                  label="Manager"
                  placeholder="Select manager"
                  data={[
                    // TODO: Load from employees API
                    { value: '1', label: 'John Doe' },
                    { value: '2', label: 'Jane Smith' },
                  ]}
                  value={form.values.managerId?.toString()}
                  onChange={(value) => form.setFieldValue('managerId', value ? parseInt(value) : undefined)}
                />
              </Grid.Col>
              <Grid.Col span={6}>
                <DateInput
                  label="Hire Date"
                  placeholder="Select hire date"
                  required
                  value={form.values.hireDate ? new Date(form.values.hireDate) : null}
                  onChange={(date) => form.setFieldValue('hireDate', date?.toISOString().split('T')[0] || '')}
                  maxDate={new Date()}
                  error={form.errors.hireDate}
                />
              </Grid.Col>
              {isEditing && (
                <Grid.Col span={6}>
                  <DateInput
                    label="Termination Date"
                    placeholder="Select termination date"
                    value={form.values.terminationDate ? new Date(form.values.terminationDate) : null}
                    onChange={(date) => form.setFieldValue('terminationDate', date?.toISOString().split('T')[0] || '')}
                  />
                </Grid.Col>
              )}
              <Grid.Col span={6}>
                <Select
                  label="Status"
                  placeholder="Select status"
                  required
                  data={[
                    { value: 'ACTIVE', label: 'Active' },
                    { value: 'INACTIVE', label: 'Inactive' },
                    { value: 'TERMINATED', label: 'Terminated' },
                    { value: 'ON_LEAVE', label: 'On Leave' },
                    { value: 'PROBATION', label: 'Probation' },
                    { value: 'SUSPENDED', label: 'Suspended' },
                  ]}
                  {...form.getInputProps('status')}
                />
              </Grid.Col>
              <Grid.Col span={6}>
                <Select
                  label="Employment Type"
                  placeholder="Select employment type"
                  required
                  data={[
                    { value: 'FULL_TIME', label: 'Full Time' },
                    { value: 'PART_TIME', label: 'Part Time' },
                    { value: 'CONTRACT', label: 'Contract' },
                    { value: 'TEMPORARY', label: 'Temporary' },
                    { value: 'INTERN', label: 'Intern' },
                  ]}
                  {...form.getInputProps('employmentType')}
                />
              </Grid.Col>
            </Grid>
          </div>

          <Divider />

          {/* Compensation Information */}
          <div>
            <Text fw={600} mb="md">Compensation Information</Text>
            <Grid>
              <Grid.Col span={6}>
                <Select
                  label="Pay Type"
                  placeholder="Select pay type"
                  required
                  data={[
                    { value: 'SALARIED', label: 'Salaried' },
                    { value: 'HOURLY', label: 'Hourly' },
                  ]}
                  {...form.getInputProps('payType')}
                />
              </Grid.Col>
              {payType === 'SALARIED' && (
                <Grid.Col span={6}>
                  <NumberInput
                    label="Annual Salary"
                    placeholder="Enter annual salary"
                    required
                    min={0}
                    decimalScale={2}
                    fixedDecimalScale
                    thousandSeparator=","
                    prefix="$"
                    {...form.getInputProps('salary')}
                  />
                </Grid.Col>
              )}
              {payType === 'HOURLY' && (
                <Grid.Col span={6}>
                  <NumberInput
                    label="Hourly Rate"
                    placeholder="Enter hourly rate"
                    required
                    min={0}
                    decimalScale={2}
                    fixedDecimalScale
                    prefix="$"
                    {...form.getInputProps('hourlyRate')}
                  />
                </Grid.Col>
              )}
            </Grid>
          </div>

          <Divider />

          {/* Sensitive Information */}
          <div>
            <Text fw={600} mb="md">Sensitive Information</Text>
            <Alert icon={<IconAlertCircle size={16} />} color="yellow" mb="md">
              This information is encrypted and only visible to authorized personnel.
            </Alert>
            <Grid>
              <Grid.Col span={6}>
                <TextInput
                  label="Bank Account"
                  placeholder="Enter bank account number"
                  {...form.getInputProps('bankAccount')}
                />
              </Grid.Col>
              <Grid.Col span={6}>
                <TextInput
                  label="Tax ID"
                  placeholder="Enter tax identification number"
                  {...form.getInputProps('taxId')}
                />
              </Grid.Col>
            </Grid>
          </div>

          {/* Form Actions */}
          <Group justify="flex-end" mt="xl">
            <Button variant="light" onClick={onCancel} disabled={isLoading}>
              Cancel
            </Button>
            <Button type="submit" loading={isLoading}>
              {isEditing ? 'Update Employee' : 'Create Employee'}
            </Button>
          </Group>
        </Stack>
      </form>
    </Box>
  );
};