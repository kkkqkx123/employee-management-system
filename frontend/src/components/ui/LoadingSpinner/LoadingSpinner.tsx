import React from 'react';
import { clsx } from 'clsx';
import { LoadingSpinnerProps } from '../types/ui.types';
import styles from './LoadingSpinner.module.css';

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