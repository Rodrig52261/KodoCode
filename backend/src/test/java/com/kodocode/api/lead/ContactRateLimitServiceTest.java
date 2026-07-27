package com.kodocode.api.lead;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kodocode.api.shared.web.BusinessException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class ContactRateLimitServiceTest {

    @Test
    void blocksSingleIpUsingDifferentEmailAddresses() {
        ContactRateLimitService service = new ContactRateLimitService(
                2, Clock.fixed(Instant.parse("2026-07-27T12:00:00Z"), ZoneOffset.UTC));

        service.check("203.0.113.10", "first@example.com");
        service.check("203.0.113.10", "second@example.com");

        assertThatThrownBy(() -> service.check("203.0.113.10", "third@example.com"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Muitas mensagens");
    }
}
