export type PageResponse<T> = { items: T[]; page: number; size: number; totalItems: number; totalPages: number };
export type Audit = { id: string; actorEmail?: string; action: string; resourceType: string; resourceId?: string; previousData?: Record<string, unknown>; newData?: Record<string, unknown>; ipAddress?: string; userAgent?: string; success: boolean; createdAt: string };
export type Lead = { id: string; name: string; company?: string; email: string; phone: string; serviceInterest: string; budgetRange: string; message: string; status: string; internalNotes?: string; privacyConsent: boolean; consentDate: string; source: string; notificationStatus: string; confirmationStatus: string; createdAt: string; updatedAt: string };
export type SectionSummary = { id: string; sectionKey: string; title: string; subtitle?: string; status: string; publishedVersion?: number; draftVersion?: number; updatedAt: string };
export type Version = { id: string; versionNumber: number; status: string; contentData: Record<string, unknown>; createdBy?: string; createdAt: string; publishedAt?: string };
export type SectionDetail = { section: SectionSummary; published?: Version; draft?: Version };
export type Dashboard = { totalLeads: number; leadsThisMonth: number; unreadLeads: number; leadsByService: Record<string, number>; recentLeads: Array<Pick<Lead, "id"|"name"|"company"|"serviceInterest"|"status"|"createdAt">>; recentContent: Array<{ id: string; sectionKey: string; version: number; status: string; actorEmail: string; createdAt: string }>; recentAudits: Audit[] };

export const labels: Record<string, string> = {
  NEW: "Novo", VIEWED: "Visualizado", IN_PROGRESS: "Em atendimento", PROPOSAL_SENT: "Proposta enviada", CONVERTED: "Convertido", ARCHIVED: "Arquivado",
  LANDING_PAGE: "Landing page", INSTITUTIONAL_SITE: "Site institucional", CRM: "CRM", WHATSAPP_CHATBOT: "Chatbot WhatsApp", CUSTOM_SYSTEM: "Sistema personalizado", UNDECIDED: "A definir",
  UP_TO_2000: "Até R$ 2.000", FROM_2000_TO_5000: "R$ 2.000 a R$ 5.000", FROM_5000_TO_10000: "R$ 5.000 a R$ 10.000", ABOVE_10000: "Acima de R$ 10.000", DISCUSS_FIRST: "Conversar antes",
};
export const formatDate = (value: string) => new Intl.DateTimeFormat("pt-BR", { dateStyle: "short", timeStyle: "short" }).format(new Date(value));
