import { test, expect } from '@playwright/test';

test.describe('Navigation', () => {
  test.beforeEach(async ({ page }) => {
    // Login before each test
    await page.goto('/login');
    await page.getByLabel(/email/i).fill('test@example.com');
    await page.getByLabel(/password/i).fill('password123');
    await page.getByRole('button', { name: /sign in/i }).click();
    await expect(page).toHaveURL(/.*\/dashboard/);
  });

  test('should navigate between main sections', async ({ page }) => {
    // Test navigation to employees
    await page.getByRole('link', { name: /employees/i }).click();
    await expect(page).toHaveURL(/.*\/employees/);
    await expect(page.getByRole('heading', { name: /employees/i })).toBeVisible();

    // Test navigation to departments
    await page.getByRole('link', { name: /departments/i }).click();
    await expect(page).toHaveURL(/.*\/departments/);
    await expect(page.getByRole('heading', { name: /departments/i })).toBeVisible();

    // Test navigation to chat
    await page.getByRole('link', { name: /chat/i }).click();
    await expect(page).toHaveURL(/.*\/chat/);
    await expect(page.getByRole('heading', { name: /chat/i })).toBeVisible();

    // Test navigation back to dashboard
    await page.getByRole('link', { name: /dashboard/i }).click();
    await expect(page).toHaveURL(/.*\/dashboard/);
  });

  test('should show active navigation state', async ({ page }) => {
    // Navigate to employees
    await page.getByRole('link', { name: /employees/i }).click();
    
    // The employees nav item should be active
    const employeesLink = page.getByRole('link', { name: /employees/i });
    await expect(employeesLink).toHaveClass(/active/);
  });

  test('should work with keyboard navigation', async ({ page }) => {
    // Focus on navigation
    await page.keyboard.press('Tab');
    
    // Navigate using arrow keys
    await page.keyboard.press('ArrowDown');
    await page.keyboard.press('Enter');
    
    // Should navigate to the focused item
    await expect(page).not.toHaveURL(/.*\/dashboard/);
  });

  test('should show breadcrumbs on detail pages', async ({ page }) => {
    // Navigate to employees
    await page.getByRole('link', { name: /employees/i }).click();
    
    // Click on an employee to view details
    await page.getByRole('button', { name: /view/i }).first().click();
    
    // Should show breadcrumbs
    await expect(page.getByRole('navigation', { name: /breadcrumb/i })).toBeVisible();
    await expect(page.getByText(/employees/i)).toBeVisible();
  });

  test('should handle mobile navigation', async ({ page }) => {
    // Set mobile viewport
    await page.setViewportSize({ width: 375, height: 667 });
    
    // Mobile menu should be collapsed by default
    const mobileMenu = page.getByRole('button', { name: /menu/i });
    await expect(mobileMenu).toBeVisible();
    
    // Click to open mobile menu
    await mobileMenu.click();
    
    // Navigation items should be visible
    await expect(page.getByRole('link', { name: /employees/i })).toBeVisible();
    await expect(page.getByRole('link', { name: /departments/i })).toBeVisible();
  });
});