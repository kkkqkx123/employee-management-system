// Employee import component for Excel file upload

import React, { useState } from 'react';
import {
  Stack,
  Group,
  Button,
  Text,
  Alert,
  Progress,
  FileInput,
  List,
  Divider,
  Paper,
  Box,
} from '@mantine/core';
import {
  IconUpload,
  IconFileSpreadsheet,
  IconAlertCircle,
  IconCheck,
  IconX,
  IconDownload,
} from '@tabler/icons-react';
import { notifications } from '@mantine/notifications';
import { useImportEmployees } from '../hooks';
import type { EmployeeImportResult } from '../types';

interface EmployeeImportProps {
  onSuccess: () => void;
  onCancel: () => void;
}

export const EmployeeImport: React.FC<EmployeeImportProps> = ({
  onSuccess,
  onCancel,
}) => {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [importResult, setImportResult] = useState<EmployeeImportResult | null>(null);
  const importMutation = useImportEmployees();

  const handleFileSelect = (file: File | null) => {
    setSelectedFile(file);
    setImportResult(null);
  };

  const handleImport = async () => {
    if (!selectedFile) {
      notifications.show({
        title: 'Error',
        message: 'Please select a file to import',
        color: 'red',
      });
      return;
    }

    try {
      const result = await importMutation.mutateAsync(selectedFile);
      setImportResult(result);
      
      if (result.failedImports === 0) {
        notifications.show({
          title: 'Success',
          message: `Successfully imported ${result.successfulImports} employees`,
          color: 'green',
        });
      } else {
        notifications.show({
          title: 'Partial Success',
          message: `Imported ${result.successfulImports} employees, ${result.failedImports} failed`,
          color: 'yellow',
        });
      }
    } catch (error: any) {
      notifications.show({
        title: 'Import Failed',
        message: error.message || 'Failed to import employees',
        color: 'red',
      });
    }
  };

  const downloadTemplate = () => {
    // Create a sample CSV template
    const headers = [
      'firstName',
      'lastName',
      'email',
      'phone',
      'mobilePhone',
      'address',
      'city',
      'state',
      'zipCode',
      'country',
      'dateOfBirth',
      'gender',
      'maritalStatus',
      'nationality',
      'departmentId',
      'positionId',
      'managerId',
      'hireDate',
      'status',
      'employmentType',
      'payType',
      'salary',
      'hourlyRate',
    ];

    const sampleData = [
      'John',
      'Doe',
      'john.doe@example.com',
      '+1-555-0123',
      '+1-555-0124',
      '123 Main St',
      'New York',
      'NY',
      '10001',
      'USA',
      '1990-01-15',
      'MALE',
      'SINGLE',
      'American',
      '1',
      '1',
      '',
      '2024-01-15',
      'ACTIVE',
      'FULL_TIME',
      'SALARIED',
      '75000',
      '',
    ];

    const csvContent = [
      headers.join(','),
      sampleData.join(','),
    ].join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = 'employee_import_template.csv';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  };

  const isImporting = importMutation.isPending;
  const progress = importMutation.progress;

  return (
    <Stack gap="lg">
      {/* Instructions */}
      <Alert icon={<IconAlertCircle size={16} />} color="blue">
        <Text size="sm">
          Upload an Excel (.xlsx) or CSV file containing employee data. 
          Make sure the file includes all required fields: firstName, lastName, email, departmentId, positionId, hireDate, and status.
        </Text>
      </Alert>

      {/* Template Download */}
      <Paper p="md" withBorder>
        <Group justify="space-between">
          <div>
            <Text fw={500}>Download Template</Text>
            <Text size="sm" c="dimmed">
              Download a sample CSV template with the correct format and required fields.
            </Text>
          </div>
          <Button
            variant="light"
            leftSection={<IconDownload size={16} />}
            onClick={downloadTemplate}
          >
            Download Template
          </Button>
        </Group>
      </Paper>

      <Divider />

      {/* File Selection */}
      <div>
        <Text fw={500} mb="md">Select File</Text>
        <FileInput
          label="Employee Data File"
          placeholder="Choose Excel or CSV file"
          accept=".xlsx,.xls,.csv"
          value={selectedFile}
          onChange={handleFileSelect}
          leftSection={<IconFileSpreadsheet size={16} />}
          disabled={isImporting}
        />
      </div>

      {/* Progress */}
      {isImporting && (
        <div>
          <Text size="sm" mb="xs">Importing employees...</Text>
          <Progress value={progress} animated />
          <Text size="xs" c="dimmed" mt="xs">{progress}% complete</Text>
        </div>
      )}

      {/* Import Results */}
      {importResult && (
        <Paper p="md" withBorder>
          <Stack gap="md">
            <Text fw={500}>Import Results</Text>
            
            <Group>
              <Group gap="xs">
                <IconCheck size={16} color="green" />
                <Text size="sm">
                  <strong>{importResult.successfulImports}</strong> employees imported successfully
                </Text>
              </Group>
              
              {importResult.failedImports > 0 && (
                <Group gap="xs">
                  <IconX size={16} color="red" />
                  <Text size="sm">
                    <strong>{importResult.failedImports}</strong> employees failed to import
                  </Text>
                </Group>
              )}
            </Group>

            {importResult.errors.length > 0 && (
              <div>
                <Text size="sm" fw={500} c="red" mb="xs">Errors:</Text>
                <List size="sm" spacing="xs">
                  {importResult.errors.slice(0, 10).map((error, index) => (
                    <List.Item key={index}>
                      <Text size="sm" c="red">{error}</Text>
                    </List.Item>
                  ))}
                  {importResult.errors.length > 10 && (
                    <List.Item>
                      <Text size="sm" c="dimmed">
                        ... and {importResult.errors.length - 10} more errors
                      </Text>
                    </List.Item>
                  )}
                </List>
              </div>
            )}

            {importResult.importedEmployees.length > 0 && (
              <div>
                <Text size="sm" fw={500} c="green" mb="xs">
                  Successfully Imported Employees:
                </Text>
                <List size="sm" spacing="xs">
                  {importResult.importedEmployees.slice(0, 5).map((employee) => (
                    <List.Item key={employee.id}>
                      <Text size="sm">
                        {employee.fullName} ({employee.employeeNumber})
                      </Text>
                    </List.Item>
                  ))}
                  {importResult.importedEmployees.length > 5 && (
                    <List.Item>
                      <Text size="sm" c="dimmed">
                        ... and {importResult.importedEmployees.length - 5} more employees
                      </Text>
                    </List.Item>
                  )}
                </List>
              </div>
            )}
          </Stack>
        </Paper>
      )}

      {/* Actions */}
      <Group justify="flex-end">
        <Button variant="light" onClick={onCancel} disabled={isImporting}>
          {importResult ? 'Close' : 'Cancel'}
        </Button>
        
        {!importResult && (
          <Button
            leftSection={<IconUpload size={16} />}
            onClick={handleImport}
            disabled={!selectedFile || isImporting}
            loading={isImporting}
          >
            Import Employees
          </Button>
        )}
        
        {importResult && importResult.successfulImports > 0 && (
          <Button onClick={onSuccess}>
            Continue
          </Button>
        )}
      </Group>
    </Stack>
  );
};