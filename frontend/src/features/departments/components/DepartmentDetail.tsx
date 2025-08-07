import React from 'react';
import { Stack, Text, Badge, Group, Divider, Card } from '@mantine/core';
import { IconUsers, IconCalendar, IconHierarchy } from '@tabler/icons-react';
import { Department } from '../types';

interface DepartmentDetailProps {
  department: Department;
}

export const DepartmentDetail: React.FC<DepartmentDetailProps> = ({ department }) => {
  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  return (
    <Stack gap="lg">
      <div>
        <Text size="xl" fw={600} mb="xs">
          {department.name}
        </Text>
        {department.description && (
          <Text c="dimmed" size="sm">
            {department.description}
          </Text>
        )}
      </div>

      <Divider />

      <Group gap="lg">
        <Card withBorder p="md" style={{ flex: 1 }}>
          <Group gap="xs" mb="xs">
            <IconUsers size={16} />
            <Text size="sm" fw={500}>
              Employees
            </Text>
          </Group>
          <Text size="lg" fw={600}>
            {department.employeeCount}
          </Text>
        </Card>

        <Card withBorder p="md" style={{ flex: 1 }}>
          <Group gap="xs" mb="xs">
            <IconHierarchy size={16} />
            <Text size="sm" fw={500}>
              Level
            </Text>
          </Group>
          <Text size="lg" fw={600}>
            {department.level}
          </Text>
        </Card>
      </Group>

      <Stack gap="sm">
        <Text size="sm" fw={500}>
          Department Path
        </Text>
        <Badge variant="light" size="lg">
          {department.path}
        </Badge>
      </Stack>

      <Divider />

      <Group gap="lg">
        <div>
          <Group gap="xs" mb="xs">
            <IconCalendar size={16} />
            <Text size="sm" fw={500}>
              Created
            </Text>
          </Group>
          <Text size="sm" c="dimmed">
            {formatDate(department.createdAt)}
          </Text>
        </div>

        <div>
          <Group gap="xs" mb="xs">
            <IconCalendar size={16} />
            <Text size="sm" fw={500}>
              Last Updated
            </Text>
          </Group>
          <Text size="sm" c="dimmed">
            {formatDate(department.updatedAt)}
          </Text>
        </div>
      </Group>

      {department.children && department.children.length > 0 && (
        <>
          <Divider />
          <div>
            <Text size="sm" fw={500} mb="xs">
              Subdepartments ({department.children.length})
            </Text>
            <Stack gap="xs">
              {department.children.map((child) => (
                <Group key={child.id} gap="xs">
                  <Badge variant="outline" size="sm">
                    {child.name}
                  </Badge>
                  <Text size="xs" c="dimmed">
                    {child.employeeCount} employees
                  </Text>
                </Group>
              ))}
            </Stack>
          </div>
        </>
      )}
    </Stack>
  );
};