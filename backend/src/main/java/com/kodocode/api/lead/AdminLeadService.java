package com.kodocode.api.lead;

import com.kodocode.api.admin.AdminUser;
import com.kodocode.api.admin.AdminUserRepository;
import com.kodocode.api.audit.AuditAction;
import com.kodocode.api.audit.AuditContext;
import com.kodocode.api.audit.AuditService;
import com.kodocode.api.security.AdminPrincipal;
import com.kodocode.api.shared.web.BusinessException;
import com.kodocode.api.shared.web.PageResponse;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminLeadService {
    private static final Set<String> SORT_FIELDS = Set.of("createdAt", "name", "company", "email", "status", "serviceInterest");
    private final ContactLeadRepository repository;
    private final AdminUserRepository users;
    private final AuditService audit;

    public AdminLeadService(ContactLeadRepository repository, AdminUserRepository users, AuditService audit) {
        this.repository = repository; this.users = users; this.audit = audit;
    }

    @Transactional(readOnly = true)
    public PageResponse<LeadResponse> list(int page, int size, String sort, String direction,
            LeadStatus status, ServiceInterest service, String search) {
        String normalizedSearch = normalizeSearch(search);
        String sortField = SORT_FIELDS.contains(sort) ? sort : "createdAt";
        Sort.Direction order = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        int safeSize = Math.min(Math.max(size, 1), 100);
        Specification<ContactLead> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) predicates.add(builder.equal(root.get("status"), status));
            if (service != null) predicates.add(builder.equal(root.get("serviceInterest"), service));
            if (normalizedSearch != null) {
                String term = "%" + normalizedSearch + "%";
                predicates.add(builder.or(builder.like(builder.lower(root.get("name")), term, '\\'),
                        builder.like(builder.lower(root.get("company")), term, '\\'),
                        builder.like(builder.lower(root.get("email")), term, '\\')));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
        var result = repository.findAll(specification, PageRequest.of(Math.max(page, 0), safeSize, Sort.by(order, sortField))).map(this::response);
        return PageResponse.from(result);
    }

    private String normalizeSearch(String value) {
        if (value == null || value.isBlank()) return null;
        String clean = value.strip().toLowerCase(Locale.ROOT);
        if (clean.length() > 120) throw new BusinessException("A busca deve ter no maximo 120 caracteres.", HttpStatus.BAD_REQUEST);
        return clean.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    @Transactional
    public LeadResponse get(UUID id, AdminPrincipal principal, AuditContext context) {
        ContactLead lead = required(id);
        if (lead.getStatus() == LeadStatus.NEW) changeStatus(lead, LeadStatus.VIEWED, principal, context);
        return response(lead);
    }

    @Transactional
    public LeadResponse status(UUID id, LeadStatus status, AdminPrincipal principal, AuditContext context) {
        ContactLead lead = required(id);
        changeStatus(lead, status, principal, context);
        return response(lead);
    }

    @Transactional
    public LeadResponse notes(UUID id, String notes, AdminPrincipal principal, AuditContext context) {
        ContactLead lead = required(id);
        String previous = lead.getInternalNotes();
        String clean = sanitizeNotes(notes);
        lead.setInternalNotes(clean);
        AdminUser actor = actor(principal);
        audit.record(actor, actor.getEmail(), AuditAction.LEAD_NOTES_CHANGE, "ContactLead", id.toString(),
                Map.of("notes", previous == null ? "" : previous), Map.of("notes", clean == null ? "" : clean), true, context);
        return response(lead);
    }

    @Transactional
    public LeadResponse archive(UUID id, AdminPrincipal principal, AuditContext context) {
        ContactLead lead = required(id);
        LeadStatus previous = lead.getStatus();
        lead.setStatus(LeadStatus.ARCHIVED);
        AdminUser actor = actor(principal);
        audit.record(actor, actor.getEmail(), AuditAction.LEAD_ARCHIVE, "ContactLead", id.toString(),
                Map.of("status", previous.name()), Map.of("status", LeadStatus.ARCHIVED.name()), true, context);
        return response(lead);
    }

    private void changeStatus(ContactLead lead, LeadStatus status, AdminPrincipal principal, AuditContext context) {
        if (status == null) throw new BusinessException("Informe o status.", HttpStatus.BAD_REQUEST);
        LeadStatus previous = lead.getStatus();
        lead.setStatus(status);
        AdminUser actor = actor(principal);
        audit.record(actor, actor.getEmail(), AuditAction.LEAD_STATUS_CHANGE, "ContactLead", lead.getId().toString(),
                Map.of("status", previous.name()), Map.of("status", status.name()), true, context);
    }

    private ContactLead required(UUID id) { return repository.findById(id).orElseThrow(() -> new BusinessException("Contato nao encontrado.", HttpStatus.NOT_FOUND)); }
    private AdminUser actor(AdminPrincipal principal) { return users.findById(principal.id()).orElseThrow(() -> new BusinessException("Administrador nao encontrado.", HttpStatus.NOT_FOUND)); }
    private String sanitizeNotes(String value) {
        if (value == null || value.isBlank()) return null;
        String clean = value.strip().replaceAll("<[^>]*>", "").replaceAll("[<>]", "");
        if (clean.length() > 4000) throw new BusinessException("Observacao excede 4000 caracteres.", HttpStatus.BAD_REQUEST);
        return clean;
    }
    private LeadResponse response(ContactLead lead) { return new LeadResponse(lead.getId(), lead.getName(), lead.getCompany(), lead.getEmail(), lead.getPhone(), lead.getServiceInterest(), lead.getBudgetRange(), lead.getMessage(), lead.getStatus(), lead.getInternalNotes(), lead.isPrivacyConsent(), lead.getConsentDate(), lead.getSource(), lead.getNotificationStatus(), lead.getConfirmationStatus(), lead.getCreatedAt(), lead.getUpdatedAt()); }

    public record LeadResponse(UUID id, String name, String company, String email, String phone, ServiceInterest serviceInterest,
            BudgetRange budgetRange, String message, LeadStatus status, String internalNotes, boolean privacyConsent,
            Instant consentDate, String source, EmailDeliveryStatus notificationStatus, EmailDeliveryStatus confirmationStatus,
            Instant createdAt, Instant updatedAt) {}
    public record StatusRequest(LeadStatus status) {}
    public record NotesRequest(String notes) {}
}
