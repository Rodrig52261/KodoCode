import type { ReactNode } from "react";
import Link from "next/link";
import { Brand } from "./brand";

export function LegalPage({ title, introduction, children }: Readonly<{ title: string; introduction: string; children: ReactNode }>) {
  return (
    <>
      <header className="border-b border-slate-200 bg-white">
        <div className="site-container flex h-18 items-center justify-between">
          <Brand />
          <Link className="text-sm font-bold text-[var(--primary-dark)] hover:text-[var(--primary)]" href="/">Voltar ao site</Link>
        </div>
      </header>
      <main className="bg-[var(--warm)]" id="conteudo-principal">
        <article className="site-container max-w-4xl py-16 sm:py-24">
          <p className="eyebrow">Informações legais</p>
          <h1 className="mt-4 text-4xl font-bold tracking-[-0.04em] text-[var(--ink)] sm:text-5xl">{title}</h1>
          <p className="mt-6 max-w-3xl text-lg leading-8 text-slate-600">{introduction}</p>
          <p className="mt-3 text-sm text-slate-500">Última atualização: julho de 2026.</p>
          <div className="mt-12 space-y-9 rounded-3xl border border-slate-200 bg-white p-6 leading-7 text-slate-600 shadow-sm sm:p-10 [&_h2]:text-xl [&_h2]:font-bold [&_h2]:text-[var(--ink)] [&_p]:mt-3 [&_ul]:mt-3 [&_ul]:list-disc [&_ul]:space-y-2 [&_ul]:pl-5">
            {children}
          </div>
        </article>
      </main>
    </>
  );
}
