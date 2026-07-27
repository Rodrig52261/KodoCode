# Kodo Code API

API REST da Kodo Code com Java 21, Spring Boot, Spring Security, JPA, PostgreSQL, Flyway e OpenAPI.

## Requisitos

- Java 21;
- Maven 3.9+;
- PostgreSQL 17; ou
- Docker, para executar sem instalar Java e PostgreSQL localmente.

## Configuração

```bash
cp .env.example .env
```

`KODO_JWT_SECRET` é obrigatório e deve ter ao menos 32 caracteres. Em produção, use um segredo aleatório, cookies HTTPS e uma origem CORS explícita.

Variáveis principais:

| Variável | Finalidade |
| --- | --- |
| `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` | Conexão PostgreSQL |
| `KODO_JWT_SECRET` | Assinatura dos access tokens |
| `KODO_CORS_ALLOWED_ORIGINS` | Origens permitidas, separadas por vírgula |
| `KODO_COOKIE_SECURE` | Exige HTTPS nos cookies |
| `KODO_BOOTSTRAP_ADMIN_*` | Provisionamento único do administrador |
| `KODO_OPENAPI_ENABLED` | Habilita documentação no perfil de produção |
| `KODO_EMAILJS_ENABLED`, `KODO_EMAILJS_NOTIFICATION_TO` | Ativação e destinatário interno dos e-mails |
| `KODO_EMAILJS_SERVICE_ID`, `KODO_EMAILJS_*_TEMPLATE_ID` | Serviço conectado e templates do EmailJS |
| `KODO_EMAILJS_PUBLIC_KEY`, `KODO_EMAILJS_PRIVATE_KEY` | Credenciais do EmailJS; a Private Key é opcional |

## Execução e testes

Com as variáveis exportadas no shell:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
mvn verify
```

Ou, a partir da raiz do repositório:

```bash
docker compose up --build postgres backend
```

O Flyway aplica automaticamente as migrations antes da validação do modelo JPA. A documentação fica disponível em `/swagger-ui.html` no perfil de desenvolvimento e é desabilitada por padrão em produção.

## Administrador inicial

Não existe cadastro público. Defina nome, e-mail e uma senha com no mínimo 12 caracteres, maiúscula, minúscula e número; ative `KODO_BOOTSTRAP_ADMIN_ENABLED` somente no primeiro start. Senhas são persistidas com BCrypt e não são exibidas em logs.

## Autenticação

Endpoints implementados:

- `GET /api/v1/auth/csrf`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `POST /api/v1/auth/change-password`
- `GET /api/v1/auth/me`

Access e refresh tokens são enviados em cookies `HttpOnly`. Requisições mutáveis também precisam do header `X-XSRF-TOKEN`, obtido do cookie CSRF. Refresh tokens são opacos, persistidos somente como SHA-256, rotacionados a cada uso e revogados no logout ou troca de senha.

## APIs da aplicação

- `GET /api/v1/public/site-content`: retorna somente a versão publicada de cada seção;
- `GET /api/v1/public/faqs`: retorna FAQs publicadas e ativas na ordem configurada.
- `POST /api/v1/public/contact`: valida, limita, salva e tenta entregar os e-mails.

Os recursos `/api/v1/admin/content`, `/faqs`, `/leads`, `/audit-logs` e `/dashboard` exigem perfil `ADMIN`. Listagens de contatos e auditoria possuem paginação e filtros. Veja os contratos completos em `/swagger-ui.html`.

As respostas públicas usam `Cache-Control: max-age=300, public, stale-while-revalidate=600`. A migration V3 cria 11 seções editoriais versionadas e 8 FAQs, sem criar usuário ou credencial artificial. As versões de seed não possuem autor; versões administrativas criadas pelo painel terão o administrador responsável.

## E-mail com EmailJS

O backend usa `POST https://api.emailjs.com/api/v1.0/email/send`; nenhuma credencial fica no frontend. Crie um serviço de e-mail no painel do EmailJS e dois templates.

### Template de notificação da empresa

- ID sugerido: `template_notification`;
- **To Email**: `{{to_email}}`;
- **Reply-To**: `{{reply_to}}`;
- assunto: `Novo contato pelo site - {{name}}`;
- corpo disponível: `{{lead_id}}`, `{{name}}`, `{{company}}`, `{{email}}`, `{{phone}}`, `{{service_interest}}`, `{{budget_range}}` e `{{message}}`.

### Template de confirmação do cliente

- ID sugerido: `template_confirmation`;
- **To Email**: `{{to_email}}`;
- **Reply-To**: `{{reply_to}}`;
- assunto: `Recebemos sua mensagem - Kodo Code`;
- use `{{name}}`, `{{company}}`, `{{service_interest}}` e `{{message}}` para personalizar o corpo.

Configure no `.env` da raiz quando usar Docker Compose:

```env
KODO_EMAILJS_ENABLED=true
KODO_EMAILJS_NOTIFICATION_TO=contato@suaempresa.com.br
KODO_EMAILJS_SERVICE_ID=service_xxxxxxx
KODO_EMAILJS_NOTIFICATION_TEMPLATE_ID=template_notification
KODO_EMAILJS_CONFIRMATION_TEMPLATE_ID=template_confirmation
KODO_EMAILJS_PUBLIC_KEY=sua_public_key
KODO_EMAILJS_PRIVATE_KEY=
KODO_EMAILJS_MINIMUM_REQUEST_INTERVAL=1100ms
```

A Private Key é opcional e só deve ser preenchida quando essa autorização estiver habilitada em **Account > Security** no EmailJS. O intervalo de 1,1 segundo protege as duas chamadas sequenciais contra o limite oficial de uma requisição por segundo.

Em desenvolvimento, deixe o envio desabilitado até preencher todos os IDs. Isso não impede a persistência: os estados ficam como `SKIPPED`. Respostas recusadas pelo EmailJS ficam como `FAILED`; o erro técnico é limitado, registrado no lead e não exposto ao visitante.

Referências oficiais: [REST `/send`](https://www.emailjs.com/docs/rest-api/send/) e [criação de template](https://www.emailjs.com/docs/tutorial/creating-email-template/).

## Produção

Use o perfil `prod`, TLS no proxy reverso, `KODO_COOKIE_SECURE=true`, credenciais de banco exclusivas, segredo JWT gerado por fonte criptográfica e uma lista CORS restrita. Não habilite o bootstrap após criar o primeiro administrador. Habilite OpenAPI em produção apenas se houver uma necessidade operacional e proteção adicional.
