import type { SiteSections } from "./content-schema";

export function OrganizationSchema({ sections }: Readonly<{ sections: SiteSections }>) {
  const siteUrl = process.env.NEXT_PUBLIC_SITE_URL ?? "http://localhost:3000";
  const data = {
    "@context": "https://schema.org",
    "@type": "ProfessionalService",
    name: sections.seo.siteName,
    url: siteUrl,
    email: sections.contact.email,
    description: sections.seo.description,
    areaServed: "BR",
    knowsAbout: sections.services.items.map((service) => service.name),
    hasOfferCatalog: {
      "@type": "OfferCatalog",
      name: "Soluções digitais",
      itemListElement: sections.services.items.map((service) => ({
        "@type": "Offer",
        itemOffered: {
          "@type": "Service",
          name: service.name,
          description: service.description,
        },
      })),
    },
  };

  return <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: JSON.stringify(data).replace(/</g, "\\u003c") }} />;
}
