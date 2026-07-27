package com.kodocode.api.config;

import java.time.Duration;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

@Component
public class SecurityConfigurationValidator implements InitializingBean {

    private final ApplicationProperties properties;
    private final Environment environment;

    public SecurityConfigurationValidator(ApplicationProperties properties, Environment environment) {
        this.properties = properties;
        this.environment = environment;
    }

    @Override
    public void afterPropertiesSet() {
        ApplicationProperties.Jwt jwt = properties.jwt();
        if (jwt.secret().chars().distinct().count() < 12) {
            throw new IllegalStateException("KODO_JWT_SECRET deve ser aleatorio e ter diversidade suficiente.");
        }
        requireRange(jwt.accessTokenTtl(), Duration.ofMinutes(1), Duration.ofHours(1), "access token");
        requireRange(jwt.refreshTokenTtl(), Duration.ofHours(1), Duration.ofDays(30), "refresh token");
        if ("none".equalsIgnoreCase(properties.cookie().sameSite()) && !properties.cookie().secure()) {
            throw new IllegalStateException("Cookies SameSite=None exigem Secure=true.");
        }
        if (environment.acceptsProfiles(Profiles.of("prod"))) {
            if (!properties.cookie().secure()) {
                throw new IllegalStateException("O perfil prod exige cookies Secure.");
            }
            if (properties.cors().allowedOrigins().stream().anyMatch(origin -> !origin.startsWith("https://"))) {
                throw new IllegalStateException("O perfil prod aceita apenas origens CORS HTTPS.");
            }
        }
    }

    private void requireRange(Duration value, Duration minimum, Duration maximum, String name) {
        if (value.compareTo(minimum) < 0 || value.compareTo(maximum) > 0) {
            throw new IllegalStateException("Duracao invalida para " + name + ".");
        }
    }
}
