package com.kodocode.api.auth;

import com.kodocode.api.admin.AdminRole;
import java.time.Instant;
import java.util.UUID;

public record AuthResponse(UUID id, String name, String email, AdminRole role, Instant accessTokenExpiresAt) {}

