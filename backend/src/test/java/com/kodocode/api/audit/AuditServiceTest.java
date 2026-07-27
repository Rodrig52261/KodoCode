package com.kodocode.api.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock AuditLogRepository repository;

    @Test
    void redactsSecretsBeforePersistence() {
        AuditService service = new AuditService(repository);

        service.record(null, "ADMIN@KODOCODE.COM.BR", AuditAction.LOGIN_FAILURE,
                "AdminUser", null, null,
                Map.of("password", "never-store-this", "reason", "invalid",
                        "items", List.of(Map.of("accessToken", "nested-secret"))), false,
                new AuditContext("127.0.0.1", "test"));

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getActorEmail()).isEqualTo("admin@kodocode.com.br");
        assertThat(captor.getValue().getNewData().get("password")).isEqualTo("[REDACTED]");
        assertThat(captor.getValue().getNewData().toString()).doesNotContain("nested-secret");
    }
}
