import React, { useState } from 'react';
import {
  ErrorBoundary,
  FeatureErrorBoundary,
  PageErrorBoundary,
  LoadingState,
  RetryButton,
  NetworkStatus,
  ErrorMessage,
  FormError,
  Button,
} from '../ui';
import { useAsyncOperation, useFormErrors } from '../../hooks';
import styles from './ErrorHandlingDemo.module.css';

// Component that throws an error for testing
const ErrorThrowingComponent: React.FC<{ shouldThrow: boolean }> = ({ shouldThrow }) => {
  if (shouldThrow) {
    throw new Error('This is a test error from ErrorThrowingComponent');
  }
  return <div className={styles.successMessage}>✅ Component loaded successfully!</div>;
};

// Async function that simulates API calls
const simulateApiCall = async (shouldFail: boolean = false, delay: number = 1000): Promise<string> => {
  await new Promise(resolve => setTimeout(resolve, delay));
  
  if (shouldFail) {
    throw new Error('API call failed - simulated network error');
  }
  
  return 'API call successful!';
};

export const ErrorHandlingDemo: React.FC = () => {
  const [throwError, setThrowError] = useState(false);
  const [apiShouldFail, setApiShouldFail] = useState(false);
  const [formData, setFormData] = useState({ email: '', password: '' });
  
  const { errors, setError, clearError, hasFieldError, getFieldError } = useFormErrors();
  
  const {
    data: apiData,
    loading: apiLoading,
    error: apiError,
    execute: executeApi,
    retry: retryApi,
  } = useAsyncOperation(simulateApiCall);

  const handleFormSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    // Clear previous errors
    clearError('email');
    clearError('password');
    
    // Simulate form validation
    if (!formData.email) {
      setError('email', 'Email is required');
    } else if (!formData.email.includes('@')) {
      setError('email', 'Please enter a valid email address');
    }
    
    if (!formData.password) {
      setError('password', 'Password is required');
    } else if (formData.password.length < 6) {
      setError('password', 'Password must be at least 6 characters long');
    }
  };

  return (
    <div className={styles.demo}>
      <h1>Error Handling & Loading States Demo</h1>
      
      {/* Network Status */}
      <section className={styles.section}>
        <h2>Network Status</h2>
        <p>Try going offline to see the network status indicator.</p>
        <NetworkStatus />
      </section>

      {/* Error Boundaries */}
      <section className={styles.section}>
        <h2>Error Boundaries</h2>
        
        <div className={styles.controls}>
          <label>
            <input
              type="checkbox"
              checked={throwError}
              onChange={(e) => setThrowError(e.target.checked)}
            />
            Throw Error in Component
          </label>
        </div>

        <div className={styles.errorBoundaryExamples}>
          <div>
            <h3>Component Level Error Boundary</h3>
            <ErrorBoundary level="component">
              <ErrorThrowingComponent shouldThrow={throwError} />
            </ErrorBoundary>
          </div>

          <div>
            <h3>Feature Level Error Boundary</h3>
            <FeatureErrorBoundary featureName="Demo Feature">
              <ErrorThrowingComponent shouldThrow={throwError} />
            </FeatureErrorBoundary>
          </div>

          <div>
            <h3>Page Level Error Boundary</h3>
            <PageErrorBoundary pageName="Demo Page">
              <ErrorThrowingComponent shouldThrow={throwError} />
            </PageErrorBoundary>
          </div>
        </div>
      </section>

      {/* Loading States */}
      <section className={styles.section}>
        <h2>Loading States & Async Operations</h2>
        
        <div className={styles.controls}>
          <label>
            <input
              type="checkbox"
              checked={apiShouldFail}
              onChange={(e) => setApiShouldFail(e.target.checked)}
            />
            Make API Call Fail
          </label>
          
          <Button
            onClick={() => executeApi(apiShouldFail)}
            disabled={apiLoading}
          >
            Execute API Call
          </Button>
        </div>

        <LoadingState
          loading={apiLoading}
          error={apiError}
          onRetry={retryApi}
          loadingText="Making API call..."
        >
          <div className={styles.apiResult}>
            {apiData && <p>✅ {apiData}</p>}
          </div>
        </LoadingState>
      </section>

      {/* Error Messages */}
      <section className={styles.section}>
        <h2>Error Messages</h2>
        
        {apiError && (
          <ErrorMessage
            error={apiError}
            title="API Error"
            onRetry={retryApi}
            variant="inline"
          />
        )}
        
        <div className={styles.errorMessageExamples}>
          <ErrorMessage
            error={new Error('This is a network error')}
            title="Network Error"
            variant="banner"
            onRetry={() => console.log('Retry clicked')}
          />
          
          <ErrorMessage
            error="This is a simple string error"
            variant="inline"
            size="sm"
            showDismiss
            onDismiss={() => console.log('Dismissed')}
          />
        </div>
      </section>

      {/* Retry Button */}
      <section className={styles.section}>
        <h2>Retry Button</h2>
        
        <div className={styles.retryExamples}>
          <RetryButton
            onRetry={() => simulateApiCall(Math.random() > 0.5)}
            showAttempts
            maxRetries={3}
          >
            Retry with Random Failure
          </RetryButton>
          
          <RetryButton
            onRetry={() => simulateApiCall(false, 500)}
            variant="outline"
            size="sm"
          >
            Always Succeed
          </RetryButton>
        </div>
      </section>

      {/* Form Validation */}
      <section className={styles.section}>
        <h2>Form Validation</h2>
        
        <form onSubmit={handleFormSubmit} className={styles.form}>
          <div className={styles.formField}>
            <label htmlFor="email">Email</label>
            <input
              id="email"
              type="email"
              value={formData.email}
              onChange={(e) => setFormData(prev => ({ ...prev, email: e.target.value }))}
              className={hasFieldError('email') ? styles.errorInput : ''}
            />
            <FormError error={getFieldError('email')} />
          </div>
          
          <div className={styles.formField}>
            <label htmlFor="password">Password</label>
            <input
              id="password"
              type="password"
              value={formData.password}
              onChange={(e) => setFormData(prev => ({ ...prev, password: e.target.value }))}
              className={hasFieldError('password') ? styles.errorInput : ''}
            />
            <FormError error={getFieldError('password')} />
          </div>
          
          <Button type="submit">Submit Form</Button>
        </form>
      </section>
    </div>
  );
};