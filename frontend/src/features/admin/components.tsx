export function Heading({ title, text }: { title: string; text: string }) {
  return (
    <div>
      <h1 className="text-3xl font-bold tracking-tight text-[var(--ink)]">{title}</h1>
      <p className="mt-2 text-slate-600">{text}</p>
    </div>
  );
}

export function State({ text }: { text: string }) {
  return (
    <div className="admin-card text-sm text-slate-600" role="status">
      {text}
    </div>
  );
}
