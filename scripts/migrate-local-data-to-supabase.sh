#!/usr/bin/env bash
set -Eeuo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
env_file="${1:-${project_root}/backend/.env}"
local_container="${KODO_LOCAL_POSTGRES_CONTAINER:-kodocode-postgres-1}"

if [[ ! -f "${env_file}" ]]; then
  echo "Arquivo de ambiente nao encontrado: ${env_file}" >&2
  exit 1
fi

read_env_last() {
  local requested_key="$1"
  local key value result=""
  while IFS='=' read -r key value || [[ -n "${key}" ]]; do
    if [[ "${key}" == "${requested_key}" ]]; then result="${value}"; fi
  done < "${env_file}"
  printf '%s' "${result}"
}

database_url="$(read_env_last DATABASE_URL)"
database_username="$(read_env_last DATABASE_USERNAME)"
database_password="$(read_env_last DATABASE_PASSWORD)"

if [[ -z "${database_url}" || -z "${database_username}" || -z "${database_password}" ]]; then
  echo "DATABASE_URL, DATABASE_USERNAME e DATABASE_PASSWORD sao obrigatorias." >&2
  exit 1
fi

# psql accepts postgresql://, while Spring uses jdbc:postgresql://. Remove any
# embedded credentials so the separately configured username/password prevail.
database_url="${database_url#jdbc:}"
if [[ "${database_url}" == *"://"*"@"* ]]; then
  database_scheme="${database_url%%://*}"
  database_url="${database_scheme}://${database_url#*@}"
fi

copy_from_local() {
  local copy_query="$1"
  docker exec "${local_container}" sh -c \
    'exec psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" --quiet --set=ON_ERROR_STOP=1 --command "$1"' \
    shell "${copy_query}"
}

local_counts="$({
  docker exec "${local_container}" sh -c \
    'exec psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" --tuples-only --no-align --quiet --set=ON_ERROR_STOP=1 --command "$1"' \
    shell "SELECT COUNT(*) || '|' ||
                  (SELECT COUNT(*) FROM contact_leads) || '|' ||
                  (SELECT COUNT(*) FROM faq_items) || '|' ||
                  (SELECT COUNT(*) FROM site_sections) || '|' ||
                  (SELECT COUNT(*) FROM site_content_versions)
           FROM admin_users;"
} | tr -d '[:space:]')"

{
  printf '%s\n' 'BEGIN;'
  printf '%s\n' 'TRUNCATE TABLE public.refresh_tokens, public.audit_logs, public.site_content_versions, public.site_sections, public.faq_items, public.contact_leads, public.admin_users CASCADE;'

  printf '%s\n' 'COPY public.admin_users (id, name, email, password_hash, role, active, failed_login_attempts, locked_until, last_login_at, created_at, updated_at, credential_version) FROM STDIN;'
  copy_from_local "COPY (SELECT id, name, email, password_hash, role, active, failed_login_attempts, locked_until, last_login_at, created_at, updated_at, credential_version FROM public.admin_users ORDER BY id) TO STDOUT;"
  printf '%s\n' '\.'

  printf '%s\n' 'COPY public.contact_leads (id, name, company, email, phone, service_interest, budget_range, message, status, internal_notes, privacy_consent, consent_date, source, created_at, updated_at, notification_status, confirmation_status, email_last_error) FROM STDIN;'
  copy_from_local "COPY (SELECT id, name, company, email, phone, service_interest, budget_range, message, status, internal_notes, privacy_consent, consent_date, source, created_at, updated_at, notification_status, confirmation_status, email_last_error FROM public.contact_leads ORDER BY id) TO STDOUT;"
  printf '%s\n' '\.'

  printf '%s\n' 'COPY public.faq_items (id, question, answer, display_order, active, status, created_at, updated_at, draft_question, draft_answer) FROM STDIN;'
  copy_from_local "COPY (SELECT id, question, answer, display_order, active, status, created_at, updated_at, draft_question, draft_answer FROM public.faq_items ORDER BY id) TO STDOUT;"
  printf '%s\n' '\.'

  printf '%s\n' 'COPY public.site_sections (id, section_key, title, subtitle, status, published_version_id, created_at, updated_at) FROM STDIN;'
  copy_from_local "COPY (SELECT id, section_key, title, subtitle, status, NULL::uuid, created_at, updated_at FROM public.site_sections ORDER BY id) TO STDOUT;"
  printf '%s\n' '\.'

  printf '%s\n' 'COPY public.site_content_versions (id, site_section_id, content_data, version_number, status, created_by, created_at, published_at) FROM STDIN;'
  copy_from_local "COPY (SELECT id, site_section_id, content_data, version_number, status, created_by, created_at, published_at FROM public.site_content_versions ORDER BY site_section_id, version_number) TO STDOUT;"
  printf '%s\n' '\.'

  printf '%s\n' 'CREATE TEMP TABLE migrated_published_versions (section_id uuid PRIMARY KEY, published_version_id uuid NOT NULL) ON COMMIT DROP;'
  printf '%s\n' 'COPY migrated_published_versions (section_id, published_version_id) FROM STDIN;'
  copy_from_local "COPY (SELECT id, published_version_id FROM public.site_sections WHERE published_version_id IS NOT NULL ORDER BY id) TO STDOUT;"
  printf '%s\n' '\.'
  printf '%s\n' 'UPDATE public.site_sections AS section SET published_version_id = mapping.published_version_id FROM migrated_published_versions AS mapping WHERE section.id = mapping.section_id;'
  printf '%s\n' 'COMMIT;'
} | PGPASSWORD="${database_password}" PGCONNECT_TIMEOUT=15 PGSSLMODE=require \
  psql "${database_url}" --username "${database_username}" --no-password --quiet --set=ON_ERROR_STOP=1

target_counts="$(PGPASSWORD="${database_password}" PGCONNECT_TIMEOUT=15 PGSSLMODE=require \
  psql "${database_url}" --username "${database_username}" --no-password --tuples-only --no-align --quiet --set=ON_ERROR_STOP=1 \
  --command "SELECT COUNT(*) || '|' ||
                    (SELECT COUNT(*) FROM contact_leads) || '|' ||
                    (SELECT COUNT(*) FROM faq_items) || '|' ||
                    (SELECT COUNT(*) FROM site_sections) || '|' ||
                    (SELECT COUNT(*) FROM site_content_versions) || '|' ||
                    (SELECT COUNT(*) FROM audit_logs) || '|' ||
                    (SELECT COUNT(*) FROM refresh_tokens)
             FROM admin_users;" | tr -d '[:space:]')"

if [[ "${target_counts}" != "${local_counts}|0|0" ]]; then
  echo "Falha na verificacao das contagens apos a migracao." >&2
  echo "Origem esperada (admin|leads|faqs|secoes|versoes): ${local_counts}" >&2
  echo "Destino obtido (inclui auditoria|sessoes): ${target_counts}" >&2
  exit 1
fi

echo "Migracao concluida: ${target_counts} (admin|leads|faqs|secoes|versoes|auditoria|sessoes)"
