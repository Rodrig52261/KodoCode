import Link from "next/link";
import { Brand } from "@/features/site/brand";

export default function NotFound() {
  return (
    <main className="grid min-h-screen place-items-center bg-[var(--warm)] px-6 py-20">
      <div className="max-w-xl text-center">
        <div className="flex justify-center"><Brand /></div>
        <p className="mt-10 font-mono text-sm font-bold tracking-[0.2em] text-[var(--primary)]">ERRO 404</p>
        <h1 className="mt-4 text-4xl font-bold tracking-[-0.04em] text-[var(--ink)] sm:text-5xl">Esta página não foi encontrada.</h1>
        <p className="mt-5 leading-7 text-slate-600">O endereço pode ter mudado ou não existir. Volte para conhecer as soluções da Kodo Code.</p>
        <Link className="button-primary mt-8" href="/">Voltar ao início</Link>
      </div>
    </main>
  );
}
