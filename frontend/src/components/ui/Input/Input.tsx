import { TextInput, TextInputProps, PasswordInput, PasswordInputProps } from '@mantine/core';
import { forwardRef } from 'react';
import classes from './Input.module.css';

export interface InputProps extends Omit<TextInputProps, 'type'> {
  /** Input type */
  type?: 'text' | 'email' | 'password' | 'number' | 'tel' | 'url';
  /** Error message */
  error?: string;
  /** Helper text */
  helperText?: string;
}

export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ className, type = 'text', error, helperText, ...props }, ref) => {
    const commonProps = {
      ref,
      className: `${classes.input} ${className || ''}`,
      error: error,
      description: helperText,
      ...props,
    };

    if (type === 'password') {
      return <PasswordInput {...(commonProps as PasswordInputProps)} />;
    }

    return <TextInput {...commonProps} type={type} />;
  }
);

Input.displayName = 'Input';