package com.kodocode.api.audit;

import jakarta.servlet.http.HttpServletRequest;

public record AuditContext(String ipAddress, String userAgent) {

    public static AuditContext from(HttpServletRequest request) {
        // Forwarded headers must only be interpreted by a separately configured,
        // trusted reverse proxy. Trusting them here lets clients spoof their IP
        // and bypass rate limits and audit attribution.
        return new AuditContext(clean(request.getRemoteAddr(), 64), clean(request.getHeader("User-Agent"), 500));
    }

    private static String clean(String value, int maxLength) {
        if (value == null) return null;
        String sanitized = value.replaceAll("[\\p{Cc}\\p{Cf}]", "").strip();
        return sanitized.length() <= maxLength ? sanitized : sanitized.substring(0, maxLength);
    }
}
