import type { Metadata } from "next";
import { LegalPage } from "@/features/site/legal-page";

export const metadata: Metadata = {
  title: "Política de privacidade | Kodo Code",
  description: "Saiba como a Kodo Code trata dados enviados pelos canais de contato.",
  alternates: { canonical: "/politica-de-privacidade" },
};

export default function PrivacyPolicyPage() {
  return (
    <LegalPage title="Política de privacidade" introduction="Esta política explica, em linguagem direta, quais dados podem ser tratados quando você entra em contato com a Kodo Code e como protegemos essas informações.">
      <section>
        <h2>Dados tratados</h2>
        <p>Podemos receber nome, empresa, e-mail, telefone, interesse, orçamento aproximado e a mensagem que você decidir enviar. Dados técnicos mínimos, como data, origem, IP e User-Agent, podem ser registrados para segurança e prevenção de abuso.</p>
      </section>
      <section>
        <h2>Finalidades</h2>
        <ul>
          <li>Responder solicitações e preparar propostas comerciais.</li>
          <li>Dar continuidade ao relacionamento solicitado por você.</li>
          <li>Proteger os canais contra spam, fraude e uso indevido.</li>
          <li>Cumprir obrigações legais quando aplicáveis.</li>
        </ul>
      </section>
      <section>
        <h2>Compartilhamento e armazenamento</h2>
        <p>Não comercializamos dados pessoais. Informações poderão ser processadas por fornecedores de infraestrutura e comunicação estritamente necessários à operação, sujeitos a medidas de segurança e aos seus próprios termos.</p>
      </section>
      <section>
        <h2>Seus direitos</h2>
        <p>Você pode solicitar confirmação de tratamento, acesso, correção ou eliminação quando aplicável. Para isso, escreva para <a className="font-semibold text-[var(--primary-dark)] underline" href="mailto:contato@kodocode.com.br">contato@kodocode.com.br</a>.</p>
      </section>
    </LegalPage>
  );
}
