package com.kodocode.api.faq;

import com.kodocode.api.content.ContentStatus;
import com.kodocode.api.shared.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "faq_items")
public class FaqItem extends BaseEntity {

    @Column(nullable = false, length = 300)
    private String question;

    @Column(nullable = false, length = 2000)
    private String answer;

    @Column(name = "draft_question", length = 300)
    private String draftQuestion;

    @Column(name = "draft_answer", length = 2000)
    private String draftAnswer;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(nullable = false)
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ContentStatus status = ContentStatus.DRAFT;
}
