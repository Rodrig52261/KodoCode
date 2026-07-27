package com.kodocode.api.audit;

import com.kodocode.api.shared.web.BusinessException;
import com.kodocode.api.shared.web.PageResponse;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminAuditQueryService {
    private final AuditLogRepository repository;
    public AdminAuditQueryService(AuditLogRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public PageResponse<AuditResponse> list(int page, int size, UUID userId, AuditAction action,
            String resource, Boolean success, Instant from, Instant to) {
        Specification<AuditLog> specification = (root, query, builder) -> {
            List<Predicate> filters = new ArrayList<>();
            if (userId != null) filters.add(builder.equal(root.get("adminUser").get("id"), userId));
            if (action != null) filters.add(builder.equal(root.get("action"), action));
            if (resource != null && !resource.isBlank()) filters.add(builder.equal(root.get("resourceType"), resource.strip()));
            if (success != null) filters.add(builder.equal(root.get("success"), success));
            if (from != null) filters.add(builder.greaterThanOrEqualTo(root.get("createdAt"), from));
            if (to != null) filters.add(builder.lessThanOrEqualTo(root.get("createdAt"), to));
            return builder.and(filters.toArray(Predicate[]::new));
        };
        var result = repository.findAll(specification, PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100),
                Sort.by(Sort.Direction.DESC, "createdAt"))).map(this::response);
        return PageResponse.from(result);
    }

    @Transactional(readOnly = true)
    public AuditResponse get(UUID id) {
        return repository.findById(id).map(this::response)
                .orElseThrow(() -> new BusinessException("Registro de auditoria nao encontrado.", HttpStatus.NOT_FOUND));
    }

    public AuditResponse response(AuditLog log) {
        return new AuditResponse(log.getId(), log.getAdminUser() == null ? null : log.getAdminUser().getId(),
                log.getActorEmail(), log.getAction(), log.getResourceType(), log.getResourceId(), log.getPreviousData(),
                log.getNewData(), log.getIpAddress(), log.getUserAgent(), log.isSuccess(), log.getCreatedAt());
    }

    public record AuditResponse(UUID id, UUID adminUserId, String actorEmail, AuditAction action, String resourceType,
            String resourceId, java.util.Map<String, Object> previousData, java.util.Map<String, Object> newData,
            String ipAddress, String userAgent, boolean success, Instant createdAt) {}
}
