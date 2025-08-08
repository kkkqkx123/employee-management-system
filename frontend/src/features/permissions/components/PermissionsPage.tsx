// Main Permissions Management Page

import React, { useState } from 'react';
import {
  Container,
  Title,
  Tabs,
  Stack,
  Group,
  Button,
  Modal,
  Alert,
  LoadingOverlay,
} from '@mantine/core';
import { useDisclosure } from '@mantine/hooks';
import { modals } from '@mantine/modals';
import {
  IconShield,
  IconUsers,
  IconMatrix,
  IconUserCheck,
  IconSettings,
  IconAlertTriangle,
} from '@tabler/icons-react';
import { RolePermissionMatrix } from './RolePermissionMatrix';
import { UserRoleAssignment } from './UserRoleAssignment';
import { RoleManagement } from './RoleManagement';
import { RoleDetailView } from './RoleDetailView';
import { PermissionImpactWarning } from './PermissionImpactWarning';
import { useDeleteRole, useRoleImpactAnalysis } from '../hooks/useRoles';
import type { RoleWithDetails, PermissionWithDetails } from '../types';
import type { User } from '../../../types/auth';

export const PermissionsPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<string>('matrix');
  const [selectedRole, setSelectedRole] = useState<RoleWithDetails | null>(null);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [roleToDelete, setRoleToDelete] = useState<RoleWithDetails | null>(null);
  
  const [roleDetailOpened, { open: openRoleDetail, close: closeRoleDetail }] = useDisclosure(false);
  const [deleteModalOpened, { open: openDeleteModal, close: closeDeleteModal }] = useDisclosure(false);

  const deleteRoleMutation = useDeleteRole();
  const { data: deleteImpact, isLoading: impactLoading } = useRoleImpactAnalysis(
    roleToDelete?.id || 0,
    'DELETE',
    undefined
  );

  // Handle role selection from different components
  const handleRoleSelect = (role: RoleWithDetails) => {
    setSelectedRole(role);
    openRoleDetail();
  };

  // Handle user selection
  const handleUserSelect = (user: User) => {
    setSelectedUser(user);
    // Could open user detail modal or navigate to user page
    console.log('Selected user:', user);
  };

  // Handle permission selection
  const handlePermissionSelect = (permission: PermissionWithDetails) => {
    // Could show permission details or impact analysis
    console.log('Selected permission:', permission);
  };

  // Handle role edit
  const handleRoleEdit = (role: RoleWithDetails) => {
    // This would typically open the role form modal
    // For now, we'll just switch to the management tab
    setActiveTab('management');
  };

  // Handle role delete with impact analysis
  const handleRoleDelete = (role: RoleWithDetails) => {
    setRoleToDelete(role);
    openDeleteModal();
  };

  // Confirm role deletion
  const confirmRoleDelete = async () => {
    if (!roleToDelete) return;

    try {
      await deleteRoleMutation.mutateAsync(roleToDelete.id);
      closeDeleteModal();
      closeRoleDetail();
      setRoleToDelete(null);
      setSelectedRole(null);
    } catch (error) {
      console.error('Failed to delete role:', error);
    }
  };

  // Cancel role deletion
  const cancelRoleDelete = () => {
    closeDeleteModal();
    setRoleToDelete(null);
  };

  return (
    <Container size="xl" py="md">
      <Stack spacing="lg">
        {/* Page Header */}
        <Group position="apart">
          <div>
            <Title order={2}>Permission Management</Title>
            <p style={{ margin: 0, color: 'var(--mantine-color-dimmed)' }}>
              Manage user roles, permissions, and access control
            </p>
          </div>
        </Group>

        {/* Main Content Tabs */}
        <Tabs value={activeTab} onTabChange={setActiveTab}>
          <Tabs.List>
            <Tabs.Tab value="matrix" icon={<IconMatrix size={16} />}>
              Role-Permission Matrix
            </Tabs.Tab>
            <Tabs.Tab value="users" icon={<IconUserCheck size={16} />}>
              User Role Assignment
            </Tabs.Tab>
            <Tabs.Tab value="management" icon={<IconSettings size={16} />}>
              Role Management
            </Tabs.Tab>
          </Tabs.List>

          <Tabs.Panel value="matrix" pt="md">
            <RolePermissionMatrix
              onRoleSelect={handleRoleSelect}
              onPermissionSelect={handlePermissionSelect}
            />
          </Tabs.Panel>

          <Tabs.Panel value="users" pt="md">
            <UserRoleAssignment
              onUserSelect={handleUserSelect}
            />
          </Tabs.Panel>

          <Tabs.Panel value="management" pt="md">
            <RoleManagement
              onRoleSelect={handleRoleSelect}
            />
          </Tabs.Panel>
        </Tabs>

        {/* Role Detail Modal */}
        <Modal
          opened={roleDetailOpened}
          onClose={closeRoleDetail}
          title="Role Details"
          size="xl"
        >
          {selectedRole && (
            <RoleDetailView
              roleId={selectedRole.id}
              onEdit={handleRoleEdit}
              onDelete={handleRoleDelete}
              onUserSelect={handleUserSelect}
            />
          )}
        </Modal>

        {/* Delete Confirmation Modal with Impact Analysis */}
        <Modal
          opened={deleteModalOpened}
          onClose={cancelRoleDelete}
          title="Confirm Role Deletion"
          size="md"
        >
          <Stack spacing="md">
            {roleToDelete && (
              <>
                <Alert icon={<IconAlertTriangle size={16} />} color="red">
                  You are about to delete the role "{roleToDelete.name}". 
                  This action cannot be undone.
                </Alert>

                {/* Impact Analysis */}
                <div style={{ position: 'relative' }}>
                  <LoadingOverlay visible={impactLoading} />
                  
                  {deleteImpact && (
                    <PermissionImpactWarning
                      impact={deleteImpact}
                      action="DELETE"
                      resourceName={`role "${roleToDelete.name}"`}
                      onProceed={confirmRoleDelete}
                      onCancel={cancelRoleDelete}
                      showActions={true}
                    />
                  )}
                </div>

                {!impactLoading && !deleteImpact && (
                  <Group position="right" spacing="sm">
                    <Button variant="light" onClick={cancelRoleDelete}>
                      Cancel
                    </Button>
                    <Button
                      color="red"
                      onClick={confirmRoleDelete}
                      loading={deleteRoleMutation.isLoading}
                    >
                      Delete Role
                    </Button>
                  </Group>
                )}
              </>
            )}
          </Stack>
        </Modal>
      </Stack>
    </Container>
  );
};

export default PermissionsPage;