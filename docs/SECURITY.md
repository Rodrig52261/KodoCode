# Segurança da aplicação

## Controles implementados

- autenticação administrativa com BCrypt custo 12, bloqueio temporário e limite por IP e por conta;
- JWT assinado de 15 minutos, versão de credencial e invalidação imediata após troca de senha;
- refresh token opaco, rotativo, armazenado somente como hash e revogado em caso de reutilização;
- cookies `HttpOnly`, CSRF por cookie/cabeçalho, CORS explícito e autorização `ROLE_ADMIN` no backend;
- conteúdo público escapado pelo React, links limitados a rotas internas e editor visual em modo texto puro;
- CSP, proteção contra framing, `nosniff`, política de referência e permissões restritas no frontend;
- validação e limites dos formulários, honeypot, tempo mínimo, deduplicação e limite por IP/e-mail;
- auditoria append-only no PostgreSQL, com redação recursiva de senhas, tokens, cookies e segredos;
- proxy `/api` same-origin, backend vinculado ao loopback e banco Supabase acessível somente pelo backend com TLS;
- frontend/backend sem capacidades Linux e com `no-new-privileges` no Compose;
- imagens mínimas atualizadas e ferramentas de pacote removidas do runtime;
- timeouts e HTTPS obrigatório para chamadas ao EmailJS.

## Responsabilidades de produção

- usar TLS, perfil `prod`, senhas exclusivas e um cofre de segredos;
- expor somente o proxy HTTPS e manter backend/banco em rede privada;
- remover completamente as credenciais de bootstrap depois do primeiro acesso;
- configurar backup criptografado, restauração testada, retenção de leads e rotação de segredos;
- centralizar rate limits ao usar mais de uma instância;
- adicionar MFA ou proteção equivalente ao painel antes de disponibilizá-lo amplamente na internet;
- executar `npm audit`, scanner de dependências Java e scanner das imagens em cada atualização.

## Resposta a incidentes

Em suspeita de comprometimento: desative o acesso administrativo no proxy, troque a senha do administrador, rotacione `KODO_JWT_SECRET`, credenciais do banco e EmailJS, revogue sessões, preserve os logs de auditoria e restaure somente a partir de backup verificado.
