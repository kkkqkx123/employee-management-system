// Employee detail view component

import React from 'react';
import {
  Stack,
  Group,
  Button,
  Text,
  Badge,
  Grid,
  Divider,
  Paper,
  Title,
  ActionIcon,
  Box,
  Card,
} from '@mantine/core';
import { IconEdit, IconMail, IconPhone, IconCalendar, IconBuilding, IconUser } from '@tabler/icons-react';
import type { Employee } from '../types';

interface EmployeeDetailProps {
  employee: Employee;
  onEdit: () => void;
  onClose: () => void;
}

export const EmployeeDetail: React.FC<EmployeeDetailProps> = ({
  employee,
  onEdit,
  onClose,
}) => {
  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ACTIVE': return 'green';
      case 'INACTIVE': return 'gray';
      case 'TERMINATED': return 'red';
      case 'ON_LEAVE': return 'blue';
      case 'PROBATION': return 'yellow';
      case 'SUSPENDED': return 'orange';
      default: return 'gray';
    }
  };

  const formatCurrency = (amount?: number) => {
    if (!amount) return 'N/A';
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(amount);
  };

  return (
    <Stack gap="lg">
      {/* Header */}
      <Group justify="space-between">
        <div>
          <Title order={2}>{employee.fullName}</Title>
          <Group gap="xs" mt="xs">
            <Badge color={getStatusColor(employee.status)} variant="light">
              {employee.status.replace('_', ' ')}
            </Badge>
            <Text c="dimmed">#{employee.employeeNumber}</Text>
          </Group>
        </div>
        <Button leftSection={<IconEdit size={16} />} onClick={onEdit}>
          Edit Employee
        </Button>
      </Group>

      <Grid>
        {/* Personal Information */}
        <Grid.Col span={6}>
          <Card withBorder>
            <Stack gap="md">
              <Group>
                <IconUser size={20} />
                <Text fw={600}>Personal Information</Text>
              </Group>
              <Divider />
              <Stack gap="xs">
                <Group justify="space-between">
                  <Text c="dimmed">Full Name:</Text>
                  <Text>{employee.fullName}</Text>
                </Group>
                <Group justify="space-between">
                  <Text c="dimmed">Email:</Text>
                  <Group gap="xs">
                    <Text>{employee.email}</Text>
                    <ActionIcon
                      size="sm"
                      variant="subtle"
                      component="a"
                      href={`mailto:${employee.email}`}
                    >
                      <IconMail size={14} />
                    </ActionIcon>
                  </Group>
                </Group>
                {employee.phone && (
                  <Group justify="space-between">
                    <Text c="dimmed">Phone:</Text>
                    <Group gap="xs">
                      <Text>{employee.phone}</Text>
                      <ActionIcon
                        size="sm"
                        variant="subtle"
                        component="a"
                        href={`tel:${employee.phone}`}
                      >
                        <IconPhone size={14} />
                      </ActionIcon>
                    </Group>
                  </Group>
                )}
                {employee.mobilePhone && (
                  <Group justify="space-between">
                    <Text c="dimmed">Mobile:</Text>
                    <Group gap="xs">
                      <Text>{employee.mobilePhone}</Text>
                      <ActionIcon
                        size="sm"
                        variant="subtle"
                        component="a"
                        href={`tel:${employee.mobilePhone}`}
                      >
                        <IconPhone size={14} />
                      </ActionIcon>
                    </Group>
                  </Group>
                )}
                {employee.dateOfBirth && (
                  <Group justify="space-between">
                    <Text c="dimmed">Date of Birth:</Text>
                    <Text>{formatDate(employee.dateOfBirth)}</Text>
                  </Group>
                )}
                {employee.gender && (
                  <Group justify="space-between">
                    <Text c="dimmed">Gender:</Text>
                    <Text>{employee.gender}</Text>
                  </Group>
                )}
                {employee.maritalStatus && (
                  <Group justify="space-between">
                    <Text c="dimmed">Marital Status:</Text>
                    <Text>{employee.maritalStatus.replace('_', ' ')}</Text>
                  </Group>
                )}
                {employee.nationality && (
                  <Group justify="space-between">
                    <Text c="dimmed">Nationality:</Text>
                    <Text>{employee.nationality}</Text>
                  </Group>
                )}
              </Stack>
            </Stack>
          </Card>
        </Grid.Col>

        {/* Employment Information */}
        <Grid.Col span={6}>
          <Card withBorder>
            <Stack gap="md">
              <Group>
                <IconBuilding size={20} />
                <Text fw={600}>Employment Information</Text>
              </Group>
              <Divider />
              <Stack gap="xs">
                <Group justify="space-between">
                  <Text c="dimmed">Department:</Text>
                  <Text>{employee.departmentName || 'N/A'}</Text>
                </Group>
                <Group justify="space-between">
                  <Text c="dimmed">Position:</Text>
                  <Text>{employee.positionName || 'N/A'}</Text>
                </Group>
                {employee.managerName && (
                  <Group justify="space-between">
                    <Text c="dimmed">Manager:</Text>
                    <Text>{employee.managerName}</Text>
                  </Group>
                )}
                <Group justify="space-between">
                  <Text c="dimmed">Hire Date:</Text>
                  <Group gap="xs">
                    <IconCalendar size={16} />
                    <Text>{formatDate(employee.hireDate)}</Text>
                  </Group>
                </Group>
                {employee.terminationDate && (
                  <Group justify="space-between">
                    <Text c="dimmed">Termination Date:</Text>
                    <Group gap="xs">
                      <IconCalendar size={16} />
                      <Text>{formatDate(employee.terminationDate)}</Text>
                    </Group>
                  </Group>
                )}
                <Group justify="space-between">
                  <Text c="dimmed">Employment Type:</Text>
                  <Text>{employee.employmentType.replace('_', ' ')}</Text>
                </Group>
                <Group justify="space-between">
                  <Text c="dimmed">Status:</Text>
                  <Badge color={getStatusColor(employee.status)} variant="light">
                    {employee.status.replace('_', ' ')}
                  </Badge>
                </Group>
              </Stack>
            </Stack>
          </Card>
        </Grid.Col>

        {/* Address Information */}
        <Grid.Col span={6}>
          <Card withBorder>
            <Stack gap="md">
              <Text fw={600}>Address Information</Text>
              <Divider />
              <Stack gap="xs">
                {employee.address && (
                  <Group justify="space-between">
                    <Text c="dimmed">Address:</Text>
                    <Text style={{ textAlign: 'right', maxWidth: '60%' }}>
                      {employee.address}
                    </Text>
                  </Group>
                )}
                {employee.city && (
                  <Group justify="space-between">
                    <Text c="dimmed">City:</Text>
                    <Text>{employee.city}</Text>
                  </Group>
                )}
                {employee.state && (
                  <Group justify="space-between">
                    <Text c="dimmed">State/Province:</Text>
                    <Text>{employee.state}</Text>
                  </Group>
                )}
                {employee.zipCode && (
                  <Group justify="space-between">
                    <Text c="dimmed">ZIP/Postal Code:</Text>
                    <Text>{employee.zipCode}</Text>
                  </Group>
                )}
                {employee.country && (
                  <Group justify="space-between">
                    <Text c="dimmed">Country:</Text>
                    <Text>{employee.country}</Text>
                  </Group>
                )}
                {!employee.address && !employee.city && !employee.state && !employee.zipCode && !employee.country && (
                  <Text c="dimmed" style={{ fontStyle: 'italic' }}>
                    No address information available
                  </Text>
                )}
              </Stack>
            </Stack>
          </Card>
        </Grid.Col>

        {/* Compensation Information */}
        <Grid.Col span={6}>
          <Card withBorder>
            <Stack gap="md">
              <Text fw={600}>Compensation Information</Text>
              <Divider />
              <Stack gap="xs">
                <Group justify="space-between">
                  <Text c="dimmed">Pay Type:</Text>
                  <Text>{employee.payType}</Text>
                </Group>
                {employee.payType === 'SALARIED' && employee.salary && (
                  <Group justify="space-between">
                    <Text c="dimmed">Annual Salary:</Text>
                    <Text fw={500}>{formatCurrency(employee.salary)}</Text>
                  </Group>
                )}
                {employee.payType === 'HOURLY' && employee.hourlyRate && (
                  <Group justify="space-between">
                    <Text c="dimmed">Hourly Rate:</Text>
                    <Text fw={500}>{formatCurrency(employee.hourlyRate)}</Text>
                  </Group>
                )}
                {employee.bankAccount && (
                  <Group justify="space-between">
                    <Text c="dimmed">Bank Account:</Text>
                    <Text>{employee.bankAccount}</Text>
                  </Group>
                )}
                {employee.taxId && (
                  <Group justify="space-between">
                    <Text c="dimmed">Tax ID:</Text>
                    <Text>{employee.taxId}</Text>
                  </Group>
                )}
              </Stack>
            </Stack>
          </Card>
        </Grid.Col>

        {/* System Information */}
        <Grid.Col span={12}>
          <Card withBorder>
            <Stack gap="md">
              <Text fw={600}>System Information</Text>
              <Divider />
              <Grid>
                <Grid.Col span={6}>
                  <Group justify="space-between">
                    <Text c="dimmed">Employee Number:</Text>
                    <Text fw={500}>{employee.employeeNumber}</Text>
                  </Group>
                </Grid.Col>
                <Grid.Col span={6}>
                  <Group justify="space-between">
                    <Text c="dimmed">Account Status:</Text>
                    <Badge color={employee.enabled ? 'green' : 'red'} variant="light">
                      {employee.enabled ? 'Enabled' : 'Disabled'}
                    </Badge>
                  </Group>
                </Grid.Col>
                <Grid.Col span={6}>
                  <Group justify="space-between">
                    <Text c="dimmed">Created:</Text>
                    <Text>{formatDate(employee.createdAt)}</Text>
                  </Group>
                </Grid.Col>
                <Grid.Col span={6}>
                  <Group justify="space-between">
                    <Text c="dimmed">Last Updated:</Text>
                    <Text>{formatDate(employee.updatedAt)}</Text>
                  </Group>
                </Grid.Col>
              </Grid>
            </Stack>
          </Card>
        </Grid.Col>
      </Grid>

      {/* Actions */}
      <Group justify="flex-end">
        <Button variant="light" onClick={onClose}>
          Close
        </Button>
        <Button leftSection={<IconEdit size={16} />} onClick={onEdit}>
          Edit Employee
        </Button>
      </Group>
    </Stack>
  );
};