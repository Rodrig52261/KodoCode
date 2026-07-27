import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import { AdminShell } from "@/features/admin/admin-shell";

const API_URL = process.env.API_INTERNAL_URL ?? process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";
export default async function AdminPanelLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  const accessToken = (await cookies()).get("kodo_access_token")?.value;
  if (!accessToken) redirect("/admin/login");
  const response = await fetch(`${API_URL}/api/v1/auth/me`, {
    headers: { Cookie: `kodo_access_token=${encodeURIComponent(accessToken)}` },
    cache: "no-store",
    signal: AbortSignal.timeout(5_000),
  }).catch(() => null);
  if (!response?.ok) redirect("/admin/login");
  const user = await response.json() as { name: string; email: string };
  return <AdminShell user={user}>{children}</AdminShell>;
}
