FROM postgres:17-alpine AS patched

# The upstream image bundles gosu as a Go binary. Replace it with Alpine's
# small, C-based su-exec implementation to avoid carrying vulnerable Go stdlib
# code in the database runtime. su-exec is command-line compatible with the
# usage in the official PostgreSQL entrypoint.
RUN apk upgrade --no-cache \
    && apk add --no-cache su-exec \
    && rm -f /usr/local/bin/gosu \
    && ln -s /sbin/su-exec /usr/local/bin/gosu

# Copy the resulting filesystem into a fresh layer so the removed vulnerable
# binary is not retained in the final image history.
FROM scratch

COPY --from=patched / /

ENV PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin \
    GOSU_VERSION=su-exec-0.3 \
    LANG=en_US.utf8 \
    PG_MAJOR=17 \
    PG_VERSION=17.10 \
    PGDATA=/var/lib/postgresql/data

VOLUME /var/lib/postgresql/data
EXPOSE 5432
ENTRYPOINT ["docker-entrypoint.sh"]
STOPSIGNAL SIGINT
CMD ["postgres"]
