import { defineConfig } from "@playwright/test";
export default defineConfig({
  testDir: "./tests/e2e", timeout: 45_000, fullyParallel: false, retries: 1,
  use: { baseURL: process.env.E2E_BASE_URL ?? "http://localhost:3001", trace: "retain-on-failure" },
});
