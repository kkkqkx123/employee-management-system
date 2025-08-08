// Role Detail View Component

import React, { useState } from 'react';
import {
  Card,
  Title,
  Text,
  Group,
  Badge,
  Stack,
  Tabs,
  Table,
  Avatar,
  Button,
  ActionIcon,
  Alert,
  LoadingOverlay,
  Tooltip,
  Divider,
  List,
  ThemeIcon,
} from '@mantine/core';
import {
  IconUser,
  IconShield,
  IconUsers,
  IconEdit,
  IconTrash,
  IconAlertTriangle,
  IconInfoCircle,
  IconCheck,
  IconX,
} from '@tabler/icons-react';
import { useRole } from '../hooks/useRoles';
import { useUsersByRole } from '../hooks/useUserRoles';
import { usePermissionsByCategory } from '../hooks/usePermissions';
import type { RoleWithDetails, PermissionWithDetails } from '../types';
import type { User } from '../../../types/auth';

interface RoleDetailViewProps {
  roleId: number;
  onEdit?: (role: RoleWithDetails) => void;
  onDelete?: (role: RoleWithDetails) => void;
  onUserSelect?: (user: User) => void;
  readOnly?: boolean;
}

interface PermissionListProps {
  permissions: PermissionWithDetails[];
  title: string;
  color?: string;
}

const PermissionList: React.FC<PermissionListProps> = ({ permissions, title, color = 'blue' }) => {
  const { data: permissionsByCategory } = usePermissionsByCategory();

  // Group permissions by category
  const groupedPermissions = permissions.reduce((acc, permission) => {
    const category = permission.category;
    if (!acc[category]) {
      acc[category] = [];
    }
    acc[category].push(permission);
    return acc;
  }, {} as Record<string, PermissionWithDetails[]>);

  if (permissions.length === 0) {
    return (
      <Text color="dimmed" size="sm" align="center" py="md">
        No {title.toLowerCase()} found
      </Text>
    );
  }

  return (
    <Stack spacing="md">
      {Object.entries(groupedPermissions).map(([category, categoryPermissions]) => (
        <div key={category}>
          <Text weight={500} size="sm" color="dimmed" mb="xs">
            {category}
          </Text>
          <List spacing="xs" size="sm">
            {categoryPermissions.map((permission) => (
              <List.Item
                key={permission.id}
                icon={
                  <ThemeIcon size="sm" color={color} variant="light">
                    <IconShield size={12} />
                  </ThemeIcon>
                }
              >
                <Group spacing="xs">
                  <Text weight={500}>{permission.name}</Text>
                  {permission.isSystem && (
                    <Badge size="xs" color="gray">System</Badge>
                  )}
                </Group>
                {permission.description && (
                  <Text size="xs" color="dimmed" ml="md">
                    {permission.description}
                  </Text>
                )}
              </List.Item>
            ))}
          </List>
        </div>
      ))}
    </Stack>
  );
};

export const RoleDetailView: React.FC<RoleDetailViewProps> = ({
  roleId,
  onEdit,
  onDelete,
  onUserSelect,
  readOnly = false,
}) => {
  const [activeTab, setActiveTab] = useState<string>('overview');

  const { data: role, isLoading: roleLoading, error: roleError } = useRole(roleId);
  const { users, isLoading: usersLoading } = useUsersByRole(roleId);

  if (roleError) {
    return (
      <Alert icon={<IconAlertTriangle size={16} />} title="Error" color="red">
        Failed to load role details. Please try again.
      </Alert>
    );
  }

  if (!role && !roleLoading) {
    return (
      <Alert icon={<IconInfoCircle size={16} />} title="Not Found" color="blue">
        Role not found.
      </Alert>
    );
  }

  const getRoleBadgeColor = (role: RoleWithDetails) => {
    if (role.isSystem) return 'gray';
    if (role.name.includes('ADMIN')) return 'red';
    if (role.name.includes('MANAGER')) return 'blue';
    return 'green';
  };

  return (
    <Card>
      <div style={{ position: 'relative' }}>
        <LoadingOverlay visible={roleLoading} />
        
        {role && (
          <Stack spacing="md">
            {/* Header */}
            <Group position="apart">
              <div>
                <Group spacing="sm" mb="xs">
                  <Title order={3}>{role.name}</Title>
                  <Badge
                    size="lg"
                    color={getRoleBadgeColor(role)}
                    variant="light"
                  >
                    {role.isSystem ? 'System Role' : 'Custom Role'}
                  </Badge>
                </Group>
                {role.description && (
                  <Text color="dimmed" size="sm">
                    {role.description}
                  </Text>
                )}
              </div>
              
              {!readOnly && (
                <Group spacing="xs">
                  {role.canEdit && (
                    <Button
                      variant="light"
                      leftIcon={<IconEdit size={16} />}
                      onClick={() => onEdit?.(role)}
                    >
                      Edit Role
                    </Button>
                  )}
                  {role.canDelete && !role.isSystem && (
                    <ActionIcon
                      variant="light"
                      color="red"
                      onClick={() => onDelete?.(role)}
                    >
                      <IconTrash size={16} />
                    </ActionIcon>
                  )}
                </Group>
              )}
            </Group>

            <Divider />

            {/* Quick Stats */}
            <Group spacing="xl">
              <Group spacing="xs">
                <ThemeIcon color="blue" variant="light">
                  <IconUsers size={18} />
                </ThemeIcon>
                <div>
                  <Text weight={500}>{role.userCount}</Text>
                  <Text size="xs" color="dimmed">Users</Text>
                </div>
              </Group>
              
              <Group spacing="xs">
                <ThemeIcon color="green" variant="light">
                  <IconShield size={18} />
                </ThemeIcon>
                <div>
                  <Text weight={500}>{role.permissions.length}</Text>
                  <Text size="xs" color="dimmed">Permissions</Text>
                </div>
              </Group>
              
              <Group spacing="xs">
                <ThemeIcon color="orange" variant="light">
                  <IconInfoCircle size={18} />
                </ThemeIcon>
                <div>
                  <Text weight={500}>
                    {new Date(role.createdAt).toLocaleDateString()}
                  </Text>
                  <Text size="xs" color="dimmed">Created</Text>
                </div>
              </Group>
            </Group>

            {/* Tabs */}
            <Tabs value={activeTab} onTabChange={setActiveTab}>
              <Tabs.List>
                <Tabs.Tab value="overview" icon={<IconInfoCircle size={16} />}>
                  Overview
                </Tabs.Tab>
                <Tabs.Tab value="permissions" icon={<IconShield size={16} />}>
                  Permissions ({role.permissions.length})
                </Tabs.Tab>
                <Tabs.Tab value="users" icon={<IconUsers size={16} />}>
                  Users ({role.userCount})
                </Tabs.Tab>
              </Tabs.List>

              <Tabs.Panel value="overview" pt="md">
                <Stack spacing="md">
                  <div>
                    <Text weight={500} mb="xs">Role Information</Text>
                    <Table>
                      <tbody>
                        <tr>
                          <td><Text weight={500}>Name</Text></td>
                          <td>{role.name}</td>
                        </tr>
                        <tr>
                          <td><Text weight={500}>Description</Text></td>
                          <td>{role.description || 'No description'}</td>
                        </tr>
                        <tr>
                          <td><Text weight={500}>Type</Text></td>
                          <td>
                            <Badge color={role.isSystem ? 'gray' : 'blue'}>
                              {role.isSystem ? 'System' : 'Custom'}
                            </Badge>
                          </td>
                        </tr>
                        <tr>
                          <td><Text weight={500}>Created</Text></td>
                          <td>{new Date(role.createdAt).toLocaleString()}</td>
                        </tr>
                        <tr>
                          <td><Text weight={500}>Last Updated</Text></td>
                          <td>{new Date(role.updatedAt).toLocaleString()}</td>
                        </tr>
                        <tr>
                          <td><Text weight={500}>Can Edit</Text></td>
                          <td>
                            {role.canEdit ? (
                              <IconCheck size={16} color="green" />
                            ) : (
                              <IconX size={16} color="red" />
                            )}
                          </td>
                        </tr>
                        <tr>
                          <td><Text weight={500}>Can Delete</Text></td>
                          <td>
                            {role.canDelete ? (
                              <IconCheck size={16} color="green" />
                            ) : (
                              <IconX size={16} color="red" />
                            )}
                          </td>
                        </tr>
                      </tbody>
                    </Table>
                  </div>

                  {role.isSystem && (
                    <Alert icon={<IconInfoCircle size={16} />} color="blue">
                      This is a system role that is essential for application functionality. 
                      Some operations may be restricted to maintain system integrity.
                    </Alert>
                  )}
                </Stack>
              </Tabs.Panel>

              <Tabs.Panel value="permissions" pt="md">
                <PermissionList
                  permissions={role.permissions}
                  title="Permissions"
                  color="green"
                />
              </Tabs.Panel>

              <Tabs.Panel value="users" pt="md">
                <div style={{ position: 'relative' }}>
                  <LoadingOverlay visible={usersLoading} />
                  
                  {users.length > 0 ? (
                    <Table striped highlightOnHover>
                      <thead>
                        <tr>
                          <th>User</th>
                          <th>Email</th>
                          <th>Status</th>
                        </tr>
                      </thead>
                      <tbody>
                        {users.map((user) => (
                          <tr key={user.id}>
                            <td>
                              <Group spacing="sm">
                                <Avatar size="sm" radius="xl">
                                  <IconUser size={16} />
                                </Avatar>
                                <div
                                  style={{ cursor: 'pointer' }}
                                  onClick={() => onUserSelect?.(user)}
                                >
                                  <Text weight={500}>
                                    {user.firstName} {user.lastName}
                                  </Text>
                                  <Text size="xs" color="dimmed">
                                    {user.username}
                                  </Text>
                                </div>
                              </Group>
                            </td>
                            <td>
                              <Text size="sm">{user.email}</Text>
                            </td>
                            <td>
                              <Badge
                                color={user.enabled ? 'green' : 'red'}
                                variant="light"
                              >
                                {user.enabled ? 'Active' : 'Inactive'}
                              </Badge>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </Table>
                  ) : (
                    <Text color="dimmed" size="sm" align="center" py="xl">
                      No users assigned to this role
                    </Text>
                  )}
                </div>
              </Tabs.Panel>
            </Tabs>
          </Stack>
        )}
      </div>
    </Card>
  );
};

export default RoleDetailView;