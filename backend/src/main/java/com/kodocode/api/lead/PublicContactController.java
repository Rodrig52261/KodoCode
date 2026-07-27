package com.kodocode.api.lead;

import com.kodocode.api.audit.AuditContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public")
public class PublicContactController {
    private final ContactSubmissionService service;
    public PublicContactController(ContactSubmissionService service) { this.service = service; }

    @PostMapping("/contact")
    @ResponseStatus(HttpStatus.CREATED)
    public PublicContactResponse contact(@Valid @RequestBody PublicContactRequest request, HttpServletRequest httpRequest) {
        return service.submit(request, AuditContext.from(httpRequest));
    }
}
