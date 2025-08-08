// Email content editor component

import React from 'react';
import {
  Paper,
  Title,
  TextInput,
  Textarea,
  Select,
  Group,
  Stack,
  Text,
} from '@mantine/core';
import { DateTimePicker } from '@mantine/dates';
import { IconEdit, IconClock, IconFlag } from '@tabler/icons-react';
import type { EmailPriority } from '../types';

interface EmailContentEditorProps {
  subject: string;
  content: string;
  priority: EmailPriority;
  scheduledAt?: string;
  onSubjectChange: (subject: string) => void;
  onContentChange: (content: string) => void;
  onPriorityChange: (priority: EmailPriority) => void;
  onScheduledAtChange: (scheduledAt: string | undefined) => void;
}

export const EmailContentEditor: React.FC<EmailContentEditorProps> = ({
  subject,
  content,
  priority,
  scheduledAt,
  onSubjectChange,
  onContentChange,
  onPriorityChange,
  onScheduledAtChange,
}) => {
  const priorityOptions = [
    { value: 'LOW', label: 'Low Priority' },
    { value: 'NORMAL', label: 'Normal Priority' },
    { value: 'HIGH', label: 'High Priority' },
  ];

  const handleScheduledAtChange = (date: Date | null) => {
    onScheduledAtChange(date ? date.toISOString() : undefined);
  };

  return (
    <Paper p="md" withBorder>
      <Stack gap="md">
        <Title order={4}>
          <Group gap="xs">
            <IconEdit size={20} />
            Email Content
          </Group>
        </Title>

        {/* Subject */}
        <TextInput
          label="Subject"
          placeholder="Enter email subject..."
          value={subject}
          onChange={(e) => onSubjectChange(e.target.value)}
          required
          error={!subject.trim() ? 'Subject is required' : null}
        />

        {/* Priority and Scheduling */}
        <Group grow>
          <Select
            label="Priority"
            placeholder="Select priority"
            value={priority}
            onChange={(value) => onPriorityChange(value as EmailPriority)}
            data={priorityOptions}
            leftSection={<IconFlag size={16} />}
          />

          <DateTimePicker
            label="Schedule Send (Optional)"
            placeholder="Send immediately"
            value={scheduledAt ? new Date(scheduledAt) : null}
            onChange={handleScheduledAtChange}
            leftSection={<IconClock size={16} />}
            clearable
            minDate={new Date()}
          />
        </Group>

        {/* Content */}
        <div>
          <Text size="sm" fw={500} mb="xs">
            Email Content *
          </Text>
          <Textarea
            placeholder="Enter your email content here..."
            value={content}
            onChange={(e) => onContentChange(e.target.value)}
            minRows={10}
            maxRows={20}
            autosize
            error={!content.trim() ? 'Content is required' : null}
          />
          <Text size="xs" c="dimmed" mt="xs">
            You can use variables in your content (e.g., {'{firstName}'}, {'{lastName}'}, {'{department}'}).
            Available variables will be shown when you select a template.
          </Text>
        </div>

        {/* Character Count */}
        <Group justify="space-between">
          <Text size="xs" c="dimmed">
            Subject: {subject.length} characters
          </Text>
          <Text size="xs" c="dimmed">
            Content: {content.length} characters
          </Text>
        </Group>
      </Stack>
    </Paper>
  );
};