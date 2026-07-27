"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useState } from "react";
import { apiRequest, initializeCsrf } from "@/lib/api/client";

const links = [["/admin/dashboard", "Visão geral"], ["/admin/conteudos", "Conteúdos"], ["/admin/contatos", "Contatos"], ["/admin/auditoria", "Auditoria"], ["/admin/senha", "Alterar senha"]] as const;

export function AdminShell({ children, user }: Readonly<{ children: React.ReactNode; user: { name: string; email: string } }>) {
  const pathname = usePathname(); const router = useRouter(); const [open, setOpen] = useState(false);
  async function logout() { try { await initializeCsrf(); await apiRequest("/api/v1/auth/logout", { method: "POST" }); } finally { router.replace("/admin/login"); router.refresh(); } }
  return <div className="min-h-screen bg-[var(--background)]">
    <button className="fixed right-4 top-4 z-50 rounded-lg bg-gradient-to-r from-sky-600 to-violet-600 px-3 py-2 text-sm text-white shadow-lg lg:hidden" onClick={() => setOpen(!open)} aria-expanded={open}>Menu</button>
    {open && <button className="fixed inset-0 z-30 bg-slate-950/35 lg:hidden" onClick={() => setOpen(false)} aria-label="Fechar menu" />}
    <aside className={`fixed inset-y-0 left-0 z-40 flex w-64 flex-col bg-[linear-gradient(160deg,#0b1533,#172554_58%,#3b176f)] p-5 text-white shadow-2xl transition-transform lg:translate-x-0 ${open ? "translate-x-0" : "-translate-x-full"}`}>
      <Link className="text-xl font-bold" href="/admin/dashboard">Kodo <span className="text-sky-300">Code</span></Link>
      <p className="mt-1 text-xs text-slate-400">Painel administrativo</p>
      <nav className="mt-8 space-y-1" aria-label="Administração">{links.map(([href, label]) => <Link onClick={() => setOpen(false)} className={`block rounded-lg px-3 py-2.5 text-sm font-semibold ${pathname.startsWith(href) ? "bg-white/12 text-white" : "text-slate-300 hover:bg-white/7"}`} href={href} key={href}>{label}</Link>)}</nav>
      <div className="mt-auto border-t border-white/10 pt-5"><p className="truncate text-sm font-bold">{user.name}</p><p className="truncate text-xs text-slate-400">{user.email}</p><button className="mt-4 text-sm font-semibold text-sky-300 hover:text-violet-200" onClick={logout}>Sair</button></div>
    </aside>
    <div className="lg:pl-64"><header className="border-b border-slate-200 bg-white px-5 py-4 sm:px-8"><p className="text-xs font-semibold uppercase tracking-widest text-slate-500">Admin / {links.find(([href]) => pathname.startsWith(href))?.[1] ?? "Painel"}</p></header><main className="p-5 sm:p-8">{children}</main></div>
  </div>;
}
