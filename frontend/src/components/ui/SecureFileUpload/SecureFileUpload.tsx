/**
 * Secure File Upload Component with validation and security checks
 */

import React, { useState, useCallback, useRef } from 'react';
import { SecurityUtils } from '../../../utils/security';
import { CSRFProtection } from '../../../utils/csrfProtection';
import { RateLimiter } from '../../../utils/security';
import styles from './SecureFileUpload.module.css';

export interface SecureFileUploadProps {
  onFileSelect: (file: File) => void;
  onUploadProgress?: (progress: number) => void;
  onUploadComplete?: (result: any) => void;
  onUploadError?: (error: string) => void;
  allowedTypes?: string[];
  allowedExtensions?: string[];
  maxSize?: number;
  maxFiles?: number;
  uploadUrl?: string;
  disabled?: boolean;
  className?: string;
  children?: React.ReactNode;
}

export const SecureFileUpload: React.FC<SecureFileUploadProps> = ({
  onFileSelect,
  onUploadProgress,
  onUploadComplete,
  onUploadError,
  allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'application/pdf', 'text/csv', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'],
  allowedExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.pdf', '.csv', '.xlsx'],
  maxSize = 10 * 1024 * 1024, // 10MB
  maxFiles = 1,
  uploadUrl,
  disabled = false,
  className = '',
  children,
}) => {
  const [isDragging, setIsDragging] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [isUploading, setIsUploading] = useState(false);
  const [selectedFiles, setSelectedFiles] = useState<File[]>([]);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const validateFile = useCallback((file: File): { isValid: boolean; error?: string } => {
    // Rate limiting check
    const rateLimitKey = `file_upload_${Date.now()}`;
    if (!RateLimiter.isAllowed(rateLimitKey, 5, 60000)) { // 5 uploads per minute
      return {
        isValid: false,
        error: 'Upload rate limit exceeded. Please wait before uploading more files.'
      };
    }

    // Use security utils for validation
    const validation = SecurityUtils.validateFile(file, {
      allowedTypes,
      maxSize,
      allowedExtensions
    });

    if (!validation.isValid) {
      return validation;
    }

    // Additional security checks
    if (file.name.length > 255) {
      return {
        isValid: false,
        error: 'File name is too long'
      };
    }

    // Check for null bytes in filename (potential security issue)
    if (file.name.includes('\0')) {
      return {
        isValid: false,
        error: 'Invalid file name'
      };
    }

    return { isValid: true };
  }, [allowedTypes, allowedExtensions, maxSize]);

  const handleFileSelection = useCallback((files: FileList | null) => {
    if (!files || files.length === 0) return;

    const fileArray = Array.from(files);
    
    // Check file count limit
    if (fileArray.length > maxFiles) {
      onUploadError?.(`Maximum ${maxFiles} file(s) allowed`);
      return;
    }

    const validFiles: File[] = [];
    const errors: string[] = [];

    for (const file of fileArray) {
      const validation = validateFile(file);
      if (validation.isValid) {
        validFiles.push(file);
      } else {
        errors.push(`${file.name}: ${validation.error}`);
      }
    }

    if (errors.length > 0) {
      onUploadError?.(errors.join('\n'));
      return;
    }

    setSelectedFiles(validFiles);
    
    // Call onFileSelect for each valid file
    validFiles.forEach(file => onFileSelect(file));

    // Auto-upload if URL is provided
    if (uploadUrl && validFiles.length > 0) {
      uploadFiles(validFiles);
    }
  }, [maxFiles, validateFile, onFileSelect, onUploadError, uploadUrl]);

  const uploadFiles = async (files: File[]) => {
    if (!uploadUrl) return;

    setIsUploading(true);
    setUploadProgress(0);

    try {
      for (let i = 0; i < files.length; i++) {
        const file = files[i];
        await uploadSingleFile(file, i, files.length);
      }
      
      onUploadComplete?.(selectedFiles);
    } catch (error) {
      onUploadError?.(error instanceof Error ? error.message : 'Upload failed');
    } finally {
      setIsUploading(false);
      setUploadProgress(0);
    }
  };

  const uploadSingleFile = async (file: File, index: number, total: number): Promise<void> => {
    const formData = new FormData();
    formData.append('file', file);
    
    // Add CSRF protection
    CSRFProtection.addToFormData(formData);

    const xhr = new XMLHttpRequest();

    return new Promise((resolve, reject) => {
      xhr.upload.addEventListener('progress', (event) => {
        if (event.lengthComputable) {
          const fileProgress = (event.loaded / event.total) * 100;
          const totalProgress = ((index * 100) + fileProgress) / total;
          setUploadProgress(totalProgress);
          onUploadProgress?.(totalProgress);
        }
      });

      xhr.addEventListener('load', () => {
        if (xhr.status >= 200 && xhr.status < 300) {
          resolve();
        } else {
          reject(new Error(`Upload failed with status ${xhr.status}`));
        }
      });

      xhr.addEventListener('error', () => {
        reject(new Error('Upload failed'));
      });

      xhr.addEventListener('abort', () => {
        reject(new Error('Upload aborted'));
      });

      // Add security headers
      const csrfHeaders = CSRFProtection.getHeaders();
      Object.entries(csrfHeaders).forEach(([key, value]) => {
        xhr.setRequestHeader(key, value);
      });

      xhr.open('POST', uploadUrl);
      xhr.send(formData);
    });
  };

  const handleDragOver = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (!disabled) {
      setIsDragging(true);
    }
  }, [disabled]);

  const handleDragLeave = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);
  }, []);

  const handleDrop = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);

    if (disabled) return;

    const files = e.dataTransfer.files;
    handleFileSelection(files);
  }, [disabled, handleFileSelection]);

  const handleFileInputChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    handleFileSelection(e.target.files);
    // Reset input value to allow selecting the same file again
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  }, [handleFileSelection]);

  const handleClick = useCallback(() => {
    if (!disabled && fileInputRef.current) {
      fileInputRef.current.click();
    }
  }, [disabled]);

  const removeFile = useCallback((index: number) => {
    setSelectedFiles(prev => prev.filter((_, i) => i !== index));
  }, []);

  return (
    <div className={`${styles.container} ${className}`}>
      <div
        className={`${styles.dropZone} ${isDragging ? styles.dragging : ''} ${disabled ? styles.disabled : ''}`}
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onDrop={handleDrop}
        onClick={handleClick}
        role="button"
        tabIndex={disabled ? -1 : 0}
        aria-label="File upload area"
        onKeyDown={(e) => {
          if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            handleClick();
          }
        }}
      >
        <input
          ref={fileInputRef}
          type="file"
          multiple={maxFiles > 1}
          accept={allowedTypes.join(',')}
          onChange={handleFileInputChange}
          className={styles.hiddenInput}
          disabled={disabled}
          aria-hidden="true"
        />

        {children || (
          <div className={styles.defaultContent}>
            <div className={styles.icon}>📁</div>
            <p className={styles.text}>
              {isDragging ? 'Drop files here' : 'Click to select files or drag and drop'}
            </p>
            <p className={styles.subtext}>
              Max size: {Math.round(maxSize / (1024 * 1024))}MB
            </p>
          </div>
        )}

        {isUploading && (
          <div className={styles.progressOverlay}>
            <div className={styles.progressBar}>
              <div 
                className={styles.progressFill}
                style={{ width: `${uploadProgress}%` }}
              />
            </div>
            <span className={styles.progressText}>
              {Math.round(uploadProgress)}%
            </span>
          </div>
        )}
      </div>

      {selectedFiles.length > 0 && (
        <div className={styles.fileList}>
          <h4>Selected Files:</h4>
          {selectedFiles.map((file, index) => (
            <div key={index} className={styles.fileItem}>
              <span className={styles.fileName}>{file.name}</span>
              <span className={styles.fileSize}>
                ({Math.round(file.size / 1024)} KB)
              </span>
              <button
                type="button"
                onClick={() => removeFile(index)}
                className={styles.removeButton}
                aria-label={`Remove ${file.name}`}
              >
                ×
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default SecureFileUpload;