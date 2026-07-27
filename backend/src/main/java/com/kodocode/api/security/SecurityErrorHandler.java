package com.kodocode.api.security;

import com.kodocode.api.audit.AuditAction;
import com.kodocode.api.audit.AuditContext;
import com.kodocode.api.audit.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class SecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(SecurityErrorHandler.class);
    private final AuditService auditService;

    public SecurityErrorHandler(AuditService auditService) {
        this.auditService = auditService;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {
        audit(request, null, false);
        write(response, HttpServletResponse.SC_UNAUTHORIZED, "Autenticacao necessaria.");
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception
    ) throws IOException {
        audit(request, request.getUserPrincipal() instanceof Authentication authentication ? authentication : null, false);
        write(response, HttpServletResponse.SC_FORBIDDEN, "Acesso nao autorizado.");
    }

    private void audit(HttpServletRequest request, Authentication authentication, boolean success) {
        try {
            AdminPrincipal principal = authentication != null && authentication.getPrincipal() instanceof AdminPrincipal value
                    ? value : null;
            auditService.record(null, principal == null ? null : principal.email(), AuditAction.UNAUTHORIZED_ACCESS,
                    "HttpRequest", request.getRequestURI(), null,
                    Map.of("method", request.getMethod()), success, AuditContext.from(request));
        } catch (RuntimeException auditFailure) {
            log.error("Could not persist unauthorized access audit", auditFailure);
        }
    }

    private void write(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"timestamp\":\"" + Instant.now() + "\",\"status\":" + status
                + ",\"message\":\"" + message + "\"}");
    }
}

