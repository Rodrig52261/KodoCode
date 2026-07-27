package com.kodocode.api.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Informe o e-mail.")
        @Email(message = "Informe um e-mail valido.")
        @Size(max = 254)
        String email,

        @NotBlank(message = "Informe a senha.")
        @Size(max = 128, message = "Credenciais invalidas.")
        String password
) {}

