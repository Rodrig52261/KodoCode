ALTER TABLE contact_leads
    ADD COLUMN notification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    ADD COLUMN confirmation_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    ADD COLUMN email_last_error VARCHAR(1000),
    ADD CONSTRAINT ck_lead_notification_status CHECK (notification_status IN ('PENDING', 'SENT', 'FAILED', 'SKIPPED')),
    ADD CONSTRAINT ck_lead_confirmation_status CHECK (confirmation_status IN ('PENDING', 'SENT', 'FAILED', 'SKIPPED'));

CREATE INDEX idx_leads_email_created ON contact_leads (LOWER(email), created_at DESC);

ALTER TABLE faq_items
    ADD COLUMN draft_question VARCHAR(300),
    ADD COLUMN draft_answer VARCHAR(2000);

CREATE OR REPLACE FUNCTION prevent_audit_log_mutation()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'audit_logs is append-only';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_audit_logs_append_only
    BEFORE UPDATE OR DELETE ON audit_logs
    FOR EACH ROW EXECUTE FUNCTION prevent_audit_log_mutation();
