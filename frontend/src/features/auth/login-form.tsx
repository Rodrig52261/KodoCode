"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { ApiError, apiRequest, initializeCsrf } from "@/lib/api/client";
import { type LoginInput, loginSchema } from "./schemas";

export function LoginForm() {
  const router = useRouter();
  const [formError, setFormError] = useState<string>();
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginInput>({ resolver: zodResolver(loginSchema) });

  async function onSubmit(values: LoginInput) {
    setFormError(undefined);
    try {
      await initializeCsrf();
      await apiRequest("/api/v1/auth/login", {
        method: "POST",
        body: JSON.stringify(values),
      });
      router.replace("/admin/dashboard");
      router.refresh();
    } catch (error) {
      setFormError(error instanceof ApiError ? error.message : "Falha inesperada ao entrar.");
    }
  }

  return (
    <form className="mt-8 space-y-5" noValidate onSubmit={handleSubmit(onSubmit)}>
      <div>
        <label className="mb-2 block text-sm font-medium" htmlFor="email">E-mail</label>
        <input
          className="w-full rounded-xl border border-[var(--border)] px-4 py-3"
          id="email"
          type="email"
          autoComplete="username"
          aria-invalid={Boolean(errors.email)}
          aria-describedby={errors.email ? "email-error" : undefined}
          {...register("email")}
        />
        {errors.email && <p className="mt-2 text-sm text-[var(--danger)]" id="email-error">{errors.email.message}</p>}
      </div>

      <div>
        <label className="mb-2 block text-sm font-medium" htmlFor="password">Senha</label>
        <input
          className="w-full rounded-xl border border-[var(--border)] px-4 py-3"
          id="password"
          type="password"
          autoComplete="current-password"
          aria-invalid={Boolean(errors.password)}
          aria-describedby={errors.password ? "password-error" : undefined}
          {...register("password")}
        />
        {errors.password && <p className="mt-2 text-sm text-[var(--danger)]" id="password-error">{errors.password.message}</p>}
      </div>

      {formError && <p className="rounded-xl bg-red-50 p-3 text-sm text-[var(--danger)]" role="alert">{formError}</p>}

      <button
        className="button-primary w-full px-5 py-3 disabled:cursor-not-allowed disabled:opacity-60"
        disabled={isSubmitting}
        type="submit"
      >
        {isSubmitting ? "Entrando..." : "Entrar"}
      </button>
    </form>
  );
}
