package com.kodocode.api.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "kodo")
public record ApplicationProperties(
        @Valid Jwt jwt,
        @Valid Cookie cookie,
        @Valid Cors cors,
        @Valid Security security,
        @Valid BootstrapAdmin bootstrapAdmin,
        @Valid Contact contact,
        @Valid Email email
) {
    public record Jwt(
            @NotBlank @Size(min = 32, max = 512) String secret,
            @NotNull Duration accessTokenTtl,
            @NotNull Duration refreshTokenTtl,
            @NotBlank String issuer
    ) {}

    public record Cookie(boolean secure,
            @NotBlank @Pattern(regexp = "(?i)Strict|Lax|None") String sameSite,
            String domain) {}

    public record Cors(@NotEmpty List<@Pattern(regexp = "https?://[^*\\s]+") String> allowedOrigins) {}

    public record Security(
            @Min(1) int maxFailedLoginAttempts,
            @NotNull Duration accountLockDuration,
            @Min(1) int loginRateLimitPerMinute
    ) {}

    public record BootstrapAdmin(boolean enabled, String name, String email, String password) {}

    public record Contact(
            @NotNull Duration minimumSubmissionDuration,
            @NotNull Duration duplicateWindow,
            @Min(1) int rateLimitPerHour
    ) {}

    public record Email(
            boolean enabled,
            String notificationTo,
            @Valid EmailJs emailJs
    ) {}

    public record EmailJs(
            String endpoint,
            String serviceId,
            String notificationTemplateId,
            String confirmationTemplateId,
            String publicKey,
            String privateKey,
            @NotNull Duration minimumRequestInterval
    ) {}
}
