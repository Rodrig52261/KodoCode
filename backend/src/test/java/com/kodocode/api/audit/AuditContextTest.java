package com.kodocode.api.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

class AuditContextTest {

    @Test
    void ignoresUntrustedForwardedAddressAndRemovesControlCharacters() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("203.0.113.10");
        when(request.getHeader("X-Forwarded-For")).thenReturn("198.51.100.99");
        when(request.getHeader("User-Agent")).thenReturn("browser\r\nforged-log");

        AuditContext context = AuditContext.from(request);

        assertThat(context.ipAddress()).isEqualTo("203.0.113.10");
        assertThat(context.userAgent()).isEqualTo("browserforged-log");
    }
}
