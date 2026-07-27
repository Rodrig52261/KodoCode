package com.kodocode.api.faq;

import com.kodocode.api.admin.AdminUser;
import com.kodocode.api.admin.AdminUserRepository;
import com.kodocode.api.audit.AuditAction;
import com.kodocode.api.audit.AuditContext;
import com.kodocode.api.audit.AuditService;
import com.kodocode.api.security.AdminPrincipal;
import com.kodocode.api.shared.web.BusinessException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminFaqService {
    private final FaqItemRepository repository;
    private final AdminUserRepository users;
    private final AuditService audit;

    public AdminFaqService(FaqItemRepository repository, AdminUserRepository users, AuditService audit) {
        this.repository = repository;
        this.users = users;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public List<FaqAdminResponse> list() {
        return repository.findAll().stream().sorted(java.util.Comparator.comparingInt(FaqItem::getDisplayOrder)).map(this::response).toList();
    }

    @Transactional
    public FaqAdminResponse draft(UUID id, FaqUpdateRequest request, AdminPrincipal principal, AuditContext context) {
        FaqItem item = required(id);
        String question = clean(request.question(), 300);
        String answer = clean(request.answer(), 2000);
        item.setDraftQuestion(question);
        item.setDraftAnswer(answer);
        item.setDisplayOrder(request.displayOrder());
        item.setActive(request.active());
        AdminUser actor = actor(principal);
        audit.record(actor, actor.getEmail(), AuditAction.FAQ_UPDATE, "FaqItem", id.toString(),
                Map.of("question", item.getQuestion(), "answer", item.getAnswer()), Map.of("question", question, "answer", answer), true, context);
        return response(item);
    }

    @Transactional
    public FaqAdminResponse publish(UUID id, AdminPrincipal principal, AuditContext context) {
        FaqItem item = required(id);
        if (item.getDraftQuestion() == null || item.getDraftAnswer() == null) {
            throw new BusinessException("Nenhum rascunho disponivel.", HttpStatus.CONFLICT);
        }
        Map<String, Object> previous = Map.of("question", item.getQuestion(), "answer", item.getAnswer());
        item.setQuestion(item.getDraftQuestion());
        item.setAnswer(item.getDraftAnswer());
        item.setDraftQuestion(null);
        item.setDraftAnswer(null);
        item.setStatus(com.kodocode.api.content.ContentStatus.PUBLISHED);
        AdminUser actor = actor(principal);
        audit.record(actor, actor.getEmail(), AuditAction.FAQ_PUBLISH, "FaqItem", id.toString(), previous,
                Map.of("question", item.getQuestion(), "answer", item.getAnswer()), true, context);
        return response(item);
    }

    private FaqItem required(UUID id) { return repository.findById(id).orElseThrow(() -> new BusinessException("FAQ nao encontrada.", HttpStatus.NOT_FOUND)); }
    private AdminUser actor(AdminPrincipal principal) { return users.findById(principal.id()).orElseThrow(() -> new BusinessException("Administrador nao encontrado.", HttpStatus.NOT_FOUND)); }
    private String clean(String value, int max) {
        if (value == null) throw new BusinessException("Preencha todos os campos.", HttpStatus.BAD_REQUEST);
        String clean = value.strip().replaceAll("<[^>]*>", "").replaceAll("[<>]", "");
        if (clean.isBlank() || clean.length() > max) throw new BusinessException("Conteudo fora do limite permitido.", HttpStatus.BAD_REQUEST);
        return clean;
    }
    private FaqAdminResponse response(FaqItem item) { return new FaqAdminResponse(item.getId(), item.getQuestion(), item.getAnswer(), item.getDraftQuestion(), item.getDraftAnswer(), item.getDisplayOrder(), item.isActive(), item.getStatus(), item.getUpdatedAt()); }

    public record FaqUpdateRequest(
            @NotBlank @Size(max = 300) String question,
            @NotBlank @Size(max = 2000) String answer,
            @Min(0) @Max(1000) int displayOrder,
            boolean active) {}
    public record FaqAdminResponse(UUID id, String question, String answer, String draftQuestion, String draftAnswer,
            int displayOrder, boolean active, com.kodocode.api.content.ContentStatus status, Instant updatedAt) {}
}
