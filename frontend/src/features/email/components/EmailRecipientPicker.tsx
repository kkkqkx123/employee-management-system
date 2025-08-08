// Email recipient picker component

import React, { useState, useEffect } from 'react';
import {
  Paper,
  Title,
  TextInput,
  Stack,
  Group,
  Button,
  Badge,
  Text,
  ScrollArea,
  ActionIcon,
  Divider,
  Loader,

  Tabs,
  Card,
  Checkbox,
} from '@mantine/core';
import {
  IconSearch,
  IconUsers,
  IconUser,
  IconBuilding,
  IconX,
  IconTrash,
  IconMail,
} from '@tabler/icons-react';
import { useDebouncedValue } from '@mantine/hooks';
import { useEmailRecipients } from '../hooks/useEmailRecipients';
import type { EmailRecipient } from '../types';

interface EmailRecipientPickerProps {
  recipients: EmailRecipient[];
  onAddRecipient: (recipient: EmailRecipient) => void;
  onRemoveRecipient: (recipientId: string) => void;
  onClearRecipients: () => void;
}

export const EmailRecipientPicker: React.FC<EmailRecipientPickerProps> = ({
  recipients,
  onAddRecipient,
  onRemoveRecipient,
  onClearRecipients,
}) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [debouncedSearchTerm] = useDebouncedValue(searchTerm, 300);
  const [selectedRecipients, setSelectedRecipients] = useState<Set<string>>(new Set());
  const [recipientCount, setRecipientCount] = useState<number>(0);

  const {
    employeeRecipients,
    departmentRecipients,
    filteredRecipients,
    isLoading,
    calculateRecipientCount,
    setSearchTerm: setApiSearchTerm,
  } = useEmailRecipients();

  // Update API search term when debounced value changes
  useEffect(() => {
    setApiSearchTerm(debouncedSearchTerm);
  }, [debouncedSearchTerm, setApiSearchTerm]);

  // Calculate total recipient count
  useEffect(() => {
    const calculateCount = async () => {
      if (recipients.length > 0) {
        try {
          const count = await calculateRecipientCount(recipients);
          setRecipientCount(count);
        } catch (error) {
          console.error('Failed to calculate recipient count:', error);
        }
      } else {
        setRecipientCount(0);
      }
    };

    calculateCount();
  }, [recipients, calculateRecipientCount]);

  const handleAddRecipient = (recipient: EmailRecipient) => {
    onAddRecipient(recipient);
  };

  const handleBulkAdd = () => {
    selectedRecipients.forEach(recipientId => {
      const recipient = filteredRecipients.find(r => r.id === recipientId);
      if (recipient) {
        onAddRecipient(recipient);
      }
    });
    setSelectedRecipients(new Set());
  };

  const handleSelectRecipient = (recipientId: string, checked: boolean) => {
    const newSelected = new Set(selectedRecipients);
    if (checked) {
      newSelected.add(recipientId);
    } else {
      newSelected.delete(recipientId);
    }
    setSelectedRecipients(newSelected);
  };

  const handleSelectAll = (recipientType: 'employee' | 'department') => {
    const relevantRecipients = recipientType === 'employee' ? employeeRecipients : departmentRecipients;
    const newSelected = new Set(selectedRecipients);
    
    relevantRecipients.forEach(recipient => {
      if (!recipients.some(r => r.id === recipient.id)) {
        newSelected.add(recipient.id);
      }
    });
    
    setSelectedRecipients(newSelected);
  };

  const isRecipientSelected = (recipientId: string) => {
    return recipients.some(r => r.id === recipientId);
  };

  const getRecipientIcon = (type: string) => {
    switch (type) {
      case 'employee':
        return <IconUser size={16} />;
      case 'department':
        return <IconBuilding size={16} />;
      default:
        return <IconUsers size={16} />;
    }
  };

  return (
    <Paper p="md" withBorder h="fit-content">
      <Stack gap="md">
        <Group justify="space-between">
          <Title order={4}>
            <Group gap="xs">
              <IconMail size={20} />
              Recipients
            </Group>
          </Title>
          {recipients.length > 0 && (
            <Badge variant="light" color="blue">
              {recipientCount} {recipientCount === 1 ? 'recipient' : 'recipients'}
            </Badge>
          )}
        </Group>

        {/* Selected Recipients */}
        {recipients.length > 0 && (
          <>
            <Group justify="space-between">
              <Text size="sm" fw={500}>Selected Recipients</Text>
              <Button
                size="xs"
                variant="subtle"
                color="red"
                leftSection={<IconTrash size={12} />}
                onClick={onClearRecipients}
              >
                Clear All
              </Button>
            </Group>

            <ScrollArea h={120}>
              <Stack gap="xs">
                {recipients.map((recipient) => (
                  <Group key={recipient.id} justify="space-between" p="xs" bg="gray.0" style={{ borderRadius: 4 }}>
                    <Group gap="xs">
                      {getRecipientIcon(recipient.type)}
                      <div>
                        <Text size="sm">{recipient.name}</Text>
                        {recipient.email && (
                          <Text size="xs" c="dimmed">{recipient.email}</Text>
                        )}
                        {recipient.employeeCount && (
                          <Text size="xs" c="dimmed">{recipient.employeeCount} employees</Text>
                        )}
                      </div>
                    </Group>
                    <ActionIcon
                      size="sm"
                      variant="subtle"
                      color="red"
                      onClick={() => onRemoveRecipient(recipient.id)}
                    >
                      <IconX size={12} />
                    </ActionIcon>
                  </Group>
                ))}
              </Stack>
            </ScrollArea>

            <Divider />
          </>
        )}

        {/* Search */}
        <TextInput
          placeholder="Search employees and departments..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          leftSection={<IconSearch size={16} />}
          rightSection={isLoading ? <Loader size="xs" /> : null}
        />

        {/* Bulk Actions */}
        {selectedRecipients.size > 0 && (
          <Group justify="space-between">
            <Text size="sm">{selectedRecipients.size} selected</Text>
            <Button size="xs" onClick={handleBulkAdd}>
              Add Selected
            </Button>
          </Group>
        )}

        {/* Recipients Tabs */}
        <Tabs defaultValue="employees">
          <Tabs.List>
            <Tabs.Tab value="employees" leftSection={<IconUser size={16} />}>
              Employees ({employeeRecipients.length})
            </Tabs.Tab>
            <Tabs.Tab value="departments" leftSection={<IconBuilding size={16} />}>
              Departments ({departmentRecipients.length})
            </Tabs.Tab>
          </Tabs.List>

          <Tabs.Panel value="employees" pt="md">
            <Stack gap="xs">
              <Group justify="space-between">
                <Text size="sm" fw={500}>Individual Employees</Text>
                <Button
                  size="xs"
                  variant="subtle"
                  onClick={() => handleSelectAll('employee')}
                >
                  Select All
                </Button>
              </Group>

              <ScrollArea h={300}>
                <Stack gap="xs">
                  {employeeRecipients.map((recipient) => {
                    const isSelected = isRecipientSelected(recipient.id);
                    const isChecked = selectedRecipients.has(recipient.id);

                    return (
                      <Card
                        key={recipient.id}
                        p="xs"
                        withBorder={false}
                        bg={isSelected ? 'green.0' : undefined}
                        style={{ opacity: isSelected ? 0.6 : 1 }}
                      >
                        <Group justify="space-between">
                          <Group gap="xs">
                            <Checkbox
                              checked={isChecked}
                              onChange={(e) => handleSelectRecipient(recipient.id, e.target.checked)}
                              disabled={isSelected}
                            />
                            <div>
                              <Text size="sm">{recipient.name}</Text>
                              <Text size="xs" c="dimmed">{recipient.email}</Text>
                            </div>
                          </Group>
                          {!isSelected && (
                            <Button
                              size="xs"
                              variant="light"
                              onClick={() => handleAddRecipient(recipient)}
                            >
                              Add
                            </Button>
                          )}
                          {isSelected && (
                            <Badge size="sm" color="green">Added</Badge>
                          )}
                        </Group>
                      </Card>
                    );
                  })}

                  {employeeRecipients.length === 0 && !isLoading && (
                    <Text c="dimmed" ta="center" py="md">
                      No employees found
                    </Text>
                  )}
                </Stack>
              </ScrollArea>
            </Stack>
          </Tabs.Panel>

          <Tabs.Panel value="departments" pt="md">
            <Stack gap="xs">
              <Group justify="space-between">
                <Text size="sm" fw={500}>Departments</Text>
                <Button
                  size="xs"
                  variant="subtle"
                  onClick={() => handleSelectAll('department')}
                >
                  Select All
                </Button>
              </Group>

              <ScrollArea h={300}>
                <Stack gap="xs">
                  {departmentRecipients.map((recipient) => {
                    const isSelected = isRecipientSelected(recipient.id);
                    const isChecked = selectedRecipients.has(recipient.id);

                    return (
                      <Card
                        key={recipient.id}
                        p="xs"
                        withBorder={false}
                        bg={isSelected ? 'green.0' : undefined}
                        style={{ opacity: isSelected ? 0.6 : 1 }}
                      >
                        <Group justify="space-between">
                          <Group gap="xs">
                            <Checkbox
                              checked={isChecked}
                              onChange={(e) => handleSelectRecipient(recipient.id, e.target.checked)}
                              disabled={isSelected}
                            />
                            <div>
                              <Text size="sm">{recipient.name}</Text>
                              <Text size="xs" c="dimmed">
                                {recipient.employeeCount} employees
                              </Text>
                            </div>
                          </Group>
                          {!isSelected && (
                            <Button
                              size="xs"
                              variant="light"
                              onClick={() => handleAddRecipient(recipient)}
                            >
                              Add
                            </Button>
                          )}
                          {isSelected && (
                            <Badge size="sm" color="green">Added</Badge>
                          )}
                        </Group>
                      </Card>
                    );
                  })}

                  {departmentRecipients.length === 0 && !isLoading && (
                    <Text c="dimmed" ta="center" py="md">
                      No departments found
                    </Text>
                  )}
                </Stack>
              </ScrollArea>
            </Stack>
          </Tabs.Panel>
        </Tabs>
      </Stack>
    </Paper>
  );
};