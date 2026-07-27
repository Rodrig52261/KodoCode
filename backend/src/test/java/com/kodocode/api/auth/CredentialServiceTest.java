package com.kodocode.api.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kodocode.api.admin.AdminUser;
import com.kodocode.api.admin.AdminUserRepository;
import com.kodocode.api.support.TestProperties;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class CredentialServiceTest {

    private static final Instant NOW = Instant.parse("2026-07-24T12:00:00Z");

    @Mock AdminUserRepository repository;
    @Mock PasswordEncoder passwordEncoder;
    private CredentialService service;
    private AdminUser user;

    @BeforeEach
    void setUp() {
        service = new CredentialService(
                repository, passwordEncoder, TestProperties.create(), Clock.fixed(NOW, ZoneOffset.UTC));
        user = new AdminUser();
        user.setId(UUID.randomUUID());
        user.setName("Admin");
        user.setEmail("admin@kodocode.com.br");
        user.setPasswordHash("hash");
        user.setActive(true);
    }

    @Test
    void authenticatesAndClearsPreviousFailures() {
        user.setFailedLoginAttempts(2);
        when(repository.findByEmailForUpdate(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct", "hash")).thenReturn(true);
        when(repository.save(user)).thenReturn(user);

        AdminUser result = service.authenticate(user.getEmail(), "correct");

        assertThat(result).isSameAs(user);
        assertThat(user.getFailedLoginAttempts()).isZero();
        assertThat(user.getLastLoginAt()).isEqualTo(NOW);
        verify(repository).save(user);
    }

    @Test
    void locksAccountWhenFailureThresholdIsReached() {
        user.setFailedLoginAttempts(2);
        when(repository.findByEmailForUpdate(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        assertThatThrownBy(() -> service.authenticate(user.getEmail(), "wrong"))
                .isInstanceOf(AuthException.class)
                .hasMessage("E-mail ou senha invalidos.");

        assertThat(user.getFailedLoginAttempts()).isZero();
        assertThat(user.getLockedUntil()).isEqualTo(NOW.plusSeconds(900));
        verify(repository).save(user);
    }

    @Test
    void changesPasswordOnlyWhenCurrentPasswordMatches() {
        when(repository.findByEmailForUpdate(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Current123456", "hash")).thenReturn(true);
        when(passwordEncoder.encode("NewPassword123")).thenReturn("new-hash");
        when(repository.save(any(AdminUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.changePassword(user, "Current123456", "NewPassword123");

        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        assertThat(user.getCredentialVersion()).isEqualTo(1);
    }

    @Test
    void performsDummyHashCheckForUnknownEmail() {
        when(repository.findByEmailForUpdate("unknown@kodocode.com.br")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.authenticate("unknown@kodocode.com.br", "wrong"))
                .isInstanceOf(AuthException.class);

        verify(passwordEncoder).matches(org.mockito.ArgumentMatchers.eq("wrong"), any(String.class));
    }
}
