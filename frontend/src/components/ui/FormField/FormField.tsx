import React, { useRef, useEffect } from 'react';
import { clsx } from 'clsx';
import { FormFieldProps } from '../types/ui.types';
import styles from './FormField.module.css';

export const FormField: React.FC<FormFieldProps> = ({
  label,
  error,
  helperText,
  required = false,
  children,
  className,
  labelClassName,
  errorClassName,
  helperClassName,
  testId,
  ariaLabel,
  ariaLabelledBy,
  ariaDescribedBy
}) => {
  const fieldId = React.useId();
  const errorId = `${fieldId}-error`;
  const helperId = `${fieldId}-helper`;
  const previousError = useRef<string | undefined>();

  // Track error changes for accessibility
  useEffect(() => {
    previousError.current = error;
  }, [error]);
  
  const fieldClasses = clsx(
    styles.formField,
    {
      [styles.hasError]: error,
      [styles.required]: required,
    },
    className
  );

  const labelClasses = clsx(
    styles.label,
    {
      [styles.required]: required,
    },
    labelClassName
  );

  const errorClasses = clsx(
    styles.error,
    errorClassName
  );

  const helperClasses = clsx(
    styles.helper,
    helperClassName
  );

  // Build describedBy string
  const describedByIds = [
    error ? errorId : null,
    helperText && !error ? helperId : null,
    ariaDescribedBy
  ].filter(Boolean).join(' ') || undefined;

  // Enhanced child props for accessibility
  const enhancedChildProps = {
    id: fieldId,
    'aria-invalid': !!error,
    'aria-describedby': describedByIds,
    'aria-label': ariaLabel,
    'aria-labelledby': ariaLabelledBy,
    'aria-required': required,
    ...(error && { 'aria-errormessage': errorId })
  };

  // Filter out undefined values
  const cleanChildProps = Object.fromEntries(
    Object.entries(enhancedChildProps).filter(([_, value]) => value !== undefined)
  );

  return (
    <div className={fieldClasses} data-testid={testId}>
      {label && (
        <label htmlFor={fieldId} className={labelClasses}>
          {label}
          {required && (
            <span 
              className={styles.requiredIndicator} 
              aria-label="required field"
              title="This field is required"
            >
              *
            </span>
          )}
        </label>
      )}
      
      <div className={styles.inputContainer}>
        {React.cloneElement(children as React.ReactElement, cleanChildProps)}
      </div>
      
      {error && (
        <div
          id={errorId}
          className={errorClasses}
          role="alert"
          aria-live="polite"
          aria-atomic="true"
        >
          <span className="sr-only">Error: </span>
          {error}
        </div>
      )}
      
      {helperText && !error && (
        <div
          id={helperId}
          className={helperClasses}
          aria-live="polite"
        >
          {helperText}
        </div>
      )}
    </div>
  );
};