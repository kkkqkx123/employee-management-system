// Employee list component with pagination, search, and bulk operations

import React, { useState, useMemo } from 'react';
import {
  Container,
  Title,
  Group,
  Button,
  TextInput,
  Select,
  Paper,
  Stack,
  Badge,
  ActionIcon,
  Menu,
  Checkbox,
  Modal,
  Text,
  Alert,
  Flex,
  Box,
} from '@mantine/core';
import {
  IconPlus,
  IconSearch,
  IconFilter,
  IconDownload,
  IconUpload,
  IconDots,
  IconEdit,
  IconTrash,
  IconEye,
  IconUsers,
  IconAlertCircle,
} from '@tabler/icons-react';
import { useDisclosure } from '@mantine/hooks';
import { notifications } from '@mantine/notifications';
import { DataTable } from '../../../components/ui/DataTable';
import { LoadingSpinner } from '../../../components/ui/LoadingSpinner';
import { useEmployees, useBulkEmployeeOperation, useDeleteEmployee } from '../hooks';
import { EmployeeForm } from './EmployeeForm';
import { EmployeeDetail } from './EmployeeDetail';
import { EmployeeImport } from './EmployeeImport';
import { EmployeeExport } from './EmployeeExport';
import type { Employee, EmployeeSearchCriteria, EmployeeStatus, EmploymentType } from '../types';

interface EmployeeListProps {
  onEmployeeSelect?: (employee: Employee) => void;
  selectionMode?: boolean;
  selectedEmployees?: number[];
  onSelectionChange?: (selectedIds: number[]) => void;
}

export const EmployeeList: React.FC<EmployeeListProps> = ({
  onEmployeeSelect,
  selectionMode = false,
  selectedEmployees = [],
  onSelectionChange,
}) => {
  // State management
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [sortBy, setSortBy] = useState<string>('lastName,asc');
  const [searchCriteria, setSearchCriteria] = useState<EmployeeSearchCriteria>({});
  const [selectedIds, setSelectedIds] = useState<number[]>(selectedEmployees);
  const [selectedEmployee, setSelectedEmployee] = useState<Employee | null>(null);

  // Modal states
  const [createModalOpened, { open: openCreateModal, close: closeCreateModal }] = useDisclosure(false);
  const [editModalOpened, { open: openEditModal, close: closeEditModal }] = useDisclosure(false);
  const [detailModalOpened, { open: openDetailModal, close: closeDetailModal }] = useDisclosure(false);
  const [importModalOpened, { open: openImportModal, close: closeImportModal }] = useDisclosure(false);
  const [exportModalOpened, { open: openExportModal, close: closeExportModal }] = useDisclosure(false);
  const [deleteModalOpened, { open: openDeleteModal, close: closeDeleteModal }] = useDisclosure(false);

  // API hooks
  const { data: employeesData, isLoading, error, refetch } = useEmployees(
    page,
    pageSize,
    sortBy,
    searchCriteria
  );
  const deleteEmployeeMutation = useDeleteEmployee();
  const bulkOperationMutation = useBulkEmployeeOperation();

  // Computed values
  const employees = employeesData?.content || [];
  const totalElements = employeesData?.totalElements || 0;
  const totalPages = employeesData?.totalPages || 0;

  // Selection handlers
  const handleSelectAll = (checked: boolean) => {
    const newSelection = checked ? employees.map(emp => emp.id) : [];
    setSelectedIds(newSelection);
    onSelectionChange?.(newSelection);
  };

  const handleSelectEmployee = (employeeId: number, checked: boolean) => {
    const newSelection = checked
      ? [...selectedIds, employeeId]
      : selectedIds.filter(id => id !== employeeId);
    setSelectedIds(newSelection);
    onSelectionChange?.(newSelection);
  };

  // Search handlers
  const handleSearch = (field: keyof EmployeeSearchCriteria, value: string | undefined) => {
    setSearchCriteria(prev => ({
      ...prev,
      [field]: value || undefined,
    }));
    setPage(0); // Reset to first page when searching
  };

  const clearFilters = () => {
    setSearchCriteria({});
    setPage(0);
  };

  // Action handlers
  const handleView = (employee: Employee) => {
    setSelectedEmployee(employee);
    if (onEmployeeSelect) {
      onEmployeeSelect(employee);
    } else {
      openDetailModal();
    }
  };

  const handleEdit = (employee: Employee) => {
    setSelectedEmployee(employee);
    openEditModal();
  };

  const handleDelete = (employee: Employee) => {
    setSelectedEmployee(employee);
    openDeleteModal();
  };

  const confirmDelete = async () => {
    if (!selectedEmployee) return;

    try {
      await deleteEmployeeMutation.mutateAsync(selectedEmployee.id);
      notifications.show({
        title: 'Success',
        message: 'Employee deleted successfully',
        color: 'green',
      });
      closeDeleteModal();
      setSelectedEmployee(null);
    } catch (error) {
      notifications.show({
        title: 'Error',
        message: 'Failed to delete employee',
        color: 'red',
      });
    }
  };

  const handleBulkDelete = async () => {
    if (selectedIds.length === 0) return;

    try {
      await bulkOperationMutation.mutateAsync({
        employeeIds: selectedIds,
        operation: 'DELETE',
      });
      notifications.show({
        title: 'Success',
        message: `${selectedIds.length} employees deleted successfully`,
        color: 'green',
      });
      setSelectedIds([]);
      onSelectionChange?.([]);
    } catch (error) {
      notifications.show({
        title: 'Error',
        message: 'Failed to delete employees',
        color: 'red',
      });
    }
  };

  // Table columns
  const columns = useMemo(() => [
    ...(selectionMode ? [{
      key: 'select',
      title: (
        <Checkbox
          checked={selectedIds.length === employees.length && employees.length > 0}
          indeterminate={selectedIds.length > 0 && selectedIds.length < employees.length}
          onChange={(event) => handleSelectAll(event.currentTarget.checked)}
        />
      ),
      render: (employee: Employee) => (
        <Checkbox
          checked={selectedIds.includes(employee.id)}
          onChange={(event) => handleSelectEmployee(employee.id, event.currentTarget.checked)}
        />
      ),
      width: 50,
    }] : []),
    {
      key: 'employeeNumber',
      title: 'Employee #',
      sortable: true,
      render: (employee: Employee) => (
        <Text fw={500}>{employee.employeeNumber}</Text>
      ),
    },
    {
      key: 'fullName',
      title: 'Name',
      sortable: true,
      render: (employee: Employee) => (
        <div>
          <Text fw={500}>{employee.fullName}</Text>
          <Text size="sm" c="dimmed">{employee.email}</Text>
        </div>
      ),
    },
    {
      key: 'departmentName',
      title: 'Department',
      sortable: true,
      render: (employee: Employee) => (
        <Text>{employee.departmentName || 'N/A'}</Text>
      ),
    },
    {
      key: 'positionName',
      title: 'Position',
      sortable: true,
      render: (employee: Employee) => (
        <Text>{employee.positionName || 'N/A'}</Text>
      ),
    },
    {
      key: 'status',
      title: 'Status',
      sortable: true,
      render: (employee: Employee) => (
        <Badge
          color={
            employee.status === 'ACTIVE' ? 'green' :
            employee.status === 'INACTIVE' ? 'gray' :
            employee.status === 'TERMINATED' ? 'red' :
            employee.status === 'ON_LEAVE' ? 'blue' :
            employee.status === 'PROBATION' ? 'yellow' :
            'orange'
          }
          variant="light"
        >
          {employee.status.replace('_', ' ')}
        </Badge>
      ),
    },
    {
      key: 'hireDate',
      title: 'Hire Date',
      sortable: true,
      render: (employee: Employee) => (
        <Text>{new Date(employee.hireDate).toLocaleDateString()}</Text>
      ),
    },
    {
      key: 'actions',
      title: 'Actions',
      render: (employee: Employee) => (
        <Group gap="xs">
          <ActionIcon
            variant="subtle"
            color="blue"
            onClick={() => handleView(employee)}
            title="View Details"
          >
            <IconEye size={16} />
          </ActionIcon>
          <ActionIcon
            variant="subtle"
            color="orange"
            onClick={() => handleEdit(employee)}
            title="Edit Employee"
          >
            <IconEdit size={16} />
          </ActionIcon>
          <ActionIcon
            variant="subtle"
            color="red"
            onClick={() => handleDelete(employee)}
            title="Delete Employee"
          >
            <IconTrash size={16} />
          </ActionIcon>
        </Group>
      ),
      width: 120,
    },
  ], [employees, selectedIds, selectionMode]);

  if (error) {
    return (
      <Container size="xl">
        <Alert icon={<IconAlertCircle size={16} />} title="Error" color="red">
          Failed to load employees. Please try again.
        </Alert>
      </Container>
    );
  }

  return (
    <Container size="xl">
      <Stack gap="lg">
        {/* Header */}
        <Group justify="space-between">
          <div>
            <Title order={1}>Employees</Title>
            <Text c="dimmed">Manage employee records and information</Text>
          </div>
          <Group>
            <Button
              leftSection={<IconUpload size={16} />}
              variant="light"
              onClick={openImportModal}
            >
              Import
            </Button>
            <Button
              leftSection={<IconDownload size={16} />}
              variant="light"
              onClick={openExportModal}
            >
              Export
            </Button>
            <Button
              leftSection={<IconPlus size={16} />}
              onClick={openCreateModal}
            >
              Add Employee
            </Button>
          </Group>
        </Group>

        {/* Filters */}
        <Paper p="md" withBorder>
          <Stack gap="md">
            <Group>
              <TextInput
                placeholder="Search by name or email..."
                leftSection={<IconSearch size={16} />}
                value={searchCriteria.firstName || searchCriteria.lastName || searchCriteria.email || ''}
                onChange={(event) => {
                  const value = event.currentTarget.value;
                  handleSearch('firstName', value);
                  handleSearch('lastName', value);
                  handleSearch('email', value);
                }}
                style={{ flex: 1 }}
              />
              <Select
                placeholder="Status"
                data={[
                  { value: '', label: 'All Statuses' },
                  { value: 'ACTIVE', label: 'Active' },
                  { value: 'INACTIVE', label: 'Inactive' },
                  { value: 'TERMINATED', label: 'Terminated' },
                  { value: 'ON_LEAVE', label: 'On Leave' },
                  { value: 'PROBATION', label: 'Probation' },
                  { value: 'SUSPENDED', label: 'Suspended' },
                ]}
                value={searchCriteria.status || ''}
                onChange={(value) => handleSearch('status', value as EmployeeStatus)}
                clearable
              />
              <Select
                placeholder="Employment Type"
                data={[
                  { value: '', label: 'All Types' },
                  { value: 'FULL_TIME', label: 'Full Time' },
                  { value: 'PART_TIME', label: 'Part Time' },
                  { value: 'CONTRACT', label: 'Contract' },
                  { value: 'TEMPORARY', label: 'Temporary' },
                  { value: 'INTERN', label: 'Intern' },
                ]}
                value={searchCriteria.employmentType || ''}
                onChange={(value) => handleSearch('employmentType', value as EmploymentType)}
                clearable
              />
              <Button variant="light" onClick={clearFilters}>
                Clear Filters
              </Button>
            </Group>

            {/* Bulk Actions */}
            {selectedIds.length > 0 && (
              <Group>
                <Text size="sm" c="dimmed">
                  {selectedIds.length} employee(s) selected
                </Text>
                <Button
                  size="xs"
                  color="red"
                  variant="light"
                  onClick={handleBulkDelete}
                  loading={bulkOperationMutation.isPending}
                >
                  Delete Selected
                </Button>
              </Group>
            )}
          </Stack>
        </Paper>

        {/* Data Table */}
        <Paper withBorder>
          {isLoading ? (
            <Box p="xl" style={{ textAlign: 'center' }}>
              <LoadingSpinner />
            </Box>
          ) : (
            <DataTable
              data={employees}
              columns={columns}
              totalRecords={totalElements}
              recordsPerPage={pageSize}
              page={page + 1}
              onPageChange={(newPage) => setPage(newPage - 1)}
              onRecordsPerPageChange={setPageSize}
              sortStatus={{
                columnAccessor: sortBy.split(',')[0],
                direction: sortBy.split(',')[1] as 'asc' | 'desc',
              }}
              onSortStatusChange={(status) => {
                setSortBy(`${status.columnAccessor},${status.direction}`);
              }}
              noRecordsText="No employees found"
            />
          )}
        </Paper>
      </Stack>

      {/* Modals */}
      <Modal
        opened={createModalOpened}
        onClose={closeCreateModal}
        title="Add New Employee"
        size="xl"
      >
        <EmployeeForm
          onSuccess={() => {
            closeCreateModal();
            refetch();
          }}
          onCancel={closeCreateModal}
        />
      </Modal>

      <Modal
        opened={editModalOpened}
        onClose={closeEditModal}
        title="Edit Employee"
        size="xl"
      >
        {selectedEmployee && (
          <EmployeeForm
            employee={selectedEmployee}
            onSuccess={() => {
              closeEditModal();
              setSelectedEmployee(null);
              refetch();
            }}
            onCancel={() => {
              closeEditModal();
              setSelectedEmployee(null);
            }}
          />
        )}
      </Modal>

      <Modal
        opened={detailModalOpened}
        onClose={closeDetailModal}
        title="Employee Details"
        size="xl"
      >
        {selectedEmployee && (
          <EmployeeDetail
            employee={selectedEmployee}
            onEdit={() => {
              closeDetailModal();
              openEditModal();
            }}
            onClose={closeDetailModal}
          />
        )}
      </Modal>

      <Modal
        opened={importModalOpened}
        onClose={closeImportModal}
        title="Import Employees"
        size="lg"
      >
        <EmployeeImport
          onSuccess={() => {
            closeImportModal();
            refetch();
          }}
          onCancel={closeImportModal}
        />
      </Modal>

      <Modal
        opened={exportModalOpened}
        onClose={closeExportModal}
        title="Export Employees"
        size="lg"
      >
        <EmployeeExport
          selectedEmployeeIds={selectedIds}
          searchCriteria={searchCriteria}
          onClose={closeExportModal}
        />
      </Modal>

      {/* Delete Confirmation Modal */}
      <Modal
        opened={deleteModalOpened}
        onClose={closeDeleteModal}
        title="Confirm Deletion"
        size="sm"
      >
        <Stack>
          <Text>
            Are you sure you want to delete employee{' '}
            <strong>{selectedEmployee?.fullName}</strong>?
          </Text>
          <Text size="sm" c="dimmed">
            This action cannot be undone.
          </Text>
          <Group justify="flex-end">
            <Button variant="light" onClick={closeDeleteModal}>
              Cancel
            </Button>
            <Button
              color="red"
              onClick={confirmDelete}
              loading={deleteEmployeeMutation.isPending}
            >
              Delete
            </Button>
          </Group>
        </Stack>
      </Modal>
    </Container>
  );
};