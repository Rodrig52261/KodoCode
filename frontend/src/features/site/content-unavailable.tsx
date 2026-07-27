import Link from "next/link";

export function ContentUnavailable() {
  return (
    <main className="grid min-h-[70vh] place-items-center px-6 py-20">
      <div className="max-w-lg rounded-3xl border border-slate-200 bg-white p-8 text-center shadow-sm">
        <p className="eyebrow">Indisponibilidade temporária</p>
        <h1 className="mt-3 text-3xl font-bold tracking-tight text-[var(--ink)]">Não foi possível carregar o conteúdo.</h1>
        <p className="mt-4 leading-7 text-slate-600">A página está segura, mas a API não respondeu como esperado. Tente novamente em alguns instantes.</p>
        <Link className="button-primary mt-7 inline-flex" href="/">Tentar novamente</Link>
      </div>
    </main>
  );
}
