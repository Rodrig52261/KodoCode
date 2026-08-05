import { cache } from "react";
import { faqResponseSchema, siteContentResponseSchema } from "./content-schema";

const API_URL = (process.env.API_INTERNAL_URL ?? process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080")
  .trim()
  .replace(/\/+$/, "");
const API_TIMEOUT_MS = 70_000;
const RETRYABLE_STATUS = new Set([408, 425, 429, 502, 503, 504]);

const wait = (milliseconds: number) => new Promise((resolve) => setTimeout(resolve, milliseconds));

async function fetchPublic(path: string): Promise<unknown> {
  let lastError: unknown;

  for (let attempt = 0; attempt < 2; attempt += 1) {
    let response: Response;
    try {
      response = await fetch(`${API_URL}${path}`, {
        cache: "no-store",
        headers: { Accept: "application/json" },
        signal: AbortSignal.timeout(attempt === 0 ? API_TIMEOUT_MS : 20_000),
      });
    } catch (error) {
      lastError = error;
      if (attempt === 1) throw error;
      await wait(1_500);
      continue;
    }

    if (response.ok) return response.json();
    if (!RETRYABLE_STATUS.has(response.status)) throw new Error(`Public API returned ${response.status}`);

    lastError = new Error(`Public API temporarily returned ${response.status}`);
    if (attempt === 1) throw lastError;
    await wait(1_500);
  }

  throw lastError instanceof Error ? lastError : new Error("Public API unavailable");
}

export const getSiteContent = cache(async () =>
  siteContentResponseSchema.parse(await fetchPublic("/api/v1/public/site-content")),
);

export const getPublicFaqs = cache(async () =>
  faqResponseSchema.parse(await fetchPublic("/api/v1/public/faqs")),
);
