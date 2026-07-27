# Implantação em produção

## Topologia recomendada

Publique o Next.js e a API Spring Boot como serviços privados atrás de um proxy HTTPS. Exponha somente o proxy. O PostgreSQL é gerenciado pelo Supabase e deve ser acessado exclusivamente pelo backend via Session Pooler com TLS. Prefira encaminhar `/api` no mesmo domínio do frontend; isso preserva cookies host-only e evita ampliar o domínio dos cookies. O frontend deve alcançar `API_INTERNAL_URL` pela rede interna.

## Variáveis obrigatórias

- `SPRING_PROFILES_ACTIVE=prod`;
- `DATABASE_URL` do Session Pooler Supabase com `sslmode=require`, `DATABASE_USERNAME` com o project ref e `DATABASE_PASSWORD` definida no cofre;
- `KODO_JWT_SECRET` aleatório (por exemplo, `openssl rand -hex 32`);
- `KODO_CORS_ALLOWED_ORIGINS=https://seudominio.com.br`;
- `KODO_COOKIE_SECURE=true`, `KODO_COOKIE_SAME_SITE=Lax` e cookies host-only sempre que possível;
- `NEXT_PUBLIC_SITE_URL` com HTTPS e `NEXT_PUBLIC_API_URL` vazio para usar `/api` no mesmo domínio;
- `API_INTERNAL_URL` com a URL privada do backend;
- serviço, templates e credenciais do EmailJS documentados no README do backend.

Segredos devem ser configurados no cofre da plataforma, não em arquivos incluídos na imagem ou variáveis permanentes do Compose. Use `KODO_BOOTSTRAP_ADMIN_ENABLED=true` somente em uma execução isolada de provisionamento; confirme o login, troque a senha e remova imediatamente as quatro variáveis `KODO_BOOTSTRAP_ADMIN_*`.

## Proxy e operação

1. Encaminhe o domínio principal ao frontend e `/api` ao backend. Se usar um subdomínio de API, planeje cuidadosamente o escopo dos cookies e do CSRF.
2. Remova cabeçalhos `Forwarded` e `X-Forwarded-*` enviados pelo cliente. Somente o proxy confiável pode recriá-los. O backend ignora esses cabeçalhos por padrão; para registrar o IP original, habilite o tratamento nativo apenas com a lista exata de proxies internos confiáveis.
3. Use `/actuator/health/readiness` para readiness e `/actuator/health/liveness` para liveness.
4. Execute migrations antes de liberar tráfego ou permita que a primeira instância as aplique sem concorrência.
5. Configure backup automático do PostgreSQL e teste restauração periodicamente.
6. Centralize os logs JSON do perfil `prod`, crie alertas para respostas 5xx e falhas de e-mail e monitore espaço/conexões do banco.
7. Limite o corpo das requisições no proxy (por exemplo, `64k`), aplique limites globais de tráfego e timeouts de conexão.
8. Em múltiplas instâncias, mova os contadores de rate limit para Redis ou para o gateway; os contadores locais protegem apenas uma instância.

## Checklist de liberação

- DNS e certificado TLS válidos;
- CORS restrito e cookies `Secure` observados no navegador;
- bootstrap desligado e OpenAPI desligada ou protegida;
- nenhum segredo de bootstrap presente no ambiente permanente;
- somente o proxy exposto publicamente; banco e porta 8080 privados;
- CSP, `X-Frame-Options`, `nosniff` e HSTS conferidos por teste HTTP;
- login, refresh, logout e troca de senha testados;
- contato persistido e ambos os e-mails entregues;
- publicação refletida na landing e restauração de versão testada;
- acesso anônimo a `/api/v1/admin/**` retornando 401;
- backup, política de retenção e responsável por incidentes definidos.
- MFA ou controle de acesso adicional no proxy para o painel administrativo avaliado antes da abertura pública.

Para atualizar, gere novas imagens imutáveis, aplique as migrations e faça rollout gradual. Não reutilize o banco de homologação em produção e nunca execute `flyway clean`.
