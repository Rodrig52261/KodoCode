CREATE TABLE admin_users (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(30) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    failed_login_attempts INTEGER NOT NULL DEFAULT 0 CHECK (failed_login_attempts >= 0),
    locked_until TIMESTAMPTZ,
    last_login_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_admin_users_email UNIQUE (email),
    CONSTRAINT ck_admin_users_role CHECK (role IN ('ADMIN'))
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    admin_user_id UUID NOT NULL REFERENCES admin_users(id),
    token_hash CHAR(64) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_refresh_tokens_hash UNIQUE (token_hash)
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(admin_user_id);
CREATE INDEX idx_refresh_tokens_expiry ON refresh_tokens(expires_at) WHERE revoked_at IS NULL;

CREATE TABLE site_sections (
    id UUID PRIMARY KEY,
    section_key VARCHAR(80) NOT NULL,
    title VARCHAR(180),
    subtitle VARCHAR(500),
    status VARCHAR(30) NOT NULL,
    published_version_id UUID,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_site_sections_key UNIQUE (section_key),
    CONSTRAINT ck_site_sections_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
);

CREATE TABLE site_content_versions (
    id UUID PRIMARY KEY,
    site_section_id UUID NOT NULL REFERENCES site_sections(id),
    content_data JSONB NOT NULL,
    version_number INTEGER NOT NULL CHECK (version_number > 0),
    status VARCHAR(30) NOT NULL,
    created_by UUID NOT NULL REFERENCES admin_users(id),
    created_at TIMESTAMPTZ NOT NULL,
    published_at TIMESTAMPTZ,
    CONSTRAINT uq_content_version UNIQUE (site_section_id, version_number),
    CONSTRAINT ck_content_version_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
);

ALTER TABLE site_sections
    ADD CONSTRAINT fk_site_sections_published_version
    FOREIGN KEY (published_version_id) REFERENCES site_content_versions(id);

CREATE INDEX idx_content_versions_section_created ON site_content_versions(site_section_id, created_at DESC);

CREATE TABLE contact_leads (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    company VARCHAR(160),
    email VARCHAR(254) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    service_interest VARCHAR(50) NOT NULL,
    budget_range VARCHAR(50) NOT NULL,
    message VARCHAR(3000) NOT NULL,
    status VARCHAR(30) NOT NULL,
    internal_notes VARCHAR(4000),
    privacy_consent BOOLEAN NOT NULL,
    consent_date TIMESTAMPTZ NOT NULL,
    source VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_lead_status CHECK (status IN ('NEW', 'VIEWED', 'IN_PROGRESS', 'PROPOSAL_SENT', 'CONVERTED', 'ARCHIVED')),
    CONSTRAINT ck_service_interest CHECK (service_interest IN ('LANDING_PAGE', 'INSTITUTIONAL_SITE', 'CRM', 'WHATSAPP_CHATBOT', 'CUSTOM_SYSTEM', 'UNDECIDED')),
    CONSTRAINT ck_budget_range CHECK (budget_range IN ('UP_TO_2000', 'FROM_2000_TO_5000', 'FROM_5000_TO_10000', 'ABOVE_10000', 'DISCUSS_FIRST')),
    CONSTRAINT ck_privacy_consent CHECK (privacy_consent = TRUE)
);

CREATE INDEX idx_leads_created_at ON contact_leads(created_at DESC);
CREATE INDEX idx_leads_status_service ON contact_leads(status, service_interest);
CREATE INDEX idx_leads_email_lower ON contact_leads(LOWER(email));

CREATE TABLE faq_items (
    id UUID PRIMARY KEY,
    question VARCHAR(300) NOT NULL,
    answer VARCHAR(2000) NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0 CHECK (display_order >= 0),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_faq_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
);

CREATE INDEX idx_faq_public_order ON faq_items(status, active, display_order);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    admin_user_id UUID REFERENCES admin_users(id),
    actor_email VARCHAR(254),
    action VARCHAR(60) NOT NULL,
    resource_type VARCHAR(80) NOT NULL,
    resource_id VARCHAR(100),
    previous_data JSONB,
    new_data JSONB,
    ip_address VARCHAR(64),
    user_agent VARCHAR(500),
    success BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_audit_created_at ON audit_logs(created_at DESC);
CREATE INDEX idx_audit_admin_action ON audit_logs(admin_user_id, action);
CREATE INDEX idx_audit_resource ON audit_logs(resource_type, resource_id);

