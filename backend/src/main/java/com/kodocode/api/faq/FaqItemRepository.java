package com.kodocode.api.faq;

import com.kodocode.api.content.ContentStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FaqItemRepository extends JpaRepository<FaqItem, UUID> {
    List<FaqItem> findByStatusAndActiveTrueOrderByDisplayOrder(ContentStatus status);
}

