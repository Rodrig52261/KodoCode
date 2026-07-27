package com.kodocode.api.auth;

import com.kodocode.api.audit.AuditContext;
import com.kodocode.api.security.AdminPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthCookieService cookieService;

    public AuthController(AuthService authService, AuthCookieService cookieService) {
        this.authService = authService;
        this.cookieService = cookieService;
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("headerName", token.getHeaderName());
    }

    @PostMapping("/login")
    public AuthResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        AuthService.AuthenticatedSession session = authService.login(request, AuditContext.from(httpRequest));
        cookieService.writeSession(response, session.accessToken(), session.refreshToken());
        return session.response();
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(
            @CookieValue(name = AuthCookieService.REFRESH_COOKIE, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        AuthService.AuthenticatedSession session = authService.refresh(refreshToken);
        cookieService.writeSession(response, session.accessToken(), session.refreshToken());
        return session.response();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = AuthCookieService.REFRESH_COOKIE, required = false) String refreshToken,
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        authService.logout(refreshToken, principal(authentication), AuditContext.from(request));
        cookieService.clearSession(response);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        authService.changePassword(requiredPrincipal(authentication), request, AuditContext.from(httpRequest));
        cookieService.clearSession(response);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public AdminPrincipal me(Authentication authentication) {
        return requiredPrincipal(authentication);
    }

    private AdminPrincipal requiredPrincipal(Authentication authentication) {
        AdminPrincipal principal = principal(authentication);
        if (principal == null) throw new AuthException("Autenticacao necessaria.", org.springframework.http.HttpStatus.UNAUTHORIZED);
        return principal;
    }

    private AdminPrincipal principal(Authentication authentication) {
        return authentication != null && authentication.getPrincipal() instanceof AdminPrincipal principal
                ? principal
                : null;
    }
}

