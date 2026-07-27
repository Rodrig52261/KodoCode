package com.kodocode.api.lead;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kodocode.api.audit.AuditContext;
import com.kodocode.api.config.ApplicationProperties;
import com.kodocode.api.email.EmailService;
import com.kodocode.api.shared.web.BusinessException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ContactSubmissionServiceTest {
    private final Instant now = Instant.parse("2026-07-25T12:00:00Z");
    private final ContactLeadRepository repository = mock(ContactLeadRepository.class);
    private final ContactRateLimitService rateLimit = mock(ContactRateLimitService.class);
    private final EmailService email = mock(EmailService.class);
    private final ContactSubmissionService service = new ContactSubmissionService(repository, rateLimit, email,
            new ApplicationProperties.Contact(Duration.ofSeconds(3), Duration.ofMinutes(10), 5), Clock.fixed(now, ZoneOffset.UTC));

    @Test
    void savesLeadEvenWhenEmailDeliveryFails() {
        when(repository.existsByEmailIgnoreCaseAndCreatedAtAfter(any(), any())).thenReturn(false);
        when(repository.saveAndFlush(any())).thenAnswer(invocation -> { ContactLead lead = invocation.getArgument(0); lead.setId(UUID.randomUUID()); return lead; });
        when(email.isEnabled()).thenReturn(true);
        doThrow(new IllegalStateException("provider unavailable")).when(email).notifyCompany(any());

        PublicContactResponse response = service.submit(validRequest(), new AuditContext("127.0.0.1", "test"));

        ArgumentCaptor<ContactLead> captor = ArgumentCaptor.forClass(ContactLead.class);
        verify(repository).saveAndFlush(captor.capture());
        verify(repository).save(captor.capture());
        assertThat(response.id()).isNotNull();
        verify(rateLimit).check("127.0.0.1", "ana@example.com");
        assertThat(captor.getAllValues().get(1).getNotificationStatus()).isEqualTo(EmailDeliveryStatus.FAILED);
        assertThat(captor.getAllValues().get(1).getConfirmationStatus()).isEqualTo(EmailDeliveryStatus.SENT);
    }

    @Test
    void rejectsSubmissionCompletedTooQuickly() {
        PublicContactRequest request = new PublicContactRequest("Ana", null, "ana@example.com", "11999999999",
                ServiceInterest.CRM, BudgetRange.DISCUSS_FIRST, "Preciso organizar o processo comercial.", true,
                now.minusSeconds(1), "");
        assertThatThrownBy(() -> service.submit(request, new AuditContext("127.0.0.1", "test")))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void rejectsCodeInsteadOfSavingSanitizedContent() {
        PublicContactRequest request = new PublicContactRequest("<script>alert(1)</script>", null, "ana@example.com", "(11) 99999-9999",
                ServiceInterest.CRM, BudgetRange.DISCUSS_FIRST, "Preciso organizar o processo comercial.", true,
                now.minusSeconds(10), "");

        assertThatThrownBy(() -> service.submit(request, new AuditContext("127.0.0.1", "test")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("codigo");
        verify(repository, never()).saveAndFlush(any());
    }

    private PublicContactRequest validRequest() {
        return new PublicContactRequest("Ana", "Empresa", "ANA@example.com", "(11) 99999-9999",
                ServiceInterest.CRM, BudgetRange.DISCUSS_FIRST, "Preciso organizar o processo comercial da empresa.", true,
                now.minusSeconds(10), "");
    }
}
