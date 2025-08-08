// Role-Permission Matrix Component

import React, { useState, useMemo } from 'react';
import {
  Table,
  Checkbox,
  Text,
  Group,
  Badge,
  ActionIcon,
  Tooltip,
  Alert,
  LoadingOverlay,
  ScrollArea,
  Button,
  TextInput,
  Select,
  Stack,
  Card,
  Title,
} from '@mantine/core';
import { IconSearch, IconAlertTriangle, IconInfoCircle, IconCheck, IconX } from '@tabler/icons-react';
import { useRolePermissionMatrix } from '../hooks/usePermissions';
import { useBulkUpdateRolePermissions } from '../hooks/usePermissions';
import { usePermissionImpactAnalysis } from '../hooks/usePermissions';
import { PERMISSION_CATEGORIES } from '../types';
import type { RoleWithDetails, PermissionWithDetails } from '../types';

interface RolePermissionMatrixProps {
  onRoleSelect?: (role: RoleWithDetails) => void;
  onPermissionSelect?: (permission: PermissionWithDetails) => void;
  readOnly?: boolean;
}

export const RolePermissionMatrix: React.FC<RolePermissionMatrixProps> = ({
  onRoleSelect,
  onPermissionSelect,
  readOnly = false,
}) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<string>('');
  const [pendingChanges, setPendingChanges] = useState<Record<number, number[]>>({});
  const [showSystemRoles, setShowSystemRoles] = useState(false);

  const { data: matrix, isLoading, error } = useRolePermissionMatrix();
  const bulkUpdateMutation = useBulkUpdateRolePermissions();

  // Filter roles and permissions based on search and category
  const filteredData = useMemo(() => {
    if (!matrix) return { roles: [], permissions: [] };

    let filteredRoles = matrix.roles.filter(role => {
      const matchesSearch = !searchTerm || 
        role.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        role.description?.toLowerCase().includes(searchTerm.toLowerCase());
      const matchesSystemFilter = showSystemRoles || !role.isSystem;
      return matchesSearch && matchesSystemFilter;
    });

    let filteredPermissions = matrix.permissions.filter(permission => {
      const matchesSearch = !searchTerm ||
        permission.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        permission.description?.toLowerCase().includes(searchTerm.toLowerCase());
      const matchesCategory = !selectedCategory || permission.category === selectedCategory;
      return matchesSearch && matchesCategory;
    });

    return { roles: filteredRoles, permissions: filteredPermissions };
  }, [matrix, searchTerm, selectedCategory, showSystemRoles]);

  // Get current assignments including pending changes
  const getCurrentAssignments = (roleId: number): number[] => {
    if (pendingChanges[roleId]) {
      return pendingChanges[roleId];
    }
    return matrix?.assignments[roleId] || [];
  };

  // Handle permission toggle
  const handlePermissionToggle = (roleId: number, permissionId: number) => {
    if (readOnly) return;

    const currentAssignments = getCurrentAssignments(roleId);
    const hasPermission = currentAssignments.includes(permissionId);
    
    const newAssignments = hasPermission
      ? currentAssignments.filter(id => id !== permissionId)
      : [...currentAssignments, permissionId];

    setPendingChanges(prev => ({
      ...prev,
      [roleId]: newAssignments,
    }));
  };

  // Save pending changes
  const handleSaveChanges = async () => {
    const updates = Object.entries(pendingChanges).map(([roleId, permissionIds]) => ({
      roleId: parseInt(roleId),
      permissionIds,
    }));

    try {
      await bulkUpdateMutation.mutateAsync(updates);
      setPendingChanges({});
    } catch (error) {
      console.error('Failed to update permissions:', error);
    }
  };

  // Cancel pending changes
  const handleCancelChanges = () => {
    setPendingChanges({});
  };

  // Check if there are pending changes
  const hasPendingChanges = Object.keys(pendingChanges).length > 0;

  // Get permission categories for filter
  const categories = Object.values(PERMISSION_CATEGORIES);

  if (error) {
    return (
      <Alert icon={<IconAlertTriangle size={16} />} title="Error" color="red">
        Failed to load role-permission matrix. Please try again.
      </Alert>
    );
  }

  return (
    <Card>
      <Stack spacing="md">
        <Group position="apart">
          <Title order={3}>Role-Permission Matrix</Title>
          {hasPendingChanges && (
            <Group spacing="xs">
              <Badge color="orange" variant="light">
                {Object.keys(pendingChanges).length} roles with changes
              </Badge>
              <Button
                size="xs"
                variant="light"
                color="green"
                onClick={handleSaveChanges}
                loading={bulkUpdateMutation.isLoading}
                leftIcon={<IconCheck size={14} />}
              >
                Save Changes
              </Button>
              <Button
                size="xs"
                variant="light"
                color="gray"
                onClick={handleCancelChanges}
                leftIcon={<IconX size={14} />}
              >
                Cancel
              </Button>
            </Group>
          )}
        </Group>

        {/* Filters */}
        <Group spacing="md">
          <TextInput
            placeholder="Search roles or permissions..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.currentTarget.value)}
            icon={<IconSearch size={16} />}
            style={{ flex: 1 }}
          />
          <Select
            placeholder="Filter by category"
            value={selectedCategory}
            onChange={(value) => setSelectedCategory(value || '')}
            data={[
              { value: '', label: 'All Categories' },
              ...categories.map(cat => ({ value: cat, label: cat })),
            ]}
            clearable
            style={{ minWidth: 200 }}
          />
          <Checkbox
            label="Show system roles"
            checked={showSystemRoles}
            onChange={(e) => setShowSystemRoles(e.currentTarget.checked)}
          />
        </Group>

        {/* Matrix Table */}
        <div style={{ position: 'relative' }}>
          <LoadingOverlay visible={isLoading} />
          
          <ScrollArea>
            <Table striped highlightOnHover>
              <thead>
                <tr>
                  <th style={{ minWidth: 200, position: 'sticky', left: 0, background: 'white', zIndex: 1 }}>
                    Role / Permission
                  </th>
                  {filteredData.permissions.map((permission) => (
                    <th key={permission.id} style={{ minWidth: 120, textAlign: 'center' }}>
                      <Tooltip
                        label={
                          <div>
                            <Text size="sm" weight={500}>{permission.name}</Text>
                            <Text size="xs" color="dimmed">{permission.description}</Text>
                            <Badge size="xs" color="blue" mt={4}>
                              {permission.category}
                            </Badge>
                          </div>
                        }
                        multiline
                        width={250}
                      >
                        <div
                          style={{ cursor: 'pointer' }}
                          onClick={() => onPermissionSelect?.(permission)}
                        >
                          <Text size="xs" weight={500} align="center">
                            {permission.name.split('_').pop()}
                          </Text>
                          <Text size="xs" color="dimmed" align="center">
                            {permission.resource}
                          </Text>
                        </div>
                      </Tooltip>
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {filteredData.roles.map((role) => (
                  <tr key={role.id}>
                    <td style={{ position: 'sticky', left: 0, background: 'white', zIndex: 1 }}>
                      <Group spacing="xs">
                        <div
                          style={{ cursor: 'pointer' }}
                          onClick={() => onRoleSelect?.(role)}
                        >
                          <Text weight={500}>{role.name}</Text>
                          <Text size="xs" color="dimmed">
                            {role.userCount} users
                          </Text>
                        </div>
                        {role.isSystem && (
                          <Badge size="xs" color="gray">System</Badge>
                        )}
                        {pendingChanges[role.id] && (
                          <Badge size="xs" color="orange">Modified</Badge>
                        )}
                      </Group>
                    </td>
                    {filteredData.permissions.map((permission) => {
                      const hasPermission = getCurrentAssignments(role.id).includes(permission.id);
                      const originalHasPermission = (matrix?.assignments[role.id] || []).includes(permission.id);
                      const isChanged = hasPermission !== originalHasPermission;
                      
                      return (
                        <td key={permission.id} style={{ textAlign: 'center' }}>
                          <Tooltip
                            label={
                              isChanged
                                ? `${hasPermission ? 'Adding' : 'Removing'} permission`
                                : hasPermission
                                ? 'Permission granted'
                                : 'Permission not granted'
                            }
                          >
                            <Checkbox
                              checked={hasPermission}
                              onChange={() => handlePermissionToggle(role.id, permission.id)}
                              disabled={readOnly || role.isSystem}
                              color={isChanged ? 'orange' : undefined}
                              size="sm"
                            />
                          </Tooltip>
                        </td>
                      );
                    })}
                  </tr>
                ))}
              </tbody>
            </Table>
          </ScrollArea>
        </div>

        {/* Info */}
        <Alert icon={<IconInfoCircle size={16} />} color="blue" variant="light">
          <Text size="sm">
            Click on role names to view details, or permission headers to see permission information.
            {!readOnly && ' Check/uncheck boxes to modify permissions, then save your changes.'}
          </Text>
        </Alert>
      </Stack>
    </Card>
  );
};

export default RolePermissionMatrix;