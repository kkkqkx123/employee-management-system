import React, { forwardRef } from 'react';
import { clsx } from 'clsx';
import { LoadingSpinner } from '../LoadingSpinner';
import { ButtonProps } from '../types/ui.types';
import styles from './Button.module.css';

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  ({
    children,
    className,
    variant = 'primary',
    size = 'md',
    disabled = false,
    loading = false,
    leftIcon,
    rightIcon,
    fullWidth = false,
    onClick,
    type = 'button',
    testId,
    ...props
  }, ref) => {
    const handleClick = (event: React.MouseEvent<HTMLButtonElement>) => {
      if (loading || disabled) return;
      onClick?.(event);
    };

    const buttonClasses = clsx(
      styles.button,
      styles[variant],
      styles[size],
      {
        [styles.disabled]: disabled || loading,
        [styles.loading]: loading,
        [styles.fullWidth]: fullWidth,
        [styles.hasLeftIcon]: leftIcon,
        [styles.hasRightIcon]: rightIcon,
      },
      className
    );

    return (
      <button
        ref={ref}
        type={type}
        className={buttonClasses}
        disabled={disabled || loading}
        onClick={handleClick}
        data-testid={testId}
        aria-disabled={disabled || loading}
        {...props}
      >
        {loading && (
          <LoadingSpinner size="sm" className={styles.loadingIcon} />
        )}
        {leftIcon && !loading && (
          <span className={styles.leftIcon}>{leftIcon}</span>
        )}
        <span className={styles.content}>{children}</span>
        {rightIcon && !loading && (
          <span className={styles.rightIcon}>{rightIcon}</span>
        )}
      </button>
    );
  }
);

Button.displayName = 'Button';