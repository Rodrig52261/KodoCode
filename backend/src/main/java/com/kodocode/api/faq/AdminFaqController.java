package com.kodocode.api.faq;

import com.kodocode.api.audit.AuditContext;
import com.kodocode.api.security.AdminPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/faqs")
public class AdminFaqController {
    private final AdminFaqService service;
    public AdminFaqController(AdminFaqService service) { this.service = service; }
    @GetMapping public List<AdminFaqService.FaqAdminResponse> list() { return service.list(); }
    @PutMapping("/{id}") public AdminFaqService.FaqAdminResponse update(@PathVariable UUID id,
            @Valid @RequestBody AdminFaqService.FaqUpdateRequest body, @AuthenticationPrincipal AdminPrincipal principal, HttpServletRequest request) {
        return service.draft(id, body, principal, AuditContext.from(request));
    }
    @PostMapping("/{id}/publish") public AdminFaqService.FaqAdminResponse publish(@PathVariable UUID id,
            @AuthenticationPrincipal AdminPrincipal principal, HttpServletRequest request) {
        return service.publish(id, principal, AuditContext.from(request));
    }
}
