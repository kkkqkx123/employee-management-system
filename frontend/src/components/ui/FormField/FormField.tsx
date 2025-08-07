import React from 'react';
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
  testId
}) => {
  const fieldId = React.useId();
  
  const fieldClasses = clsx(
    styles.formField,
    {
      [styles.hasError]: error,
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

  return (
    <div className={fieldClasses} data-testid={testId}>
      {label && (
        <label htmlFor={fieldId} className={labelClasses}>
          {label}
          {required && <span className={styles.requiredIndicator} aria-label="required">*</span>}
        </label>
      )}
      
      <div className={styles.inputContainer}>
        {React.cloneElement(children as React.ReactElement, {
          id: fieldId,
          'aria-invalid': !!error,
          'aria-describedby': error ? `${fieldId}-error` : helperText ? `${fieldId}-helper` : undefined,
        })}
      </div>
      
      {error && (
        <div
          id={`${fieldId}-error`}
          className={errorClasses}
          role="alert"
          aria-live="polite"
        >
          {error}
        </div>
      )}
      
      {helperText && !error && (
        <div
          id={`${fieldId}-helper`}
          className={helperClasses}
        >
          {helperText}
        </div>
      )}
    </div>
  );
};