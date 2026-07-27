package com.kodocode.api.content;

import com.kodocode.api.shared.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "site_sections")
public class SiteSection extends BaseEntity {

    @Column(name = "section_key", nullable = false, unique = true, length = 80)
    private String sectionKey;

    @Column(length = 180)
    private String title;

    @Column(length = 500)
    private String subtitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ContentStatus status = ContentStatus.DRAFT;

    @Column(name = "published_version_id")
    private UUID publishedVersionId;
}

