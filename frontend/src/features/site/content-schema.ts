import { z } from "zod";

const internalLinkSchema = z.string().max(500).refine(
  (value) => /^#[A-Za-z][A-Za-z0-9_-]{0,79}$/.test(value)
    || (value.startsWith("/") && !value.startsWith("//") && !value.includes("\\") && !/\s/.test(value)),
  "Link interno inválido.",
);

const linkSchema = z.object({
  label: z.string().min(1),
  href: internalLinkSchema,
});

const titledItemSchema = z.object({
  title: z.string().min(1),
  description: z.string().min(1),
});

const sectionsSchema = z.object({
  seo: z.object({
    title: z.string().min(1),
    description: z.string().min(1),
    siteName: z.string().min(1),
    locale: z.string().min(1),
    keywords: z.array(z.string()),
  }),
  navigation: z.object({
    items: z.array(linkSchema),
    ctaLabel: z.string().min(1),
    ctaHref: internalLinkSchema,
  }),
  hero: z.object({
    eyebrow: z.string().min(1),
    title: z.string().min(1),
    description: z.string().min(1),
    primaryCta: linkSchema,
    secondaryCta: linkSchema,
    highlights: z.array(z.string()),
    visual: z.object({
      eyebrow: z.string().min(1),
      steps: z.array(titledItemSchema),
      status: z.string().min(1),
    }),
  }),
  benefits: z.object({
    eyebrow: z.string().min(1),
    title: z.string().min(1),
    description: z.string().min(1),
    items: z.array(titledItemSchema.extend({ icon: z.string() })),
  }),
  services: z.object({
    eyebrow: z.string().min(1),
    title: z.string().min(1),
    description: z.string().min(1),
    items: z.array(z.object({
      name: z.string().min(1),
      description: z.string().min(1),
      benefits: z.array(z.string()),
      ctaLabel: z.string().min(1),
      icon: z.string(),
    })),
  }),
  process: z.object({
    eyebrow: z.string().min(1),
    title: z.string().min(1),
    description: z.string().min(1),
    items: z.array(titledItemSchema.extend({ number: z.string().min(1) })),
  }),
  differentials: z.object({
    eyebrow: z.string().min(1),
    title: z.string().min(1),
    items: z.array(titledItemSchema),
  }),
  about: z.object({
    eyebrow: z.string().min(1),
    title: z.string().min(1),
    paragraphs: z.array(z.string()),
    mission: z.string().min(1),
    vision: z.string().min(1),
    values: z.array(z.string()),
    missionLabel: z.string().min(1),
    visionLabel: z.string().min(1),
    valuesLabel: z.string().min(1),
  }),
  cta: z.object({
    eyebrow: z.string().min(1),
    title: z.string().min(1),
    description: z.string().min(1),
    buttonLabel: z.string().min(1),
    buttonHref: internalLinkSchema,
  }),
  contact: z.object({
    eyebrow: z.string().min(1),
    title: z.string().min(1),
    description: z.string().min(1),
    email: z.string().email(),
    emailLabel: z.string().min(1),
    emailPrompt: z.string().min(1),
    responseTime: z.string().min(1),
  }),
  faq: z.object({
    eyebrow: z.string().min(1),
    title: z.string().min(1),
    description: z.string().min(1),
  }),
  footer: z.object({
    description: z.string().min(1),
    email: z.string().email(),
    copyrightName: z.string().min(1),
    servicesTitle: z.string().min(1),
    institutionalTitle: z.string().min(1),
    closingText: z.string().min(1),
    serviceLinks: z.array(z.string()),
    legalLinks: z.array(linkSchema),
    socialLinks: z.array(linkSchema),
  }),
});

export const siteContentResponseSchema = z.object({
  sections: sectionsSchema,
  publishedAt: z.string().nullable(),
});

export const faqResponseSchema = z.array(z.object({
  id: z.string().uuid(),
  question: z.string().min(1),
  answer: z.string().min(1),
  displayOrder: z.number().int().nonnegative(),
}));

export type SiteContent = z.infer<typeof siteContentResponseSchema>;
export type SiteSections = SiteContent["sections"];
export type PublicFaq = z.infer<typeof faqResponseSchema>[number];
