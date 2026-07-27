import type { Metadata } from "next";
import { ContentUnavailable } from "@/features/site/content-unavailable";
import { LandingPage } from "@/features/site/landing-page";
import { getPublicFaqs, getSiteContent } from "@/features/site/public-api";

export const dynamic = "force-dynamic";

export async function generateMetadata(): Promise<Metadata> {
  try {
    const { sections } = await getSiteContent();
    return {
      title: sections.seo.title,
      description: sections.seo.description,
      keywords: sections.seo.keywords,
      alternates: { canonical: "/" },
      openGraph: {
        type: "website",
        locale: sections.seo.locale,
        siteName: sections.seo.siteName,
        title: sections.seo.title,
        description: sections.seo.description,
        url: "/",
      },
      twitter: {
        card: "summary_large_image",
        title: sections.seo.title,
        description: sections.seo.description,
      },
    };
  } catch {
    return {};
  }
}

export default async function HomePage() {
  const publicData = await Promise.all([getSiteContent(), getPublicFaqs()])
    .then(([content, faqs]) => ({ content, faqs }))
    .catch(() => null);

  if (!publicData) return <ContentUnavailable />;
  return <LandingPage sections={publicData.content.sections} faqs={publicData.faqs} />;
}
