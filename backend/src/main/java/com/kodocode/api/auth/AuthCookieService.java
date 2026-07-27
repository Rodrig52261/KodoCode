package com.kodocode.api.auth;

import com.kodocode.api.config.ApplicationProperties;
import com.kodocode.api.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class AuthCookieService {

    public static final String ACCESS_COOKIE = "kodo_access_token";
    public static final String REFRESH_COOKIE = "kodo_refresh_token";

    private final ApplicationProperties properties;

    public AuthCookieService(ApplicationProperties properties) {
        this.properties = properties;
    }

    public void writeSession(
            HttpServletResponse response,
            JwtService.IssuedAccessToken accessToken,
            RefreshTokenService.IssuedRefreshToken refreshToken
    ) {
        add(response, cookie(ACCESS_COOKIE, accessToken.value(), "/", properties.jwt().accessTokenTtl()).build());
        add(response, cookie(REFRESH_COOKIE, refreshToken.value(), "/api/v1/auth", properties.jwt().refreshTokenTtl()).build());
    }

    public void clearSession(HttpServletResponse response) {
        add(response, cookie(ACCESS_COOKIE, "", "/", Duration.ZERO).build());
        add(response, cookie(REFRESH_COOKIE, "", "/api/v1/auth", Duration.ZERO).build());
    }

    private ResponseCookie.ResponseCookieBuilder cookie(String name, String value, String path, Duration maxAge) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(properties.cookie().secure())
                .sameSite(properties.cookie().sameSite())
                .path(path)
                .maxAge(maxAge);
        if (properties.cookie().domain() != null && !properties.cookie().domain().isBlank()) {
            builder.domain(properties.cookie().domain());
        }
        return builder;
    }

    private void add(HttpServletResponse response, ResponseCookie cookie) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}

