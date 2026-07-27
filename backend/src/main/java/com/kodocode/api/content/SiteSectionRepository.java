package com.kodocode.api.content;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteSectionRepository extends JpaRepository<SiteSection, UUID> {
    Optional<SiteSection> findBySectionKey(String sectionKey);
}

