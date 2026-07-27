package com.kodocode.api.audit;

import com.kodocode.api.shared.web.PageResponse;
import java.time.Instant;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
public class AdminAuditController {
    private final AdminAuditQueryService service;
    public AdminAuditController(AdminAuditQueryService service) { this.service = service; }

    @GetMapping public PageResponse<AdminAuditQueryService.AuditResponse> list(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID userId, @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) String resource, @RequestParam(required = false) Boolean success,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return service.list(page, size, userId, action, resource, success, from, to);
    }
    @GetMapping("/{id}") public AdminAuditQueryService.AuditResponse get(@PathVariable UUID id) { return service.get(id); }
}
