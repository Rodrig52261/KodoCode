package com.kodocode.api.content;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kodocode.api.admin.AdminRole;
import com.kodocode.api.admin.AdminUser;
import com.kodocode.api.admin.AdminUserRepository;
import com.kodocode.api.audit.AuditContext;
import com.kodocode.api.audit.AuditService;
import com.kodocode.api.security.AdminPrincipal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class AdminContentServiceTest {
    @Test
    void createsSanitizedDraftThenPublishesIt() {
        SiteSectionRepository sections = mock(SiteSectionRepository.class);
        SiteContentVersionRepository versions = mock(SiteContentVersionRepository.class);
        AdminUserRepository users = mock(AdminUserRepository.class);
        AuditService audit = mock(AuditService.class);
        AdminContentService service = new AdminContentService(sections, versions, users, audit);
        UUID sectionId = UUID.randomUUID(); UUID userId = UUID.randomUUID(); UUID publishedId = UUID.randomUUID();
        SiteSection section = new SiteSection(); section.setId(sectionId); section.setSectionKey("hero"); section.setTitle("Hero");
        section.setStatus(ContentStatus.PUBLISHED); section.setPublishedVersionId(publishedId); section.setUpdatedAt(Instant.now());
        AdminUser user = new AdminUser(); user.setId(userId); user.setEmail("admin@example.com"); user.setRole(AdminRole.ADMIN);
        SiteContentVersion published = version(section, publishedId, 1, ContentStatus.PUBLISHED,
                Map.of("title", "Original", "primaryCta", Map.of("label", "Contato", "href", "#contato"),
                        "visual", Map.of("icon", "shield")));
        AtomicReference<SiteContentVersion> draft = new AtomicReference<>();
        when(sections.findBySectionKey("hero")).thenReturn(Optional.of(section));
        when(users.findById(userId)).thenReturn(Optional.of(user));
        when(versions.findById(any())).thenAnswer(invocation -> {
            UUID id = invocation.getArgument(0);
            if (publishedId.equals(id)) return Optional.of(published);
            return draft.get() != null && draft.get().getId().equals(id) ? Optional.of(draft.get()) : Optional.empty();
        });
        when(versions.findFirstBySiteSectionIdAndStatusOrderByVersionNumberDesc(sectionId, ContentStatus.DRAFT))
                .thenAnswer(invocation -> Optional.ofNullable(draft.get()));
        when(versions.findBySiteSectionIdOrderByVersionNumberDesc(sectionId)).thenReturn(List.of(published));
        when(versions.save(any())).thenAnswer(invocation -> { SiteContentVersion value = invocation.getArgument(0); value.setId(UUID.randomUUID()); value.setCreatedAt(Instant.now()); draft.set(value); return value; });

        AdminPrincipal principal = new AdminPrincipal(userId, user.getEmail(), AdminRole.ADMIN);
        assertThatThrownBy(() -> service.saveDraft("hero",
                Map.of("title", "Novo titulo", "primaryCta", Map.of("label", "Contato", "href", "javascript:alert(1)"),
                        "visual", Map.of("icon", "shield")), principal, new AuditContext("127.0.0.1", "test")))
                .isInstanceOf(com.kodocode.api.shared.web.BusinessException.class)
                .hasMessageContaining("Links devem usar");

        var detail = service.saveDraft("hero", Map.of("title", "<b>Novo titulo</b>",
                        "primaryCta", Map.of("label", "Contato", "href", "#contato"),
                        "visual", Map.of("icon", "malicious")),
                principal, new AuditContext("127.0.0.1", "test"));

        assertThat(detail.draft().contentData().get("title")).isEqualTo("Novo titulo");
        assertThat(((Map<?, ?>) detail.draft().contentData().get("visual")).get("icon")).isEqualTo("shield");
        var publishedDetail = service.publish("hero", principal, new AuditContext("127.0.0.1", "test"));
        assertThat(publishedDetail.published().status()).isEqualTo(ContentStatus.PUBLISHED);
        assertThat(section.getPublishedVersionId()).isEqualTo(draft.get().getId());
        verify(audit, org.mockito.Mockito.atLeast(2)).record(any(), any(), any(), any(), any(), any(), any(), any(Boolean.class), any());
    }

    private SiteContentVersion version(SiteSection section, UUID id, int number, ContentStatus status, Map<String, Object> content) {
        SiteContentVersion value = new SiteContentVersion(); value.setId(id); value.setSiteSection(section); value.setVersionNumber(number);
        value.setStatus(status); value.setContentData(content); value.setCreatedAt(Instant.now()); return value;
    }
}
