package com.kodocode.api.lead;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record PublicContactRequest(
        @NotBlank(message = "Informe seu nome.") @Size(max = 120) @NoCode String name,
        @Size(max = 160) @NoCode String company,
        @NotBlank(message = "Informe seu e-mail.") @Email(message = "Informe um e-mail valido.") @Size(max = 254) String email,
        @NotBlank(message = "Informe seu telefone ou WhatsApp.")
        @Size(max = 15)
        @Pattern(regexp = "^\\(\\d{2}\\) (?:\\d{4}-\\d{4}|\\d{5}-\\d{4})$", message = "Use um telefone com DDD, por exemplo: (11) 99999-9999.") String phone,
        @NotNull(message = "Selecione o servico de interesse.") ServiceInterest serviceInterest,
        @NotNull(message = "Selecione uma faixa de orcamento.") BudgetRange budgetRange,
        @NotBlank(message = "Conte um pouco sobre seu projeto.") @Size(min = 20, max = 2000) @NoCode String message,
        @AssertTrue(message = "E necessario aceitar a politica de privacidade.") boolean privacyConsent,
        @NotNull @PastOrPresent Instant formStartedAt,
        @Size(max = 0) String website
) {}
