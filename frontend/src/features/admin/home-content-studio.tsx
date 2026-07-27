"use client";

import { useEffect, useMemo, useState } from "react";
import { LandingPage, type LandingPageEditor } from "@/features/site/landing-page";
import { siteContentResponseSchema, type PublicFaq, type SiteSections } from "@/features/site/content-schema";
import { ApiError, apiRequest, initializeCsrf } from "@/lib/api/client";
import type { SectionDetail, SectionSummary, Version } from "./types";

type JsonObject = Record<string, unknown>;
type Path = Array<string | number>;
type FaqAdmin = {
  id: string;
  question: string;
  answer: string;
  draftQuestion?: string;
  draftAnswer?: string;
  displayOrder: number;
  active: boolean;
  status: string;
};
type FaqEditing = FaqAdmin & { editingQuestion: string; editingAnswer: string };

const defaultFaqs: Record<string, { question: string; answer: string }> = {
  "30000000-0000-4000-8000-000000000001": {
    question: "Quanto custa desenvolver um site?",
    answer: "O valor depende do objetivo, da quantidade de páginas, das integrações e do nível de personalização. Depois de entender sua necessidade, apresentamos um escopo claro com investimento e prazo.",
  },
  "30000000-0000-4000-8000-000000000002": {
    question: "Quanto tempo demora para o projeto ficar pronto?",
    answer: "Uma landing page costuma exigir menos tempo que um site institucional ou sistema personalizado. O cronograma é definido após o levantamento do escopo e inclui etapas de validação com o cliente.",
  },
  "30000000-0000-4000-8000-000000000003": {
    question: "A Kodo Code oferece manutenção?",
    answer: "Sim. Podemos combinar suporte, correções e evolução contínua após a publicação, de acordo com a necessidade do projeto.",
  },
  "30000000-0000-4000-8000-000000000004": {
    question: "O cliente poderá solicitar alterações?",
    answer: "Sim. As etapas de design e desenvolvimento incluem validações. Mudanças dentro do escopo são organizadas durante o projeto; novas necessidades podem ser planejadas separadamente.",
  },
  "30000000-0000-4000-8000-000000000005": {
    question: "O site funciona em celulares?",
    answer: "Sim. As interfaces são desenvolvidas de forma responsiva e testadas em diferentes tamanhos de tela para oferecer uma experiência consistente.",
  },
  "30000000-0000-4000-8000-000000000006": {
    question: "A Kodo Code também desenvolve sistemas personalizados?",
    answer: "Sim. Desenvolvemos sistemas alinhados às regras e aos processos específicos da empresa, incluindo painéis, fluxos internos e integrações.",
  },
  "30000000-0000-4000-8000-000000000007": {
    question: "Como funciona a automação para WhatsApp?",
    answer: "Mapeamos as perguntas e etapas do atendimento para automatizar respostas, coletar informações e encaminhar cada contato de forma mais organizada.",
  },
  "30000000-0000-4000-8000-000000000008": {
    question: "Como o orçamento é calculado?",
    answer: "Consideramos escopo, regras de negócio, design, integrações, prazo e suporte esperado. Assim, a proposta reflete o trabalho necessário e evita custos pouco claros.",
  },
};

function clone<T>(value: T): T {
  return structuredClone(value);
}

function same(left: unknown, right: unknown) {
  return JSON.stringify(left) === JSON.stringify(right);
}

function updateAtPath(node: JsonObject, path: Path, value: string) {
  const copy = clone(node);
  let current: unknown = copy;
  for (let index = 0; index < path.length - 1; index += 1) {
    current = (current as Record<string | number, unknown>)[path[index]];
  }
  (current as Record<string | number, unknown>)[path.at(-1)!] = value;
  return copy;
}

export function HomeContentStudio() {
  const [details, setDetails] = useState<SectionDetail[]>([]);
  const [content, setContent] = useState<Record<string, JsonObject>>({});
  const [baseline, setBaseline] = useState<Record<string, JsonObject>>({});
  const [faqs, setFaqs] = useState<FaqEditing[]>([]);
  const [faqBaseline, setFaqBaseline] = useState<Record<string, { question: string; answer: string }>>({});
  const [draftKeys, setDraftKeys] = useState<Set<string>>(new Set());
  const [faqDraftIds, setFaqDraftIds] = useState<Set<string>>(new Set());
  const [mode, setMode] = useState<"edit" | "preview">("edit");
  const [previewedFingerprint, setPreviewedFingerprint] = useState<string>();
  const [busy, setBusy] = useState<"loading" | "idle" | "restoring" | "saving" | "publishing">("loading");
  const [feedback, setFeedback] = useState<{ kind: "success" | "error"; text: string }>();

  async function load() {
    try {
      const [summaries, faqItems] = await Promise.all([
        apiRequest<SectionSummary[]>("/api/v1/admin/content"),
        apiRequest<FaqAdmin[]>("/api/v1/admin/faqs"),
      ]);
      const loadedDetails = await Promise.all(summaries.map((item) => apiRequest<SectionDetail>(`/api/v1/admin/content/${item.sectionKey}`)));
      const loadedContent = Object.fromEntries(loadedDetails.map((detail) => [
        detail.section.sectionKey,
        clone(detail.draft?.contentData ?? detail.published?.contentData ?? {}),
      ]));
      const editingFaqs = faqItems.map((faq) => ({
        ...faq,
        editingQuestion: faq.draftQuestion ?? faq.question,
        editingAnswer: faq.draftAnswer ?? faq.answer,
      }));
      setDetails(loadedDetails);
      setContent(loadedContent);
      setBaseline(clone(loadedContent));
      setFaqs(editingFaqs);
      setFaqBaseline(Object.fromEntries(editingFaqs.map((faq) => [faq.id, { question: faq.editingQuestion, answer: faq.editingAnswer }])));
      setDraftKeys(new Set(loadedDetails.filter((detail) => detail.draft).map((detail) => detail.section.sectionKey)));
      setFaqDraftIds(new Set(editingFaqs.filter((faq) => faq.draftQuestion != null && faq.draftAnswer != null).map((faq) => faq.id)));
      setFeedback(undefined);
    } catch (error) {
      setFeedback({ kind: "error", text: error instanceof ApiError ? error.message : "Não foi possível carregar o editor." });
    } finally {
      setBusy("idle");
    }
  }

  useEffect(() => {
    const initialLoad = window.setTimeout(() => { void load(); }, 0);
    return () => window.clearTimeout(initialLoad);
  }, []);

  const dirtySectionKeys = useMemo(() => Object.keys(content).filter((key) => !same(content[key], baseline[key])), [content, baseline]);
  const dirtyFaqIds = useMemo(() => faqs.filter((faq) => {
    const saved = faqBaseline[faq.id];
    return !saved || faq.editingQuestion !== saved.question || faq.editingAnswer !== saved.answer;
  }).map((faq) => faq.id), [faqs, faqBaseline]);
  const dirtyCount = dirtySectionKeys.length + dirtyFaqIds.length;
  const fingerprint = useMemo(() => JSON.stringify({ content, faqs: faqs.map((faq) => [faq.id, faq.editingQuestion, faq.editingAnswer]) }), [content, faqs]);
  const parsedPage = useMemo(() => siteContentResponseSchema.safeParse({ sections: content, publishedAt: null }), [content]);
  const previewFaqs = useMemo<PublicFaq[]>(() => faqs.filter((faq) => faq.active).map((faq) => ({
    id: faq.id,
    question: faq.editingQuestion,
    answer: faq.editingAnswer,
    displayOrder: faq.displayOrder,
  })), [faqs]);
  const hasDrafts = draftKeys.size > 0 || faqDraftIds.size > 0;
  const wasPreviewed = previewedFingerprint === fingerprint;
  const isWorking = busy === "restoring" || busy === "saving" || busy === "publishing";

  const editor = useMemo<LandingPageEditor>(() => ({
    onSectionTextChange(section, path, value) {
      setContent((current) => ({
        ...current,
        [section]: updateAtPath(current[section], path, value),
      }));
      setFeedback(undefined);
    },
    onFaqTextChange(id, field, value) {
      setFaqs((current) => current.map((faq) => faq.id === id
        ? { ...faq, [field === "question" ? "editingQuestion" : "editingAnswer"]: value }
        : faq));
      setFeedback(undefined);
    },
  }), []);

  async function saveDrafts() {
    if (!parsedPage.success) {
      setFeedback({ kind: "error", text: "Há um texto obrigatório vazio ou um e-mail inválido. Corrija o campo marcado antes de salvar." });
      return;
    }
    if (dirtyCount === 0) {
      setFeedback({ kind: "success", text: "Todas as alterações já estão salvas como rascunho." });
      return;
    }
    setBusy("saving");
    setFeedback(undefined);
    try {
      await initializeCsrf();
      const sectionResults = await Promise.all(dirtySectionKeys.map(async (key) => ({
        key,
        detail: await apiRequest<SectionDetail>(`/api/v1/admin/content/${key}`, {
          method: "PUT",
          body: JSON.stringify({ contentData: content[key] }),
        }),
      })));
      const faqResults = await Promise.all(faqs.filter((faq) => dirtyFaqIds.includes(faq.id)).map((faq) => apiRequest<FaqAdmin>(`/api/v1/admin/faqs/${faq.id}`, {
        method: "PUT",
        body: JSON.stringify({ question: faq.editingQuestion, answer: faq.editingAnswer, displayOrder: faq.displayOrder, active: faq.active }),
      })));

      setDetails((current) => current.map((detail) => sectionResults.find((result) => result.key === detail.section.sectionKey)?.detail ?? detail));
      setDraftKeys((current) => new Set([...current, ...sectionResults.map((result) => result.key)]));
      setFaqDraftIds((current) => new Set([...current, ...faqResults.map((faq) => faq.id)]));
      setBaseline(clone(content));
      setFaqBaseline(Object.fromEntries(faqs.map((faq) => [faq.id, { question: faq.editingQuestion, answer: faq.editingAnswer }])));
      setFeedback({ kind: "success", text: `${dirtyCount} ${dirtyCount === 1 ? "alteração salva" : "alterações salvas"} no rascunho.` });
    } catch (error) {
      setFeedback({ kind: "error", text: error instanceof ApiError ? error.message : "Não foi possível salvar o rascunho." });
    } finally {
      setBusy("idle");
    }
  }

  function togglePreview() {
    if (mode === "preview") {
      setMode("edit");
      return;
    }
    if (!parsedPage.success) {
      setFeedback({ kind: "error", text: "Revise os textos obrigatórios antes de abrir a visualização limpa." });
      return;
    }
    setPreviewedFingerprint(fingerprint);
    setMode("preview");
  }

  async function restoreDefaults() {
    if (!window.confirm("Restaurar todos os textos padrão da landing page? Suas alterações atuais serão substituídas, mas nada será publicado automaticamente.")) return;
    setBusy("restoring");
    setFeedback(undefined);
    try {
      const defaults = await Promise.all(details.map(async (detail) => {
        const versions = await apiRequest<Version[]>(`/api/v1/admin/content/${detail.section.sectionKey}/versions`);
        const original = versions.reduce<Version | undefined>((oldest, version) => !oldest || version.versionNumber < oldest.versionNumber ? version : oldest, undefined);
        if (!original) throw new Error(`Conteúdo padrão indisponível para ${detail.section.sectionKey}.`);
        return [detail.section.sectionKey, clone(original.contentData)] as const;
      }));
      setContent(Object.fromEntries(defaults));
      setFaqs((current) => current.map((faq) => {
        const standard = defaultFaqs[faq.id];
        return standard ? { ...faq, editingQuestion: standard.question, editingAnswer: standard.answer } : faq;
      }));
      setMode("edit");
      setPreviewedFingerprint(undefined);
      setFeedback({ kind: "success", text: "Conteúdo padrão restaurado no editor. Revise e salve como rascunho para manter as alterações." });
    } catch (error) {
      setFeedback({ kind: "error", text: error instanceof ApiError ? error.message : "Não foi possível recuperar o conteúdo padrão." });
    } finally {
      setBusy("idle");
    }
  }

  async function publishAll() {
    if (dirtyCount > 0 || !wasPreviewed || !hasDrafts) return;
    if (!window.confirm("Publicar todos os rascunhos revisados na página inicial?")) return;
    setBusy("publishing");
    setFeedback(undefined);
    try {
      await initializeCsrf();
      for (const key of draftKeys) await apiRequest(`/api/v1/admin/content/${key}/publish`, { method: "POST" });
      for (const id of faqDraftIds) await apiRequest(`/api/v1/admin/faqs/${id}/publish`, { method: "POST" });
      setMode("edit");
      setPreviewedFingerprint(undefined);
      await load();
      setFeedback({ kind: "success", text: "Página inicial publicada com sucesso." });
    } catch (error) {
      setFeedback({ kind: "error", text: error instanceof ApiError ? error.message : "A publicação não foi concluída." });
    } finally {
      setBusy("idle");
    }
  }

  if (details.length === 0) {
    return <div className="admin-card" role="status">{feedback?.text ?? "Preparando a página completa para edição..."}</div>;
  }

  return (
    <div className="visual-home-studio">
      <header className="visual-editor-toolbar">
        <div className="min-w-0">
          <p className="text-xs font-bold uppercase tracking-[.17em] text-violet-700">Editor visual da página inicial</p>
          <h1 className="mt-1 text-xl font-bold tracking-tight text-[var(--ink)] sm:text-2xl">
            {mode === "edit" ? "Clique em qualquer texto para editar" : "Visualização limpa da página"}
          </h1>
          <p className="mt-1 text-xs leading-5 text-slate-500 sm:text-sm">
            {mode === "edit" ? "Os contornos tracejados indicam os textos editáveis. Pressione Esc para cancelar uma edição." : "Assim seus clientes verão a home. Volte ao editor para fazer ajustes."}
          </p>
        </div>
        <div className="flex shrink-0 flex-wrap gap-2">
          <button className="admin-button admin-button-danger" disabled={isWorking} onClick={() => void restoreDefaults()} type="button">
            {busy === "restoring" ? "Restaurando..." : "Restaurar conteúdo padrão"}
          </button>
          <button className="admin-button admin-button-secondary" disabled={isWorking || !parsedPage.success} onClick={togglePreview} type="button">
            {mode === "preview" ? "Continuar editando" : "Visualizar sem marcações"}
          </button>
          <button className="admin-button admin-button-secondary" disabled={isWorking || dirtyCount === 0} onClick={() => void saveDrafts()} type="button">
            {busy === "saving" ? "Salvando..." : "Salvar rascunho"}
          </button>
          <button className="admin-button" disabled={isWorking || dirtyCount > 0 || !wasPreviewed || !hasDrafts} onClick={() => void publishAll()} type="button">
            {busy === "publishing" ? "Publicando..." : "Publicar página"}
          </button>
        </div>
        <div className="visual-editor-status">
          <span className={`inline-flex items-center gap-2 font-semibold ${dirtyCount > 0 ? "text-amber-700" : "text-emerald-700"}`}>
            <span className={`size-2 rounded-full ${dirtyCount > 0 ? "bg-amber-500" : "bg-emerald-500"}`} />
            {dirtyCount > 0 ? `${dirtyCount} alterações não salvas` : "Rascunho salvo"}
          </span>
          <span className={wasPreviewed ? "font-semibold text-blue-700" : "text-slate-500"}>{wasPreviewed ? "Visual revisado" : "Revise o visual antes de publicar"}</span>
          <span className="text-slate-500">{draftKeys.size + faqDraftIds.size} áreas aguardando publicação</span>
        </div>
      </header>

      {feedback && <p className={`mx-3 mb-3 rounded-xl border p-4 text-sm sm:mx-5 ${feedback.kind === "success" ? "border-emerald-200 bg-emerald-50 text-emerald-800" : "border-red-200 bg-red-50 text-red-800"}`} role="status">{feedback.text}</p>}

      <div
        className="visual-editor-canvas"
        onClickCapture={(event) => {
          if (mode === "preview") event.preventDefault();
        }}
        onSubmitCapture={(event) => event.preventDefault()}
      >
        <LandingPage
          editor={mode === "edit" ? editor : undefined}
          faqs={previewFaqs}
          preview={mode === "preview"}
          sections={content as unknown as SiteSections}
        />
      </div>
    </div>
  );
}
