import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { vi, describe, it, expect } from 'vitest';
import { LoadingState } from '../LoadingState';

describe('LoadingState', () => {
  it('renders children when not loading and no error', () => {
    render(
      <LoadingState loading={false} error={null}>
        <div>Content loaded</div>
      </LoadingState>
    );

    expect(screen.getByText('Content loaded')).toBeInTheDocument();
  });

  it('renders loading spinner when loading is true', () => {
    render(
      <LoadingState loading={true} error={null}>
        <div>Content</div>
      </LoadingState>
    );

    expect(screen.getByTestId('loading-state')).toBeInTheDocument();
    expect(screen.getByRole('status')).toBeInTheDocument();
    expect(screen.getByText('Loading...')).toBeInTheDocument();
  });

  it('renders custom loading text', () => {
    render(
      <LoadingState loading={true} error={null} loadingText="Please wait...">
        <div>Content</div>
      </LoadingState>
    );

    expect(screen.getByText('Please wait...')).toBeInTheDocument();
  });

  it('renders error state when error is present', () => {
    const error = new Error('Something went wrong');

    render(
      <LoadingState loading={false} error={error}>
        <div>Content</div>
      </LoadingState>
    );

    expect(screen.getByTestId('error-state')).toBeInTheDocument();
    expect(screen.getByRole('alert')).toBeInTheDocument();
    expect(screen.getByText('Something went wrong')).toBeInTheDocument();
  });

  it('renders custom error text', () => {
    const error = new Error('API Error');

    render(
      <LoadingState 
        loading={false} 
        error={error} 
        errorText="Failed to load data"
      >
        <div>Content</div>
      </LoadingState>
    );

    expect(screen.getByText('Failed to load data')).toBeInTheDocument();
  });

  it('shows retry button when onRetry is provided', () => {
    const error = new Error('Network error');
    const onRetry = vi.fn();

    render(
      <LoadingState loading={false} error={error} onRetry={onRetry}>
        <div>Content</div>
      </LoadingState>
    );

    const retryButton = screen.getByRole('button', { name: /try again/i });
    expect(retryButton).toBeInTheDocument();

    fireEvent.click(retryButton);
    expect(onRetry).toHaveBeenCalledTimes(1);
  });

  it('applies overlay styles when overlay is true', () => {
    render(
      <LoadingState loading={true} error={null} overlay={true}>
        <div>Content</div>
      </LoadingState>
    );

    const loadingContainer = screen.getByTestId('loading-state');
    expect(loadingContainer).toHaveClass('overlay');
  });

  it('applies size classes correctly', () => {
    render(
      <LoadingState loading={true} error={null} size="lg">
        <div>Content</div>
      </LoadingState>
    );

    const loadingContainer = screen.getByTestId('loading-state');
    expect(loadingContainer).toHaveClass('lg');
  });

  it('applies custom className', () => {
    render(
      <LoadingState 
        loading={true} 
        error={null} 
        className="custom-class"
      >
        <div>Content</div>
      </LoadingState>
    );

    const loadingContainer = screen.getByTestId('loading-state');
    expect(loadingContainer).toHaveClass('custom-class');
  });

  it('uses custom testId', () => {
    render(
      <LoadingState 
        loading={true} 
        error={null} 
        testId="custom-loading"
      >
        <div>Content</div>
      </LoadingState>
    );

    expect(screen.getByTestId('custom-loading-loading')).toBeInTheDocument();
  });

  it('prioritizes loading state over error state', () => {
    const error = new Error('Test error');

    render(
      <LoadingState loading={true} error={error}>
        <div>Content</div>
      </LoadingState>
    );

    expect(screen.getByTestId('loading-state')).toBeInTheDocument();
    expect(screen.queryByTestId('error-state')).not.toBeInTheDocument();
  });
});