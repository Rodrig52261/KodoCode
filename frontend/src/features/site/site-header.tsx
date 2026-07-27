"use client";

import { useEffect, useState } from "react";
import { Brand } from "./brand";
import type { SiteSections } from "./content-schema";
import { EditableText } from "@/features/admin/editable-text";

type Navigation = SiteSections["navigation"];

export function SiteHeader({ navigation, onTextChange }: Readonly<{
  navigation: Navigation;
  onTextChange?: (path: Array<string | number>, value: string) => void;
}>) {
  const [open, setOpen] = useState(false);

  useEffect(() => {
    if (!open) return;
    const closeOnEscape = (event: KeyboardEvent) => {
      if (event.key === "Escape") setOpen(false);
    };
    window.addEventListener("keydown", closeOnEscape);
    return () => window.removeEventListener("keydown", closeOnEscape);
  }, [open]);

  return (
    <header className="site-header sticky top-0 z-50 border-b border-sky-100/80 bg-white/88 backdrop-blur-xl">
      <div className="site-container flex h-18 items-center justify-between">
        <Brand />

        <nav className="hidden items-center gap-7 lg:flex" aria-label="Navegação principal">
          {navigation.items.map((item, index) => (
            <a className="nav-link" href={onTextChange ? undefined : item.href} key={item.href}>
              {onTextChange ? <EditableText value={item.label} label={`item ${index + 1} do menu`} onChange={(value) => onTextChange(["items", index, "label"], value)} /> : item.label}
            </a>
          ))}
        </nav>

        <div className="hidden lg:block">
          <a className="button-primary button-small" href={onTextChange ? undefined : navigation.ctaHref}>
            {onTextChange ? <EditableText value={navigation.ctaLabel} label="botão do menu" onChange={(value) => onTextChange(["ctaLabel"], value)} /> : navigation.ctaLabel}
          </a>
        </div>

        <button
          aria-controls="mobile-navigation"
          aria-expanded={open}
          aria-label={open ? "Fechar menu" : "Abrir menu"}
          className="grid size-11 place-items-center rounded-xl border border-sky-100 bg-sky-50/70 text-[var(--ink)] lg:hidden"
          onClick={() => setOpen((current) => !current)}
          type="button"
        >
          <span aria-hidden="true" className="relative block h-4 w-5">
            <span className={`absolute left-0 top-0 h-0.5 w-5 bg-current transition ${open ? "translate-y-[7px] rotate-45" : ""}`} />
            <span className={`absolute left-0 top-[7px] h-0.5 w-5 bg-current transition ${open ? "opacity-0" : ""}`} />
            <span className={`absolute left-0 top-[14px] h-0.5 w-5 bg-current transition ${open ? "-translate-y-[7px] -rotate-45" : ""}`} />
          </span>
        </button>
      </div>

      <div
        className={`overflow-hidden border-t border-sky-100 bg-white transition-[max-height,opacity] duration-300 lg:hidden ${open ? "max-h-[32rem] opacity-100" : "max-h-0 opacity-0"}`}
        id="mobile-navigation"
      >
        <nav aria-label="Navegação móvel" className="site-container flex flex-col gap-1 py-5">
          {navigation.items.map((item, index) => (
            <a
              className="rounded-xl px-3 py-3 text-base font-medium text-slate-700 hover:bg-gradient-to-r hover:from-sky-50 hover:to-violet-50"
              href={onTextChange ? undefined : item.href}
              key={item.href}
              onClick={() => setOpen(false)}
            >
              {onTextChange ? <EditableText value={item.label} label={`item ${index + 1} do menu móvel`} onChange={(value) => onTextChange(["items", index, "label"], value)} /> : item.label}
            </a>
          ))}
          <a className="button-primary mt-3 justify-center" href={onTextChange ? undefined : navigation.ctaHref} onClick={() => setOpen(false)}>
            {onTextChange ? <EditableText value={navigation.ctaLabel} label="botão do menu móvel" onChange={(value) => onTextChange(["ctaLabel"], value)} /> : navigation.ctaLabel}
          </a>
        </nav>
      </div>
    </header>
  );
}
