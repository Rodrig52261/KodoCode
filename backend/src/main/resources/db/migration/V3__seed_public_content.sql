ALTER TABLE site_content_versions
    ALTER COLUMN created_by DROP NOT NULL;

INSERT INTO site_sections (id, section_key, title, subtitle, status, created_at, updated_at) VALUES
    ('10000000-0000-4000-8000-000000000001', 'seo', 'SEO', 'Metadados da página pública', 'PUBLISHED', NOW(), NOW()),
    ('10000000-0000-4000-8000-000000000002', 'navigation', 'Navegação', 'Cabeçalho principal', 'PUBLISHED', NOW(), NOW()),
    ('10000000-0000-4000-8000-000000000003', 'hero', 'Destaque principal', 'Proposta de valor', 'PUBLISHED', NOW(), NOW()),
    ('10000000-0000-4000-8000-000000000004', 'benefits', 'Benefícios', 'Resultados para o cliente', 'PUBLISHED', NOW(), NOW()),
    ('10000000-0000-4000-8000-000000000005', 'services', 'Soluções', 'Serviços da Kodo Code', 'PUBLISHED', NOW(), NOW()),
    ('10000000-0000-4000-8000-000000000006', 'process', 'Como trabalhamos', 'Etapas do projeto', 'PUBLISHED', NOW(), NOW()),
    ('10000000-0000-4000-8000-000000000007', 'differentials', 'Diferenciais', 'Forma de entrega', 'PUBLISHED', NOW(), NOW()),
    ('10000000-0000-4000-8000-000000000008', 'about', 'Sobre', 'Identidade da empresa', 'PUBLISHED', NOW(), NOW()),
    ('10000000-0000-4000-8000-000000000009', 'cta', 'Chamada para ação', 'Convite para conversar', 'PUBLISHED', NOW(), NOW()),
    ('10000000-0000-4000-8000-00000000000a', 'contact', 'Contato', 'Canais comerciais', 'PUBLISHED', NOW(), NOW()),
    ('10000000-0000-4000-8000-00000000000b', 'footer', 'Rodapé', 'Informações institucionais', 'PUBLISHED', NOW(), NOW());

INSERT INTO site_content_versions
    (id, site_section_id, content_data, version_number, status, created_at, published_at)
VALUES
    (
        '20000000-0000-4000-8000-000000000001',
        '10000000-0000-4000-8000-000000000001',
        $$
        {
          "title": "Kodo Code | Sites, sistemas e automações para empresas",
          "description": "Desenvolvemos landing pages, sites institucionais, CRMs, chatbots e sistemas personalizados para organizar processos e apoiar o crescimento do seu negócio.",
          "siteName": "Kodo Code",
          "locale": "pt_BR",
          "keywords": ["desenvolvimento de sites", "sistemas personalizados", "CRM", "automação", "chatbot WhatsApp"]
        }
        $$::jsonb,
        1, 'PUBLISHED', NOW(), NOW()
    ),
    (
        '20000000-0000-4000-8000-000000000002',
        '10000000-0000-4000-8000-000000000002',
        $$
        {
          "items": [
            {"label": "Início", "href": "#inicio"},
            {"label": "Soluções", "href": "#solucoes"},
            {"label": "Como trabalhamos", "href": "#processo"},
            {"label": "Sobre", "href": "#sobre"},
            {"label": "Contato", "href": "#contato"}
          ],
          "ctaLabel": "Solicitar orçamento",
          "ctaHref": "#contato"
        }
        $$::jsonb,
        1, 'PUBLISHED', NOW(), NOW()
    ),
    (
        '20000000-0000-4000-8000-000000000003',
        '10000000-0000-4000-8000-000000000003',
        $$
        {
          "eyebrow": "Tecnologia aplicada ao seu negócio",
          "title": "Transformamos ideias e processos em soluções digitais inteligentes.",
          "description": "Desenvolvemos sites, sistemas, CRMs e automações que ajudam empresas a melhorar o atendimento, organizar processos e aumentar suas vendas.",
          "primaryCta": {"label": "Solicitar orçamento", "href": "#contato"},
          "secondaryCta": {"label": "Conhecer soluções", "href": "#solucoes"},
          "highlights": ["Projetos sob medida", "Comunicação clara", "Suporte após a entrega"]
        }
        $$::jsonb,
        1, 'PUBLISHED', NOW(), NOW()
    ),
    (
        '20000000-0000-4000-8000-000000000004',
        '10000000-0000-4000-8000-000000000004',
        $$
        {
          "eyebrow": "Estrutura para crescer",
          "title": "Tecnologia que resolve necessidades reais",
          "description": "Cada escolha técnica parte do que sua empresa precisa alcançar hoje e do que poderá precisar amanhã.",
          "items": [
            {"title": "Soluções personalizadas", "description": "Projetos planejados para as regras, o momento e os objetivos do seu negócio.", "icon": "layers"},
            {"title": "Atendimento próximo", "description": "Conversas objetivas e acompanhamento claro durante toda a execução.", "icon": "message"},
            {"title": "Tecnologia adequada", "description": "Ferramentas escolhidas pelo resultado esperado, sem complexidade desnecessária.", "icon": "code"},
            {"title": "Experiência responsiva", "description": "Interfaces rápidas e fáceis de usar em computadores, tablets e celulares.", "icon": "devices"},
            {"title": "Segurança e organização", "description": "Boas práticas para proteger informações e manter o projeto sustentável.", "icon": "shield"},
            {"title": "Automação de tarefas", "description": "Menos trabalho repetitivo e mais tempo para atender, vender e decidir.", "icon": "automation"},
            {"title": "Suporte após a entrega", "description": "Continuidade para correções, melhorias e novas necessidades do negócio.", "icon": "support"},
            {"title": "Preparado para evolução", "description": "Uma base organizada para que a solução acompanhe o crescimento da empresa.", "icon": "growth"}
          ]
        }
        $$::jsonb,
        1, 'PUBLISHED', NOW(), NOW()
    ),
    (
        '20000000-0000-4000-8000-000000000005',
        '10000000-0000-4000-8000-000000000005',
        $$
        {
          "eyebrow": "Soluções",
          "title": "O digital certo para cada desafio",
          "description": "Da presença online à organização de operações, construímos soluções alinhadas ao seu objetivo comercial.",
          "items": [
            {
              "name": "Landing pages",
              "description": "Páginas focadas em apresentar uma oferta, captar contatos e aumentar conversões.",
              "benefits": ["Mensagem objetiva", "Foco em conversão", "Carregamento rápido"],
              "ctaLabel": "Solicitar orçamento",
              "icon": "window"
            },
            {
              "name": "Sites institucionais",
              "description": "Sites profissionais para apresentar sua empresa, serviços, diferenciais e canais de contato.",
              "benefits": ["Presença profissional", "Boa experiência móvel", "Base preparada para SEO"],
              "ctaLabel": "Conhecer solução",
              "icon": "building"
            },
            {
              "name": "CRM",
              "description": "Sistemas para organizar clientes, oportunidades, históricos, tarefas e processos comerciais.",
              "benefits": ["Visão do funil", "Histórico centralizado", "Rotina comercial organizada"],
              "ctaLabel": "Conhecer solução",
              "icon": "users"
            },
            {
              "name": "Chatbot para WhatsApp",
              "description": "Automação de atendimento, perguntas frequentes, coleta de dados e encaminhamento de clientes.",
              "benefits": ["Respostas mais rápidas", "Triagem automática", "Atendimento consistente"],
              "ctaLabel": "Solicitar orçamento",
              "icon": "chatbot"
            },
            {
              "name": "Sistemas personalizados",
              "description": "Soluções desenvolvidas de acordo com as regras e necessidades específicas da sua empresa.",
              "benefits": ["Fluxos sob medida", "Integrações", "Evolução planejada"],
              "ctaLabel": "Conversar sobre o projeto",
              "icon": "settings"
            }
          ]
        }
        $$::jsonb,
        1, 'PUBLISHED', NOW(), NOW()
    ),
    (
        '20000000-0000-4000-8000-000000000006',
        '10000000-0000-4000-8000-000000000006',
        $$
        {
          "eyebrow": "Como trabalhamos",
          "title": "Um processo claro do primeiro contato à evolução",
          "description": "Você acompanha cada decisão sem precisar dominar termos técnicos.",
          "items": [
            {"number": "01", "title": "Entendimento do negócio", "description": "Conhecemos sua operação, público, dificuldades e objetivo principal."},
            {"number": "02", "title": "Planejamento da solução", "description": "Definimos escopo, prioridades, prazos e a abordagem mais adequada."},
            {"number": "03", "title": "Criação do design", "description": "Organizamos conteúdo e interface para uma experiência clara e profissional."},
            {"number": "04", "title": "Desenvolvimento", "description": "Construímos a solução com código organizado e acompanhamento contínuo."},
            {"number": "05", "title": "Testes e validação", "description": "Revisamos funcionalidades, segurança, conteúdo e uso em diferentes telas."},
            {"number": "06", "title": "Publicação", "description": "Preparamos o ambiente e colocamos a solução no ar com segurança."},
            {"number": "07", "title": "Suporte e evolução", "description": "Acompanhamos o resultado e planejamos melhorias quando fizer sentido."}
          ]
        }
        $$::jsonb,
        1, 'PUBLISHED', NOW(), NOW()
    ),
    (
        '20000000-0000-4000-8000-000000000007',
        '10000000-0000-4000-8000-000000000007',
        $$
        {
          "eyebrow": "Por que a Kodo Code",
          "title": "Clareza para decidir. Qualidade para continuar.",
          "items": [
            {"title": "Objetivo em primeiro lugar", "description": "O projeto nasce do resultado que sua empresa precisa alcançar."},
            {"title": "Comunicação sem ruído", "description": "Você entende o andamento, as escolhas e o que será entregue."},
            {"title": "Código organizado", "description": "Uma base mais simples de manter, corrigir e ampliar."},
            {"title": "Segurança desde o início", "description": "Proteção de dados e acessos considerada em toda a arquitetura."},
            {"title": "Responsividade real", "description": "A experiência é pensada para cada tamanho de tela."},
            {"title": "Continuidade", "description": "A solução pode receber manutenção e crescer junto com o negócio."}
          ]
        }
        $$::jsonb,
        1, 'PUBLISHED', NOW(), NOW()
    ),
    (
        '20000000-0000-4000-8000-000000000008',
        '10000000-0000-4000-8000-000000000008',
        $$
        {
          "eyebrow": "Sobre a Kodo Code",
          "title": "Tecnologia próxima, responsável e orientada a resultado",
          "paragraphs": [
            "A Kodo Code desenvolve soluções digitais para pequenos e médios negócios que precisam apresentar melhor seus serviços, organizar processos e atender clientes com mais eficiência.",
            "Nossa forma de trabalho combina escuta, planejamento e execução técnica. Traduzimos necessidades do negócio em soluções claras, seguras e possíveis de evoluir."
          ],
          "mission": "Criar soluções digitais úteis que simplifiquem processos e apoiem o crescimento sustentável dos nossos clientes.",
          "vision": "Ser uma parceira de tecnologia reconhecida pela clareza, confiança e qualidade das entregas.",
          "values": ["Transparência", "Responsabilidade", "Simplicidade", "Qualidade", "Evolução contínua"]
        }
        $$::jsonb,
        1, 'PUBLISHED', NOW(), NOW()
    ),
    (
        '20000000-0000-4000-8000-000000000009',
        '10000000-0000-4000-8000-000000000009',
        $$
        {
          "eyebrow": "Vamos tirar a ideia do papel?",
          "title": "Pronto para transformar sua ideia em uma solução digital?",
          "description": "Conte o que sua empresa precisa. Ajudamos a organizar o desafio e identificar o melhor caminho.",
          "buttonLabel": "Falar sobre meu projeto",
          "buttonHref": "#contato"
        }
        $$::jsonb,
        1, 'PUBLISHED', NOW(), NOW()
    ),
    (
        '20000000-0000-4000-8000-00000000000a',
        '10000000-0000-4000-8000-00000000000a',
        $$
        {
          "eyebrow": "Contato",
          "title": "Vamos entender o próximo passo do seu negócio",
          "description": "Conte sua necessidade e o resultado que espera alcançar. Vamos analisar o cenário e retornar com os próximos passos.",
          "email": "contato@kodocode.com.br",
          "emailLabel": "Escrever para a Kodo Code",
          "responseTime": "Retorno comercial em até 1 dia útil"
        }
        $$::jsonb,
        1, 'PUBLISHED', NOW(), NOW()
    ),
    (
        '20000000-0000-4000-8000-00000000000b',
        '10000000-0000-4000-8000-00000000000b',
        $$
        {
          "description": "Sites, sistemas e automações desenvolvidos com clareza para apoiar empresas em crescimento.",
          "email": "contato@kodocode.com.br",
          "copyrightName": "Kodo Code",
          "serviceLinks": ["Landing pages", "Sites institucionais", "CRM", "Chatbot para WhatsApp", "Sistemas personalizados"],
          "legalLinks": [
            {"label": "Política de privacidade", "href": "/politica-de-privacidade"},
            {"label": "Termos de uso", "href": "/termos-de-uso"}
          ],
          "socialLinks": []
        }
        $$::jsonb,
        1, 'PUBLISHED', NOW(), NOW()
    );

UPDATE site_sections section
SET published_version_id = version.id
FROM site_content_versions version
WHERE version.site_section_id = section.id
  AND version.version_number = 1;

INSERT INTO faq_items
    (id, question, answer, display_order, active, status, created_at, updated_at)
VALUES
    ('30000000-0000-4000-8000-000000000001', 'Quanto custa desenvolver um site?', 'O valor depende do objetivo, da quantidade de páginas, das integrações e do nível de personalização. Depois de entender sua necessidade, apresentamos um escopo claro com investimento e prazo.', 1, TRUE, 'PUBLISHED', NOW(), NOW()),
    ('30000000-0000-4000-8000-000000000002', 'Quanto tempo demora para o projeto ficar pronto?', 'Uma landing page costuma exigir menos tempo que um site institucional ou sistema personalizado. O cronograma é definido após o levantamento do escopo e inclui etapas de validação com o cliente.', 2, TRUE, 'PUBLISHED', NOW(), NOW()),
    ('30000000-0000-4000-8000-000000000003', 'A Kodo Code oferece manutenção?', 'Sim. Podemos combinar suporte, correções e evolução contínua após a publicação, de acordo com a necessidade do projeto.', 3, TRUE, 'PUBLISHED', NOW(), NOW()),
    ('30000000-0000-4000-8000-000000000004', 'O cliente poderá solicitar alterações?', 'Sim. As etapas de design e desenvolvimento incluem validações. Mudanças dentro do escopo são organizadas durante o projeto; novas necessidades podem ser planejadas separadamente.', 4, TRUE, 'PUBLISHED', NOW(), NOW()),
    ('30000000-0000-4000-8000-000000000005', 'O site funciona em celulares?', 'Sim. As interfaces são desenvolvidas de forma responsiva e testadas em diferentes tamanhos de tela para oferecer uma experiência consistente.', 5, TRUE, 'PUBLISHED', NOW(), NOW()),
    ('30000000-0000-4000-8000-000000000006', 'A Kodo Code também desenvolve sistemas personalizados?', 'Sim. Desenvolvemos sistemas alinhados às regras e aos processos específicos da empresa, incluindo painéis, fluxos internos e integrações.', 6, TRUE, 'PUBLISHED', NOW(), NOW()),
    ('30000000-0000-4000-8000-000000000007', 'Como funciona a automação para WhatsApp?', 'Mapeamos as perguntas e etapas do atendimento para automatizar respostas, coletar informações e encaminhar cada contato de forma mais organizada.', 7, TRUE, 'PUBLISHED', NOW(), NOW()),
    ('30000000-0000-4000-8000-000000000008', 'Como o orçamento é calculado?', 'Consideramos escopo, regras de negócio, design, integrações, prazo e suporte esperado. Assim, a proposta reflete o trabalho necessário e evita custos pouco claros.', 8, TRUE, 'PUBLISHED', NOW(), NOW());
