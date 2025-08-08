import React from 'react';
import { clsx } from 'clsx';
import styles from './FormError.module.css';

interface FormErrorProps {
  error?: string | string[];
  touched?: boolean;
  className?: string;
  testId?: string;
}

export const FormError: React.FC<FormErrorProps> = ({
  error,
  touched = true,
  className,
  testId,
}) => {
  if (!error || !touched) {
    return null;
  }

  const errors = Array.isArray(error) ? error : [error];

  return (
    <div
      className={clsx(styles.formError, className)}
      role="alert"
      aria-live="polite"
      data-testid={testId}
    >
      {errors.map((err, index) => (
        <p key={index} className={styles.errorMessage}>
          <span className={styles.errorIcon} aria-hidden="true">
            ⚠️
          </span>
          {err}
        </p>
      ))}
    </div>
  );
};