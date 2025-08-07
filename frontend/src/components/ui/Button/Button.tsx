import { Button as MantineButton, ButtonProps as MantineButtonProps } from '@mantine/core';
import { forwardRef } from 'react';
import classes from './Button.module.css';

export interface ButtonProps extends MantineButtonProps {
  /** Button variant */
  variant?: 'filled' | 'outline' | 'light' | 'subtle' | 'transparent';
  /** Button size */
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl';
  /** Loading state */
  loading?: boolean;
  /** Disabled state */
  disabled?: boolean;
  /** Full width button */
  fullWidth?: boolean;
}

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant = 'filled', size = 'md', ...props }, ref) => {
    return (
      <MantineButton
        ref={ref}
        className={`${classes.button} ${className || ''}`}
        variant={variant}
        size={size}
        {...props}
      />
    );
  }
);

Button.displayName = 'Button';