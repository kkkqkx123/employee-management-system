import { Loader, LoaderProps, Center } from '@mantine/core';
import classes from './LoadingSpinner.module.css';

export interface LoadingSpinnerProps extends LoaderProps {
  /** Loading text */
  text?: string;
  /** Whether to center the spinner */
  centered?: boolean;
  /** Spinner size */
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl';
}

export const LoadingSpinner = ({
  text,
  centered = false,
  size = 'md',
  className,
  ...props
}: LoadingSpinnerProps) => {
  const spinner = (
    <div className={`${classes.container} ${className || ''}`}>
      <Loader size={size} {...props} />
      {text && <div className={classes.text}>{text}</div>}
    </div>
  );

  if (centered) {
    return <Center>{spinner}</Center>;
  }

  return spinner;
};