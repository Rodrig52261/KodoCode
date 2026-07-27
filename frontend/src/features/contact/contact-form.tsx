"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { type ChangeEvent, useState } from "react";
import { useForm, useWatch } from "react-hook-form";
import { ApiError, apiRequest, initializeCsrf } from "@/lib/api/client";
import { CONTACT_LIMITS, contactSchema, formatBrazilianPhone, type ContactInput } from "./schema";

const services = [
  ["LANDING_PAGE", "Landing page"], ["INSTITUTIONAL_SITE", "Site institucional"], ["CRM", "CRM"],
  ["WHATSAPP_CHATBOT", "Chatbot para WhatsApp"], ["CUSTOM_SYSTEM", "Sistema personalizado"], ["UNDECIDED", "Ainda não sei qual solução preciso"],
] as const;
const budgets = [
  ["UP_TO_2000", "Até R$ 2.000"], ["FROM_2000_TO_5000", "De R$ 2.000 a R$ 5.000"],
  ["FROM_5000_TO_10000", "De R$ 5.000 a R$ 10.000"], ["ABOVE_10000", "Acima de R$ 10.000"], ["DISCUSS_FIRST", "Quero conversar antes de definir"],
] as const;

export function ContactForm() {
  const [startedAt, setStartedAt] = useState(() => new Date().toISOString());
  const [feedback, setFeedback] = useState<{ kind: "success" | "error"; text: string }>();
  const { register, handleSubmit, reset, control, formState: { errors, isSubmitting } } = useForm<ContactInput>({
    resolver: zodResolver(contactSchema), defaultValues: { privacyConsent: false, website: "" },
  });
  const messageLength = useWatch({ control, name: "message" })?.length ?? 0;
  const phoneRegistration = register("phone");

  async function submit(values: ContactInput) {
    setFeedback(undefined);
    try {
      await initializeCsrf();
      const result = await apiRequest<{ message: string }>("/api/v1/public/contact", {
        method: "POST", body: JSON.stringify({ ...values, company: values.company || null, formStartedAt: startedAt }),
      });
      setFeedback({ kind: "success", text: result.message });
      reset();
      setStartedAt(new Date().toISOString());
    } catch (error) {
      setFeedback({ kind: "error", text: error instanceof ApiError ? error.message : "Não foi possível enviar. Tente novamente." });
    }
  }

  const field = (name: "name" | "company" | "email", label: string, type = "text", autoComplete?: string) => (
    <div>
      <label className="form-label" htmlFor={`contact-${name}`}>{label}</label>
      <input className="form-input" id={`contact-${name}`} type={type} autoComplete={autoComplete} maxLength={CONTACT_LIMITS[name]} spellCheck={name !== "email"} autoCapitalize={name === "email" ? "none" : "words"} aria-invalid={Boolean(errors[name])} aria-describedby={errors[name] ? `${name}-error` : undefined} {...register(name)} />
      {errors[name] && <p className="form-error" id={`${name}-error`}>{errors[name]?.message}</p>}
    </div>
  );

  function handlePhoneChange(event: ChangeEvent<HTMLInputElement>) {
    event.target.value = formatBrazilianPhone(event.target.value);
    void phoneRegistration.onChange(event);
  }

  return (
    <form className="contact-form rounded-3xl border border-[var(--border)] bg-white p-6 sm:p-8" noValidate onSubmit={handleSubmit(submit)}>
      <div className="grid gap-5 sm:grid-cols-2">
        {field("name", "Nome *", "text", "name")}
        {field("company", "Empresa", "text", "organization")}
        {field("email", "E-mail *", "email", "email")}
        <div>
          <label className="form-label" htmlFor="contact-phone">Telefone ou WhatsApp *</label>
          <input className="form-input" id="contact-phone" type="tel" inputMode="tel" autoComplete="tel-national" maxLength={CONTACT_LIMITS.phone} placeholder="(11) 99999-9999" aria-invalid={Boolean(errors.phone)} aria-describedby={errors.phone ? "phone-error" : undefined} {...phoneRegistration} onChange={handlePhoneChange} />
          {errors.phone && <p className="form-error" id="phone-error">{errors.phone.message}</p>}
        </div>
        <div>
          <label className="form-label" htmlFor="contact-service">Serviço de interesse *</label>
          <select className="form-input" id="contact-service" aria-invalid={Boolean(errors.serviceInterest)} {...register("serviceInterest")}>
            <option value="">Selecione</option>{services.map(([value, label]) => <option key={value} value={value}>{label}</option>)}
          </select>
          {errors.serviceInterest && <p className="form-error">{errors.serviceInterest.message}</p>}
        </div>
        <div>
          <label className="form-label" htmlFor="contact-budget">Faixa de orçamento *</label>
          <select className="form-input" id="contact-budget" aria-invalid={Boolean(errors.budgetRange)} {...register("budgetRange")}>
            <option value="">Selecione</option>{budgets.map(([value, label]) => <option key={value} value={value}>{label}</option>)}
          </select>
          {errors.budgetRange && <p className="form-error">{errors.budgetRange.message}</p>}
        </div>
      </div>
      <div className="mt-5">
        <div className="flex justify-between gap-4"><label className="form-label" htmlFor="contact-message">Mensagem *</label><span className="text-xs text-slate-500">{messageLength}/{CONTACT_LIMITS.message}</span></div>
        <textarea className="form-input min-h-36 resize-y" id="contact-message" maxLength={CONTACT_LIMITS.message} aria-invalid={Boolean(errors.message)} aria-describedby={errors.message ? "message-error" : undefined} {...register("message")} />
        {errors.message && <p className="form-error" id="message-error">{errors.message.message}</p>}
      </div>
      <div className="absolute -left-[10000px]" aria-hidden="true"><label htmlFor="contact-website">Website</label><input id="contact-website" tabIndex={-1} autoComplete="off" {...register("website")} /></div>
      <div className="mt-5 flex items-start gap-3">
        <input className="mt-1 size-4 accent-[var(--primary)]" id="contact-consent" type="checkbox" aria-invalid={Boolean(errors.privacyConsent)} {...register("privacyConsent")} />
        <label className="text-sm leading-6 text-slate-600" htmlFor="contact-consent">Li e aceito a <a className="font-semibold text-[var(--primary-dark)] underline" href="/politica-de-privacidade" rel="noopener noreferrer" target="_blank">política de privacidade</a>. *</label>
      </div>
      {errors.privacyConsent && <p className="form-error">{errors.privacyConsent.message}</p>}
      {feedback && <p className={`mt-5 rounded-xl p-4 text-sm ${feedback.kind === "success" ? "bg-emerald-50 text-emerald-800" : "bg-red-50 text-[var(--danger)]"}`} role="status">{feedback.text}</p>}
      <button className="button-primary mt-6 w-full disabled:cursor-not-allowed disabled:opacity-60" disabled={isSubmitting} type="submit">{isSubmitting ? "Enviando..." : "Solicitar orçamento"}</button>
      <p className="mt-3 text-center text-xs text-slate-500">Seus dados serão usados somente para responder à sua solicitação.</p>
    </form>
  );
}
