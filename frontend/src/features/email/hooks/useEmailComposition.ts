// Email composition hook

import { useState, useCallback } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { notifications } from '@mantine/notifications';
import { EmailApiService } from '../services/emailApi';
import type {
  EmailComposition,
  EmailRecipient,
  BulkEmailRequest,
} from '../types';

export const useEmailComposition = () => {
  const [composition, setComposition] = useState<EmailComposition>({
    subject: '',
    content: '',
    recipients: [],
    variables: {},
    priority: 'NORMAL' as const,
  });

  const [sendJobId, setSendJobId] = useState<string | null>(null);

  // Update composition
  const updateComposition = useCallback((updates: Partial<EmailComposition>) => {
    setComposition(prev => ({ ...prev, ...updates }));
  }, []);

  // Add recipient
  const addRecipient = useCallback((recipient: EmailRecipient) => {
    setComposition(prev => ({
      ...prev,
      recipients: [...prev.recipients.filter(r => r.id !== recipient.id), recipient],
    }));
  }, []);

  // Remove recipient
  const removeRecipient = useCallback((recipientId: string) => {
    setComposition(prev => ({
      ...prev,
      recipients: prev.recipients.filter(r => r.id !== recipientId),
    }));
  }, []);

  // Clear all recipients
  const clearRecipients = useCallback(() => {
    setComposition(prev => ({ ...prev, recipients: [] }));
  }, []);

  // Update variables
  const updateVariables = useCallback((variables: Record<string, string>) => {
    setComposition(prev => ({ ...prev, variables }));
  }, []);

  // Email preview mutation
  const previewMutation = useMutation({
    mutationFn: async () => {
      const recipientIds = composition.recipients.map(r => r.id);
      const recipientTypes = composition.recipients.map(r => r.type).filter(type => type !== 'group') as ('employee' | 'department')[];
      
      const response = await EmailApiService.previewEmail({
        templateId: composition.templateId,
        subject: composition.subject,
        content: composition.content,
        variables: composition.variables,
        recipientIds,
        recipientTypes,
      });
      
      return response.data;
    },
    onError: (error: any) => {
      notifications.show({
        title: 'Preview Error',
        message: error.message || 'Failed to generate email preview',
        color: 'red',
      });
    },
  });

  // Email validation mutation
  const validateMutation = useMutation({
    mutationFn: async () => {
      const recipientIds = composition.recipients.map(r => r.id);
      
      const response = await EmailApiService.validateEmail({
        subject: composition.subject,
        content: composition.content,
        recipientIds,
        variables: composition.variables,
      });
      
      return response.data;
    },
    onError: (error: any) => {
      notifications.show({
        title: 'Validation Error',
        message: error.message || 'Failed to validate email',
        color: 'red',
      });
    },
  });

  // Send email mutation
  const sendMutation = useMutation({
    mutationFn: async () => {
      const recipientIds = composition.recipients.map(r => r.id);
      const recipientTypes = composition.recipients.map(r => r.type).filter(type => type !== 'group') as ('employee' | 'department')[];
      
      const request: BulkEmailRequest = {
        templateId: composition.templateId,
        subject: composition.subject,
        content: composition.content,
        recipientIds,
        recipientTypes,
        variables: composition.variables,
        priority: composition.priority,
        scheduledAt: composition.scheduledAt,
      };
      
      const response = await EmailApiService.sendBulkEmail(request);
      return response.data;
    },
    onSuccess: (data) => {
      setSendJobId(data.jobId);
      notifications.show({
        title: 'Email Sent',
        message: 'Your email is being sent. You can track the progress below.',
        color: 'green',
      });
    },
    onError: (error: any) => {
      notifications.show({
        title: 'Send Error',
        message: error.message || 'Failed to send email',
        color: 'red',
      });
    },
  });

  // Email send progress query
  const { data: sendProgress, isLoading: isLoadingProgress } = useQuery({
    queryKey: ['email', 'progress', sendJobId],
    queryFn: async () => {
      if (!sendJobId) throw new Error('No job ID');
      const response = await EmailApiService.getEmailSendProgress(sendJobId);
      return response.data;
    },
    enabled: !!sendJobId,
    refetchInterval: (query) => {
      // Stop polling when job is complete
      return query.state.data?.inProgress ? 2000 : false;
    },
  });

  // Reset composition
  const resetComposition = useCallback(() => {
    setComposition({
      subject: '',
      content: '',
      recipients: [],
      variables: {},
      priority: 'NORMAL' as const,
    });
    setSendJobId(null);
  }, []);

  // Load template into composition
  const loadTemplate = useCallback((template: any) => {
    setComposition(prev => ({
      ...prev,
      templateId: template.id,
      subject: template.subject,
      content: template.content,
      variables: template.variables?.reduce((acc: Record<string, string>, variable: any) => {
        acc[variable.name] = variable.defaultValue || '';
        return acc;
      }, {}) || {},
    }));
  }, []);

  return {
    composition,
    updateComposition,
    addRecipient,
    removeRecipient,
    clearRecipients,
    updateVariables,
    loadTemplate,
    resetComposition,
    
    // Mutations
    previewEmail: previewMutation.mutateAsync,
    isPreviewLoading: previewMutation.isPending,
    previewData: previewMutation.data,
    
    validateEmail: validateMutation.mutateAsync,
    isValidating: validateMutation.isPending,
    validationResult: validateMutation.data,
    
    sendEmail: sendMutation.mutateAsync,
    isSending: sendMutation.isPending,
    
    // Progress tracking
    sendProgress,
    isLoadingProgress,
    sendJobId,
  };
};