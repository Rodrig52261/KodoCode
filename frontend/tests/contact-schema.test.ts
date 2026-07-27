import { describe, expect, it } from "vitest";
import { CONTACT_LIMITS, contactSchema, formatBrazilianPhone } from "@/features/contact/schema";

const valid = { name: "Ana Souza", company: "ACME", email: "ana@example.com", phone: "(11) 99999-9999", serviceInterest: "CRM", budgetRange: "DISCUSS_FIRST", message: "Quero organizar o processo comercial da empresa.", privacyConsent: true, website: "" };

describe("contactSchema", () => {
  it("accepts a complete contact", () => expect(contactSchema.safeParse(valid).success).toBe(true));
  it("rejects spam honeypot and missing consent", () => expect(contactSchema.safeParse({ ...valid, website: "bot", privacyConsent: false }).success).toBe(false));
  it("rejects short messages and invalid phone", () => expect(contactSchema.safeParse({ ...valid, message: "Olá", phone: "123" }).success).toBe(false));
  it("rejects HTML, scripts and common code fragments", () => {
    expect(contactSchema.safeParse({ ...valid, name: "<script>alert(1)</script>" }).success).toBe(false);
    expect(contactSchema.safeParse({ ...valid, message: "const token = document.cookie; esta mensagem completa o tamanho mínimo." }).success).toBe(false);
    expect(contactSchema.safeParse({ ...valid, message: "SELECT senha FROM usuarios; esta mensagem completa o tamanho mínimo." }).success).toBe(false);
  });
  it("enforces the configured text limits", () => {
    expect(contactSchema.safeParse({ ...valid, name: "A".repeat(CONTACT_LIMITS.name + 1) }).success).toBe(false);
    expect(contactSchema.safeParse({ ...valid, message: "A".repeat(CONTACT_LIMITS.message + 1) }).success).toBe(false);
  });
  it("formats Brazilian landline and mobile numbers", () => {
    expect(formatBrazilianPhone("1133334444")).toBe("(11) 3333-4444");
    expect(formatBrazilianPhone("11999998888")).toBe("(11) 99999-8888");
    expect(formatBrazilianPhone("11999998888999")).toBe("(11) 99999-8888");
  });
});
