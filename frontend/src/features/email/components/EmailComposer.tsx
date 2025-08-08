// Email composer component

import React, { useState } from 'react';
import {
  Container,
  Paper,
  Title,
  Grid,
  Stack,
  Button,
  Group,
  Divider,
  Alert,
  LoadingOverlay,
} from '@mantine/core';
import { IconSend, IconEye, IconCheck, IconAlertCircle } from '@tabler/icons-react';
import { useEmailComposition } from '../hooks/useEmailComposition';
import { EmailTemplateSelector } from './EmailTemplateSelector';
import { EmailRecipientPicker } from './EmailRecipientPicker';
import { EmailContentEditor } from './EmailContentEditor';
import { EmailVariableEditor } from './EmailVariableEditor';
import { EmailPreviewModal } from './EmailPreviewModal';
import { EmailSendProgress } from './EmailSendProgress';

export const EmailComposer: React.FC = () => {
  const [showPreview, setShowPreview] = useState(false);
  const [showProgress, setShowProgress] = useState(false);

  const {
    composition,
    updateComposition,
    addRecipient,
    removeRecipient,
    clearRecipients,
    updateVariables,
    loadTemplate,
    resetComposition,
    
    previewEmail,
    isPreviewLoading,
    previewData,
    
    validateEmail,
    isValidating,
    validationResult,
    
    sendEmail,
    isSending,
    
    sendProgress,
    sendJobId,
  } = useEmailComposition();

  const handlePreview = async () => {
    try {
      await previewEmail();
      setShowPreview(true);
    } catch (error) {
      // Error is handled in the hook
    }
  };

  const handleValidateAndSend = async () => {
    try {
      // First validate the email
      const validation = await validateEmail();
      
      if (!validation.isValid) {
        // Validation errors are shown in the UI
        return;
      }
      
      // If validation passes, send the email
      await sendEmail();
      setShowProgress(true);
    } catch (error) {
      // Error is handled in the hook
    }
  };

  const handleReset = () => {
    resetComposition();
    setShowPreview(false);
    setShowProgress(false);
  };

  const isFormValid = composition.subject.trim() && 
                     composition.content.trim() && 
                     composition.recipients.length > 0;

  return (
    <Container size="xl" py="md">
      <Paper shadow="sm" p="lg" pos="relative">
        <LoadingOverlay visible={isSending} />
        
        <Stack gap="lg">
          <Group justify="space-between">
            <Title order={2}>Compose Email</Title>
            <Group>
              <Button
                variant="outline"
                onClick={handleReset}
                disabled={isSending}
              >
                Reset
              </Button>
              <Button
                variant="outline"
                leftSection={<IconEye size={16} />}
                onClick={handlePreview}
                disabled={!isFormValid || isPreviewLoading}
                loading={isPreviewLoading}
              >
                Preview
              </Button>
              <Button
                leftSection={<IconSend size={16} />}
                onClick={handleValidateAndSend}
                disabled={!isFormValid || isSending}
                loading={isSending || isValidating}
              >
                Send Email
              </Button>
            </Group>
          </Group>

          <Divider />

          {/* Validation Results */}
          {validationResult && !validationResult.isValid && (
            <Alert
              icon={<IconAlertCircle size={16} />}
              title="Validation Errors"
              color="red"
            >
              <Stack gap="xs">
                {validationResult.errors.map((error, index) => (
                  <div key={index}>
                    <strong>{error.field}:</strong> {error.message}
                  </div>
                ))}
              </Stack>
            </Alert>
          )}

          {validationResult && validationResult.isValid && (
            <Alert
              icon={<IconCheck size={16} />}
              title="Validation Passed"
              color="green"
            >
              Your email is ready to send!
            </Alert>
          )}

          <Grid>
            <Grid.Col span={{ base: 12, md: 8 }}>
              <Stack gap="lg">
                {/* Template Selection */}
                <EmailTemplateSelector
                  onTemplateSelect={loadTemplate}
                  selectedTemplateId={composition.templateId}
                />

                {/* Content Editor */}
                <EmailContentEditor
                  subject={composition.subject}
                  content={composition.content}
                  priority={composition.priority}
                  scheduledAt={composition.scheduledAt}
                  onSubjectChange={(subject) => updateComposition({ subject })}
                  onContentChange={(content) => updateComposition({ content })}
                  onPriorityChange={(priority) => updateComposition({ priority })}
                  onScheduledAtChange={(scheduledAt) => updateComposition({ scheduledAt })}
                />

                {/* Variable Editor */}
                {Object.keys(composition.variables).length > 0 && (
                  <EmailVariableEditor
                    variables={composition.variables}
                    onVariablesChange={updateVariables}
                  />
                )}
              </Stack>
            </Grid.Col>

            <Grid.Col span={{ base: 12, md: 4 }}>
              {/* Recipient Picker */}
              <EmailRecipientPicker
                recipients={composition.recipients}
                onAddRecipient={addRecipient}
                onRemoveRecipient={removeRecipient}
                onClearRecipients={clearRecipients}
              />
            </Grid.Col>
          </Grid>
        </Stack>
      </Paper>

      {/* Preview Modal */}
      <EmailPreviewModal
        opened={showPreview}
        onClose={() => setShowPreview(false)}
        previewData={previewData}
        composition={composition}
        isLoading={isPreviewLoading}
      />

      {/* Send Progress Modal */}
      {sendJobId && (
        <EmailSendProgress
          opened={showProgress}
          onClose={() => setShowProgress(false)}
          jobId={sendJobId}
          progress={sendProgress}
        />
      )}
    </Container>
  );
};