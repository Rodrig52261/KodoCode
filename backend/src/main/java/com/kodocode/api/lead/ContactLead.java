package com.kodocode.api.lead;

import com.kodocode.api.shared.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "contact_leads")
public class ContactLead extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 160)
    private String company;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(nullable = false, length = 30)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_interest", nullable = false, length = 50)
    private ServiceInterest serviceInterest;

    @Enumerated(EnumType.STRING)
    @Column(name = "budget_range", nullable = false, length = 50)
    private BudgetRange budgetRange;

    @Column(nullable = false, length = 3000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LeadStatus status = LeadStatus.NEW;

    @Column(name = "internal_notes", length = 4000)
    private String internalNotes;

    @Column(name = "privacy_consent", nullable = false)
    private boolean privacyConsent;

    @Column(name = "consent_date", nullable = false)
    private Instant consentDate;

    @Column(nullable = false, length = 100)
    private String source;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_status", nullable = false, length = 20)
    private EmailDeliveryStatus notificationStatus = EmailDeliveryStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "confirmation_status", nullable = false, length = 20)
    private EmailDeliveryStatus confirmationStatus = EmailDeliveryStatus.PENDING;

    @Column(name = "email_last_error", length = 1000)
    private String emailLastError;
}
