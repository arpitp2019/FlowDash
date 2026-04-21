import { expect, test } from '@playwright/test';

const email = `habits-${Date.now()}-${Math.random().toString(36).slice(2)}@example.com`;
const today = new Date().toISOString().slice(0, 10);

test('Habits shows checklist, calendar consistency, and analytics', async ({ page }) => {
  await page.goto('/login', { waitUntil: 'domcontentloaded' });
  await page.locator('.segmented').getByRole('button', { name: 'Create account' }).click();

  await page.getByLabel('Email').fill(email);
  await page.getByLabel('Display name').fill('Habit Tester');
  await page.getByLabel('Password').fill('Secret123!');
  await page.locator('.auth-form').getByRole('button', { name: 'Create account' }).click();

  await expect(page.getByRole('link', { name: 'Habits' })).toBeVisible();
  await page.getByRole('link', { name: 'Habits' }).click();
  await expect(page).toHaveURL(/\/habits$/);
  await expect(page.getByRole('button', { name: 'Checklist' })).toBeVisible();
  await expect(page.getByRole('button', { name: 'Calendar' })).toBeVisible();
  await expect(page.getByRole('button', { name: 'Analytics' })).toBeVisible();
  await expect(page.getByRole('heading', { name: 'Daily Habit Checklist' })).toBeVisible();

  await page.getByRole('button', { name: 'New habit' }).click();
  await expect(page.getByRole('dialog', { name: 'Create habit' })).toBeVisible();
  await page.getByLabel('Habit title').fill('Morning walk');
  await page.getByLabel('Description').fill('Walk after tea before opening messages.');
  await page.getByLabel('Type').selectOption('BUILD');
  await page.getByLabel('Reminder time').fill('07:30');
  await page.getByRole('button', { name: 'Create habit' }).click();

  await page.getByRole('button', { name: 'New habit' }).click();
  await page.getByLabel('Habit title').fill('Read pages');
  await page.getByLabel('Description').fill('Read before entertainment.');
  await page.getByLabel('Type').selectOption('NUMERIC');
  await page.getByLabel('Target value').fill('20');
  await page.getByLabel('Target unit').fill('pages');
  await page.getByRole('button', { name: 'Create habit' }).click();

  const walkCheckbox = page.getByRole('checkbox', { name: 'Complete Morning walk' });
  await walkCheckbox.click();
  await expect(walkCheckbox).toBeChecked();
  await expect(page.locator('.habit-row.complete').filter({ hasText: 'Morning walk' }).first()).toBeVisible();

  const readInput = page.getByLabel('Read pages value');
  await readInput.fill('25');
  const readCheckbox = page.getByRole('checkbox', { name: 'Complete Read pages' });
  await readCheckbox.click();
  await expect(readCheckbox).toBeChecked();
  await expect(page.locator('.habit-row.complete').filter({ hasText: 'Read pages' }).first()).toBeVisible();

  await page.getByRole('button', { name: 'Calendar' }).click();
  await expect(page.getByRole('heading', { name: 'Calendar Consistency View' })).toBeVisible();
  await expect(page.locator(`.calendar-day[data-date="${today}"]`)).toHaveAttribute('data-status', 'full');
  await page.getByRole('button', { name: 'Analytics' }).click();
  await expect(page.getByRole('heading', { name: 'Habit Analytics Dashboard' })).toBeVisible();
  await expect(page.locator('.habit-analytics-card').filter({ hasText: 'Morning walk' }).first()).toBeVisible();

  await page.getByRole('button', { name: 'Checklist' }).click();
  await expect(page.getByRole('heading', { name: 'Daily Habit Checklist' })).toBeVisible();
  await expect(page.locator('.habit-row.complete').filter({ hasText: 'Morning walk' }).first()).toBeVisible();

  await page.reload({ waitUntil: 'domcontentloaded' });
  await expect(page.getByRole('button', { name: 'Checklist' })).toBeVisible();
  await expect(page.getByRole('heading', { name: 'Daily Habit Checklist' })).toBeVisible();
  await expect(page.locator(`.calendar-day[data-date="${today}"]`)).toHaveAttribute('data-status', 'full');
  await expect(page.locator('.habit-row.complete').filter({ hasText: 'Morning walk' }).first()).toBeVisible();
});
