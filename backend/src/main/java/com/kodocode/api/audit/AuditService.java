package com.kodocode.api.audit;

import com.kodocode.api.admin.AdminUser;
import java.util.Map;
import java.util.LinkedHashMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private static final String REDACTED = "[REDACTED]";

    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(
            AdminUser user,
            String actorEmail,
            AuditAction action,
            String resourceType,
            String resourceId,
            Map<String, ?> previousData,
            Map<String, ?> newData,
            boolean success,
            AuditContext context
    ) {
        AuditLog log = new AuditLog();
        log.setAdminUser(user);
        log.setActorEmail(normalizeEmail(actorEmail));
        log.setAction(action);
        log.setResourceType(resourceType);
        log.setResourceId(resourceId);
        log.setPreviousData(sanitize(previousData));
        log.setNewData(sanitize(newData));
        log.setSuccess(success);
        if (context != null) {
            log.setIpAddress(context.ipAddress());
            log.setUserAgent(context.userAgent());
        }
        repository.save(log);
    }

    private Map<String, Object> sanitize(Map<String, ?> data) {
        if (data == null) return null;
        return redact(data);
    }

    private Map<String, Object> redact(Map<String, ?> data) {
        Map<String, Object> sanitized = new LinkedHashMap<>();
        data.forEach((key, value) -> {
            String normalizedKey = key.toLowerCase();
            if (normalizedKey.contains("password") || normalizedKey.contains("token")
                    || normalizedKey.contains("secret") || normalizedKey.contains("cookie")) {
                sanitized.put(key, REDACTED);
            } else {
                sanitized.put(key, sanitizeValue(value));
            }
        });
        return sanitized;
    }

    private Object sanitizeValue(Object value) {
        if (value instanceof Map<?, ?> nested) {
            Map<String, Object> stringKeyed = new LinkedHashMap<>();
            nested.forEach((nestedKey, nestedValue) -> stringKeyed.put(String.valueOf(nestedKey), nestedValue));
            return redact(stringKeyed);
        }
        if (value instanceof Iterable<?> values) {
            java.util.ArrayList<Object> sanitized = new java.util.ArrayList<>();
            values.forEach(item -> sanitized.add(sanitizeValue(item)));
            return sanitized;
        }
        return value;
    }

    private String normalizeEmail(String email) {
        if (email == null) return null;
        String normalized = email.strip().toLowerCase();
        return normalized.length() <= 254 ? normalized : normalized.substring(0, 254);
    }

    public Map<String, Object> result(String detail) {
        return Map.of("result", detail);
    }
}
