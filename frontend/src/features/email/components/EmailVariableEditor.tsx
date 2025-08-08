// Email variable editor component

import React from 'react';
import {
  Paper,
  Title,
  TextInput,
  Stack,
  Group,
  Text,
  Badge,
  SimpleGrid,
  Alert,
} from '@mantine/core';
import { IconVariable, IconInfoCircle } from '@tabler/icons-react';

interface EmailVariableEditorProps {
  variables: Record<string, string>;
  onVariablesChange: (variables: Record<string, string>) => void;
}

export const EmailVariableEditor: React.FC<EmailVariableEditorProps> = ({
  variables,
  onVariablesChange,
}) => {
  const handleVariableChange = (variableName: string, value: string) => {
    onVariablesChange({
      ...variables,
      [variableName]: value,
    });
  };

  const variableEntries = Object.entries(variables);

  if (variableEntries.length === 0) {
    return null;
  }

  return (
    <Paper p="md" withBorder>
      <Stack gap="md">
        <Title order={4}>
          <Group gap="xs">
            <IconVariable size={20} />
            Template Variables
          </Group>
        </Title>

        <Alert
          icon={<IconInfoCircle size={16} />}
          title="Template Variables"
          color="blue"
          variant="light"
        >
          These variables will be automatically replaced in your email content when sent.
          You can customize the default values below.
        </Alert>

        <SimpleGrid cols={{ base: 1, sm: 2 }} spacing="md">
          {variableEntries.map(([variableName, value]) => (
            <div key={variableName}>
              <Group gap="xs" mb="xs">
                <Text size="sm" fw={500}>
                  {variableName}
                </Text>
                <Badge size="xs" variant="light">
                  {`{${variableName}}`}
                </Badge>
              </Group>
              <TextInput
                placeholder={`Enter value for ${variableName}`}
                value={value}
                onChange={(e) => handleVariableChange(variableName, e.target.value)}
                description={getVariableDescription(variableName)}
              />
            </div>
          ))}
        </SimpleGrid>

        <Text size="xs" c="dimmed">
          <strong>Note:</strong> When sending to multiple recipients, individual-specific variables 
          (like firstName, lastName, email) will be automatically populated for each recipient. 
          The values you set here will be used as fallbacks if the data is not available.
        </Text>
      </Stack>
    </Paper>
  );
};

// Helper function to provide descriptions for common variables
const getVariableDescription = (variableName: string): string => {
  const descriptions: Record<string, string> = {
    firstName: 'Recipient\'s first name',
    lastName: 'Recipient\'s last name',
    fullName: 'Recipient\'s full name',
    email: 'Recipient\'s email address',
    department: 'Recipient\'s department',
    position: 'Recipient\'s job position',
    company: 'Company name',
    companyAddress: 'Company address',
    companyPhone: 'Company phone number',
    companyEmail: 'Company email address',
    senderName: 'Sender\'s name',
    senderTitle: 'Sender\'s title',
    currentDate: 'Current date',
    currentYear: 'Current year',
  };

  return descriptions[variableName] || 'Custom variable';
};