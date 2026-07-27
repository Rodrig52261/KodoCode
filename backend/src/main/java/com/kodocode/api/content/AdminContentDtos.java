package com.kodocode.api.content;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public final class AdminContentDtos {
    private AdminContentDtos() {}

    public record SectionSummary(UUID id, String sectionKey, String title, String subtitle,
            ContentStatus status, Integer publishedVersion, Integer draftVersion, Instant updatedAt) {}

    public record SectionDetail(SectionSummary section, VersionDetail published, VersionDetail draft) {}

    public record VersionDetail(UUID id, int versionNumber, ContentStatus status, Map<String, Object> contentData,
            String createdBy, Instant createdAt, Instant publishedAt) {}

    public record UpdateContentRequest(Map<String, Object> contentData) {}
}
