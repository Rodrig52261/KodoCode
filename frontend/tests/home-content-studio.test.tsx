import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { HomeContentStudio } from "@/features/admin/home-content-studio";

const apiRequest = vi.hoisted(() => vi.fn());
vi.mock("@/lib/api/client", () => ({
  ApiError: class ApiError extends Error {},
  apiRequest,
  initializeCsrf: vi.fn(),
}));

const sections = {
  seo: { title: "Kodo Code", description: "Descrição para busca", siteName: "Kodo Code", locale: "pt_BR", keywords: ["tecnologia"] },
  navigation: { items: [{ label: "Início", href: "#inicio" }], ctaLabel: "Orçamento", ctaHref: "#contato" },
  hero: {
    eyebrow: "Tecnologia", title: "Soluções digitais inteligentes", description: "Sites e sistemas para empresas.",
    primaryCta: { label: "Solicitar orçamento", href: "#contato" }, secondaryCta: { label: "Conhecer soluções", href: "#solucoes" },
    highlights: ["Sob medida"], visual: { eyebrow: "Fluxo", steps: [{ title: "Atendimento", description: "Contato organizado" }], status: "Conectado" },
  },
  benefits: { eyebrow: "Benefícios", title: "Tecnologia útil", description: "Resultado real.", items: [{ title: "Segurança", description: "Proteção desde o início.", icon: "shield" }] },
  services: { eyebrow: "Soluções", title: "Serviços", description: "Escolha a solução.", items: [{ name: "CRM", description: "Organize clientes.", benefits: ["Funil"], ctaLabel: "Conhecer", icon: "users" }] },
  process: { eyebrow: "Processo", title: "Como trabalhamos", description: "Etapas claras.", items: [{ number: "01", title: "Entendimento", description: "Conhecemos o negócio." }] },
  differentials: { eyebrow: "Diferenciais", title: "Clareza", items: [{ title: "Comunicação", description: "Sem ruído." }] },
  about: { eyebrow: "Sobre", title: "Kodo Code", paragraphs: ["Tecnologia orientada a resultado."], mission: "Simplificar.", vision: "Ser referência.", values: ["Transparência"], missionLabel: "Missão", visionLabel: "Visão", valuesLabel: "Valores" },
  faq: { eyebrow: "Perguntas frequentes", title: "Tire suas dúvidas", description: "Respostas objetivas." },
  cta: { eyebrow: "Vamos conversar", title: "Tire a ideia do papel", description: "Conte seu desafio.", buttonLabel: "Falar", buttonHref: "#contato" },
  contact: { eyebrow: "Contato", title: "Fale com a gente", description: "Envie sua necessidade.", email: "contato@example.com", emailLabel: "Enviar e-mail", emailPrompt: "Prefere e-mail?", responseTime: "Até 1 dia útil" },
  footer: { description: "Soluções digitais.", email: "contato@example.com", copyrightName: "Kodo Code", servicesTitle: "Soluções", institutionalTitle: "Institucional", closingText: "Construído com clareza.", serviceLinks: ["CRM"], legalLinks: [{ label: "Privacidade", href: "/politica-de-privacidade" }], socialLinks: [] },
};

const faq = { id: "30000000-0000-4000-8000-000000000001", question: "Quanto tempo demora?", answer: "Depende do escopo.", displayOrder: 1, active: true, status: "PUBLISHED" };

describe("HomeContentStudio", () => {
  beforeEach(() => {
    apiRequest.mockReset();
    apiRequest.mockImplementation((path: string) => {
      if (path === "/api/v1/admin/content") return Promise.resolve(Object.keys(sections).map((sectionKey, index) => ({ id: `10000000-0000-4000-8000-${String(index + 1).padStart(12, "0")}`, sectionKey, title: sectionKey, status: "PUBLISHED", publishedVersion: 1, updatedAt: "2026-01-01T00:00:00Z" })));
      if (path === "/api/v1/admin/faqs") return Promise.resolve([faq]);
      if (path.endsWith("/versions")) {
        const key = path.replace("/api/v1/admin/content/", "").replace("/versions", "") as keyof typeof sections;
        return Promise.resolve([{ id: key, versionNumber: 1, status: "PUBLISHED", contentData: sections[key], createdAt: "2026-01-01T00:00:00Z" }]);
      }
      const key = path.replace("/api/v1/admin/content/", "") as keyof typeof sections;
      if (key in sections) return Promise.resolve({ section: { id: key, sectionKey: key, title: key, status: "PUBLISHED", publishedVersion: 1, updatedAt: "2026-01-01T00:00:00Z" }, published: { id: key, versionNumber: 1, status: "PUBLISHED", contentData: sections[key], createdAt: "2026-01-01T00:00:00Z" } });
      return Promise.reject(new Error(`Unexpected API call: ${path}`));
    });
  });

  it("renders the complete home with inline editing and a clean preview", async () => {
    render(<HomeContentStudio />);

    expect(await screen.findByRole("heading", { name: "Clique em qualquer texto para editar" })).toBeVisible();
    expect(screen.getByRole("heading", { name: "Soluções digitais inteligentes" })).toBeVisible();
    const titleEditor = screen.getByRole("textbox", { name: "Editar título principal" });
    expect(titleEditor).toBeVisible();
    expect(titleEditor).toHaveAttribute("contenteditable", "plaintext-only");
    expect(screen.getByRole("textbox", { name: "Editar pergunta 1" })).toBeVisible();

    titleEditor.innerText = "Uma nova mensagem principal";
    fireEvent.blur(titleEditor);
    expect(await screen.findByRole("heading", { name: "Uma nova mensagem principal" })).toBeVisible();
    expect(screen.getByText("1 alterações não salvas")).toBeVisible();

    fireEvent.click(screen.getByRole("button", { name: "Visualizar sem marcações" }));

    expect(await screen.findByRole("heading", { name: "Visualização limpa da página" })).toBeVisible();
    expect(screen.getByRole("heading", { name: "Uma nova mensagem principal" })).toBeVisible();
    expect(screen.queryByRole("textbox", { name: "Editar título principal" })).not.toBeInTheDocument();
    fireEvent.click(screen.getByRole("button", { name: "Continuar editando" }));
    await waitFor(() => expect(screen.getByRole("textbox", { name: "Editar título principal" })).toBeVisible());

    vi.spyOn(window, "confirm").mockReturnValueOnce(true);
    fireEvent.click(screen.getByRole("button", { name: "Restaurar conteúdo padrão" }));
    expect(await screen.findByRole("heading", { name: "Soluções digitais inteligentes" })).toBeVisible();
    expect(screen.getByText("Conteúdo padrão restaurado no editor. Revise e salve como rascunho para manter as alterações.")).toBeVisible();
  });
});
