import type { NextConfig } from "next";

const publicApiUrl = process.env.NEXT_PUBLIC_API_URL?.trim() ?? "";
const internalApiUrl = (process.env.API_INTERNAL_URL ?? "http://localhost:8080").replace(/\/$/, "");
const siteUrl = process.env.NEXT_PUBLIC_SITE_URL ?? "http://localhost:3000";
let apiOrigin = "";
try {
  if (publicApiUrl) apiOrigin = new URL(publicApiUrl).origin;
} catch {
  throw new Error("NEXT_PUBLIC_API_URL deve ser uma URL absoluta ou ficar vazio para usar o proxy same-origin.");
}
const isDevelopment = process.env.NODE_ENV !== "production";
const isHttps = siteUrl.startsWith("https://");
const contentSecurityPolicy = [
  "default-src 'self'",
  "base-uri 'self'",
  "object-src 'none'",
  "frame-ancestors 'none'",
  "form-action 'self'",
  `script-src 'self' 'unsafe-inline'${isDevelopment ? " 'unsafe-eval'" : ""}`,
  "style-src 'self' 'unsafe-inline'",
  "img-src 'self' data:",
  "font-src 'self' data:",
  `connect-src 'self'${apiOrigin ? ` ${apiOrigin}` : ""}${isDevelopment ? " ws: wss:" : ""}`,
  "manifest-src 'self'",
].join("; ");

const nextConfig: NextConfig = {
  output: "standalone",
  poweredByHeader: false,
  reactStrictMode: true,
  async rewrites() {
    return [{ source: "/api/:path*", destination: `${internalApiUrl}/api/:path*` }];
  },
  async headers() {
    const securityHeaders = [
      { key: "Content-Security-Policy", value: contentSecurityPolicy },
      { key: "X-Content-Type-Options", value: "nosniff" },
      { key: "Referrer-Policy", value: "strict-origin-when-cross-origin" },
      { key: "Permissions-Policy", value: "camera=(), microphone=(), geolocation=(), payment=(), usb=(), browsing-topics=()" },
      { key: "X-Frame-Options", value: "DENY" },
      { key: "X-DNS-Prefetch-Control", value: "off" },
      { key: "Cross-Origin-Opener-Policy", value: "same-origin" },
      { key: "Cross-Origin-Resource-Policy", value: "same-origin" },
    ];
    if (isHttps) securityHeaders.push({ key: "Strict-Transport-Security", value: "max-age=31536000" });
    return [{ source: "/(.*)", headers: securityHeaders }];
  },
};

export default nextConfig;
