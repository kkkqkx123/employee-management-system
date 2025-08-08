import { test, expect } from '@playwright/test';

test.describe('Employee Management', () => {
  test.beforeEach(async ({ page }) => {
    // Login before each test
    await page.goto('/login');
    await page.getByLabel(/email/i).fill('test@example.com');
    await page.getByLabel(/password/i).fill('password123');
    await page.getByRole('button', { name: /sign in/i }).click();
    await expect(page).toHaveURL(/.*\/dashboard/);
    
    // Navigate to employees page
    await page.getByRole('link', { name: /employees/i }).click();
    await expect(page).toHaveURL(/.*\/employees/);
  });

  test('should display employee list', async ({ page }) => {
    await expect(page.getByRole('heading', { name: /employees/i })).toBeVisible();
    await expect(page.getByRole('table')).toBeVisible();
    
    // Should have at least one employee row
    const rows = page.getByRole('row');
    await expect(rows).toHaveCountGreaterThan(1); // Header + at least one data row
  });

  test('should search employees', async ({ page }) => {
    const searchInput = page.getByPlaceholder(/search employees/i);
    await searchInput.fill('John');
    
    // Wait for search results
    await page.waitForTimeout(500);
    
    // Should filter results
    const employeeRows = page.getByRole('row').filter({ hasText: 'John' });
    await expect(employeeRows).toHaveCountGreaterThan(0);
  });

  test('should create new employee', async ({ page }) => {
    await page.getByRole('button', { name: /add employee/i }).click();
    
    // Fill employee form
    await page.getByLabel(/first name/i).fill('Jane');
    await page.getByLabel(/last name/i).fill('Smith');
    await page.getByLabel(/email/i).fill('jane.smith@company.com');
    await page.getByLabel(/employee number/i).fill('EMP002');
    
    // Select department
    await page.getByLabel(/department/i).click();
    await page.getByText('Engineering').click();
    
    // Submit form
    await page.getByRole('button', { name: /save/i }).click();
    
    // Should show success message
    await expect(page.getByText(/employee created successfully/i)).toBeVisible();
    
    // Should return to employee list
    await expect(page).toHaveURL(/.*\/employees/);
  });

  test('should validate required fields', async ({ page }) => {
    await page.getByRole('button', { name: /add employee/i }).click();
    
    // Try to submit without filling required fields
    await page.getByRole('button', { name: /save/i }).click();
    
    // Should show validation errors
    await expect(page.getByText(/first name is required/i)).toBeVisible();
    await expect(page.getByText(/last name is required/i)).toBeVisible();
    await expect(page.getByText(/email is required/i)).toBeVisible();
  });

  test('should edit employee', async ({ page }) => {
    // Click edit button on first employee
    await page.getByRole('button', { name: /edit/i }).first().click();
    
    // Update employee information
    const firstNameInput = page.getByLabel(/first name/i);
    await firstNameInput.clear();
    await firstNameInput.fill('Updated Name');
    
    // Save changes
    await page.getByRole('button', { name: /save/i }).click();
    
    // Should show success message
    await expect(page.getByText(/employee updated successfully/i)).toBeVisible();
  });

  test('should delete employee with confirmation', async ({ page }) => {
    // Click delete button on first employee
    await page.getByRole('button', { name: /delete/i }).first().click();
    
    // Should show confirmation dialog
    await expect(page.getByText(/are you sure/i)).toBeVisible();
    
    // Confirm deletion
    await page.getByRole('button', { name: /confirm/i }).click();
    
    // Should show success message
    await expect(page.getByText(/employee deleted successfully/i)).toBeVisible();
  });
});