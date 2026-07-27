import { LoginForm } from "@/features/auth/login-form";

export default function LoginPage() {
  return (
    <main className="grid min-h-screen place-items-center bg-[radial-gradient(circle_at_15%_10%,rgb(56_189_248_/_12%),transparent_28rem),radial-gradient(circle_at_90%_85%,rgb(139_92_246_/_12%),transparent_28rem)] px-6 py-12" id="conteudo-principal">
      <section className="w-full max-w-md rounded-2xl border border-[var(--border)] bg-white p-8 shadow-[0_1.5rem_4rem_rgb(30_64_175_/_10%)]" aria-labelledby="login-title">
        <p className="text-sm font-semibold uppercase tracking-[0.16em] text-violet-700">Kodo Code</p>
        <h1 className="mt-2 text-3xl font-semibold" id="login-title">Painel administrativo</h1>
        <p className="mt-2 text-slate-600">Acesso exclusivo para administradores autorizados.</p>
        <LoginForm />
      </section>
    </main>
  );
}
