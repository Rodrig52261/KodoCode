import { ImageResponse } from "next/og";

export const alt = "Kodo Code — soluções digitais inteligentes";
export const size = { width: 1200, height: 630 };
export const contentType = "image/png";

export default function OpenGraphImage() {
  return new ImageResponse(
    <div style={{ width: "100%", height: "100%", display: "flex", alignItems: "center", justifyContent: "center", background: "linear-gradient(135deg, #07122e, #172554 58%, #4c1d95)", color: "white", fontFamily: "sans-serif", position: "relative" }}>
      <div style={{ position: "absolute", width: 420, height: 420, borderRadius: 999, right: -80, top: -100, background: "#38bdf8", opacity: 0.3 }} />
      <div style={{ display: "flex", width: 980, flexDirection: "column" }}>
        <div style={{ display: "flex", alignItems: "center", gap: 22, fontSize: 34, fontWeight: 700 }}>
          <div style={{ display: "flex", width: 64, height: 64, borderRadius: 16, alignItems: "center", justifyContent: "center", background: "white", color: "#0b1533" }}>K</div>
          Kodo <span style={{ color: "#7dd3fc", marginLeft: -14 }}>Code</span>
        </div>
        <div style={{ display: "flex", maxWidth: 900, marginTop: 70, fontSize: 68, lineHeight: 1.08, letterSpacing: -3, fontWeight: 750 }}>
          Ideias e processos transformados em soluções digitais.
        </div>
        <div style={{ display: "flex", marginTop: 38, color: "#b8c7d6", fontSize: 26 }}>Sites · Sistemas · CRMs · Automações</div>
      </div>
    </div>,
    size,
  );
}
