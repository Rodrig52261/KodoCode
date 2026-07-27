import type { MetadataRoute } from "next";

export default function sitemap(): MetadataRoute.Sitemap {
  const siteUrl = process.env.NEXT_PUBLIC_SITE_URL ?? "http://localhost:3000";
  const updatedAt = new Date();
  return [
    { url: siteUrl, lastModified: updatedAt, changeFrequency: "weekly", priority: 1 },
    { url: `${siteUrl}/politica-de-privacidade`, lastModified: updatedAt, changeFrequency: "yearly", priority: 0.2 },
    { url: `${siteUrl}/termos-de-uso`, lastModified: updatedAt, changeFrequency: "yearly", priority: 0.2 },
  ];
}
