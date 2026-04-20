import { expect, test } from '@playwright/test';

function captureUnexpectedErrors(page) {
  const errors = [];

  page.on('pageerror', (error) => {
    errors.push(error.message);
  });

  page.on('console', (message) => {
    if (message.type() !== 'error') {
      return;
    }

    const text = message.text();
    const isExpectedStaticAuthMiss = text.includes('Failed to load resource') && (text.includes('401') || text.includes('404'));
    if (!isExpectedStaticAuthMiss) {
      errors.push(text);
    }
  });

  return errors;
}

async function expectLoginCard(page) {
  await expect(page.getByRole('heading', { name: 'FlowDash' })).toBeVisible();
  await expect(page.getByText('Professional focus and decision-making for a small team')).toBeVisible();
  await expect(page.getByLabel('Email')).toBeVisible();
  await expect(page.getByLabel('Password')).toBeVisible();
  await expect(page.getByRole('button', { name: 'Sign in' }).last()).toBeVisible();
  await expect(page.getByRole('button', { name: 'Create account' })).toBeVisible();
}

async function mockSignedOutSession(page) {
  await page.route('**/api/me', async (route) => {
    await route.fulfill({
      status: 401,
      contentType: 'application/json',
      body: JSON.stringify({ message: 'Unauthorized' })
    });
  });
}

test('renders the login screen after root auth redirect under the Pages subpath', async ({ page }) => {
  const errors = captureUnexpectedErrors(page);

  await mockSignedOutSession(page);
  await page.goto('/FlowDash/');
  await expect(page).toHaveURL(/\/FlowDash\/login$/);
  await expectLoginCard(page);

  expect(errors).toEqual([]);
});

test('renders the login route directly under the Pages subpath', async ({ page }) => {
  const errors = captureUnexpectedErrors(page);

  await mockSignedOutSession(page);
  await page.goto('/FlowDash/login');
  await expect(page).toHaveURL(/\/FlowDash\/login$/);
  await expectLoginCard(page);

  expect(errors).toEqual([]);
});
