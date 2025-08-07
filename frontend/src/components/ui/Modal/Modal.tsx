import { Modal as MantineModal, ModalProps as MantineModalProps } from '@mantine/core';
import { ReactNode } from 'react';
import classes from './Modal.module.css';

export interface ModalProps extends Omit<MantineModalProps, 'children'> {
  /** Modal content */
  children: ReactNode;
  /** Modal title */
  title?: string;
  /** Whether modal is open */
  opened: boolean;
  /** Function to close modal */
  onClose: () => void;
  /** Modal size */
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl' | 'auto';
  /** Whether to center modal */
  centered?: boolean;
  /** Whether to show close button */
  withCloseButton?: boolean;
}

export const Modal = ({
  children,
  title,
  opened,
  onClose,
  size = 'md',
  centered = true,
  withCloseButton = true,
  className,
  ...props
}: ModalProps) => {
  return (
    <MantineModal
      opened={opened}
      onClose={onClose}
      title={title}
      size={size}
      centered={centered}
      withCloseButton={withCloseButton}
      className={`${classes.modal} ${className || ''}`}
      overlayProps={{ backgroundOpacity: 0.55, blur: 3 }}
      {...props}
    >
      {children}
    </MantineModal>
  );
};