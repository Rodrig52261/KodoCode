package com.kodocode.api.content;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SiteContentVersionRepository extends JpaRepository<SiteContentVersion, UUID> {
    List<SiteContentVersion> findBySiteSectionIdOrderByVersionNumberDesc(UUID siteSectionId);
    Optional<SiteContentVersion> findFirstBySiteSectionIdAndStatusOrderByVersionNumberDesc(UUID siteSectionId, ContentStatus status);
    Optional<SiteContentVersion> findByIdAndSiteSectionId(UUID id, UUID siteSectionId);
    List<SiteContentVersion> findTop8ByCreatedByIsNotNullOrderByCreatedAtDesc();

    @Query("""
            select version from SiteContentVersion version
            join fetch version.siteSection section
            where version.id = section.publishedVersionId
              and version.status = :status
              and section.status = :status
            order by section.sectionKey
            """)
    List<SiteContentVersion> findPublishedVersions(@Param("status") ContentStatus status);
}
