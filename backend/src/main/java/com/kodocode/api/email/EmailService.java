package com.kodocode.api.email;

import com.kodocode.api.lead.ContactLead;

public interface EmailService {
    boolean isEnabled();
    void notifyCompany(ContactLead lead);
    void confirmToCustomer(ContactLead lead);
}
