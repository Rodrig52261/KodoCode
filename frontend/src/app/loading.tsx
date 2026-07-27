export default function Loading() {
  return (
    <main className="min-h-screen bg-white" aria-busy="true" aria-label="Carregando conteúdo">
      <div className="h-18 border-b border-slate-100" />
      <div className="bg-[var(--ink)] py-24">
        <div className="site-container grid gap-12 lg:grid-cols-2">
          <div className="space-y-5">
            <div className="h-4 w-48 animate-pulse rounded bg-white/15" />
            <div className="h-14 max-w-xl animate-pulse rounded-xl bg-white/10" />
            <div className="h-14 max-w-md animate-pulse rounded-xl bg-white/10" />
            <div className="h-6 max-w-lg animate-pulse rounded bg-white/10" />
          </div>
          <div className="h-96 animate-pulse rounded-3xl bg-white/5" />
        </div>
      </div>
    </main>
  );
}
