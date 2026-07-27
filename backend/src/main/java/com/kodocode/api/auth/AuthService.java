package com.kodocode.api.auth;

import com.kodocode.api.admin.AdminUser;
import com.kodocode.api.admin.AdminUserRepository;
import com.kodocode.api.audit.AuditAction;
import com.kodocode.api.audit.AuditContext;
import com.kodocode.api.audit.AuditService;
import com.kodocode.api.security.AdminPrincipal;
import com.kodocode.api.security.JwtService;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final CredentialService credentialService;
    private final LoginRateLimitService rateLimitService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final AuditService auditService;
    private final AdminUserRepository adminUserRepository;

    public AuthService(
            CredentialService credentialService,
            LoginRateLimitService rateLimitService,
            RefreshTokenService refreshTokenService,
            JwtService jwtService,
            AuditService auditService,
            AdminUserRepository adminUserRepository
    ) {
        this.credentialService = credentialService;
        this.rateLimitService = rateLimitService;
        this.refreshTokenService = refreshTokenService;
        this.jwtService = jwtService;
        this.auditService = auditService;
        this.adminUserRepository = adminUserRepository;
    }

    public AuthenticatedSession login(LoginRequest request, AuditContext context) {
        String email = normalizeEmail(request.email());
        try {
            rateLimitService.check(context == null ? "unknown" : context.ipAddress(), email);
        } catch (AuthException exception) {
            auditService.record(null, email, AuditAction.LOGIN_FAILURE, "AdminUser", null,
                    null, Map.of("reason", exception.getStatus().name()), false, context);
            throw exception;
        }

        AdminUser user;
        try {
            user = credentialService.authenticate(email, request.password());
        } catch (AuthException exception) {
            auditService.record(null, email, AuditAction.LOGIN_FAILURE, "AdminUser", null,
                    null, Map.of("reason", exception.getStatus().name()), false, context);
            throw exception;
        }

        JwtService.IssuedAccessToken accessToken = jwtService.issue(user);
        RefreshTokenService.IssuedRefreshToken refreshToken = refreshTokenService.issue(user);
        auditService.record(user, user.getEmail(), AuditAction.LOGIN, "AdminUser", user.getId().toString(),
                null, Map.of("result", "success"), true, context);
        return session(user, accessToken, refreshToken);
    }

    public AuthenticatedSession refresh(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new AuthException("Sessao expirada. Entre novamente.", HttpStatus.UNAUTHORIZED);
        }
        RefreshTokenService.RotatedRefreshToken rotated = refreshTokenService.rotate(rawRefreshToken);
        JwtService.IssuedAccessToken accessToken = jwtService.issue(rotated.user());
        return session(rotated.user(), accessToken, rotated.token());
    }

    public void logout(String rawRefreshToken, AdminPrincipal principal, AuditContext context) {
        refreshTokenService.revoke(rawRefreshToken);
        AdminUser user = principal == null ? null : adminUserRepository.findById(principal.id()).orElse(null);
        auditService.record(user, principal == null ? null : principal.email(), AuditAction.LOGOUT,
                "AdminUser", principal == null ? null : principal.id().toString(), null,
                Map.of("result", "session_revoked"), true, context);
    }

    public void changePassword(AdminPrincipal principal, ChangePasswordRequest request, AuditContext context) {
        AdminUser reference = adminUserRepository.findById(principal.id())
                .orElseThrow(() -> new AuthException("Sessao invalida.", HttpStatus.UNAUTHORIZED));
        try {
            AdminUser user = credentialService.changePassword(reference, request.currentPassword(), request.newPassword());
            refreshTokenService.revokeAll(user);
            auditService.record(user, user.getEmail(), AuditAction.PASSWORD_CHANGE, "AdminUser", user.getId().toString(),
                    null, Map.of("result", "password_changed_sessions_revoked"), true, context);
        } catch (AuthException exception) {
            auditService.record(reference, reference.getEmail(), AuditAction.PASSWORD_CHANGE, "AdminUser",
                    reference.getId().toString(), null, Map.of("reason", exception.getStatus().name()), false, context);
            throw exception;
        }
    }

    private AuthenticatedSession session(
            AdminUser user,
            JwtService.IssuedAccessToken accessToken,
            RefreshTokenService.IssuedRefreshToken refreshToken
    ) {
        AuthResponse response = new AuthResponse(
                user.getId(), user.getName(), user.getEmail(), user.getRole(), accessToken.expiresAt());
        return new AuthenticatedSession(response, accessToken, refreshToken);
    }

    private String normalizeEmail(String email) {
        return email.strip().toLowerCase();
    }

    public record AuthenticatedSession(
            AuthResponse response,
            JwtService.IssuedAccessToken accessToken,
            RefreshTokenService.IssuedRefreshToken refreshToken
    ) {}
}
