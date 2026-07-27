package com.kodocode.api.lead;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface ContactLeadRepository extends JpaRepository<ContactLead, UUID>, JpaSpecificationExecutor<ContactLead> {
    boolean existsByEmailIgnoreCaseAndCreatedAtAfter(String email, Instant createdAt);
    long countByStatus(LeadStatus status);
    long countByCreatedAtAfter(Instant instant);
    java.util.List<ContactLead> findTop5ByOrderByCreatedAtDesc();

    @Query("select lead.serviceInterest as serviceInterest, count(lead) as total from ContactLead lead group by lead.serviceInterest")
    java.util.List<ServiceCount> countByServiceInterest();

    interface ServiceCount {
        ServiceInterest getServiceInterest();
        long getTotal();
    }
}
