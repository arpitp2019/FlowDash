import { spawn } from 'node:child_process';

const previewUrl = 'http://127.0.0.1:4173/FlowDash/';
const server = spawn(process.execPath, ['tests/serve-pages-preview.mjs'], {
  cwd: process.cwd(),
  stdio: 'inherit'
});

try {
  await waitForPreview();

  const testExitCode = await runPlaywright();
  process.exitCode = testExitCode;
} finally {
  server.kill();
}

async function waitForPreview() {
  const deadline = Date.now() + 60_000;

  while (Date.now() < deadline) {
    try {
      const response = await fetch(previewUrl);
      if (response.ok) {
        return;
      }
    } catch {
      // Keep polling until the preview server is ready.
    }

    await new Promise((resolve) => setTimeout(resolve, 250));
  }

  throw new Error(`Timed out waiting for ${previewUrl}`);
}

function runPlaywright() {
  return new Promise((resolve, reject) => {
    const child = spawn(process.execPath, ['node_modules/@playwright/test/cli.js', 'test', 'tests/pages-subpath.spec.js', '--reporter=line'], {
      cwd: process.cwd(),
      stdio: 'inherit'
    });

    child.on('error', reject);
    child.on('exit', (code) => resolve(code ?? 1));
  });
}
