package com.kodocode.api.lead;

import com.kodocode.api.audit.AuditContext;
import com.kodocode.api.security.AdminPrincipal;
import com.kodocode.api.shared.web.PageResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/leads")
public class AdminLeadController {
    private final AdminLeadService service;
    public AdminLeadController(AdminLeadService service) { this.service = service; }

    @GetMapping public PageResponse<AdminLeadService.LeadResponse> list(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort, @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) LeadStatus status, @RequestParam(name = "service", required = false) ServiceInterest serviceInterest,
            @RequestParam(required = false) String search) {
        return service.list(page, size, sort, direction, status, serviceInterest, search);
    }

    @GetMapping("/{id}") public AdminLeadService.LeadResponse get(@PathVariable UUID id, @AuthenticationPrincipal AdminPrincipal principal, HttpServletRequest request) { return service.get(id, principal, AuditContext.from(request)); }
    @PatchMapping("/{id}/status") public AdminLeadService.LeadResponse status(@PathVariable UUID id, @RequestBody AdminLeadService.StatusRequest body, @AuthenticationPrincipal AdminPrincipal principal, HttpServletRequest request) { return service.status(id, body.status(), principal, AuditContext.from(request)); }
    @PatchMapping("/{id}/notes") public AdminLeadService.LeadResponse notes(@PathVariable UUID id, @RequestBody AdminLeadService.NotesRequest body, @AuthenticationPrincipal AdminPrincipal principal, HttpServletRequest request) { return service.notes(id, body.notes(), principal, AuditContext.from(request)); }
    @PatchMapping("/{id}/archive") public AdminLeadService.LeadResponse archive(@PathVariable UUID id, @AuthenticationPrincipal AdminPrincipal principal, HttpServletRequest request) { return service.archive(id, principal, AuditContext.from(request)); }
}
