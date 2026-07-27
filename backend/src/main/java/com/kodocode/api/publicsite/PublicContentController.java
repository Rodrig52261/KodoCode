package com.kodocode.api.publicsite;

import java.time.Duration;
import java.util.List;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public")
public class PublicContentController {

    private static final CacheControl PUBLIC_CACHE = CacheControl.maxAge(Duration.ofMinutes(5))
            .staleWhileRevalidate(Duration.ofMinutes(10))
            .cachePublic();

    private final PublicContentService service;

    public PublicContentController(PublicContentService service) {
        this.service = service;
    }

    @GetMapping("/site-content")
    public ResponseEntity<PublicSiteContentResponse> siteContent() {
        return ResponseEntity.ok().cacheControl(PUBLIC_CACHE).body(service.getSiteContent());
    }

    @GetMapping("/faqs")
    public ResponseEntity<List<PublicFaqResponse>> faqs() {
        return ResponseEntity.ok().cacheControl(PUBLIC_CACHE).body(service.getFaqs());
    }
}
