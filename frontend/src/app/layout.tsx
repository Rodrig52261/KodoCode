import type { Metadata, Viewport } from "next";
import "./globals.css";

const siteUrl = process.env.NEXT_PUBLIC_SITE_URL ?? "http://localhost:3000";

export const metadata: Metadata = {
  metadataBase: new URL(siteUrl),
  title: "Kodo Code | Soluções digitais inteligentes",
  description: "Sites, sistemas, CRMs e automações pensados para o seu negócio.",
  applicationName: "Kodo Code",
  authors: [{ name: "Kodo Code" }],
  creator: "Kodo Code",
  formatDetection: { email: false, address: false, telephone: false },
};

export const viewport: Viewport = {
  width: "device-width",
  initialScale: 1,
  themeColor: "#0b1533",
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="pt-BR">
      <body>
        <a className="fixed left-3 top-3 z-[100] -translate-y-20 rounded-lg bg-white px-4 py-3 font-bold text-[var(--ink)] shadow-lg transition focus:translate-y-0" href="#conteudo-principal">
          Ir para o conteúdo
        </a>
        {children}
      </body>
    </html>
  );
}
