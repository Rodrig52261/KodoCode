FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B verify

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN apk upgrade --no-cache
RUN addgroup -S kodocode && adduser -S kodocode -G kodocode
COPY --from=build /app/target/kodocode-api-*.jar app.jar
COPY --chmod=755 docker-entrypoint.sh /usr/local/bin/kodocode-entrypoint
USER kodocode
EXPOSE 8080
ENTRYPOINT ["kodocode-entrypoint"]
