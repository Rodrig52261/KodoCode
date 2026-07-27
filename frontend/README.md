# Kodo Code Front-end

## Requisitos

- Node.js 20.9 ou superior;
- API disponível na URL configurada.

## Execução

```bash
cp .env.example .env.local
npm ci
npm run dev
```

`NEXT_PUBLIC_API_URL` deve ficar vazio para o navegador usar o proxy same-origin `/api` (recomendado). `API_INTERNAL_URL` atende o proxy e os Server Components; no Docker Compose ela aponta para `http://backend:8080` pela rede privada. `NEXT_PUBLIC_SITE_URL` define URLs canônicas, sitemap e metadados sociais.

## Verificações

```bash
npm run lint
npm test
npm run build
npm audit
```

Os overrides de `postcss`, `minimatch` e `brace-expansion` no `package.json` mantêm correções de segurança para dependências transitivas. Revise-os somente junto com um novo `npm audit` e a suíte completa.

O navegador envia os cookies de autenticação diretamente à API. O cookie do access token é `HttpOnly`; o JavaScript acessa somente o cookie CSRF necessário para requisições mutáveis.

A landing pública consome seções e FAQs da API, com validação Zod do contrato. Inclui formulário validado, navegação responsiva, SEO dinâmico, Schema.org, Open Graph, sitemap, robots, páginas legais, loading, erro e 404.

O painel valida a sessão no servidor e oferece dashboard, conteúdos/FAQs, contatos, auditoria e alteração de senha. Requisições administrativas tentam uma única renovação silenciosa quando o access token expira.
