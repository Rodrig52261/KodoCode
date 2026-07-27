import { describe, expect, it } from "vitest";
import { loginSchema } from "@/features/auth/schemas";

describe("loginSchema", () => {
  it("aceita credenciais com e-mail valido", () => {
    expect(loginSchema.safeParse({ email: "admin@kodocode.com.br", password: "senha" }).success).toBe(true);
  });

  it("rejeita e-mail invalido e senha vazia", () => {
    expect(loginSchema.safeParse({ email: "invalido", password: "" }).success).toBe(false);
  });
});

