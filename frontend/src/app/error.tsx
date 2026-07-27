"use client";

export default function GlobalError({ reset }: Readonly<{ error: Error & { digest?: string }; reset: () => void }>) {
  return (
    <main className="grid min-h-screen place-items-center px-6 py-20">
      <div className="max-w-lg text-center">
        <p className="eyebrow">Algo não saiu como esperado</p>
        <h1 className="mt-4 text-4xl font-bold tracking-tight text-[var(--ink)]">Não foi possível abrir esta página.</h1>
        <p className="mt-5 leading-7 text-slate-600">Tente carregar novamente. Se o problema continuar, volte em alguns instantes.</p>
        <button className="button-primary mt-7" onClick={reset} type="button">Tentar novamente</button>
      </div>
    </main>
  );
}
