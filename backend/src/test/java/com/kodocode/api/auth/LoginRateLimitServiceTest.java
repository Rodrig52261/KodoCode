package com.kodocode.api.auth;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class LoginRateLimitServiceTest {

    @Test
    void blocksRequestsBeyondConfiguredWindowLimit() {
        LoginRateLimitService service = new LoginRateLimitService(
                2, Clock.fixed(Instant.parse("2026-07-24T12:00:00Z"), ZoneOffset.UTC));

        service.check("127.0.0.1", "admin@kodocode.com.br");
        service.check("127.0.0.1", "admin@kodocode.com.br");

        assertThatThrownBy(() -> service.check("127.0.0.1", "admin@kodocode.com.br"))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("Muitas tentativas");
    }

    @Test
    void blocksIpSprayAcrossDifferentAccounts() {
        LoginRateLimitService service = new LoginRateLimitService(
                2, Clock.fixed(Instant.parse("2026-07-24T12:00:00Z"), ZoneOffset.UTC));

        service.check("127.0.0.1", "first@example.com");
        service.check("127.0.0.1", "second@example.com");

        assertThatThrownBy(() -> service.check("127.0.0.1", "third@example.com"))
                .isInstanceOf(AuthException.class);
    }
}
