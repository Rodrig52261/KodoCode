ALTER TABLE admin_users
    ADD COLUMN credential_version INTEGER NOT NULL DEFAULT 0,
    ADD CONSTRAINT ck_admin_users_credential_version CHECK (credential_version >= 0);
