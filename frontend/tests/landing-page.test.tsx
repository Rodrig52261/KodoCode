import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { faqResponseSchema, type SiteSections, siteContentResponseSchema } from "@/features/site/content-schema";
import { LandingPage } from "@/features/site/landing-page";

const sections: SiteSections = {
  seo: { title: "Kodo Code", description: "Descrição", siteName: "Kodo Code", locale: "pt_BR", keywords: [] },
  navigation: { items: [{ label: "Início", href: "#inicio" }, { label: "Soluções", href: "#solucoes" }], ctaLabel: "Orçamento", ctaHref: "#contato" },
  hero: {
    eyebrow: "Tecnologia",
    title: "Soluções digitais inteligentes",
    description: "Sites e sistemas para empresas.",
    primaryCta: { label: "Solicitar orçamento", href: "#contato" },
    secondaryCta: { label: "Conhecer soluções", href: "#solucoes" },
    highlights: ["Sob medida"],
    visual: { eyebrow: "Fluxo inteligente", steps: [{ title: "Atendimento", description: "Contato organizado" }], status: "Processo conectado" },
  },
  benefits: {
    eyebrow: "Benefícios",
    title: "Tecnologia útil",
    description: "Resultado real.",
    items: [{ title: "Segurança", description: "Proteção desde o início.", icon: "shield" }],
  },
  services: {
    eyebrow: "Soluções",
    title: "Serviços",
    description: "Escolha a solução.",
    items: [{ name: "CRM", description: "Organize clientes.", benefits: ["Funil"], ctaLabel: "Conhecer", icon: "users" }],
  },
  process: {
    eyebrow: "Processo",
    title: "Como trabalhamos",
    description: "Etapas claras.",
    items: [{ number: "01", title: "Entendimento", description: "Conhecemos o negócio." }],
  },
  differentials: { eyebrow: "Diferenciais", title: "Clareza", items: [{ title: "Comunicação", description: "Sem ruído." }] },
  about: {
    eyebrow: "Sobre",
    title: "Kodo Code",
    paragraphs: ["Tecnologia orientada a resultado."],
    mission: "Simplificar processos.",
    vision: "Ser uma parceira confiável.",
    values: ["Transparência"],
    missionLabel: "Missão",
    visionLabel: "Visão",
    valuesLabel: "Valores",
  },
  cta: { eyebrow: "Vamos conversar", title: "Tire a ideia do papel", description: "Conte seu desafio.", buttonLabel: "Falar", buttonHref: "#contato" },
  contact: {
    eyebrow: "Contato",
    title: "Fale com a gente",
    description: "Envie sua necessidade.",
    email: "contato@kodocode.com.br",
    emailLabel: "Enviar e-mail",
    emailPrompt: "Prefere e-mail?",
    responseTime: "Até 1 dia útil",
  },
  faq: { eyebrow: "Perguntas frequentes", title: "Tire suas dúvidas", description: "Respostas objetivas." },
  footer: {
    description: "Soluções digitais.",
    email: "contato@kodocode.com.br",
    copyrightName: "Kodo Code",
    servicesTitle: "Soluções",
    institutionalTitle: "Institucional",
    closingText: "Soluções digitais com clareza.",
    serviceLinks: ["CRM"],
    legalLinks: [{ label: "Privacidade", href: "/politica-de-privacidade" }],
    socialLinks: [],
  },
};

const faqs = faqResponseSchema.parse([{
  id: "30000000-0000-4000-8000-000000000001",
  question: "Quanto tempo demora?",
  answer: "Depende do escopo.",
  displayOrder: 1,
}]);

describe("LandingPage", () => {
  it("renders API-driven sections and accessible navigation", () => {
    render(<LandingPage sections={sections} faqs={faqs} />);

    expect(screen.getByRole("heading", { level: 1, name: "Soluções digitais inteligentes" })).toBeInTheDocument();
    expect(screen.getByRole("navigation", { name: "Navegação principal" })).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "CRM" })).toBeInTheDocument();
    expect(screen.getByText("Quanto tempo demora?")).toBeInTheDocument();
    expect(screen.getAllByRole("link", { name: "contato@kodocode.com.br" }))
      .toEqual(expect.arrayContaining([expect.objectContaining({ href: "mailto:contato@kodocode.com.br" })]));
  });

  it("rejects an incomplete public content contract", () => {
    expect(() => siteContentResponseSchema.parse({ sections: { hero: sections.hero }, publishedAt: null })).toThrow();
  });
});
