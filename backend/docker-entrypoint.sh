#!/bin/sh
set -eu

# Providers such as Supabase expose a libpq URL. Spring/Hikari expects JDBC and
# does not support URI userinfo. Normalize the scheme and remove embedded
# credentials so DATABASE_USERNAME/DATABASE_PASSWORD are always authoritative.
database_url="${DATABASE_URL:-}"
database_url="${database_url#jdbc:}"

case "${database_url}" in
  *://*@*)
    database_scheme="${database_url%%://*}"
    database_url="${database_scheme}://${database_url#*@}"
    ;;
esac

case "${database_url}" in
  postgresql://*) export DATABASE_URL="jdbc:${database_url}" ;;
  postgres://*) export DATABASE_URL="jdbc:postgresql://${database_url#postgres://}" ;;
  jdbc:postgresql://*) export DATABASE_URL="${database_url}" ;;
  *) export DATABASE_URL="${DATABASE_URL:-}" ;;
esac

unset database_url database_scheme

exec java -jar /app/app.jar "$@"
