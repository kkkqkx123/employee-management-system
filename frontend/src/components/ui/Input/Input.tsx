import React, { forwardRef } from 'react';
import { clsx } from 'clsx';
import { InputProps } from '../types/ui.types';
import styles from './Input.module.css';

export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({
    type = 'text',
    size = 'md',
    variant = 'outline',
    error = false,
    disabled = false,
    readOnly = false,
    leftIcon,
    rightIcon,
    className,
    testId,
    ...props
  }, ref) => {
    const inputClasses = clsx(
      styles.input,
      styles[size],
      styles[variant],
      {
        [styles.error]: error,
        [styles.disabled]: disabled,
        [styles.readOnly]: readOnly,
        [styles.hasLeftIcon]: leftIcon,
        [styles.hasRightIcon]: rightIcon,
      },
      className
    );

    const wrapperClasses = clsx(
      styles.wrapper,
      {
        [styles.error]: error,
        [styles.disabled]: disabled,
      }
    );

    return (
      <div className={wrapperClasses} data-testid={testId}>
        {leftIcon && (
          <span className={styles.leftIcon}>
            {leftIcon}
          </span>
        )}
        <input
          ref={ref}
          type={type}
          className={inputClasses}
          disabled={disabled}
          readOnly={readOnly}
          {...props}
        />
        {rightIcon && (
          <span className={styles.rightIcon}>
            {rightIcon}
          </span>
        )}
      </div>
    );
  }
);

Input.displayName = 'Input';