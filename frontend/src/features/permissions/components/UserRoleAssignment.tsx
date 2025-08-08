// User Role Assignment Component

import React, { useState, useMemo } from 'react';
import {
  Table,
  Text,
  Group,
  Badge,
  Button,
  TextInput,
  Select,
  MultiSelect,
  Modal,
  Stack,
  Card,
  Title,
  Alert,
  LoadingOverlay,
  ActionIcon,
  Tooltip,
  Avatar,
  Pagination,
} from '@mantine/core';
import { useDisclosure } from '@mantine/hooks';
import { IconSearch, IconEdit, IconAlertTriangle, IconUser, IconCheck, IconX } from '@tabler/icons-react';
import { useUserRoles, useUpdateUserRoles, useValidateUserRoles } from '../hooks/useUserRoles';
import { useRoles } from '../hooks/useRoles';
import type { UserRoleAssignment, UserRoleUpdateRequest } from '../types';
import type { User } from '../../../types/auth';

interface UserRoleAssignmentProps {
  onUserSelect?: (user: User) => void;
  readOnly?: boolean;
}

interface EditUserRolesModalProps {
  user: User;
  currentRoles: number[];
  opened: boolean;
  onClose: () => void;
  onSave: (userId: number, roleIds: number[]) => void;
  isLoading?: boolean;
}

const EditUserRolesModal: React.FC<EditUserRolesModalProps> = ({
  user,
  currentRoles,
  opened,
  onClose,
  onSave,
  isLoading = false,
}) => {
  const [selectedRoles, setSelectedRoles] = useState<string[]>(
    currentRoles.map(id => id.toString())
  );
  const [validationError, setValidationError] = useState<string>('');

  const { data: rolesData } = useRoles();
  const validateMutation = useValidateUserRoles();

  const roles = rolesData?.content || [];
  const roleOptions = roles.map(role => ({
    value: role.id.toString(),
    label: `${role.name} (${role.userCount} users)`,
    disabled: role.isSystem && !currentRoles.includes(role.id),
  }));

  const handleSave = async () => {
    const roleIds = selectedRoles.map(id => parseInt(id));
    
    // Validate role assignment
    try {
      const validation = await validateMutation.mutateAsync({
        userId: user.id,
        roleIds,
      });

      if (!validation.data.valid) {
        setValidationError(validation.data.conflicts.join(', '));
        return;
      }

      onSave(user.id, roleIds);
      onClose();
    } catch (error) {
      setValidationError('Failed to validate role assignment');
    }
  };

  const handleClose = () => {
    setSelectedRoles(currentRoles.map(id => id.toString()));
    setValidationError('');
    onClose();
  };

  return (
    <Modal
      opened={opened}
      onClose={handleClose}
      title={`Edit Roles for ${user.firstName} ${user.lastName}`}
      size="md"
    >
      <Stack spacing="md">
        <Group spacing="sm">
          <Avatar size="sm" radius="xl">
            <IconUser size={16} />
          </Avatar>
          <div>
            <Text weight={500}>{user.firstName} {user.lastName}</Text>
            <Text size="sm" color="dimmed">{user.email}</Text>
          </div>
        </Group>

        <MultiSelect
          label="Assigned Roles"
          placeholder="Select roles for this user"
          data={roleOptions}
          value={selectedRoles}
          onChange={setSelectedRoles}
          searchable
          clearable
          maxDropdownHeight={200}
        />

        {validationError && (
          <Alert icon={<IconAlertTriangle size={16} />} color="red">
            {validationError}
          </Alert>
        )}

        <Group position="right" spacing="sm">
          <Button variant="light" onClick={handleClose}>
            Cancel
          </Button>
          <Button
            onClick={handleSave}
            loading={isLoading || validateMutation.isLoading}
            leftIcon={<IconCheck size={16} />}
          >
            Save Changes
          </Button>
        </Group>
      </Stack>
    </Modal>
  );
};

export const UserRoleAssignment: React.FC<UserRoleAssignmentProps> = ({
  onUserSelect,
  readOnly = false,
}) => {
  const [page, setPage] = useState(1);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedRole, setSelectedRole] = useState<string>('');
  const [editingUser, setEditingUser] = useState<UserRoleAssignment | null>(null);
  const [modalOpened, { open: openModal, close: closeModal }] = useDisclosure(false);

  const pageSize = 20;

  // Build search params
  const searchParams = useMemo(() => ({
    page: page - 1,
    size: pageSize,
    ...(searchTerm && { username: searchTerm }),
    ...(selectedRole && { roleId: parseInt(selectedRole) }),
  }), [page, searchTerm, selectedRole]);

  const { data: userRolesData, isLoading, error } = useUserRoles(searchParams);
  const { data: rolesData } = useRoles();
  const updateUserRolesMutation = useUpdateUserRoles();

  const userRoles = userRolesData?.content || [];
  const totalPages = userRolesData?.totalPages || 1;
  const roles = rolesData?.content || [];

  // Role filter options
  const roleFilterOptions = [
    { value: '', label: 'All Roles' },
    ...roles.map(role => ({
      value: role.id.toString(),
      label: `${role.name} (${role.userCount} users)`,
    })),
  ];

  const handleEditUser = (userRole: UserRoleAssignment) => {
    setEditingUser(userRole);
    openModal();
  };

  const handleSaveUserRoles = async (userId: number, roleIds: number[]) => {
    try {
      await updateUserRolesMutation.mutateAsync({ userId, roleIds });
      closeModal();
      setEditingUser(null);
    } catch (error) {
      console.error('Failed to update user roles:', error);
    }
  };

  const getRolesBadges = (userRoles: UserRoleAssignment) => {
    return userRoles.roles.map(role => (
      <Badge
        key={role.id}
        size="sm"
        color={role.name === 'ADMIN' ? 'red' : role.name === 'MANAGER' ? 'blue' : 'gray'}
        variant="light"
      >
        {role.name}
      </Badge>
    ));
  };

  if (error) {
    return (
      <Alert icon={<IconAlertTriangle size={16} />} title="Error" color="red">
        Failed to load user role assignments. Please try again.
      </Alert>
    );
  }

  return (
    <Card>
      <Stack spacing="md">
        <Title order={3}>User Role Assignment</Title>

        {/* Filters */}
        <Group spacing="md">
          <TextInput
            placeholder="Search by username or email..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.currentTarget.value)}
            icon={<IconSearch size={16} />}
            style={{ flex: 1 }}
          />
          <Select
            placeholder="Filter by role"
            value={selectedRole}
            onChange={(value) => setSelectedRole(value || '')}
            data={roleFilterOptions}
            clearable
            style={{ minWidth: 200 }}
          />
        </Group>

        {/* User Roles Table */}
        <div style={{ position: 'relative' }}>
          <LoadingOverlay visible={isLoading} />
          
          <Table striped highlightOnHover>
            <thead>
              <tr>
                <th>User</th>
                <th>Assigned Roles</th>
                <th>Effective Permissions</th>
                <th>Last Updated</th>
                {!readOnly && <th>Actions</th>}
              </tr>
            </thead>
            <tbody>
              {userRoles.map((userRole) => (
                <tr key={userRole.userId}>
                  <td>
                    <Group spacing="sm">
                      <Avatar size="sm" radius="xl">
                        <IconUser size={16} />
                      </Avatar>
                      <div
                        style={{ cursor: 'pointer' }}
                        onClick={() => onUserSelect?.(userRole.user)}
                      >
                        <Text weight={500}>
                          {userRole.user.firstName} {userRole.user.lastName}
                        </Text>
                        <Text size="sm" color="dimmed">
                          {userRole.user.email}
                        </Text>
                      </div>
                    </Group>
                  </td>
                  <td>
                    <Group spacing={4}>
                      {getRolesBadges(userRole)}
                      {userRole.roles.length === 0 && (
                        <Text size="sm" color="dimmed">No roles assigned</Text>
                      )}
                    </Group>
                  </td>
                  <td>
                    <Tooltip
                      label={
                        <div>
                          <Text size="sm" weight={500}>Effective Permissions:</Text>
                          {userRole.effectivePermissions.slice(0, 10).map(perm => (
                            <Text key={perm} size="xs">{perm}</Text>
                          ))}
                          {userRole.effectivePermissions.length > 10 && (
                            <Text size="xs" color="dimmed">
                              +{userRole.effectivePermissions.length - 10} more...
                            </Text>
                          )}
                        </div>
                      }
                      multiline
                      width={300}
                    >
                      <Badge variant="light" color="blue">
                        {userRole.effectivePermissions.length} permissions
                      </Badge>
                    </Tooltip>
                  </td>
                  <td>
                    <Text size="sm" color="dimmed">
                      {new Date(userRole.user.updatedAt).toLocaleDateString()}
                    </Text>
                  </td>
                  {!readOnly && (
                    <td>
                      <ActionIcon
                        variant="light"
                        color="blue"
                        onClick={() => handleEditUser(userRole)}
                        disabled={!userRole.user.enabled}
                      >
                        <IconEdit size={16} />
                      </ActionIcon>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </Table>

          {userRoles.length === 0 && !isLoading && (
            <Text align="center" color="dimmed" py="xl">
              No user role assignments found
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

        {/* Edit User Roles Modal */}
        {editingUser && (
          <EditUserRolesModal
            user={editingUser.user}
            currentRoles={editingUser.roles.map(r => r.id)}
            opened={modalOpened}
            onClose={closeModal}
            onSave={handleSaveUserRoles}
            isLoading={updateUserRolesMutation.isLoading}
          />
        )}
      </Stack>
    </Card>
  );
};

export default UserRoleAssignment;