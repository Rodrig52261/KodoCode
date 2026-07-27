UPDATE site_content_versions version
SET content_data = version.content_data ||
    '{"visual":{"eyebrow":"Fluxo inteligente","steps":[{"title":"Atendimento","description":"Contato recebido e organizado"},{"title":"Processo","description":"Informações em um só lugar"},{"title":"Automação","description":"Próxima ação sem retrabalho"}],"status":"Processo conectado"}}'::jsonb
FROM site_sections section
WHERE version.site_section_id = section.id
  AND section.section_key = 'hero'
  AND NOT version.content_data ? 'visual';

UPDATE site_content_versions version
SET content_data = version.content_data ||
    '{"missionLabel":"Missão","visionLabel":"Visão","valuesLabel":"Valores"}'::jsonb
FROM site_sections section
WHERE version.site_section_id = section.id
  AND section.section_key = 'about';

UPDATE site_content_versions version
SET content_data = version.content_data ||
    '{"emailPrompt":"Prefere e-mail?"}'::jsonb
FROM site_sections section
WHERE version.site_section_id = section.id
  AND section.section_key = 'contact';

UPDATE site_content_versions version
SET content_data = version.content_data ||
    '{"servicesTitle":"Soluções","institutionalTitle":"Institucional","closingText":"Soluções digitais construídas com clareza."}'::jsonb
FROM site_sections section
WHERE version.site_section_id = section.id
  AND section.section_key = 'footer';

INSERT INTO site_sections (id, section_key, title, subtitle, status, created_at, updated_at)
VALUES ('10000000-0000-4000-8000-00000000000c', 'faq', 'Perguntas frequentes', 'Introdução da seção de dúvidas', 'PUBLISHED', NOW(), NOW());

INSERT INTO site_content_versions
    (id, site_section_id, content_data, version_number, status, created_at, published_at)
VALUES (
    '20000000-0000-4000-8000-00000000000c',
    '10000000-0000-4000-8000-00000000000c',
    '{"eyebrow":"Perguntas frequentes","title":"Antes de começar, tire suas dúvidas","description":"Respostas objetivas para ajudar você a entender como um projeto é planejado e desenvolvido."}'::jsonb,
    1, 'PUBLISHED', NOW(), NOW()
);

UPDATE site_sections
SET published_version_id = '20000000-0000-4000-8000-00000000000c'
WHERE id = '10000000-0000-4000-8000-00000000000c';
