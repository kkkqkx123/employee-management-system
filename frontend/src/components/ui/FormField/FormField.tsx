import { Box, Text } from '@mantine/core';
import { ReactNode } from 'react';
import classes from './FormField.module.css';

export interface FormFieldProps {
  /** Field label */
  label?: string;
  /** Field description/helper text */
  description?: string;
  /** Error message */
  error?: string;
  /** Whether field is required */
  required?: boolean;
  /** Field content */
  children: ReactNode;
  /** Custom class name */
  className?: string;
}

export const FormField = ({
  label,
  description,
  error,
  required = false,
  children,
  className,
}: FormFieldProps) => {
  return (
    <Box className={`${classes.container} ${className || ''}`}>
      {label && (
        <Text
          component="label"
          size="sm"
          fw={500}
          className={classes.label}
        >
          {label}
          {required && <span className={classes.required}>*</span>}
        </Text>
      )}
      
      {description && (
        <Text size="xs" c="dimmed" className={classes.description}>
          {description}
        </Text>
      )}
      
      <div className={classes.field}>
        {children}
      </div>
      
      {error && (
        <Text size="xs" c="red" className={classes.error}>
          {error}
        </Text>
      )}
    </Box>
  );
};