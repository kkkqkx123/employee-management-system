// Employee export component for Excel/CSV generation

import React, { useState } from 'react';
import {
  Stack,
  Group,
  Button,
  Text,
  Select,
  Checkbox,
  Paper,
  Alert,
  MultiSelect,
  Divider,
} from '@mantine/core';
import { IconDownload, IconAlertCircle } from '@tabler/icons-react';
import { notifications } from '@mantine/notifications';
import { useExportEmployees } from '../hooks';
import type { EmployeeExportRequest, EmployeeSearchCriteria } from '../types';

interface EmployeeExportProps {
  selectedEmployeeIds?: number[];
  searchCriteria?: EmployeeSearchCriteria;
  onClose: () => void;
}

export const EmployeeExport: React.FC<EmployeeExportProps> = ({
  selectedEmployeeIds = [],
  searchCriteria = {},
  onClose,
}) => {
  const [exportType, setExportType] = useState<'selected' | 'filtered' | 'all'>('all');
  const [format, setFormat] = useState<'EXCEL' | 'CSV'>('EXCEL');
  const [includeFields, setIncludeFields] = useState<string[]>([
    'employeeNumber',
    'firstName',
    'lastName',
    'email',
    'departmentName',
    'positionName',
    'status',
    'hireDate',
  ]);

  const exportMutation = useExportEmployees();

  // Available fields for export
  const availableFields = [
    { value: 'employeeNumber', label: 'Employee Number' },
    { value: 'firstName', label: 'First Name' },
    { value: 'lastName', label: 'Last Name' },
    { value: 'fullName', label: 'Full Name' },
    { value: 'email', label: 'Email' },
    { value: 'phone', label: 'Phone' },
    { value: 'mobilePhone', label: 'Mobile Phone' },
    { value: 'address', label: 'Address' },
    { value: 'city', label: 'City' },
    { value: 'state', label: 'State/Province' },
    { value: 'zipCode', label: 'ZIP/Postal Code' },
    { value: 'country', label: 'Country' },
    { value: 'dateOfBirth', label: 'Date of Birth' },
    { value: 'gender', label: 'Gender' },
    { value: 'maritalStatus', label: 'Marital Status' },
    { value: 'nationality', label: 'Nationality' },
    { value: 'departmentName', label: 'Department' },
    { value: 'positionName', label: 'Position' },
    { value: 'managerName', label: 'Manager' },
    { value: 'hireDate', label: 'Hire Date' },
    { value: 'terminationDate', label: 'Termination Date' },
    { value: 'status', label: 'Status' },
    { value: 'employmentType', label: 'Employment Type' },
    { value: 'payType', label: 'Pay Type' },
    { value: 'salary', label: 'Salary' },
    { value: 'hourlyRate', label: 'Hourly Rate' },
    { value: 'enabled', label: 'Account Status' },
    { value: 'createdAt', label: 'Created Date' },
    { value: 'updatedAt', label: 'Last Updated' },
  ];

  const handleExport = async () => {
    if (includeFields.length === 0) {
      notifications.show({
        title: 'Error',
        message: 'Please select at least one field to export',
        color: 'red',
      });
      return;
    }

    const exportRequest: EmployeeExportRequest = {
      format,
      includeFields,
    };

    // Set export scope based on selection
    if (exportType === 'selected') {
      if (selectedEmployeeIds.length === 0) {
        notifications.show({
          title: 'Error',
          message: 'No employees selected for export',
          color: 'red',
        });
        return;
      }
      exportRequest.employeeIds = selectedEmployeeIds;
    } else if (exportType === 'filtered') {
      exportRequest.searchCriteria = searchCriteria;
    }
    // For 'all', we don't set any filters

    try {
      await exportMutation.mutateAsync(exportRequest);
      notifications.show({
        title: 'Success',
        message: 'Employee data exported successfully',
        color: 'green',
      });
      onClose();
    } catch (error: any) {
      notifications.show({
        title: 'Export Failed',
        message: error.message || 'Failed to export employee data',
        color: 'red',
      });
    }
  };

  const getExportDescription = () => {
    switch (exportType) {
      case 'selected':
        return `Export ${selectedEmployeeIds.length} selected employee(s)`;
      case 'filtered':
        return 'Export employees matching current search filters';
      case 'all':
        return 'Export all employees in the system';
      default:
        return '';
    }
  };

  const isExporting = exportMutation.isPending;

  return (
    <Stack gap="lg">
      {/* Export Scope */}
      <div>
        <Text fw={500} mb="md">Export Scope</Text>
        <Stack gap="xs">
          <Checkbox
            label={`Selected Employees (${selectedEmployeeIds.length})`}
            checked={exportType === 'selected'}
            onChange={() => setExportType('selected')}
            disabled={selectedEmployeeIds.length === 0}
          />
          <Checkbox
            label="Filtered Employees"
            description="Export employees matching current search filters"
            checked={exportType === 'filtered'}
            onChange={() => setExportType('filtered')}
          />
          <Checkbox
            label="All Employees"
            description="Export all employees in the system"
            checked={exportType === 'all'}
            onChange={() => setExportType('all')}
          />
        </Stack>
        
        <Alert icon={<IconAlertCircle size={16} />} color="blue" mt="md">
          {getExportDescription()}
        </Alert>
      </div>

      <Divider />

      {/* Export Format */}
      <div>
        <Text fw={500} mb="md">Export Format</Text>
        <Select
          label="File Format"
          data={[
            { value: 'EXCEL', label: 'Excel (.xlsx)' },
            { value: 'CSV', label: 'CSV (.csv)' },
          ]}
          value={format}
          onChange={(value) => setFormat(value as 'EXCEL' | 'CSV')}
        />
      </div>

      <Divider />

      {/* Field Selection */}
      <div>
        <Text fw={500} mb="md">Fields to Export</Text>
        <MultiSelect
          label="Select Fields"
          description="Choose which employee fields to include in the export"
          data={availableFields}
          value={includeFields}
          onChange={setIncludeFields}
          searchable
          placeholder="Select fields to export"
        />
        
        <Group mt="md">
          <Button
            size="xs"
            variant="light"
            onClick={() => setIncludeFields(availableFields.map(f => f.value))}
          >
            Select All
          </Button>
          <Button
            size="xs"
            variant="light"
            onClick={() => setIncludeFields([])}
          >
            Clear All
          </Button>
          <Button
            size="xs"
            variant="light"
            onClick={() => setIncludeFields([
              'employeeNumber',
              'firstName',
              'lastName',
              'email',
              'departmentName',
              'positionName',
              'status',
              'hireDate',
            ])}
          >
            Basic Fields
          </Button>
        </Group>
      </div>

      {/* Preview */}
      <Paper p="md" withBorder>
        <Text size="sm" fw={500} mb="xs">Export Preview</Text>
        <Text size="sm" c="dimmed">
          <strong>Scope:</strong> {getExportDescription()}
        </Text>
        <Text size="sm" c="dimmed">
          <strong>Format:</strong> {format === 'EXCEL' ? 'Excel (.xlsx)' : 'CSV (.csv)'}
        </Text>
        <Text size="sm" c="dimmed">
          <strong>Fields:</strong> {includeFields.length} field(s) selected
        </Text>
        {includeFields.length > 0 && (
          <Text size="xs" c="dimmed" mt="xs">
            {includeFields.slice(0, 5).join(', ')}
            {includeFields.length > 5 && ` and ${includeFields.length - 5} more...`}
          </Text>
        )}
      </Paper>

      {/* Actions */}
      <Group justify="flex-end">
        <Button variant="light" onClick={onClose} disabled={isExporting}>
          Cancel
        </Button>
        <Button
          leftSection={<IconDownload size={16} />}
          onClick={handleExport}
          disabled={includeFields.length === 0}
          loading={isExporting}
        >
          Export {format === 'EXCEL' ? 'Excel' : 'CSV'}
        </Button>
      </Group>
    </Stack>
  );
};