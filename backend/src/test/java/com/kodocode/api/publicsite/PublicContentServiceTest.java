package com.kodocode.api.publicsite;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.kodocode.api.content.ContentStatus;
import com.kodocode.api.content.SiteContentVersion;
import com.kodocode.api.content.SiteContentVersionRepository;
import com.kodocode.api.content.SiteSection;
import com.kodocode.api.faq.FaqItem;
import com.kodocode.api.faq.FaqItemRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PublicContentServiceTest {

    @Mock SiteContentVersionRepository contentRepository;
    @Mock FaqItemRepository faqRepository;

    @Test
    void assemblesPublishedSectionsAndLatestPublicationDate() {
        SiteSection hero = section("hero");
        SiteContentVersion first = version(hero, Map.of("title", "Titulo"), Instant.parse("2026-01-01T10:00:00Z"));
        SiteSection seo = section("seo");
        SiteContentVersion latest = version(seo, Map.of("title", "SEO"), Instant.parse("2026-01-02T10:00:00Z"));
        when(contentRepository.findPublishedVersions(ContentStatus.PUBLISHED)).thenReturn(List.of(first, latest));

        PublicSiteContentResponse response = service().getSiteContent();

        assertThat(response.sections()).containsOnlyKeys("hero", "seo");
        assertThat(response.sections().get("hero")).containsEntry("title", "Titulo");
        assertThat(response.publishedAt()).isEqualTo(Instant.parse("2026-01-02T10:00:00Z"));
    }

    @Test
    void returnsOnlyFaqFieldsRequiredByThePublicContract() {
        FaqItem item = new FaqItem();
        item.setId(UUID.randomUUID());
        item.setQuestion("Pergunta?");
        item.setAnswer("Resposta.");
        item.setDisplayOrder(2);
        when(faqRepository.findByStatusAndActiveTrueOrderByDisplayOrder(ContentStatus.PUBLISHED))
                .thenReturn(List.of(item));

        assertThat(service().getFaqs()).containsExactly(
                new PublicFaqResponse(item.getId(), "Pergunta?", "Resposta.", 2));
    }

    private PublicContentService service() {
        return new PublicContentService(contentRepository, faqRepository);
    }

    private SiteSection section(String key) {
        SiteSection section = new SiteSection();
        section.setSectionKey(key);
        return section;
    }

    private SiteContentVersion version(SiteSection section, Map<String, Object> data, Instant publishedAt) {
        SiteContentVersion version = new SiteContentVersion();
        version.setId(UUID.randomUUID());
        version.setSiteSection(section);
        version.setContentData(data);
        version.setStatus(ContentStatus.PUBLISHED);
        version.setPublishedAt(publishedAt);
        return version;
    }
}
