# Testing Guide

This document provides comprehensive information about the testing setup and practices for the frontend application.

## Testing Stack

### Core Testing Tools
- **Vitest** - Fast unit test runner with native ESM support
- **React Testing Library** - Component testing utilities focused on user behavior
- **Playwright** - End-to-end testing framework for cross-browser testing
- **Storybook** - Component development and visual regression testing
- **MSW (Mock Service Worker)** - API mocking for tests
- **Jest-axe** - Accessibility testing utilities

### Additional Testing Libraries
- **@faker-js/faker** - Generate realistic mock data
- **@testing-library/user-event** - Simulate user interactions
- **Chromatic** - Visual regression testing service

## Test Categories

### 1. Unit Tests (70% of test suite)
Test individual components, hooks, and utilities in isolation.

```bash
# Run all unit tests
npm run test

# Run tests in watch mode
npm run test:watch

# Run tests with UI
npm run test:ui
```

**Location**: `src/**/*.{test,spec}.{js,ts,jsx,tsx}`

**Example**:
```typescript
import { render, screen } from '@/test/utils/test-utils';
import { Button } from './Button';

test('should render button with text', () => {
  render(<Button>Click me</Button>);
  expect(screen.getByRole('button', { name: /click me/i })).toBeInTheDocument();
});
```

### 2. Integration Tests (20% of test suite)
Test feature workflows and component interactions.

```bash
# Run integration tests
npm run test:integration
```

**Location**: `src/**/*.integration.{test,spec}.{js,ts,jsx,tsx}`

**Example**:
```typescript
import { render, screen, fireEvent } from '@/test/utils/test-utils';
import { EmployeesPage } from './EmployeesPage';

test('should create new employee', async () => {
  render(<EmployeesPage />);
  
  fireEvent.click(screen.getByRole('button', { name: /add employee/i }));
  // ... test complete workflow
});
```

### 3. End-to-End Tests (10% of test suite)
Test critical user paths across the entire application.

```bash
# Run E2E tests
npm run test:e2e

# Run E2E tests with UI
npm run test:e2e:ui

# Debug E2E tests
npm run test:e2e:debug
```

**Location**: `e2e/**/*.spec.ts`

**Example**:
```typescript
import { test, expect } from '@playwright/test';

test('should login and navigate to dashboard', async ({ page }) => {
  await page.goto('/login');
  await page.fill('[name="email"]', 'test@example.com');
  await page.fill('[name="password"]', 'password123');
  await page.click('button[type="submit"]');
  
  await expect(page).toHaveURL(/.*\/dashboard/);
});
```

### 4. Visual Regression Tests
Test component appearance and prevent visual regressions.

```bash
# Start Storybook
npm run storybook

# Build Storybook
npm run build-storybook

# Run visual regression tests
npm run chromatic
```

**Location**: `src/**/*.stories.{js,ts,jsx,tsx}`

## Test Utilities

### Mock Data Factories
Use factories to generate consistent test data:

```typescript
import { createMockEmployee, createMockDepartment } from '@/test/factories';

const employee = createMockEmployee({ firstName: 'John', lastName: 'Doe' });
const department = createMockDepartment({ name: 'Engineering' });
```

### Custom Render Function
Use the enhanced render function that includes all providers:

```typescript
import { render, screen } from '@/test/utils/test-utils';

// Automatically wraps with Router, QueryClient, and MantineProvider
render(<MyComponent />);
```

### Accessibility Testing
Test components for accessibility compliance:

```typescript
import { expectToHaveNoA11yViolations } from '@/test/utils/test-utils';

test('should be accessible', async () => {
  const { container } = render(<MyComponent />);
  await expectToHaveNoA11yViolations(container);
});
```

## Coverage Requirements

### Global Thresholds
- **Branches**: 80%
- **Functions**: 80%
- **Lines**: 80%
- **Statements**: 80%

### Enhanced Thresholds for Critical Areas
- **UI Components** (`src/components/ui/`): 90%
- **Authentication** (`src/features/auth/`): 85%
- **Custom Hooks** (`src/hooks/`): 85%

```bash
# Generate coverage report
npm run test:coverage
```

## CI/CD Integration

### GitHub Actions Workflow
The testing pipeline includes:

1. **Unit Tests** - Run with coverage reporting
2. **Integration Tests** - Test feature workflows
3. **E2E Tests** - Cross-browser testing with Playwright
4. **Visual Regression** - Chromatic integration
5. **Accessibility Tests** - Automated a11y checks

### Quality Gates
- Minimum 80% code coverage
- Zero failed tests
- Maximum 5-minute test duration
- Bundle size under 2MB

## Best Practices

### Writing Tests

1. **Follow AAA Pattern**:
   ```typescript
   test('should do something', () => {
     // Arrange
     const props = { value: 'test' };
     
     // Act
     render(<Component {...props} />);
     
     // Assert
     expect(screen.getByText('test')).toBeInTheDocument();
   });
   ```

2. **Test User Behavior, Not Implementation**:
   ```typescript
   // Good - tests user interaction
   fireEvent.click(screen.getByRole('button', { name: /submit/i }));
   
   // Bad - tests implementation details
   fireEvent.click(screen.getByTestId('submit-button'));
   ```

3. **Use Semantic Queries**:
   ```typescript
   // Preferred order
   screen.getByRole('button', { name: /submit/i });
   screen.getByLabelText(/email/i);
   screen.getByText(/welcome/i);
   screen.getByDisplayValue(/john/i);
   ```

### Mock Management

1. **Use MSW for API Mocking**:
   ```typescript
   server.use(
     http.get('/api/employees', () => {
       return HttpResponse.json({ data: mockEmployees });
     })
   );
   ```

2. **Mock External Dependencies**:
   ```typescript
   vi.mock('socket.io-client', () => ({
     io: vi.fn(() => ({
       on: vi.fn(),
       emit: vi.fn(),
       disconnect: vi.fn(),
     })),
   }));
   ```

### Performance Testing

1. **Test Loading States**:
   ```typescript
   test('should show loading spinner', () => {
     render(<AsyncComponent />);
     expect(screen.getByText(/loading/i)).toBeInTheDocument();
   });
   ```

2. **Test Error Boundaries**:
   ```typescript
   test('should handle errors gracefully', () => {
     const ThrowError = () => { throw new Error('Test error'); };
     render(<ErrorBoundary><ThrowError /></ErrorBoundary>);
     expect(screen.getByText(/something went wrong/i)).toBeInTheDocument();
   });
   ```

## Debugging Tests

### Common Issues

1. **Async Operations**: Use `waitFor` or `findBy` queries
2. **User Events**: Use `@testing-library/user-event` for realistic interactions
3. **Timers**: Use `vi.useFakeTimers()` for time-dependent tests
4. **Network Requests**: Ensure MSW handlers are properly configured

### Debug Commands

```bash
# Run specific test file
npm run test Button.test.tsx

# Run tests matching pattern
npm run test -- --testNamePattern="should render"

# Debug with browser
npm run test:e2e:debug

# View test coverage details
npm run test:coverage && open coverage/index.html
```

This comprehensive testing setup ensures high code quality, prevents regressions, and provides confidence in deployments.