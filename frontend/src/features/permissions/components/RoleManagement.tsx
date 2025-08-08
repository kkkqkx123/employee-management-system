// Role Management Component

import React, { useState } from 'react';
import {
  Card,
  Title,
  Group,
  Button,
  Table,
  Text,
  Badge,
  ActionIcon,
  Modal,
  TextInput,
  Textarea,
  MultiSelect,
  Stack,
  Alert,
  LoadingOverlay,
  Tooltip,
  Pagination,
  Menu,
} from '@mantine/core';
import { useForm } from '@mantine/form';
import { useDisclosure } from '@mantine/hooks';
import { modals } from '@mantine/modals';
import {
  IconPlus,
  IconEdit,
  IconTrash,
  IconAlertTriangle,
  IconInfoCircle,
  IconDots,
  IconUsers,
  IconShield,
} from '@tabler/icons-react';
import {
  useRoles,
  useCreateRole,
  useUpdateRole,
  useDeleteRole,
  useRoleImpactAnalysis,
} from '../hooks/useRoles';
import { usePermissionsByCategory } from '../hooks/usePermissions';
import type { RoleWithDetails, RoleCreateRequest, RoleUpdateRequest } from '../types';

interface RoleFormModalProps {
  role?: RoleWithDetails;
  opened: boolean;
  onClose: () => void;
  onSubmit: (data: RoleCreateRequest | RoleUpdateRequest) => void;
  isLoading?: boolean;
}

const RoleFormModal: React.FC<RoleFormModalProps> = ({
  role,
  opened,
  onClose,
  onSubmit,
  isLoading = false,
}) => {
  const { data: permissionsByCategory } = usePermissionsByCategory();
  const isEditing = !!role;

  const form = useForm({
    initialValues: {
      name: role?.name || '',
      description: role?.description || '',
      permissionIds: role?.permissions.map(p => p.id.toString()) || [],
    },
    validate: {
      name: (value) => (!value ? 'Role name is required' : null),
      permissionIds: (value) => (value.length === 0 ? 'At least one permission is required' : null),
    },
  });

  // Create permission options grouped by category
  const permissionOptions = permissionsByCategory
    ? Object.entries(permissionsByCategory).flatMap(([category, permissions]) =>
        permissions.map(permission => ({
          value: permission.id.toString(),
          label: `${permission.name} - ${permission.description}`,
          group: category,
          disabled: permission.isSystem && !role?.permissions.some(p => p.id === permission.id),
        }))
      )
    : [];

  const handleSubmit = (values: typeof form.values) => {
    const data = {
      ...values,
      permissionIds: values.permissionIds.map(id => parseInt(id)),
    };

    if (isEditing && role) {
      onSubmit({ ...data, id: role.id } as RoleUpdateRequest);
    } else {
      onSubmit(data as RoleCreateRequest);
    }
  };

  const handleClose = () => {
    form.reset();
    onClose();
  };

  return (
    <Modal
      opened={opened}
      onClose={handleClose}
      title={isEditing ? `Edit Role: ${role?.name}` : 'Create New Role'}
      size="lg"
    >
      <form onSubmit={form.onSubmit(handleSubmit)}>
        <Stack spacing="md">
          <TextInput
            label="Role Name"
            placeholder="Enter role name"
            required
            {...form.getInputProps('name')}
            disabled={role?.isSystem}
          />

          <Textarea
            label="Description"
            placeholder="Enter role description"
            minRows={3}
            {...form.getInputProps('description')}
          />

          <MultiSelect
            label="Permissions"
            placeholder="Select permissions for this role"
            data={permissionOptions}
            searchable
            clearable
            required
            maxDropdownHeight={300}
            {...form.getInputProps('permissionIds')}
          />

          {role?.isSystem && (
            <Alert icon={<IconInfoCircle size={16} />} color="blue">
              This is a system role. Some properties cannot be modified.
            </Alert>
          )}

          <Group position="right" spacing="sm">
            <Button variant="light" onClick={handleClose}>
              Cancel
            </Button>
            <Button type="submit" loading={isLoading} leftIcon={<IconShield size={16} />}>
              {isEditing ? 'Update Role' : 'Create Role'}
            </Button>
          </Group>
        </Stack>
      </form>
    </Modal>
  );
};

interface RoleManagementProps {
  onRoleSelect?: (role: RoleWithDetails) => void;
}

export const RoleManagement: React.FC<RoleManagementProps> = ({ onRoleSelect }) => {
  const [page, setPage] = useState(1);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedRole, setSelectedRole] = useState<RoleWithDetails | null>(null);
  const [modalOpened, { open: openModal, close: closeModal }] = useDisclosure(false);

  const pageSize = 20;

  const searchParams = {
    page: page - 1,
    size: pageSize,
    ...(searchTerm && { name: searchTerm }),
  };

  const { data: rolesData, isLoading, error } = useRoles(searchParams);
  const createRoleMutation = useCreateRole();
  const updateRoleMutation = useUpdateRole();
  const deleteRoleMutation = useDeleteRole();

  const roles = rolesData?.content || [];
  const totalPages = rolesData?.totalPages || 1;

  const handleCreateRole = () => {
    setSelectedRole(null);
    openModal();
  };

  const handleEditRole = (role: RoleWithDetails) => {
    setSelectedRole(role);
    openModal();
  };

  const handleDeleteRole = (role: RoleWithDetails) => {
    modals.openConfirmModal({
      title: 'Delete Role',
      children: (
        <Stack spacing="sm">
          <Text>
            Are you sure you want to delete the role "{role.name}"?
          </Text>
          {role.userCount > 0 && (
            <Alert icon={<IconAlertTriangle size={16} />} color="orange">
              This role is assigned to {role.userCount} user(s). 
              Deleting it will remove the role from all users.
            </Alert>
          )}
          <Text size="sm" color="dimmed">
            This action cannot be undone.
          </Text>
        </Stack>
      ),
      labels: { confirm: 'Delete Role', cancel: 'Cancel' },
      confirmProps: { color: 'red' },
      onConfirm: () => deleteRoleMutation.mutate(role.id),
    });
  };

  const handleSubmitRole = async (data: RoleCreateRequest | RoleUpdateRequest) => {
    try {
      if ('id' in data) {
        await updateRoleMutation.mutateAsync(data);
      } else {
        await createRoleMutation.mutateAsync(data);
      }
      closeModal();
      setSelectedRole(null);
    } catch (error) {
      console.error('Failed to save role:', error);
    }
  };

  const getRoleBadgeColor = (role: RoleWithDetails) => {
    if (role.isSystem) return 'gray';
    if (role.name.includes('ADMIN')) return 'red';
    if (role.name.includes('MANAGER')) return 'blue';
    return 'green';
  };

  if (error) {
    return (
      <Alert icon={<IconAlertTriangle size={16} />} title="Error" color="red">
        Failed to load roles. Please try again.
      </Alert>
    );
  }

  return (
    <Card>
      <Stack spacing="md">
        <Group position="apart">
          <Title order={3}>Role Management</Title>
          <Button leftIcon={<IconPlus size={16} />} onClick={handleCreateRole}>
            Create Role
          </Button>
        </Group>

        {/* Search */}
        <TextInput
          placeholder="Search roles..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.currentTarget.value)}
          icon={<IconShield size={16} />}
        />

        {/* Roles Table */}
        <div style={{ position: 'relative' }}>
          <LoadingOverlay visible={isLoading} />
          
          <Table striped highlightOnHover>
            <thead>
              <tr>
                <th>Role Name</th>
                <th>Description</th>
                <th>Users</th>
                <th>Permissions</th>
                <th>Type</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {roles.map((role) => (
                <tr key={role.id}>
                  <td>
                    <Group spacing="sm">
                      <div
                        style={{ cursor: 'pointer' }}
                        onClick={() => onRoleSelect?.(role)}
                      >
                        <Text weight={500}>{role.name}</Text>
                      </div>
                      <Badge
                        size="sm"
                        color={getRoleBadgeColor(role)}
                        variant="light"
                      >
                        {role.isSystem ? 'System' : 'Custom'}
                      </Badge>
                    </Group>
                  </td>
                  <td>
                    <Text size="sm" color="dimmed" lineClamp={2}>
                      {role.description || 'No description'}
                    </Text>
                  </td>
                  <td>
                    <Group spacing="xs">
                      <IconUsers size={16} />
                      <Text size="sm">{role.userCount}</Text>
                    </Group>
                  </td>
                  <td>
                    <Badge variant="light" color="blue">
                      {role.permissions.length} permissions
                    </Badge>
                  </td>
                  <td>
                    <Text size="sm">
                      {new Date(role.createdAt).toLocaleDateString()}
                    </Text>
                  </td>
                  <td>
                    <Menu shadow="md" width={200}>
                      <Menu.Target>
                        <ActionIcon variant="light">
                          <IconDots size={16} />
                        </ActionIcon>
                      </Menu.Target>

                      <Menu.Dropdown>
                        <Menu.Item
                          icon={<IconEdit size={14} />}
                          onClick={() => handleEditRole(role)}
                          disabled={!role.canEdit}
                        >
                          Edit Role
                        </Menu.Item>
                        <Menu.Item
                          icon={<IconTrash size={14} />}
                          color="red"
                          onClick={() => handleDeleteRole(role)}
                          disabled={!role.canDelete || role.isSystem}
                        >
                          Delete Role
                        </Menu.Item>
                      </Menu.Dropdown>
                    </Menu>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>

          {roles.length === 0 && !isLoading && (
            <Text align="center" color="dimmed" py="xl">
              No roles found
            </Text>
          )}
        </div>

        {/* Pagination */}
        {totalPages > 1 && (
          <Group position="center">
            <Pagination
              value={page}
              onChange={setPage}
              total={totalPages}
              size="sm"
            />
          </Group>
        )}

        {/* Role Form Modal */}
        <RoleFormModal
          role={selectedRole || undefined}
          opened={modalOpened}
          onClose={closeModal}
          onSubmit={handleSubmitRole}
          isLoading={createRoleMutation.isLoading || updateRoleMutation.isLoading}
        />
      </Stack>
    </Card>
  );
};

export default RoleManagement;