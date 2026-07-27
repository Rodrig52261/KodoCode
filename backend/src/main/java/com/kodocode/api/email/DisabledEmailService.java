package com.kodocode.api.email;

import com.kodocode.api.lead.ContactLead;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "kodo.email", name = "enabled", havingValue = "false", matchIfMissing = true)
public class DisabledEmailService implements EmailService {
    @Override public boolean isEnabled() { return false; }
    @Override public void notifyCompany(ContactLead lead) {}
    @Override public void confirmToCustomer(ContactLead lead) {}
}
