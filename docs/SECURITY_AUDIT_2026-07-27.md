# Auditoria de segurança — 27/07/2026

## Resultado

A aplicação foi revisada de ponta a ponta: frontend Next.js, API Spring Boot, autenticação, editor de conteúdo, formulário de contato, EmailJS, PostgreSQL, Docker Compose, dependências e configuração. Todos os achados exploráveis identificados durante a revisão foram corrigidos. O ambiente corrigido está em execução e saudável.

Após a auditoria, os dados de negócio foram migrados para o Supabase PostgreSQL via Session Pooler/TLS. Auditorias e refresh tokens anteriores não foram migrados. O PostgreSQL local foi retirado da composição ativa e mantido apenas como backup recuperável.

Não foram encontrados segredos com formato de chave privada ou token no código-fonte. Os arquivos locais `.env` estão com modo `600` e o bootstrap administrativo está desativado.

## Achados corrigidos

| Risco | Correção aplicada |
| --- | --- |
| Falsificação de IP por `X-Forwarded-For`, permitindo enganar auditoria e rate limit | O backend usa o endereço real da conexão e ignora forwarded headers por padrão. |
| Bypass de rate limit alternando e-mails | Limites independentes por IP e por conta/e-mail para login e contato. |
| Links perigosos persistidos pelo editor (`javascript:`, URLs protocol-relative) | Sanitização no backend e schema equivalente no frontend; somente rotas internas e âncoras seguras. |
| JWT continuava válido após troca de senha | Versão de credencial incluída e validada no token; a troca de senha invalida todos os access tokens anteriores. |
| Chamada EmailJS podia seguir redirects e ficar presa | HTTPS obrigatório, host validado, redirects desativados e timeouts de conexão/resposta. |
| Campos de auditoria podiam manter segredos aninhados | Redação recursiva de senhas, tokens, cookies e chaves sensíveis. |
| Busca administrativa aceitava curingas SQL e texto excessivo | Tamanho limitado e escape de `%`, `_` e `\\` em consultas `LIKE`. |
| Swagger/OpenAPI acessível anonimamente | Rotas protegidas por `ROLE_ADMIN`; somente health checks permanecem públicos. |
| Browser sem política defensiva consistente | CSP, anti-framing, `nosniff`, Referrer Policy, Permissions Policy, COOP e CORP. |
| Navegador dependia de `localhost:8080` e acesso cross-origin ao backend | Proxy same-origin `/api`; backend permanece na rede privada do Compose e é o único cliente do Supabase. |
| Contêineres com superfície e rede maiores que o necessário | Usuários sem privilégio, capabilities removidas, `no-new-privileges`, init, `/tmp` temporário e redes segmentadas. |
| Dependências vulneráveis nas imagens | PostgreSQL JDBC atualizado; Alpine atualizado; npm/corepack/yarn removidos do runtime; `gosu` vulnerável substituído por `su-exec` em imagem achatada. |
| Credenciais de bootstrap enviadas permanentemente ao contêiner | Variáveis removidas do Compose normal e bootstrap desativado. |

## Evidências de validação

- Backend: **26 testes**, sem falhas; build Maven concluído.
- Frontend: **11 testes**, sem falhas; ESLint e build de produção concluídos.
- `npm audit`: **0 vulnerabilidades**.
- Trivy backend final: **0 HIGH/CRITICAL** no Alpine e no JAR.
- Trivy frontend: **0 HIGH/CRITICAL** no Alpine e nos pacotes da aplicação; a alteração posterior do proxy não mudou dependências ou camada base.
- Trivy PostgreSQL endurecido: **0 HIGH/CRITICAL**.
- Migrações Flyway: versões 1–6 aplicadas; versão 6 invalida tokens após mudança de senha.
- Testes HTTP: home `200`, conteúdo público pelo proxy `200`, CSRF `200`, admin anônimo `401`, escrita sem CSRF `403`, preflight de origem não autorizada `403` e conteúdo com código `400`.
- Backend e PostgreSQL não são serviços públicos no desenho de produção; frontend, backend e banco estão em redes separadas conforme sua função.

## Ações obrigatórias antes de produção

Estas ações dependem da infraestrutura e de credenciais reais, portanto não podem ser automatizadas com segurança no repositório:

1. Ativar HTTPS, `SPRING_PROFILES_ACTIVE=prod`, cookies `Secure` e `NEXT_PUBLIC_SITE_URL` com o domínio final.
2. Definir senha forte e exclusiva do PostgreSQL e armazenar banco, JWT e EmailJS em um cofre de segredos.
3. Remover dos arquivos locais os valores antigos de `KODO_BOOTSTRAP_ADMIN_*` e trocar a senha administrativa, mesmo com o bootstrap já desativado.
4. Adicionar MFA ou restringir o painel no proxy/VPN antes de expô-lo amplamente.
5. Limitar o corpo HTTP no proxy, centralizar rate limits ao escalar horizontalmente e configurar backups criptografados com restauração testada.

Consulte também [SECURITY.md](SECURITY.md) e [PRODUCTION.md](PRODUCTION.md).
