// Email send progress component

import React from 'react';
import {
  Modal,
  Stack,
  Group,
  Text,
  Progress,
  Alert,
  Badge,
  ScrollArea,
  Paper,
  Button,
  Divider,
} from '@mantine/core';
import {
  IconMail,
  IconCheck,
  IconX,
  IconClock,
  IconAlertCircle,
  IconRefresh,
} from '@tabler/icons-react';
import type { EmailSendProgress as EmailSendProgressType } from '../types';

interface EmailSendProgressProps {
  opened: boolean;
  onClose: () => void;
  jobId: string;
  progress?: EmailSendProgressType;
}

export const EmailSendProgress: React.FC<EmailSendProgressProps> = ({
  opened,
  onClose,
  jobId,
  progress,
}) => {
  if (!progress) {
    return (
      <Modal
        opened={opened}
        onClose={onClose}
        title="Email Send Progress"
        size="md"
      >
        <Group justify="center" py="xl">
          <IconClock size={24} />
          <Text>Loading progress...</Text>
        </Group>
      </Modal>
    );
  }

  const progressPercentage = progress.total > 0 ? Math.round((progress.sent / progress.total) * 100) : 0;
  const isComplete = !progress.inProgress;
  const hasErrors = progress.failed > 0;

  const getProgressColor = () => {
    if (hasErrors && isComplete) return 'red';
    if (isComplete) return 'green';
    return 'blue';
  };

  const getStatusIcon = () => {
    if (progress.inProgress) return <IconClock size={20} />;
    if (hasErrors) return <IconAlertCircle size={20} />;
    return <IconCheck size={20} />;
  };

  const getStatusText = () => {
    if (progress.inProgress) return 'Sending emails...';
    if (hasErrors && progress.sent === 0) return 'Failed to send emails';
    if (hasErrors) return 'Completed with errors';
    return 'All emails sent successfully';
  };

  return (
    <Modal
      opened={opened}
      onClose={onClose}
      title={
        <Group gap="xs">
          <IconMail size={20} />
          <Text fw={500}>Email Send Progress</Text>
        </Group>
      }
      size="md"
      closeOnClickOutside={isComplete}
      closeOnEscape={isComplete}
    >
      <Stack gap="md">
        {/* Job ID */}
        <Paper p="sm" bg="gray.0">
          <Group justify="space-between">
            <Text size="sm" c="dimmed">Job ID:</Text>
            <Text size="sm" ff="monospace">{jobId}</Text>
          </Group>
        </Paper>

        {/* Status */}
        <Group gap="xs">
          {getStatusIcon()}
          <Text fw={500}>{getStatusText()}</Text>
          {progress.inProgress && (
            <Badge color="blue" variant="light">
              In Progress
            </Badge>
          )}
        </Group>

        {/* Progress Bar */}
        <div>
          <Group justify="space-between" mb="xs">
            <Text size="sm">Progress</Text>
            <Text size="sm">{progressPercentage}%</Text>
          </Group>
          <Progress
            value={progressPercentage}
            color={getProgressColor()}
            size="lg"
            animated={progress.inProgress}
          />
        </div>

        {/* Statistics */}
        <Group justify="space-around">
          <div style={{ textAlign: 'center' }}>
            <Text size="xl" fw={700} c="blue">
              {progress.total}
            </Text>
            <Text size="sm" c="dimmed">Total</Text>
          </div>
          <div style={{ textAlign: 'center' }}>
            <Text size="xl" fw={700} c="green">
              {progress.sent}
            </Text>
            <Text size="sm" c="dimmed">Sent</Text>
          </div>
          <div style={{ textAlign: 'center' }}>
            <Text size="xl" fw={700} c="red">
              {progress.failed}
            </Text>
            <Text size="sm" c="dimmed">Failed</Text>
          </div>
        </Group>

        {/* Errors */}
        {progress.errors.length > 0 && (
          <>
            <Divider />
            <div>
              <Group justify="space-between" mb="xs">
                <Text size="sm" fw={500} c="red">
                  <Group gap="xs">
                    <IconX size={16} />
                    Send Errors ({progress.errors.length})
                  </Group>
                </Text>
              </Group>
              
              <ScrollArea h={200}>
                <Stack gap="xs">
                  {progress.errors.map((error, index) => (
                    <Alert
                      key={index}
                      color="red"
                      variant="light"
                      p="sm"
                    >
                      <Group justify="space-between" align="flex-start">
                        <div>
                          <Text size="sm" fw={500}>{error.recipient}</Text>
                          <Text size="xs" c="dimmed">{error.error}</Text>
                        </div>
                      </Group>
                    </Alert>
                  ))}
                </Stack>
              </ScrollArea>
            </div>
          </>
        )}

        {/* Success Message */}
        {isComplete && !hasErrors && (
          <Alert
            icon={<IconCheck size={16} />}
            title="Success!"
            color="green"
            variant="light"
          >
            All {progress.sent} emails have been sent successfully.
          </Alert>
        )}

        {/* Partial Success Message */}
        {isComplete && hasErrors && progress.sent > 0 && (
          <Alert
            icon={<IconAlertCircle size={16} />}
            title="Partially Completed"
            color="yellow"
            variant="light"
          >
            {progress.sent} out of {progress.total} emails were sent successfully. 
            {progress.failed} emails failed to send.
          </Alert>
        )}

        {/* Actions */}
        <Group justify="flex-end" mt="md">
          {progress.inProgress ? (
            <Button
              variant="outline"
              leftSection={<IconRefresh size={16} />}
              onClick={() => window.location.reload()}
            >
              Refresh
            </Button>
          ) : (
            <Button onClick={onClose}>
              Close
            </Button>
          )}
        </Group>
      </Stack>
    </Modal>
  );
};