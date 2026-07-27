package com.kodocode.api.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kodocode.api.admin.AdminRole;
import com.kodocode.api.admin.AdminUser;
import com.kodocode.api.support.TestProperties;
import io.jsonwebtoken.JwtException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    @Test
    void issuesAndValidatesSignedAccessToken() {
        Instant now = Instant.parse("2026-07-24T12:00:00Z");
        JwtService service = new JwtService(TestProperties.create(), Clock.fixed(now, ZoneOffset.UTC));
        AdminUser user = new AdminUser();
        user.setId(UUID.randomUUID());
        user.setRole(AdminRole.ADMIN);
        user.setCredentialVersion(3);

        JwtService.IssuedAccessToken issued = service.issue(user);

        assertThat(service.parse(issued.value()).getSubject()).isEqualTo(user.getId().toString());
        assertThat(service.parse(issued.value()).get("cv", Integer.class)).isEqualTo(3);
        assertThat(issued.expiresAt()).isEqualTo(now.plusSeconds(900));
    }

    @Test
    void rejectsTamperedToken() {
        JwtService service = new JwtService(TestProperties.create());
        AdminUser user = new AdminUser();
        user.setId(UUID.randomUUID());
        user.setRole(AdminRole.ADMIN);
        String token = service.issue(user).value();

        assertThatThrownBy(() -> service.parse(token + "tampered")).isInstanceOf(JwtException.class);
    }
}
