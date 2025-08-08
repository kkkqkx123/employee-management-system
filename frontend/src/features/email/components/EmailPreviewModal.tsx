// Email preview modal component

import React from 'react';
import {
  Modal,
  Stack,
  Group,
  Text,
  Paper,
  Badge,
  Divider,
  ScrollArea,
  Loader,
  Alert,
  Button,
} from '@mantine/core';
import { IconEye, IconMail, IconUsers, IconClock, IconAlertCircle } from '@tabler/icons-react';
import type { EmailPreview, EmailComposition } from '../types';

interface EmailPreviewModalProps {
  opened: boolean;
  onClose: () => void;
  previewData?: EmailPreview;
  composition: EmailComposition;
  isLoading: boolean;
}

export const EmailPreviewModal: React.FC<EmailPreviewModalProps> = ({
  opened,
  onClose,
  previewData,
  composition,
  isLoading,
}) => {
  const formatRecipientCount = (count: number) => {
    return `${count} ${count === 1 ? 'recipient' : 'recipients'}`;
  };

  const formatPriority = (priority: string) => {
    const colors = {
      LOW: 'gray',
      NORMAL: 'blue',
      HIGH: 'red',
    };
    return { color: colors[priority as keyof typeof colors] || 'blue', label: priority };
  };

  return (
    <Modal
      opened={opened}
      onClose={onClose}
      title={
        <Group gap="xs">
          <IconEye size={20} />
          <Text fw={500}>Email Preview</Text>
        </Group>
      }
      size="lg"
    >
      <Stack gap="md">
        {isLoading ? (
          <Group justify="center" py="xl">
            <Loader />
            <Text>Generating preview...</Text>
          </Group>
        ) : previewData ? (
          <>
            {/* Email Metadata */}
            <Paper p="md" bg="gray.0">
              <Stack gap="sm">
                <Group justify="space-between">
                  <Group gap="xs">
                    <IconMail size={16} />
                    <Text size="sm" fw={500}>Email Details</Text>
                  </Group>
                  <Badge
                    color={formatPriority(composition.priority).color}
                    variant="light"
                  >
                    {formatPriority(composition.priority).label} Priority
                  </Badge>
                </Group>

                <Group>
                  <Group gap="xs">
                    <IconUsers size={14} />
                    <Text size="sm">{formatRecipientCount(previewData.recipientCount)}</Text>
                  </Group>
                  
                  {composition.scheduledAt && (
                    <Group gap="xs">
                      <IconClock size={14} />
                      <Text size="sm">
                        Scheduled: {new Date(composition.scheduledAt).toLocaleString()}
                      </Text>
                    </Group>
                  )}
                </Group>

                {previewData.estimatedDeliveryTime && (
                  <Text size="xs" c="dimmed">
                    Estimated delivery time: {previewData.estimatedDeliveryTime}
                  </Text>
                )}
              </Stack>
            </Paper>

            <Divider />

            {/* Recipients Summary */}
            <div>
              <Text size="sm" fw={500} mb="xs">Recipients</Text>
              <Stack gap="xs">
                {composition.recipients.map((recipient) => (
                  <Group key={recipient.id} gap="xs">
                    <Badge size="sm" variant="outline">
                      {recipient.type}
                    </Badge>
                    <Text size="sm">{recipient.name}</Text>
                    {recipient.email && (
                      <Text size="xs" c="dimmed">({recipient.email})</Text>
                    )}
                    {recipient.employeeCount && (
                      <Text size="xs" c="dimmed">
                        ({recipient.employeeCount} employees)
                      </Text>
                    )}
                  </Group>
                ))}
              </Stack>
            </div>

            <Divider />

            {/* Email Content Preview */}
            <div>
              <Text size="sm" fw={500} mb="xs">Subject</Text>
              <Paper p="sm" bg="blue.0" style={{ borderLeft: '4px solid var(--mantine-color-blue-5)' }}>
                <Text fw={500}>{previewData.subject}</Text>
              </Paper>
            </div>

            <div>
              <Text size="sm" fw={500} mb="xs">Content</Text>
              <ScrollArea h={300}>
                <Paper p="md" bg="gray.0" style={{ whiteSpace: 'pre-wrap' }}>
                  {previewData.content}
                </Paper>
              </ScrollArea>
            </div>

            {/* Variables Used */}
            {Object.keys(composition.variables).length > 0 && (
              <>
                <Divider />
                <div>
                  <Text size="sm" fw={500} mb="xs">Variables</Text>
                  <Stack gap="xs">
                    {Object.entries(composition.variables).map(([key, value]) => (
                      <Group key={key} justify="space-between">
                        <Badge size="sm" variant="light">{`{${key}}`}</Badge>
                        <Text size="sm">{value || '<empty>'}</Text>
                      </Group>
                    ))}
                  </Stack>
                </div>
              </>
            )}

            {/* Warning for empty variables */}
            {Object.values(composition.variables).some(value => !value.trim()) && (
              <Alert
                icon={<IconAlertCircle size={16} />}
                title="Empty Variables"
                color="yellow"
                variant="light"
              >
                Some variables are empty. They will be automatically populated with recipient data when possible,
                or left blank if no data is available.
              </Alert>
            )}
          </>
        ) : (
          <Alert
            icon={<IconAlertCircle size={16} />}
            title="Preview Not Available"
            color="red"
          >
            Failed to generate email preview. Please check your email content and try again.
          </Alert>
        )}

        <Group justify="flex-end" mt="md">
          <Button variant="outline" onClick={onClose}>
            Close Preview
          </Button>
        </Group>
      </Stack>
    </Modal>
  );
};