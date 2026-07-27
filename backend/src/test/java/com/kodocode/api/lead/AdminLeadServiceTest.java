package com.kodocode.api.lead;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kodocode.api.admin.AdminRole;
import com.kodocode.api.admin.AdminUser;
import com.kodocode.api.admin.AdminUserRepository;
import com.kodocode.api.audit.AuditContext;
import com.kodocode.api.audit.AuditService;
import com.kodocode.api.security.AdminPrincipal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AdminLeadServiceTest {
    @Test
    void viewingNewLeadChangesStatusAndAuditsAction() {
        ContactLeadRepository leads = mock(ContactLeadRepository.class); AdminUserRepository users = mock(AdminUserRepository.class);
        AuditService audit = mock(AuditService.class); AdminLeadService service = new AdminLeadService(leads, users, audit);
        UUID leadId = UUID.randomUUID(); UUID userId = UUID.randomUUID();
        ContactLead lead = new ContactLead(); lead.setId(leadId); lead.setName("Ana"); lead.setEmail("ana@example.com");
        lead.setPhone("11999999999"); lead.setMessage("Mensagem suficientemente completa."); lead.setServiceInterest(ServiceInterest.CRM);
        lead.setBudgetRange(BudgetRange.DISCUSS_FIRST); lead.setStatus(LeadStatus.NEW); lead.setPrivacyConsent(true);
        lead.setCreatedAt(Instant.now()); lead.setUpdatedAt(Instant.now());
        AdminUser admin = new AdminUser(); admin.setId(userId); admin.setEmail("admin@example.com"); admin.setRole(AdminRole.ADMIN);
        when(leads.findById(leadId)).thenReturn(Optional.of(lead)); when(users.findById(userId)).thenReturn(Optional.of(admin));

        var response = service.get(leadId, new AdminPrincipal(userId, admin.getEmail(), AdminRole.ADMIN), new AuditContext("127.0.0.1", "test"));

        assertThat(response.status()).isEqualTo(LeadStatus.VIEWED);
        verify(audit).record(any(), any(), any(), any(), any(), any(), any(), any(Boolean.class), any());
    }
}
