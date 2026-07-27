package com.kodocode.api.content;

import com.kodocode.api.admin.AdminUser;
import com.kodocode.api.admin.AdminUserRepository;
import com.kodocode.api.audit.AuditAction;
import com.kodocode.api.audit.AuditContext;
import com.kodocode.api.audit.AuditService;
import com.kodocode.api.security.AdminPrincipal;
import com.kodocode.api.shared.web.BusinessException;
import java.time.Instant;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminContentService {

    private final SiteSectionRepository sectionRepository;
    private final SiteContentVersionRepository versionRepository;
    private final AdminUserRepository userRepository;
    private final AuditService auditService;

    public AdminContentService(SiteSectionRepository sectionRepository, SiteContentVersionRepository versionRepository,
            AdminUserRepository userRepository, AuditService auditService) {
        this.sectionRepository = sectionRepository;
        this.versionRepository = versionRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<AdminContentDtos.SectionSummary> list() {
        return sectionRepository.findAll().stream()
                .sorted(java.util.Comparator.comparing(SiteSection::getSectionKey))
                .map(this::summary).toList();
    }

    @Transactional(readOnly = true)
    public AdminContentDtos.SectionDetail get(String key) {
        SiteSection section = requiredSection(key);
        return detail(section);
    }

    @Transactional(readOnly = true)
    public List<AdminContentDtos.VersionDetail> versions(String key) {
        SiteSection section = requiredSection(key);
        return versionRepository.findBySiteSectionIdOrderByVersionNumberDesc(section.getId()).stream().map(this::version).toList();
    }

    @Transactional
    public AdminContentDtos.SectionDetail saveDraft(String key, Map<String, Object> requested,
            AdminPrincipal principal, AuditContext context) {
        SiteSection section = requiredSection(key);
        SiteContentVersion baseline = currentDraft(section);
        if (baseline == null) baseline = currentPublished(section);
        if (baseline == null) throw notFound("Conteudo publicado nao encontrado.");
        Map<String, Object> sanitized = sanitizeAndValidateShape(requested, baseline.getContentData());
        AdminUser actor = requiredUser(principal);
        archiveDrafts(section, null);

        SiteContentVersion draft = new SiteContentVersion();
        draft.setSiteSection(section);
        draft.setContentData(sanitized);
        draft.setVersionNumber(nextVersion(section));
        draft.setStatus(ContentStatus.DRAFT);
        draft.setCreatedBy(actor);
        versionRepository.save(draft);
        auditService.record(actor, actor.getEmail(), AuditAction.CONTENT_UPDATE, "SiteSection", section.getId().toString(),
                baseline.getContentData(), sanitized, true, context);
        return detail(section);
    }

    @Transactional
    public AdminContentDtos.SectionDetail publish(String key, AdminPrincipal principal, AuditContext context) {
        SiteSection section = requiredSection(key);
        SiteContentVersion draft = currentDraft(section);
        if (draft == null) throw new BusinessException("Nenhum rascunho disponivel para publicar.", HttpStatus.CONFLICT);
        SiteContentVersion previous = currentPublished(section);
        if (previous != null) previous.setStatus(ContentStatus.ARCHIVED);
        archiveDrafts(section, draft.getId());
        draft.setStatus(ContentStatus.PUBLISHED);
        draft.setPublishedAt(Instant.now());
        section.setPublishedVersionId(draft.getId());
        section.setStatus(ContentStatus.PUBLISHED);
        AdminUser actor = requiredUser(principal);
        auditService.record(actor, actor.getEmail(), AuditAction.CONTENT_PUBLISH, "SiteSection", section.getId().toString(),
                previous == null ? null : previous.getContentData(), draft.getContentData(), true, context);
        return detail(section);
    }

    @Transactional
    public AdminContentDtos.SectionDetail restore(String key, java.util.UUID versionId,
            AdminPrincipal principal, AuditContext context) {
        SiteSection section = requiredSection(key);
        SiteContentVersion source = versionRepository.findByIdAndSiteSectionId(versionId, section.getId())
                .orElseThrow(() -> notFound("Versao nao encontrada."));
        AdminUser actor = requiredUser(principal);
        archiveDrafts(section, null);
        SiteContentVersion restored = new SiteContentVersion();
        restored.setSiteSection(section);
        restored.setContentData(deepCopy(source.getContentData()));
        restored.setVersionNumber(nextVersion(section));
        restored.setStatus(ContentStatus.DRAFT);
        restored.setCreatedBy(actor);
        versionRepository.save(restored);
        auditService.record(actor, actor.getEmail(), AuditAction.CONTENT_RESTORE, "SiteSection", section.getId().toString(),
                null, Map.of("sourceVersion", source.getVersionNumber(), "newVersion", restored.getVersionNumber()), true, context);
        return detail(section);
    }

    private AdminContentDtos.SectionDetail detail(SiteSection section) {
        SiteContentVersion published = currentPublished(section);
        SiteContentVersion draft = currentDraft(section);
        return new AdminContentDtos.SectionDetail(summary(section), published == null ? null : version(published), draft == null ? null : version(draft));
    }

    private AdminContentDtos.SectionSummary summary(SiteSection section) {
        SiteContentVersion published = currentPublished(section);
        SiteContentVersion draft = currentDraft(section);
        return new AdminContentDtos.SectionSummary(section.getId(), section.getSectionKey(), section.getTitle(), section.getSubtitle(),
                section.getStatus(), published == null ? null : published.getVersionNumber(),
                draft == null ? null : draft.getVersionNumber(), section.getUpdatedAt());
    }

    private AdminContentDtos.VersionDetail version(SiteContentVersion value) {
        return new AdminContentDtos.VersionDetail(value.getId(), value.getVersionNumber(), value.getStatus(), value.getContentData(),
                value.getCreatedBy() == null ? "Sistema" : value.getCreatedBy().getEmail(), value.getCreatedAt(), value.getPublishedAt());
    }

    private SiteContentVersion currentPublished(SiteSection section) {
        return section.getPublishedVersionId() == null ? null : versionRepository.findById(section.getPublishedVersionId()).orElse(null);
    }

    private SiteContentVersion currentDraft(SiteSection section) {
        return versionRepository.findFirstBySiteSectionIdAndStatusOrderByVersionNumberDesc(section.getId(), ContentStatus.DRAFT).orElse(null);
    }

    private int nextVersion(SiteSection section) {
        return versionRepository.findBySiteSectionIdOrderByVersionNumberDesc(section.getId()).stream()
                .findFirst().map(value -> value.getVersionNumber() + 1).orElse(1);
    }

    private void archiveDrafts(SiteSection section, java.util.UUID exceptId) {
        versionRepository.findBySiteSectionIdOrderByVersionNumberDesc(section.getId()).stream()
                .filter(version -> version.getStatus() == ContentStatus.DRAFT)
                .filter(version -> exceptId == null || !version.getId().equals(exceptId))
                .forEach(version -> version.setStatus(ContentStatus.ARCHIVED));
    }

    private SiteSection requiredSection(String key) {
        return sectionRepository.findBySectionKey(key).orElseThrow(() -> notFound("Secao nao encontrada."));
    }

    private AdminUser requiredUser(AdminPrincipal principal) {
        return userRepository.findById(principal.id()).orElseThrow(() -> notFound("Administrador nao encontrado."));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> sanitizeAndValidateShape(Map<String, Object> requested, Map<String, Object> baseline) {
        if (requested == null || !requested.keySet().equals(baseline.keySet())) {
            throw new BusinessException("A estrutura da secao nao pode ser alterada.", HttpStatus.BAD_REQUEST);
        }
        Object sanitized = sanitizeNode(requested, baseline, 0, "");
        return (Map<String, Object>) sanitized;
    }

    private Object sanitizeNode(Object requested, Object baseline, int depth, String fieldName) {
        if (depth > 8) throw new BusinessException("Conteudo excede a profundidade permitida.", HttpStatus.BAD_REQUEST);
        if (baseline instanceof Map<?, ?> baseMap) {
            if (!(requested instanceof Map<?, ?> requestMap) || !requestMap.keySet().equals(baseMap.keySet())) throw shapeError();
            Map<String, Object> result = new LinkedHashMap<>();
            baseMap.forEach((key, value) -> result.put(String.valueOf(key), sanitizeNode(requestMap.get(key), value, depth + 1, String.valueOf(key))));
            return result;
        }
        if (baseline instanceof List<?> baseList) {
            if (!(requested instanceof List<?> requestList) || requestList.size() != baseList.size()) throw shapeError();
            List<Object> result = new ArrayList<>();
            for (int index = 0; index < baseList.size(); index++) result.add(sanitizeNode(requestList.get(index), baseList.get(index), depth + 1, fieldName));
            return result;
        }
        if (!(baseline instanceof String) || !(requested instanceof String text)) throw shapeError();
        if (java.util.Set.of("icon", "number", "locale").contains(fieldName)) return baseline;
        String clean = text.strip().replaceAll("<[^>]*>", "").replaceAll("[<>]", "")
                .replaceAll("[\\p{Cc}\\p{Cf}&&[^\\r\\n\\t]]", "");
        if (clean.isBlank() || clean.length() > 4000) throw new BusinessException("Textos devem ter entre 1 e 4000 caracteres.", HttpStatus.BAD_REQUEST);
        if (java.util.Set.of("href", "buttonHref", "ctaHref").contains(fieldName)) return safeInternalLink(clean);
        if (fieldName.equals("email") && !clean.matches("(?i)^[a-z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?(?:\\.[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?)+$")) {
            throw new BusinessException("Informe um e-mail valido.", HttpStatus.BAD_REQUEST);
        }
        return clean;
    }

    private String safeInternalLink(String value) {
        if (value.length() > 500 || value.contains("\\") || value.chars().anyMatch(Character::isWhitespace)) {
            throw unsafeLink();
        }
        if (value.matches("#[A-Za-z][A-Za-z0-9_-]{0,79}")) return value;
        try {
            URI uri = URI.create(value);
            if (value.startsWith("/") && !value.startsWith("//") && !uri.isAbsolute()
                    && uri.getHost() == null && uri.getUserInfo() == null) return value;
        } catch (IllegalArgumentException ignored) {
            // Converted to a stable validation response below.
        }
        throw unsafeLink();
    }

    private BusinessException unsafeLink() {
        return new BusinessException("Links devem usar uma rota interna iniciada por / ou uma ancora iniciada por #.", HttpStatus.BAD_REQUEST);
    }

    private Map<String, Object> deepCopy(Map<String, Object> source) {
        return sanitizeAndValidateShape(source, source);
    }

    private BusinessException shapeError() { return new BusinessException("A estrutura da secao nao pode ser alterada.", HttpStatus.BAD_REQUEST); }
    private BusinessException notFound(String message) { return new BusinessException(message, HttpStatus.NOT_FOUND); }
}
