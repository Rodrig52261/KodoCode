package com.kodocode.api.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kodocode.api.admin.AdminUser;
import com.kodocode.api.support.TestProperties;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    private static final Instant NOW = Instant.parse("2026-07-24T12:00:00Z");

    @Mock RefreshTokenRepository repository;
    private RefreshTokenService service;
    private AdminUser user;

    @BeforeEach
    void setUp() {
        service = new RefreshTokenService(
                repository, TestProperties.create(), Clock.fixed(NOW, ZoneOffset.UTC));
        user = new AdminUser();
        user.setId(UUID.randomUUID());
        user.setActive(true);
    }

    @Test
    void storesOnlyHashWhenIssuingToken() {
        RefreshTokenService.IssuedRefreshToken issued = service.issue(user);
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);

        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getTokenHash()).hasSize(64).isNotEqualTo(issued.value());
        assertThat(captor.getValue().getExpiresAt()).isEqualTo(NOW.plusSeconds(604800));
    }

    @Test
    void rotatesUsableTokenAndRevokesPreviousOne() {
        String rawToken = "existing-refresh-token";
        RefreshToken existing = new RefreshToken();
        existing.setAdminUser(user);
        existing.setExpiresAt(NOW.plusSeconds(3600));
        when(repository.findByTokenHashForUpdate(service.hash(rawToken))).thenReturn(Optional.of(existing));

        RefreshTokenService.RotatedRefreshToken rotated = service.rotate(rawToken);

        assertThat(existing.getRevokedAt()).isEqualTo(NOW);
        assertThat(rotated.user()).isSameAs(user);
        assertThat(rotated.token().value()).isNotBlank();
        verify(repository).save(any(RefreshToken.class));
    }

    @Test
    void rejectsExpiredToken() {
        String rawToken = "expired-refresh-token";
        RefreshToken existing = new RefreshToken();
        existing.setAdminUser(user);
        existing.setExpiresAt(NOW.minusSeconds(1));
        when(repository.findByTokenHashForUpdate(service.hash(rawToken))).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.rotate(rawToken))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("Sessao expirada");
    }
}

