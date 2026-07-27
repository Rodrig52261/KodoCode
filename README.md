# Kodo Code

Site institucional e painel administrativo da Kodo Code, separados em uma aplicação Next.js e uma API Spring Boot.

## Funcionalidades concluídas

- landing page responsiva, conteúdo público vindo da API, FAQs, SEO e páginas legais;
- formulário validado, proteção antispam, persistência antes do envio de e-mail e confirmação ao cliente;
- autenticação por cookies HttpOnly, refresh rotativo, CSRF, bloqueio de login e alteração de senha;
- painel com dashboard, editor textual, rascunho, publicação, versões e restauração;
- gestão de FAQs e contatos com busca, filtros, status, observações internas e arquivamento;
- auditoria consultável e protegida contra atualização e remoção no banco;
- Docker, migrations, testes automatizados e documentação OpenAPI.

## Execução com Docker

Requisitos: Docker com Compose.

```bash
cp .env.example .env
cp backend/.env.example backend/.env
openssl rand -base64 48
```

Configure em `backend/.env` a conexão **Session Pooler** do Supabase e copie o valor gerado para `KODO_JWT_SECRET`. Para criar o primeiro administrador, forneça temporariamente em uma execução isolada do backend:

```dotenv
KODO_BOOTSTRAP_ADMIN_ENABLED=true
KODO_BOOTSTRAP_ADMIN_NAME=Administrador
KODO_BOOTSTRAP_ADMIN_EMAIL=admin@seudominio.com.br
KODO_BOOTSTRAP_ADMIN_PASSWORD=UmaSenhaForte123
```

As credenciais de bootstrap não são repassadas pelo `compose.yaml` normal, evitando que a senha inicial permaneça exposta no ambiente do container. Use um override temporário e não versionado somente no primeiro provisionamento. Depois, execute normalmente:

```bash
docker compose up --build
```

Após o primeiro provisionamento, remova todas as variáveis `KODO_BOOTSTRAP_ADMIN_*`, altere a senha pelo painel e recrie o backend. O provisionamento é idempotente e nunca sobrescreve um usuário existente.

Serviços locais:

- Frontend: `http://localhost:3001`
- Login: `http://localhost:3001/admin/login`
- API: `http://localhost:8080`
- OpenAPI em desenvolvimento: `http://localhost:8080/swagger-ui.html` (exige sessão administrativa)
- PostgreSQL: projeto gerenciado no Supabase, sem porta local exposta

O conteúdo inicial é aplicado pela migration `V3__seed_public_content.sql`. Antes de publicar em um domínio real, revise `NEXT_PUBLIC_SITE_URL` e os textos/dados de contato pelo painel.

## E-mail

O envio usa a interface `EmailService` e a API REST do EmailJS. O contato é confirmado no banco antes das duas tentativas de e-mail e falhas ficam registradas no lead. Consulte [configuração detalhada do backend](backend/README.md#e-mail-com-emailjs).

## Implantação

Use um proxy TLS na frente dos serviços, perfil Spring `prod`, cookies seguros, banco gerenciado com backup, CORS restrito ao domínio real e segredos fornecidos pelo gerenciador da plataforma. Nunca copie o `.env` local para a imagem. Após o primeiro start, desative o bootstrap do administrador.

## Projetos

- [Frontend](frontend/README.md)
- [Backend](backend/README.md)
- [Arquitetura, modelo de dados e fluxos](docs/ARCHITECTURE.md)
- [Checklist de implantação em produção](docs/PRODUCTION.md)

Nunca versionar `.env`, segredos JWT, senhas, tokens ou credenciais de banco e e-mail.
