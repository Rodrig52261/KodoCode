FROM node:24-alpine AS dependencies
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci

FROM node:24-alpine AS build
WORKDIR /app
ARG NEXT_PUBLIC_API_URL=
ENV NEXT_PUBLIC_API_URL=$NEXT_PUBLIC_API_URL
ARG API_INTERNAL_URL=http://localhost:8080
ENV API_INTERNAL_URL=$API_INTERNAL_URL
ARG NEXT_PUBLIC_SITE_URL=http://localhost:3000
ENV NEXT_PUBLIC_SITE_URL=$NEXT_PUBLIC_SITE_URL
COPY --from=dependencies /app/node_modules ./node_modules
COPY . .
RUN npm run build

FROM node:24-alpine AS runtime
WORKDIR /app
ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1
ARG NEXT_PUBLIC_API_URL=
ENV NEXT_PUBLIC_API_URL=$NEXT_PUBLIC_API_URL
ARG NEXT_PUBLIC_SITE_URL=http://localhost:3000
ENV NEXT_PUBLIC_SITE_URL=$NEXT_PUBLIC_SITE_URL
RUN apk upgrade --no-cache
RUN rm -rf /usr/local/lib/node_modules/npm
RUN rm -rf /usr/local/lib/node_modules/corepack
RUN rm -rf /opt/yarn-v1.22.22
RUN rm -f /usr/local/bin/npm /usr/local/bin/npx /usr/local/bin/corepack /usr/local/bin/yarn /usr/local/bin/yarnpkg
RUN addgroup -S nextjs && adduser -S nextjs -G nextjs
COPY --chown=nextjs:nextjs --from=build /app/.next/standalone ./
COPY --chown=nextjs:nextjs --from=build /app/.next/static ./.next/static
COPY --chown=nextjs:nextjs --from=build /app/public ./public
USER nextjs
EXPOSE 3000
CMD ["node", "server.js"]
