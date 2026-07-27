import { cache } from "react";
import { faqResponseSchema, siteContentResponseSchema } from "./content-schema";

const API_URL = process.env.API_INTERNAL_URL ?? process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

async function fetchPublic(path: string): Promise<unknown> {
  const response = await fetch(`${API_URL}${path}`, {
    cache: "no-store",
    headers: { Accept: "application/json" },
    signal: AbortSignal.timeout(5_000),
  });

  if (!response.ok) throw new Error(`Public API returned ${response.status}`);
  return response.json();
}

export const getSiteContent = cache(async () =>
  siteContentResponseSchema.parse(await fetchPublic("/api/v1/public/site-content")),
);

export const getPublicFaqs = cache(async () =>
  faqResponseSchema.parse(await fetchPublic("/api/v1/public/faqs")),
);
