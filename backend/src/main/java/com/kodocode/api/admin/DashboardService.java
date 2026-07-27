package com.kodocode.api.admin;

import com.kodocode.api.audit.AdminAuditQueryService;
import com.kodocode.api.audit.AuditLogRepository;
import com.kodocode.api.content.SiteContentVersionRepository;
import com.kodocode.api.lead.AdminLeadService;
import com.kodocode.api.lead.ContactLeadRepository;
import com.kodocode.api.lead.LeadStatus;
import com.kodocode.api.lead.ServiceInterest;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {
    private final ContactLeadRepository leads;
    private final SiteContentVersionRepository versions;
    private final AuditLogRepository audits;
    private final AdminAuditQueryService auditMapper;

    public DashboardService(ContactLeadRepository leads, SiteContentVersionRepository versions,
            AuditLogRepository audits, AdminAuditQueryService auditMapper) {
        this.leads = leads; this.versions = versions; this.audits = audits; this.auditMapper = auditMapper;
    }

    @Transactional(readOnly = true)
    public DashboardResponse dashboard() {
        Instant monthStart = ZonedDateTime.now(ZoneOffset.UTC).withDayOfMonth(1).toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant();
        Map<ServiceInterest, Long> byService = new java.util.EnumMap<>(ServiceInterest.class);
        leads.countByServiceInterest().forEach(item -> byService.put(item.getServiceInterest(), item.getTotal()));
        List<RecentLead> recent = leads.findTop5ByOrderByCreatedAtDesc().stream().map(lead ->
                new RecentLead(lead.getId(), lead.getName(), lead.getCompany(), lead.getServiceInterest(), lead.getStatus(), lead.getCreatedAt())).toList();
        List<RecentContent> content = versions.findTop8ByCreatedByIsNotNullOrderByCreatedAtDesc().stream().map(version ->
                new RecentContent(version.getId(), version.getSiteSection().getSectionKey(), version.getVersionNumber(),
                        version.getStatus().name(), version.getCreatedBy().getEmail(), version.getCreatedAt())).toList();
        var recentAudits = audits.findTop8ByOrderByCreatedAtDesc().stream().map(auditMapper::response).toList();
        return new DashboardResponse(leads.count(), leads.countByCreatedAtAfter(monthStart), leads.countByStatus(LeadStatus.NEW),
                byService, recent, content, recentAudits);
    }

    public record DashboardResponse(long totalLeads, long leadsThisMonth, long unreadLeads,
            Map<ServiceInterest, Long> leadsByService, List<RecentLead> recentLeads,
            List<RecentContent> recentContent, List<AdminAuditQueryService.AuditResponse> recentAudits) {}
    public record RecentLead(java.util.UUID id, String name, String company, ServiceInterest serviceInterest, LeadStatus status, Instant createdAt) {}
    public record RecentContent(java.util.UUID id, String sectionKey, int version, String status, String actorEmail, Instant createdAt) {}
}
