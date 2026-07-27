import { z } from "zod";

export const CONTACT_LIMITS = {
  name: 120,
  company: 160,
  email: 254,
  phone: 15,
  message: 2000,
} as const;

const unsafeContent = /<\s*\/?\s*[a-z][^>]*>|(?:javascript|data)\s*:|```|<\?|<%|\$\{|=>|(?:^|\s)(?:function|class|import|export|const|let|var)\s+[a-z_$]|\b(?:select\s+.+\s+from|insert\s+into|delete\s+from|drop\s+table|union\s+select)\b/i;

const safeText = (maximum: number) => z.string().trim().max(maximum, `Use no máximo ${maximum} caracteres.`)
  .refine((value) => !unsafeContent.test(value), "Não insira código, HTML ou scripts.");

export const contactSchema = z.object({
  name: safeText(CONTACT_LIMITS.name).min(2, "Informe seu nome."),
  company: safeText(CONTACT_LIMITS.company).optional(),
  email: z.string().trim().max(CONTACT_LIMITS.email, "E-mail muito longo.").pipe(z.email("Informe um e-mail válido.")),
  phone: z.string().trim().max(CONTACT_LIMITS.phone).regex(/^\(\d{2}\) (?:\d{4}-\d{4}|\d{5}-\d{4})$/, "Use um telefone com DDD, por exemplo: (11) 99999-9999."),
  serviceInterest: z.enum(["LANDING_PAGE", "INSTITUTIONAL_SITE", "CRM", "WHATSAPP_CHATBOT", "CUSTOM_SYSTEM", "UNDECIDED"], { message: "Selecione um serviço." }),
  budgetRange: z.enum(["UP_TO_2000", "FROM_2000_TO_5000", "FROM_5000_TO_10000", "ABOVE_10000", "DISCUSS_FIRST"], { message: "Selecione uma faixa de orçamento." }),
  message: safeText(CONTACT_LIMITS.message).min(20, "Conte um pouco mais sobre o projeto (mínimo de 20 caracteres)."),
  privacyConsent: z.boolean().refine(Boolean, "Aceite a política de privacidade para continuar."),
  website: z.string().max(0).optional(),
});

export type ContactInput = z.infer<typeof contactSchema>;

export function formatBrazilianPhone(value: string) {
  const digits = value.replace(/\D/g, "").slice(0, 11);
  if (!digits) return "";
  if (digits.length <= 2) return `(${digits}`;

  const areaCode = digits.slice(0, 2);
  const number = digits.slice(2);
  const prefixLength = digits.length === 11 ? 5 : 4;
  if (number.length <= prefixLength) return `(${areaCode}) ${number}`;
  return `(${areaCode}) ${number.slice(0, prefixLength)}-${number.slice(prefixLength)}`;
}
