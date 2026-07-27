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
USER kodocode
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
