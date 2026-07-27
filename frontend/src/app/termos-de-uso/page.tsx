import type { Metadata } from "next";
import { LegalPage } from "@/features/site/legal-page";

export const metadata: Metadata = {
  title: "Termos de uso | Kodo Code",
  description: "Condições gerais para utilização do site institucional da Kodo Code.",
  alternates: { canonical: "/termos-de-uso" },
};

export default function TermsPage() {
  return (
    <LegalPage title="Termos de uso" introduction="Ao navegar neste site, você concorda com estas condições gerais de utilização dos conteúdos e canais institucionais da Kodo Code.">
      <section>
        <h2>Finalidade do site</h2>
        <p>O site apresenta a Kodo Code, suas soluções e seus canais de contato. Os conteúdos possuem caráter informativo e não constituem proposta comercial definitiva.</p>
      </section>
      <section>
        <h2>Orçamentos e contratação</h2>
        <p>Escopo, valores, prazos, responsabilidades e condições de suporte serão definidos em proposta ou contrato específico. O envio de uma solicitação não cria obrigação de contratação para nenhuma das partes.</p>
      </section>
      <section>
        <h2>Uso permitido</h2>
        <p>Você não deve tentar comprometer a segurança, obter acesso não autorizado, executar automações abusivas ou utilizar o site para fins ilícitos.</p>
      </section>
      <section>
        <h2>Propriedade intelectual</h2>
        <p>Textos, identidade visual, interfaces e demais materiais do site são protegidos pela legislação aplicável. A reprodução comercial depende de autorização.</p>
      </section>
      <section>
        <h2>Contato</h2>
        <p>Dúvidas sobre estes termos podem ser enviadas para <a className="font-semibold text-[var(--primary-dark)] underline" href="mailto:contato@kodocode.com.br">contato@kodocode.com.br</a>.</p>
      </section>
    </LegalPage>
  );
}
