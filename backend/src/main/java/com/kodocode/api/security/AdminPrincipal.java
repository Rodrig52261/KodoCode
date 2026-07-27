package com.kodocode.api.security;

import com.kodocode.api.admin.AdminRole;
import java.util.UUID;

public record AdminPrincipal(UUID id, String email, AdminRole role) {}

