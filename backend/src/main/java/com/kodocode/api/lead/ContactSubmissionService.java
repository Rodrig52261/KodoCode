package com.kodocode.api.lead;

import com.kodocode.api.audit.AuditContext;
import com.kodocode.api.config.ApplicationProperties;
import com.kodocode.api.email.EmailService;
import com.kodocode.api.shared.web.BusinessException;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ContactSubmissionService {

    private static final Logger log = LoggerFactory.getLogger(ContactSubmissionService.class);
    private final ContactLeadRepository repository;
    private final ContactRateLimitService rateLimit;
    private final EmailService emailService;
    private final ApplicationProperties.Contact properties;
    private final Clock clock;

    @Autowired
    public ContactSubmissionService(ContactLeadRepository repository, ContactRateLimitService rateLimit,
            EmailService emailService, ApplicationProperties applicationProperties) {
        this(repository, rateLimit, emailService, applicationProperties.contact(), Clock.systemUTC());
    }

    ContactSubmissionService(ContactLeadRepository repository, ContactRateLimitService rateLimit,
            EmailService emailService, ApplicationProperties.Contact properties, Clock clock) {
        this.repository = repository;
        this.rateLimit = rateLimit;
        this.emailService = emailService;
        this.properties = properties;
        this.clock = clock;
    }

    public PublicContactResponse submit(PublicContactRequest request, AuditContext context) {
        Instant now = clock.instant();
        if (request.website() != null && !request.website().isBlank()) {
            throw new BusinessException("Nao foi possivel enviar a mensagem.", HttpStatus.BAD_REQUEST);
        }
        if (request.formStartedAt().plus(properties.minimumSubmissionDuration()).isAfter(now)
                || request.formStartedAt().plusSeconds(86_400).isBefore(now)) {
            throw new BusinessException("Aguarde alguns segundos e tente novamente.", HttpStatus.BAD_REQUEST);
        }
        rejectCode(request.name());
        rejectCode(request.company());
        rejectCode(request.message());

        String email = clean(request.email()).toLowerCase(Locale.ROOT);
        String ip = context == null || context.ipAddress() == null ? "unknown" : context.ipAddress();
        rateLimit.check(ip, email);
        if (repository.existsByEmailIgnoreCaseAndCreatedAtAfter(email, now.minus(properties.duplicateWindow()))) {
            throw new BusinessException("Esta mensagem ja foi recebida. Aguarde nosso retorno.", HttpStatus.CONFLICT);
        }

        ContactLead lead = new ContactLead();
        lead.setName(clean(request.name()));
        lead.setCompany(cleanNullable(request.company()));
        lead.setEmail(email);
        lead.setPhone(clean(request.phone()));
        lead.setServiceInterest(request.serviceInterest());
        lead.setBudgetRange(request.budgetRange());
        lead.setMessage(clean(request.message()));
        lead.setPrivacyConsent(true);
        lead.setConsentDate(now);
        lead.setSource("landing-page");
        repository.saveAndFlush(lead);

        deliverEmails(lead);
        return new PublicContactResponse(lead.getId(), "Mensagem recebida. Retornaremos em breve.");
    }

    private void deliverEmails(ContactLead lead) {
        if (!emailService.isEnabled()) {
            lead.setNotificationStatus(EmailDeliveryStatus.SKIPPED);
            lead.setConfirmationStatus(EmailDeliveryStatus.SKIPPED);
            repository.save(lead);
            return;
        }
        StringBuilder errors = new StringBuilder();
        try {
            emailService.notifyCompany(lead);
            lead.setNotificationStatus(EmailDeliveryStatus.SENT);
        } catch (RuntimeException exception) {
            lead.setNotificationStatus(EmailDeliveryStatus.FAILED);
            appendError(errors, "notification", exception);
        }
        try {
            emailService.confirmToCustomer(lead);
            lead.setConfirmationStatus(EmailDeliveryStatus.SENT);
        } catch (RuntimeException exception) {
            lead.setConfirmationStatus(EmailDeliveryStatus.FAILED);
            appendError(errors, "confirmation", exception);
        }
        lead.setEmailLastError(errors.isEmpty() ? null : limit(errors.toString(), 1000));
        repository.save(lead);
    }

    private void appendError(StringBuilder target, String type, RuntimeException exception) {
        log.error("Contact e-mail delivery failed: type={}", type, exception);
        if (!target.isEmpty()) target.append("; ");
        target.append(type).append(':').append(exception.getClass().getSimpleName());
    }

    private String clean(String value) {
        String cleaned = value.strip().replaceAll("[\\p{Cntrl}&&[^\\r\\n\\t]]", "");
        if (cleaned.isBlank()) throw new BusinessException("Revise os campos informados.", HttpStatus.BAD_REQUEST);
        return cleaned;
    }

    private void rejectCode(String value) {
        if (value != null && ContactTextPolicy.containsCode(value)) {
            throw new BusinessException("Nao insira codigo, HTML ou scripts.", HttpStatus.BAD_REQUEST);
        }
    }

    private String cleanNullable(String value) { return value == null || value.isBlank() ? null : clean(value); }
    private String limit(String value, int max) { return value.length() <= max ? value : value.substring(0, max); }
}
