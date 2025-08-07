# UI Components Module Implementation Guide

## Overview
This document provides detailed implementation specifications for the core UI component library, including reusable components like DataTable, FormField, LoadingSpinner, Button, Input, and Modal components.

## File Structure
```
src/components/ui/
├── Button/
│   ├── Button.tsx
│   ├── Button.module.css
│   ├── Button.test.tsx
│   └── index.ts
├── DataTable/
│   ├── DataTable.tsx
│   ├── DataTableHeader.tsx
│   ├── DataTableRow.tsx
│   ├── DataTablePagination.tsx
│   ├── DataTable.module.css
│   ├── DataTable.test.tsx
│   └── index.ts
├── FormField/
│   ├── FormField.tsx
│   ├── FormField.module.css
│   ├── FormField.test.tsx
│   └── index.ts
├── LoadingSpinner/
│   ├── LoadingSpinner.tsx
│   ├── LoadingSpinner.module.css
│   ├── LoadingSpinner.test.tsx
│   └── index.ts
├── Modal/
│   ├── Modal.tsx
│   ├── Modal.module.css
│   ├── Modal.test.tsx
│   └── index.ts
└── index.ts
```

## Type Definitions

### ui.types.ts
```typescript
export interface BaseComponentProps {
  className?: string;
  children?: React.ReactNode;
  testId?: string;
}

export interface ButtonProps extends BaseComponentProps {
  variant?: 'primary' | 'secondary' | 'outline' | 'ghost' | 'danger';
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl';
  disabled?: boolean;
  loading?: boolean;
  leftIcon?: React.ReactNode;
  rightIcon?: React.ReactNode;
  fullWidth?: boolean;
  onClick?: (event: React.MouseEvent<HTMLButtonElement>) => void;
  type?: 'button' | 'submit' | 'reset';
}

export interface Column<T = any> {
  key: string;
  title: string;
  dataIndex?: keyof T;
  render?: (value: any, record: T, index: number) => React.ReactNode;
  sortable?: boolean;
  filterable?: boolean;
  width?: number | string;
  align?: 'left' | 'center' | 'right';
  fixed?: 'left' | 'right';
}

export interface DataTableProps<T = any> extends BaseComponentProps {
  data: T[];
  columns: Column<T>[];
  loading?: boolean;
  pagination?: PaginationConfig;
  rowSelection?: RowSelectionConfig<T>;
  onRow?: (record: T, index: number) => React.HTMLAttributes<HTMLTableRowElement>;
  scroll?: { x?: number | string; y?: number | string };
  size?: 'small' | 'middle' | 'large';
  bordered?: boolean;
  showHeader?: boolean;
}

export interface PaginationConfig {
  current: number;
  pageSize: number;
  total: number;
  showSizeChanger?: boolean;
  showQuickJumper?: boolean;
  showTotal?: (total: number, range: [number, number]) => string;
  onChange?: (page: number, pageSize: number) => void;
  onShowSizeChange?: (current: number, size: number) => void;
}

export interface RowSelectionConfig<T> {
  type?: 'checkbox' | 'radio';
  selectedRowKeys?: React.Key[];
  onChange?: (selectedRowKeys: React.Key[], selectedRows: T[]) => void;
  getCheckboxProps?: (record: T) => { disabled?: boolean };
  onSelect?: (record: T, selected: boolean, selectedRows: T[]) => void;
  onSelectAll?: (selected: boolean, selectedRows: T[], changeRows: T[]) => void;
}
```

## Button Component

### Button.tsx
```typescript
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
```

### Button.module.css
```css
.button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  text-decoration: none;
  cursor: pointer;
  transition: all 0.2s ease-in-out;
  position: relative;
  white-space: nowrap;
  user-select: none;
  outline: none;
  font-family: inherit;
}

.button:focus-visible {
  outline: 2px solid var(--color-primary-500);
  outline-offset: 2px;
}

/* Sizes */
.xs {
  height: 24px;
  padding: 0 8px;
  font-size: 12px;
  line-height: 1;
}

.sm {
  height: 32px;
  padding: 0 12px;
  font-size: 14px;
  line-height: 1;
}

.md {
  height: 40px;
  padding: 0 16px;
  font-size: 14px;
  line-height: 1;
}

.lg {
  height: 48px;
  padding: 0 20px;
  font-size: 16px;
  line-height: 1;
}

.xl {
  height: 56px;
  padding: 0 24px;
  font-size: 18px;
  line-height: 1;
}

/* Variants */
.primary {
  background-color: var(--color-primary-500);
  color: white;
}

.primary:hover:not(.disabled) {
  background-color: var(--color-primary-600);
}

.primary:active:not(.disabled) {
  background-color: var(--color-primary-700);
}

.secondary {
  background-color: var(--color-gray-100);
  color: var(--color-gray-900);
}

.secondary:hover:not(.disabled) {
  background-color: var(--color-gray-200);
}

.outline {
  background-color: transparent;
  color: var(--color-primary-500);
  border: 1px solid var(--color-primary-500);
}

.outline:hover:not(.disabled) {
  background-color: var(--color-primary-50);
}

.ghost {
  background-color: transparent;
  color: var(--color-gray-700);
}

.ghost:hover:not(.disabled) {
  background-color: var(--color-gray-100);
}

.danger {
  background-color: var(--color-red-500);
  color: white;
}

.danger:hover:not(.disabled) {
  background-color: var(--color-red-600);
}

/* States */
.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.loading {
  cursor: wait;
}

.fullWidth {
  width: 100%;
}

/* Icons */
.leftIcon {
  margin-right: 8px;
  display: flex;
  align-items: center;
}

.rightIcon {
  margin-left: 8px;
  display: flex;
  align-items: center;
}

.loadingIcon {
  margin-right: 8px;
}

.content {
  display: flex;
  align-items: center;
}
```#
# LoadingSpinner Component

### LoadingSpinner.tsx
```typescript
import React from 'react';
import { clsx } from 'clsx';
import styles from './LoadingSpinner.module.css';

interface LoadingSpinnerProps {
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl';
  color?: 'primary' | 'secondary' | 'white';
  overlay?: boolean;
  className?: string;
  testId?: string;
}

export const LoadingSpinner: React.FC<LoadingSpinnerProps> = ({
  size = 'md',
  color = 'primary',
  overlay = false,
  className,
  testId
}) => {
  const spinnerClasses = clsx(
    styles.spinner,
    styles[size],
    styles[color],
    {
      [styles.overlay]: overlay,
    },
    className
  );

  const content = (
    <div className={styles.spinnerContainer}>
      <div className={spinnerClasses} data-testid={testId} role="status" aria-label="Loading">
        <div className={styles.dot1}></div>
        <div className={styles.dot2}></div>
        <div className={styles.dot3}></div>
      </div>
    </div>
  );

  if (overlay) {
    return (
      <div className={styles.overlayContainer}>
        {content}
      </div>
    );
  }

  return content;
};
```

### LoadingSpinner.module.css
```css
.spinnerContainer {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.spinner {
  display: inline-flex;
  align-items: center;
  gap: 2px;
}

.spinner div {
  border-radius: 50%;
  animation: loading 1.4s infinite ease-in-out both;
}

.spinner .dot1 {
  animation-delay: -0.32s;
}

.spinner .dot2 {
  animation-delay: -0.16s;
}

.spinner .dot3 {
  animation-delay: 0s;
}

/* Sizes */
.xs div {
  width: 4px;
  height: 4px;
}

.sm div {
  width: 6px;
  height: 6px;
}

.md div {
  width: 8px;
  height: 8px;
}

.lg div {
  width: 12px;
  height: 12px;
}

.xl div {
  width: 16px;
  height: 16px;
}

/* Colors */
.primary div {
  background-color: var(--color-primary-500);
}

.secondary div {
  background-color: var(--color-gray-500);
}

.white div {
  background-color: white;
}

/* Overlay */
.overlayContainer {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

@keyframes loading {
  0%, 80%, 100% {
    transform: scale(0);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}
```

## FormField Component

### FormField.tsx
```typescript
import React from 'react';
import { clsx } from 'clsx';
import styles from './FormField.module.css';

interface FormFieldProps {
  label?: string;
  error?: string;
  helperText?: string;
  required?: boolean;
  children: React.ReactNode;
  className?: string;
  labelClassName?: string;
  errorClassName?: string;
  helperClassName?: string;
  testId?: string;
}

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
```### FormFie
ld.module.css
```css
.formField {
  margin-bottom: 16px;
}

.label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-gray-700);
  margin-bottom: 4px;
  line-height: 1.4;
}

.required .requiredIndicator {
  color: var(--color-red-500);
  margin-left: 2px;
}

.inputContainer {
  position: relative;
}

.error {
  font-size: 12px;
  color: var(--color-red-500);
  margin-top: 4px;
  line-height: 1.4;
}

.helper {
  font-size: 12px;
  color: var(--color-gray-500);
  margin-top: 4px;
  line-height: 1.4;
}

.hasError .inputContainer input,
.hasError .inputContainer textarea,
.hasError .inputContainer select {
  border-color: var(--color-red-500);
}

.hasError .inputContainer input:focus,
.hasError .inputContainer textarea:focus,
.hasError .inputContainer select:focus {
  border-color: var(--color-red-500);
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1);
}
```

## Modal Component

### Modal.tsx
```typescript
import React, { useEffect, useRef } from 'react';
import { createPortal } from 'react-dom';
import { clsx } from 'clsx';
import { Button } from '../Button';
import styles from './Modal.module.css';

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title?: string;
  children: React.ReactNode;
  size?: 'sm' | 'md' | 'lg' | 'xl' | 'full';
  closeOnOverlayClick?: boolean;
  closeOnEscape?: boolean;
  showCloseButton?: boolean;
  footer?: React.ReactNode;
  className?: string;
  overlayClassName?: string;
  contentClassName?: string;
  testId?: string;
}

export const Modal: React.FC<ModalProps> = ({
  isOpen,
  onClose,
  title,
  children,
  size = 'md',
  closeOnOverlayClick = true,
  closeOnEscape = true,
  showCloseButton = true,
  footer,
  className,
  overlayClassName,
  contentClassName,
  testId
}) => {
  const modalRef = useRef<HTMLDivElement>(null);
  const previousActiveElement = useRef<HTMLElement | null>(null);

  useEffect(() => {
    if (isOpen) {
      previousActiveElement.current = document.activeElement as HTMLElement;
      modalRef.current?.focus();
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
      previousActiveElement.current?.focus();
    }

    return () => {
      document.body.style.overflow = '';
    };
  }, [isOpen]);

  useEffect(() => {
    const handleEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape' && closeOnEscape) {
        onClose();
      }
    };

    if (isOpen) {
      document.addEventListener('keydown', handleEscape);
    }

    return () => {
      document.removeEventListener('keydown', handleEscape);
    };
  }, [isOpen, closeOnEscape, onClose]);

  const handleOverlayClick = (event: React.MouseEvent) => {
    if (event.target === event.currentTarget && closeOnOverlayClick) {
      onClose();
    }
  };

  const modalClasses = clsx(
    styles.modal,
    styles[size],
    className
  );

  const overlayClasses = clsx(
    styles.overlay,
    overlayClassName
  );

  const contentClasses = clsx(
    styles.content,
    contentClassName
  );

  if (!isOpen) return null;

  const modalContent = (
    <div
      className={overlayClasses}
      onClick={handleOverlayClick}
      role="dialog"
      aria-modal="true"
      aria-labelledby={title ? 'modal-title' : undefined}
      data-testid={testId}
    >
      <div
        ref={modalRef}
        className={modalClasses}
        tabIndex={-1}
      >
        {(title || showCloseButton) && (
          <div className={styles.header}>
            {title && (
              <h2 id="modal-title" className={styles.title}>
                {title}
              </h2>
            )}
            {showCloseButton && (
              <Button
                variant="ghost"
                size="sm"
                onClick={onClose}
                className={styles.closeButton}
                aria-label="Close modal"
              >
                ×
              </Button>
            )}
          </div>
        )}
        
        <div className={contentClasses}>
          {children}
        </div>
        
        {footer && (
          <div className={styles.footer}>
            {footer}
          </div>
        )}
      </div>
    </div>
  );

  return createPortal(modalContent, document.body);
};
```

### Modal.module.css
```css
.overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 16px;
}

.modal {
  background: white;
  border-radius: 8px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  max-height: calc(100vh - 32px);
  display: flex;
  flex-direction: column;
  outline: none;
}

/* Sizes */
.sm {
  width: 100%;
  max-width: 400px;
}

.md {
  width: 100%;
  max-width: 500px;
}

.lg {
  width: 100%;
  max-width: 700px;
}

.xl {
  width: 100%;
  max-width: 900px;
}

.full {
  width: calc(100vw - 32px);
  height: calc(100vh - 32px);
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid var(--color-gray-200);
}

.title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--color-gray-900);
}

.closeButton {
  font-size: 24px;
  line-height: 1;
  padding: 4px;
  min-width: auto;
  height: auto;
}

.content {
  padding: 24px;
  overflow-y: auto;
  flex: 1;
}

.footer {
  padding: 16px 24px;
  border-top: 1px solid var(--color-gray-200);
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 640px) {
  .overlay {
    padding: 8px;
  }
  
  .modal {
    max-height: calc(100vh - 16px);
  }
  
  .header,
  .content,
  .footer {
    padding-left: 16px;
    padding-right: 16px;
  }
}
```