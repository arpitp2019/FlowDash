import { expect, test } from '@playwright/test';

const email = `goals-${Date.now()}-${Math.random().toString(36).slice(2)}@example.com`;
const today = new Date().toISOString().slice(0, 10);

test('Goals supports checklist progress, yearly calendar, and analytics pages', async ({ page }) => {
  await page.goto('/login', { waitUntil: 'domcontentloaded' });
  await page.locator('.segmented').getByRole('button', { name: 'Create account' }).click();

  await page.getByLabel('Email').fill(email);
  await page.getByLabel('Display name').fill('Goal Tester');
  await page.getByLabel('Password').fill('Secret123!');
  await page.locator('.auth-form').getByRole('button', { name: 'Create account' }).click();

  await expect(page.getByRole('link', { name: 'Goals' })).toBeVisible();
  await page.getByRole('link', { name: 'Goals' }).click();
  await expect(page).toHaveURL(/\/goals\/checklist$/);
  await expect(page.getByRole('link', { name: 'Checklist' })).toBeVisible();
  await expect(page.getByRole('link', { name: 'Calendar' })).toBeVisible();
  await expect(page.getByRole('link', { name: 'Analytics' })).toBeVisible();

  await page.getByLabel('Title').fill('Launch portfolio');
  await page.getByLabel('Description').fill('Ship the public version and polish screenshots.');
  await page.getByLabel('Due date').fill(today);
  await page.getByRole('button', { name: 'Create goal' }).click();

  await page.getByLabel('Title').fill('Deep work block');
  await page.getByLabel('Description').fill('Protect one focused block before messages.');
  await page.getByRole('button', { name: 'Create goal' }).click();

  const portfolioCheckbox = page.getByRole('checkbox', { name: 'Progress Launch portfolio' });
  await portfolioCheckbox.click();
  await expect(portfolioCheckbox).toBeChecked();
  await expect(page.locator('.goal-row.complete').filter({ hasText: 'Launch portfolio' }).first()).toBeVisible();

  await page.getByRole('link', { name: 'Calendar' }).click();
  await expect(page).toHaveURL(/\/goals\/calendar$/);
  await expect(page.getByRole('heading', { name: 'Calendar' })).toBeVisible();
  await expect(page.locator(`.goal-day[data-date="${today}"]`)).not.toHaveAttribute('data-level', '0');

  await page.getByRole('link', { name: 'Analytics' }).click();
  await expect(page).toHaveURL(/\/goals\/analytics$/);
  await expect(page.getByRole('heading', { name: 'Analytics' })).toBeVisible();
  await expect(page.locator('.goal-analytics-card').filter({ hasText: 'Launch portfolio' }).first()).toBeVisible();
});
