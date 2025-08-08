import React, { Component, ErrorInfo, ReactNode } from 'react';
import { Button } from '../Button';
import { Modal } from '../Modal';
import { createErrorReport, logError, getErrorSeverity } from '../../../utils/errorHandling';
import styles from './ErrorBoundary.module.css';

interface Props {
  children: ReactNode;
  fallback?: ReactNode;
  onError?: (error: Error, errorInfo: ErrorInfo) => void;
  showErrorDetails?: boolean;
  level?: 'page' | 'feature' | 'component';
}

interface State {
  hasError: boolean;
  error: Error | null;
  errorInfo: ErrorInfo | null;
  showDetails: boolean;
}

export class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      hasError: false,
      error: null,
      errorInfo: null,
      showDetails: false,
    };
  }

  static getDerivedStateFromError(error: Error): Partial<State> {
    return {
      hasError: true,
      error,
    };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    // Log error
    logError(error, `ErrorBoundary-${this.props.level || 'unknown'}`);

    // Create error report
    const errorReport = createErrorReport(error, {
      level: this.props.level,
      componentStack: errorInfo.componentStack,
    });

    // Store error info in state
    this.setState({
      errorInfo,
    });

    // Call custom error handler if provided
    if (this.props.onError) {
      this.props.onError(error, errorInfo);
    }

    // Send error report to monitoring service in production
    if (import.meta.env.PROD) {
      this.sendErrorReport(errorReport);
    }
  }

  private sendErrorReport = async (errorReport: any) => {
    try {
      // In a real application, send to error monitoring service
      // await errorMonitoringService.report(errorReport);
      console.log('Error report:', errorReport);
    } catch (reportError) {
      console.error('Failed to send error report:', reportError);
    }
  };

  private handleRetry = () => {
    this.setState({
      hasError: false,
      error: null,
      errorInfo: null,
      showDetails: false,
    });
  };

  private handleReload = () => {
    window.location.reload();
  };

  private toggleDetails = () => {
    this.setState(prevState => ({
      showDetails: !prevState.showDetails,
    }));
  };

  private renderErrorFallback() {
    const { error, errorInfo } = this.state;
    const { level = 'component', showErrorDetails = false } = this.props;
    
    if (!error) return null;

    const severity = getErrorSeverity(error);
    const isComponentLevel = level === 'component';
    const isFeatureLevel = level === 'feature';
    const isPageLevel = level === 'page';

    return (
      <div 
        className={`${styles.errorBoundary} ${styles[level]} ${styles[severity]}`}
        role="alert"
        aria-live="assertive"
      >
        <div className={styles.errorContent}>
          <div className={styles.errorIcon}>
            {severity === 'critical' ? '🚨' : severity === 'high' ? '⚠️' : '❌'}
          </div>
          
          <div className={styles.errorMessage}>
            <h3 className={styles.errorTitle}>
              {isPageLevel && 'Page Error'}
              {isFeatureLevel && 'Feature Error'}
              {isComponentLevel && 'Component Error'}
            </h3>
            
            <p className={styles.errorDescription}>
              {isPageLevel && 'An error occurred while loading this page.'}
              {isFeatureLevel && 'An error occurred in this feature.'}
              {isComponentLevel && 'An error occurred in this component.'}
            </p>

            {error.message && (
              <p className={styles.errorDetails}>
                {error.message}
              </p>
            )}
          </div>

          <div className={styles.errorActions}>
            {isComponentLevel && (
              <Button
                variant="primary"
                size="sm"
                onClick={this.handleRetry}
                aria-label="Retry loading component"
              >
                Try Again
              </Button>
            )}
            
            {(isFeatureLevel || isPageLevel) && (
              <>
                <Button
                  variant="primary"
                  size="sm"
                  onClick={this.handleRetry}
                  aria-label="Retry loading"
                >
                  Try Again
                </Button>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={this.handleReload}
                  aria-label="Reload page"
                >
                  Reload Page
                </Button>
              </>
            )}

            {(showErrorDetails || import.meta.env.DEV) && (
              <Button
                variant="ghost"
                size="sm"
                onClick={this.toggleDetails}
                aria-label="Toggle error details"
              >
                {this.state.showDetails ? 'Hide Details' : 'Show Details'}
              </Button>
            )}
          </div>
        </div>

        {this.state.showDetails && (
          <Modal
            isOpen={this.state.showDetails}
            onClose={this.toggleDetails}
            title="Error Details"
            size="lg"
          >
            <div className={styles.errorDetailsModal}>
              <div className={styles.errorSection}>
                <h4>Error Message</h4>
                <pre className={styles.errorCode}>{error.message}</pre>
              </div>

              <div className={styles.errorSection}>
                <h4>Stack Trace</h4>
                <pre className={styles.errorCode}>{error.stack}</pre>
              </div>

              {errorInfo && (
                <div className={styles.errorSection}>
                  <h4>Component Stack</h4>
                  <pre className={styles.errorCode}>{errorInfo.componentStack}</pre>
                </div>
              )}

              <div className={styles.errorSection}>
                <h4>Error Report</h4>
                <pre className={styles.errorCode}>
                  {JSON.stringify(createErrorReport(error), null, 2)}
                </pre>
              </div>
            </div>
          </Modal>
        )}
      </div>
    );
  }

  render() {
    if (this.state.hasError) {
      // Use custom fallback if provided
      if (this.props.fallback) {
        return this.props.fallback;
      }

      // Use default error fallback
      return this.renderErrorFallback();
    }

    return this.props.children;
  }
}