package com.kodocode.api.publicsite;

import java.util.UUID;

public record PublicFaqResponse(
        UUID id,
        String question,
        String answer,
        int displayOrder
) {}
