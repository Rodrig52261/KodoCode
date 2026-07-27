package com.kodocode.api.content;

import com.kodocode.api.audit.AuditContext;
import com.kodocode.api.security.AdminPrincipal;
import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("/api/v1/admin/content")
public class AdminContentController {
    private final AdminContentService service;
    public AdminContentController(AdminContentService service) { this.service = service; }

    @GetMapping public List<AdminContentDtos.SectionSummary> list() { return service.list(); }
    @GetMapping("/{key}") public AdminContentDtos.SectionDetail get(@PathVariable String key) { return service.get(key); }
    @GetMapping("/{key}/versions") public List<AdminContentDtos.VersionDetail> versions(@PathVariable String key) { return service.versions(key); }

    @PutMapping("/{key}")
    public AdminContentDtos.SectionDetail update(@PathVariable String key, @RequestBody AdminContentDtos.UpdateContentRequest body,
            @AuthenticationPrincipal AdminPrincipal principal, HttpServletRequest request) {
        return service.saveDraft(key, body.contentData(), principal, AuditContext.from(request));
    }

    @PostMapping("/{key}/publish")
    public AdminContentDtos.SectionDetail publish(@PathVariable String key, @AuthenticationPrincipal AdminPrincipal principal, HttpServletRequest request) {
        return service.publish(key, principal, AuditContext.from(request));
    }

    @PostMapping("/{key}/restore/{versionId}")
    public AdminContentDtos.SectionDetail restore(@PathVariable String key, @PathVariable UUID versionId,
            @AuthenticationPrincipal AdminPrincipal principal, HttpServletRequest request) {
        return service.restore(key, versionId, principal, AuditContext.from(request));
    }
}
