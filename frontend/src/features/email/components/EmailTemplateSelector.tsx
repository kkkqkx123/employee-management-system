// Email template selector component

import React, { useState } from 'react';
import {
  Paper,
  Title,
  Select,
  Group,
  Button,
  Stack,
  Text,
  Badge,
  Card,
  SimpleGrid,
  Loader,
  Alert,
  Modal,
  ScrollArea,
} from '@mantine/core';
import { IconTemplate, IconEye, IconX } from '@tabler/icons-react';
import { useEmailTemplates, useEmailTemplate } from '../hooks/useEmailTemplates';
import type { EmailTemplate } from '../types';

interface EmailTemplateSelectorProps {
  onTemplateSelect: (template: any) => void;
  selectedTemplateId?: number;
}

export const EmailTemplateSelector: React.FC<EmailTemplateSelectorProps> = ({
  onTemplateSelect,
  selectedTemplateId,
}) => {
  const [selectedCategory, setSelectedCategory] = useState<string>('');
  const [previewTemplateId, setPreviewTemplateId] = useState<number | null>(null);

  const { data: templates, isLoading, error } = useEmailTemplates();
  const { data: previewTemplate, isLoading: isLoadingPreview } = useEmailTemplate(previewTemplateId || undefined);

  // Group templates by category
  const templatesByCategory = React.useMemo(() => {
    if (!templates) return {};
    
    return templates.reduce((acc, template) => {
      const category = template.category || 'OTHER';
      if (!acc[category]) {
        acc[category] = [];
      }
      acc[category].push(template);
      return acc;
    }, {} as Record<string, EmailTemplate[]>);
  }, [templates]);

  // Filter templates by selected category
  const filteredTemplates = React.useMemo(() => {
    if (!templates) return [];
    if (!selectedCategory) return templates;
    return templates.filter(template => template.category === selectedCategory);
  }, [templates, selectedCategory]);

  const categories = Object.keys(templatesByCategory);

  const handleTemplateSelect = (template: EmailTemplate) => {
    onTemplateSelect(template);
  };

  const handlePreviewTemplate = (templateId: number) => {
    setPreviewTemplateId(templateId);
  };

  const closePreview = () => {
    setPreviewTemplateId(null);
  };

  if (isLoading) {
    return (
      <Paper p="md">
        <Group>
          <Loader size="sm" />
          <Text>Loading email templates...</Text>
        </Group>
      </Paper>
    );
  }

  if (error) {
    return (
      <Alert color="red" title="Error Loading Templates">
        Failed to load email templates. Please try again.
      </Alert>
    );
  }

  return (
    <>
      <Paper p="md" withBorder>
        <Stack gap="md">
          <Group justify="space-between">
            <Title order={4}>
              <Group gap="xs">
                <IconTemplate size={20} />
                Email Templates
              </Group>
            </Title>
            {selectedTemplateId && (
              <Button
                variant="subtle"
                size="xs"
                leftSection={<IconX size={14} />}
                onClick={() => onTemplateSelect({ id: undefined, subject: '', content: '', variables: {} })}
              >
                Clear Template
              </Button>
            )}
          </Group>

          {/* Category Filter */}
          <Select
            placeholder="Filter by category"
            value={selectedCategory}
            onChange={(value) => setSelectedCategory(value || '')}
            data={[
              { value: '', label: 'All Categories' },
              ...categories.map(category => ({
                value: category,
                label: category.charAt(0) + category.slice(1).toLowerCase(),
              })),
            ]}
            clearable
          />

          {/* Template Grid */}
          {filteredTemplates.length > 0 ? (
            <SimpleGrid cols={{ base: 1, sm: 2, lg: 3 }} spacing="sm">
              {filteredTemplates.map((template) => (
                <Card
                  key={template.id}
                  p="sm"
                  withBorder
                  style={{
                    cursor: 'pointer',
                    borderColor: selectedTemplateId === template.id ? 'var(--mantine-color-blue-5)' : undefined,
                    backgroundColor: selectedTemplateId === template.id ? 'var(--mantine-color-blue-0)' : undefined,
                  }}
                >
                  <Stack gap="xs">
                    <Group justify="space-between" align="flex-start">
                      <Text fw={500} size="sm" lineClamp={2}>
                        {template.name}
                      </Text>
                      <Badge
                        size="xs"
                        variant="light"
                        color={template.active ? 'green' : 'gray'}
                      >
                        {template.category}
                      </Badge>
                    </Group>

                    <Text size="xs" c="dimmed" lineClamp={2}>
                      {template.subject}
                    </Text>

                    <Group justify="space-between" mt="xs">
                      <Button
                        size="xs"
                        variant="light"
                        onClick={() => handleTemplateSelect(template)}
                        disabled={!template.active}
                      >
                        Use Template
                      </Button>
                      <Button
                        size="xs"
                        variant="subtle"
                        leftSection={<IconEye size={12} />}
                        onClick={() => handlePreviewTemplate(template.id)}
                      >
                        Preview
                      </Button>
                    </Group>
                  </Stack>
                </Card>
              ))}
            </SimpleGrid>
          ) : (
            <Text c="dimmed" ta="center" py="xl">
              {selectedCategory ? 'No templates found in this category.' : 'No email templates available.'}
            </Text>
          )}
        </Stack>
      </Paper>

      {/* Template Preview Modal */}
      <Modal
        opened={!!previewTemplateId}
        onClose={closePreview}
        title="Template Preview"
        size="lg"
      >
        {isLoadingPreview ? (
          <Group justify="center" py="xl">
            <Loader />
            <Text>Loading template preview...</Text>
          </Group>
        ) : previewTemplate ? (
          <Stack gap="md">
            <div>
              <Text fw={500} mb="xs">Template Name</Text>
              <Text>{previewTemplate.name}</Text>
            </div>

            <div>
              <Text fw={500} mb="xs">Category</Text>
              <Badge variant="light">{previewTemplate.category}</Badge>
            </div>

            <div>
              <Text fw={500} mb="xs">Subject</Text>
              <Text>{previewTemplate.subject}</Text>
            </div>

            <div>
              <Text fw={500} mb="xs">Content</Text>
              <ScrollArea h={300}>
                <Paper p="sm" bg="gray.0" style={{ whiteSpace: 'pre-wrap' }}>
                  {previewTemplate.content}
                </Paper>
              </ScrollArea>
            </div>

            {previewTemplate.variableDefinitions && previewTemplate.variableDefinitions.length > 0 && (
              <div>
                <Text fw={500} mb="xs">Available Variables</Text>
                <Stack gap="xs">
                  {previewTemplate.variableDefinitions.map((variable, index) => (
                    <Group key={index} justify="space-between">
                      <Text size="sm">
                        <Text span fw={500}>{variable.name}</Text>
                        {variable.required && <Text span c="red"> *</Text>}
                      </Text>
                      <Text size="xs" c="dimmed">
                        {variable.description}
                      </Text>
                    </Group>
                  ))}
                </Stack>
              </div>
            )}

            <Group justify="flex-end" mt="md">
              <Button variant="outline" onClick={closePreview}>
                Close
              </Button>
              <Button onClick={() => {
                handleTemplateSelect(previewTemplate);
                closePreview();
              }}>
                Use This Template
              </Button>
            </Group>
          </Stack>
        ) : null}
      </Modal>
    </>
  );
};