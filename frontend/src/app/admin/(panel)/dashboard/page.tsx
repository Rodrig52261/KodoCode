"use client";
import Link from "next/link";
import { useEffect, useState } from "react";
import { apiRequest } from "@/lib/api/client";
import { type Dashboard, formatDate, labels } from "@/features/admin/types";
import { Heading, State } from "@/features/admin/components";

export default function DashboardPage() {
 const [data,setData]=useState<Dashboard>(); const [error,setError]=useState("");
 useEffect(()=>{apiRequest<Dashboard>("/api/v1/admin/dashboard").then(setData).catch(e=>setError(e.message));},[]);
 if(error) return <State text={error}/>; if(!data) return <State text="Carregando indicadores..."/>;
 return <><Heading title="Visão geral" text="Acompanhe os contatos e as alterações recentes."/><div className="mt-7 grid gap-4 sm:grid-cols-3">{[["Total de contatos",data.totalLeads],["Recebidos no mês",data.leadsThisMonth],["Não visualizados",data.unreadLeads]].map(([label,value])=><article className="admin-card" key={label}><p className="text-sm text-slate-500">{label}</p><p className="mt-2 text-4xl font-bold text-[var(--ink)]">{value}</p></article>)}</div>
 <div className="mt-6 grid gap-6 xl:grid-cols-2"><section className="admin-card"><h2 className="font-bold">Por serviço</h2><div className="mt-4 space-y-3">{Object.entries(data.leadsByService).map(([key,value])=><div className="flex justify-between text-sm" key={key}><span>{labels[key]??key}</span><strong>{value}</strong></div>)}{!Object.keys(data.leadsByService).length&&<p className="text-sm text-slate-500">Ainda não há contatos.</p>}</div></section>
 <section className="admin-card"><div className="flex justify-between"><h2 className="font-bold">Contatos recentes</h2><Link className="admin-link" href="/admin/contatos">Ver todos</Link></div><div className="mt-4 divide-y divide-slate-100">{data.recentLeads.map(l=><Link className="block py-3 text-sm" href={`/admin/contatos?id=${l.id}`} key={l.id}><strong>{l.name}</strong><span className="ml-2 text-slate-500">{labels[l.serviceInterest]??l.serviceInterest} · {formatDate(l.createdAt)}</span></Link>)}</div></section>
 <section className="admin-card"><h2 className="font-bold">Conteúdo recente</h2><div className="mt-4 divide-y divide-slate-100">{data.recentContent.map(c=><div className="py-3 text-sm" key={c.id}><strong>{c.sectionKey}</strong><span className="ml-2 text-slate-500">v{c.version} · {formatDate(c.createdAt)}</span></div>)}</div></section>
 <section className="admin-card"><div className="flex justify-between"><h2 className="font-bold">Auditoria recente</h2><Link className="admin-link" href="/admin/auditoria">Ver todos</Link></div><div className="mt-4 divide-y divide-slate-100">{data.recentAudits.map(a=><div className="py-3 text-sm" key={a.id}><strong>{a.action}</strong><span className="ml-2 text-slate-500">{a.actorEmail??"Sistema"} · {formatDate(a.createdAt)}</span></div>)}</div></section></div></>;
}
