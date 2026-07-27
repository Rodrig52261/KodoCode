package com.kodocode.api.publicsite;

import com.kodocode.api.content.ContentStatus;
import com.kodocode.api.content.SiteContentVersion;
import com.kodocode.api.content.SiteContentVersionRepository;
import com.kodocode.api.faq.FaqItemRepository;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublicContentService {

    private final SiteContentVersionRepository contentRepository;
    private final FaqItemRepository faqRepository;

    public PublicContentService(
            SiteContentVersionRepository contentRepository,
            FaqItemRepository faqRepository
    ) {
        this.contentRepository = contentRepository;
        this.faqRepository = faqRepository;
    }

    @Transactional(readOnly = true)
    public PublicSiteContentResponse getSiteContent() {
        List<SiteContentVersion> versions = contentRepository.findPublishedVersions(ContentStatus.PUBLISHED);
        Map<String, Map<String, Object>> sections = new LinkedHashMap<>();
        Instant publishedAt = null;

        for (SiteContentVersion version : versions) {
            sections.put(version.getSiteSection().getSectionKey(), version.getContentData());
            if (version.getPublishedAt() != null && (publishedAt == null || version.getPublishedAt().isAfter(publishedAt))) {
                publishedAt = version.getPublishedAt();
            }
        }

        return new PublicSiteContentResponse(sections, publishedAt);
    }

    @Transactional(readOnly = true)
    public List<PublicFaqResponse> getFaqs() {
        return faqRepository.findByStatusAndActiveTrueOrderByDisplayOrder(ContentStatus.PUBLISHED).stream()
                .map(item -> new PublicFaqResponse(
                        item.getId(), item.getQuestion(), item.getAnswer(), item.getDisplayOrder()))
                .toList();
    }
}
