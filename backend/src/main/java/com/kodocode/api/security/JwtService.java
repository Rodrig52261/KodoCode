package com.kodocode.api.security;

import com.kodocode.api.admin.AdminUser;
import com.kodocode.api.config.ApplicationProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class JwtService {

    private final ApplicationProperties properties;
    private final SecretKey signingKey;
    private final Clock clock;

    @Autowired
    public JwtService(ApplicationProperties properties) {
        this(properties, Clock.systemUTC());
    }

    JwtService(ApplicationProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
        byte[] secret = properties.jwt().secret().getBytes(StandardCharsets.UTF_8);
        if (secret.length < 32) {
            throw new IllegalStateException("KODO_JWT_SECRET deve conter pelo menos 32 bytes.");
        }
        this.signingKey = Keys.hmacShaKeyFor(secret);
    }

    public IssuedAccessToken issue(AdminUser user) {
        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plus(properties.jwt().accessTokenTtl());
        String value = Jwts.builder()
                .issuer(properties.jwt().issuer())
                .subject(user.getId().toString())
                .claim("role", user.getRole().name())
                .claim("cv", user.getCredentialVersion())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
        return new IssuedAccessToken(value, expiresAt);
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(properties.jwt().issuer())
                .clock(() -> Date.from(clock.instant()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public record IssuedAccessToken(String value, Instant expiresAt) {}
}
