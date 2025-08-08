import { useState, useCallback } from 'react';
import { getFieldErrors, isValidationError } from '../utils/errorHandling';

interface UseFormErrorsReturn {
  errors: Record<string, string>;
  hasErrors: boolean;
  setError: (field: string, message: string) => void;
  setErrors: (errors: Record<string, string>) => void;
  clearError: (field: string) => void;
  clearErrors: () => void;
  handleApiError: (error: unknown) => void;
  getFieldError: (field: string) => string | undefined;
  hasFieldError: (field: string) => boolean;
}

export function useFormErrors(): UseFormErrorsReturn {
  const [errors, setErrorsState] = useState<Record<string, string>>({});

  const hasErrors = Object.keys(errors).length > 0;

  const setError = useCallback((field: string, message: string) => {
    setErrorsState(prev => ({
      ...prev,
      [field]: message,
    }));
  }, []);

  const setErrors = useCallback((newErrors: Record<string, string>) => {
    setErrorsState(newErrors);
  }, []);

  const clearError = useCallback((field: string) => {
    setErrorsState(prev => {
      const { [field]: _, ...rest } = prev;
      return rest;
    });
  }, []);

  const clearErrors = useCallback(() => {
    setErrorsState({});
  }, []);

  const handleApiError = useCallback((error: unknown) => {
    if (isValidationError(error)) {
      const fieldErrors = getFieldErrors(error);
      setErrors(fieldErrors);
    } else {
      // Clear field errors for non-validation errors
      clearErrors();
    }
  }, [setErrors, clearErrors]);

  const getFieldError = useCallback((field: string): string | undefined => {
    return errors[field];
  }, [errors]);

  const hasFieldError = useCallback((field: string): boolean => {
    return field in errors;
  }, [errors]);

  return {
    errors,
    hasErrors,
    setError,
    setErrors,
    clearError,
    clearErrors,
    handleApiError,
    getFieldError,
    hasFieldError,
  };
}