package com.kodocode.api.publicsite;

import java.time.Instant;
import java.util.Map;

public record PublicSiteContentResponse(
        Map<String, Map<String, Object>> sections,
        Instant publishedAt
) {}
