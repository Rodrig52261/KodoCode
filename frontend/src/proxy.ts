import { NextRequest, NextResponse } from "next/server";

export function proxy(request: NextRequest) {
  const hasAccessCookie = request.cookies.has("kodo_access_token");

  if (!hasAccessCookie) {
    return NextResponse.redirect(new URL("/admin/login", request.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: ["/admin/dashboard/:path*", "/admin/conteudos/:path*", "/admin/contatos/:path*", "/admin/auditoria/:path*", "/admin/senha/:path*"],
};
