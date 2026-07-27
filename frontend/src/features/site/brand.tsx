import Link from "next/link";

export function Brand({ inverted = false }: Readonly<{ inverted?: boolean }>) {
  return (
    <Link className="group inline-flex items-center gap-3" href="/" aria-label="Kodo Code — início">
      <span className={`grid size-10 place-items-center rounded-xl shadow-sm ${inverted ? "bg-white" : "bg-gradient-to-br from-sky-500 to-violet-600"}`}>
        <svg aria-hidden="true" className="size-6" viewBox="0 0 24 24" fill="none">
          <path d="M7 5v14M17 5l-7 7 7 7" stroke={inverted ? "#0b1533" : "#fff"} strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round" />
          <path d="M14.2 5H19" stroke={inverted ? "#7c3aed" : "#bae6fd"} strokeWidth="2.4" strokeLinecap="round" />
        </svg>
      </span>
      <span className={`text-lg font-bold tracking-[-0.03em] ${inverted ? "text-white" : "text-[var(--ink)]"}`}>
        Kodo <span className={inverted ? "text-sky-300" : "text-[var(--primary-dark)]"}>Code</span>
      </span>
    </Link>
  );
}
