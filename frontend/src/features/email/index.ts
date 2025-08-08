// Email feature exports

// Components
export { EmailComposer } from './components/EmailComposer';
export { EmailTemplateSelector } from './components/EmailTemplateSelector';
export { EmailRecipientPicker } from './components/EmailRecipientPicker';
export { EmailContentEditor } from './components/EmailContentEditor';
export { EmailVariableEditor } from './components/EmailVariableEditor';
export { EmailPreviewModal } from './components/EmailPreviewModal';
export { EmailSendProgress } from './components/EmailSendProgress';

// Hooks
export { useEmailTemplates, useEmailTemplate, useEmailTemplatesByCategory } from './hooks/useEmailTemplates';
export { useEmailComposition } from './hooks/useEmailComposition';
export { useEmailRecipients } from './hooks/useEmailRecipients';

// Services
export { EmailApiService } from './services/emailApi';

// Types
export type {
  EmailRecipient,
  EmailComposition,
  EmailPreview,
  EmailSendProgress as EmailSendProgressType,
  EmailSendError,
  EmailValidationResult,
  EmailValidationError,
  BulkEmailRequest,
  EmailTemplateVariable,
  EmailTemplateWithVariables,
  EmailTemplate,
  EmailRequest,
  EmailPriority,
  EmailTemplateCategory,
} from './types';