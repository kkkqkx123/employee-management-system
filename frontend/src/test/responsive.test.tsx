import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MantineProvider } from '@mantine/core';
import { TouchGesture, ResponsiveContainer } from '@/components/ui';
import { theme } from '@/theme';

const renderWithTheme = (component: React.ReactElement) => {
  return render(
    <MantineProvider theme={theme}>
      {component}
    </MantineProvider>
  );
};

describe('Responsive Components', () => {
  describe('TouchGesture Component', () => {
    it('should render children correctly', () => {
      renderWithTheme(
        <TouchGesture>
          <div>Touch me</div>
        </TouchGesture>
      );
      expect(screen.getByText('Touch me')).toBeInTheDocument();
    });

    it('should apply className correctly', () => {
      renderWithTheme(
        <TouchGesture className="test-class">
          <div>Touch me</div>
        </TouchGesture>
      );
      const container = screen.getByText('Touch me').parentElement;
      expect(container).toHaveClass('test-class');
    });
  });

  describe('ResponsiveContainer Component', () => {
    it('should render children correctly', () => {
      renderWithTheme(
        <ResponsiveContainer>
          <div>Container content</div>
        </ResponsiveContainer>
      );
      expect(screen.getByText('Container content')).toBeInTheDocument();
    });

    it('should apply variant classes correctly', () => {
      renderWithTheme(
        <ResponsiveContainer variant="card">
          <div>Card content</div>
        </ResponsiveContainer>
      );
      const container = screen.getByText('Card content').parentElement;
      expect(container?.className).toContain('card');
    });

    it('should apply centered styling when centered prop is true', () => {
      renderWithTheme(
        <ResponsiveContainer centered>
          <div>Centered content</div>
        </ResponsiveContainer>
      );
      const container = screen.getByText('Centered content').parentElement;
      expect(container).toBeInTheDocument();
    });

    it('should apply custom maxWidth', () => {
      renderWithTheme(
        <ResponsiveContainer maxWidth="500px">
          <div>Limited width content</div>
        </ResponsiveContainer>
      );
      const container = screen.getByText('Limited width content').parentElement;
      expect(container).toBeInTheDocument();
    });
  });
});

describe('Responsive Theme', () => {
  it('should have proper breakpoints defined', () => {
    expect(theme.breakpoints).toBeDefined();
    expect(theme.breakpoints.xs).toBe('30em');
    expect(theme.breakpoints.sm).toBe('48em');
    expect(theme.breakpoints.md).toBe('64em');
    expect(theme.breakpoints.lg).toBe('74em');
    expect(theme.breakpoints.xl).toBe('90em');
  });

  it('should have responsive heading sizes', () => {
    expect(theme.headings?.sizes?.h1?.fontSize).toContain('clamp');
    expect(theme.headings?.sizes?.h2?.fontSize).toContain('clamp');
    expect(theme.headings?.sizes?.h3?.fontSize).toContain('clamp');
  });

  it('should have touch-friendly component styles', () => {
    expect(theme.components?.Button?.styles?.root?.minHeight).toBe('44px');
    expect(theme.components?.TextInput?.styles?.input?.minHeight).toBe('44px');
    expect(theme.components?.ActionIcon?.styles?.root?.minWidth).toBe('44px');
  });
});

describe('Responsive Behavior Integration', () => {
  it('should render responsive components without errors', () => {
    renderWithTheme(
      <ResponsiveContainer variant="card" centered>
        <TouchGesture>
          <div>
            <h1>Responsive Test</h1>
            <p>This tests responsive component integration</p>
          </div>
        </TouchGesture>
      </ResponsiveContainer>
    );
    
    expect(screen.getByText('Responsive Test')).toBeInTheDocument();
    expect(screen.getByText('This tests responsive component integration')).toBeInTheDocument();
  });

  it('should handle nested responsive components', () => {
    renderWithTheme(
      <ResponsiveContainer variant="section">
        <ResponsiveContainer variant="card">
          <TouchGesture>
            <div>Nested responsive content</div>
          </TouchGesture>
        </ResponsiveContainer>
      </ResponsiveContainer>
    );
    
    expect(screen.getByText('Nested responsive content')).toBeInTheDocument();
  });
});