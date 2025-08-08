import React, { forwardRef, useRef, useEffect, memo } from 'react';
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
    ariaLabel,
    ariaDescribedBy,
    ariaPressed,
    role,
    autoFocus = false,
    ...props
  }, ref) => {
    const internalRef = useRef<HTMLButtonElement>(null);
    const buttonRef = ref || internalRef;
    const loadingAnnouncementId = useRef<string>(`loading-announcement-${Math.random().toString(36).substr(2, 9)}`);

    // Auto focus on mount if specified
    useEffect(() => {
      if (autoFocus && buttonRef && 'current' in buttonRef && buttonRef.current) {
        buttonRef.current.focus();
      }
    }, [autoFocus, buttonRef]);

    const handleClick = (event: React.MouseEvent<HTMLButtonElement>) => {
      if (loading || disabled) {
        event.preventDefault();
        return;
      }
      onClick?.(event);
    };

    const handleKeyDown = (event: React.KeyboardEvent<HTMLButtonElement>) => {
      // Handle space key for button activation (Enter is handled by default)
      if (event.key === ' ') {
        event.preventDefault();
        if (!loading && !disabled) {
          handleClick(event as any);
        }
      }
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

    // Determine ARIA attributes
    const ariaAttributes = {
      'aria-label': ariaLabel,
      'aria-describedby': ariaDescribedBy || (loading ? loadingAnnouncementId.current : undefined),
      'aria-pressed': ariaPressed,
      'aria-disabled': disabled || loading,
      'aria-busy': loading,
      role: role || (ariaPressed !== undefined ? 'button' : undefined),
    };

    // Filter out undefined values
    const cleanAriaAttributes = Object.fromEntries(
      Object.entries(ariaAttributes).filter(([_, value]) => value !== undefined)
    );

    return (
      <>
        <button
          ref={buttonRef}
          type={type}
          className={buttonClasses}
          disabled={disabled || loading}
          onClick={handleClick}
          onKeyDown={handleKeyDown}
          data-testid={testId}
          {...cleanAriaAttributes}
          {...props}
        >
          {loading && (
            <>
              <LoadingSpinner 
                size="sm" 
                className={styles.loadingIcon}
                aria-hidden="true"
              />
              <span className="sr-only">Loading...</span>
            </>
          )}
          {leftIcon && !loading && (
            <span className={styles.leftIcon} aria-hidden="true">
              {leftIcon}
            </span>
          )}
          <span className={styles.content}>
            {children}
          </span>
          {rightIcon && !loading && (
            <span className={styles.rightIcon} aria-hidden="true">
              {rightIcon}
            </span>
          )}
        </button>
        
        {/* Hidden loading announcement for screen readers */}
        {loading && (
          <div
            id={loadingAnnouncementId.current}
            className="sr-only"
            aria-live="polite"
            aria-atomic="true"
          >
            Button is loading, please wait
          </div>
        )}
      </>
    );
  }
);

Button.displayName = 'Button';

// Memoize the Button component for performance
export const MemoizedButton = memo(Button);