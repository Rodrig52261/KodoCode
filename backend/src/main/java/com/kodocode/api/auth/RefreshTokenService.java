package com.kodocode.api.auth;

import com.kodocode.api.admin.AdminUser;
import com.kodocode.api.config.ApplicationProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final ApplicationProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Clock clock;

    @Autowired
    public RefreshTokenService(RefreshTokenRepository repository, ApplicationProperties properties) {
        this(repository, properties, Clock.systemUTC());
    }

    RefreshTokenService(RefreshTokenRepository repository, ApplicationProperties properties, Clock clock) {
        this.repository = repository;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional
    public IssuedRefreshToken issue(AdminUser user) {
        return create(user);
    }

    @Transactional(noRollbackFor = AuthException.class)
    public RotatedRefreshToken rotate(String rawToken) {
        Instant now = clock.instant();
        RefreshToken existing = repository.findByTokenHashForUpdate(hash(rawToken))
                .orElseThrow(this::invalidRefreshToken);

        if (!existing.isUsableAt(now) || !existing.getAdminUser().isActive()) {
            if (existing.getRevokedAt() != null) {
                repository.revokeAllForUser(existing.getAdminUser().getId(), now);
            }
            throw invalidRefreshToken();
        }

        existing.setRevokedAt(now);
        IssuedRefreshToken replacement = create(existing.getAdminUser());
        return new RotatedRefreshToken(existing.getAdminUser(), replacement);
    }

    @Transactional
    public void revoke(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) return;
        repository.findByTokenHashForUpdate(hash(rawToken)).ifPresent(token -> {
            if (token.getRevokedAt() == null) token.setRevokedAt(clock.instant());
        });
    }

    @Transactional
    public void revokeAll(AdminUser user) {
        repository.revokeAllForUser(user.getId(), clock.instant());
    }

    private IssuedRefreshToken create(AdminUser user) {
        byte[] bytes = new byte[64];
        secureRandom.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Instant expiresAt = clock.instant().plus(properties.jwt().refreshTokenTtl());

        RefreshToken entity = new RefreshToken();
        entity.setAdminUser(user);
        entity.setTokenHash(hash(raw));
        entity.setExpiresAt(expiresAt);
        repository.save(entity);
        return new IssuedRefreshToken(raw, expiresAt);
    }

    String hash(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 indisponivel.", exception);
        }
    }

    private AuthException invalidRefreshToken() {
        return new AuthException("Sessao expirada. Entre novamente.", HttpStatus.UNAUTHORIZED);
    }

    public record IssuedRefreshToken(String value, Instant expiresAt) {}
    public record RotatedRefreshToken(AdminUser user, IssuedRefreshToken token) {}
}
