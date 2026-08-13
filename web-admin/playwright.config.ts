import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  testMatch: /.*\.spec\.ts/,
  timeout: 30000,
  fullyParallel: false,
  retries: 0,
  use: {
    baseURL: 'http://localhost:3000',
    headless: true,
  },
  webServer: {
    command: 'node server.js',
    url: 'http://localhost:3000/api/faq',
    reuseExistingServer: true,
    env: {
      FAQ_DATA_DIR: './tests/fixtures',
      PORT: '3000',
    },
    timeout: 20000,
  },
});
