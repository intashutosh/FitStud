
package com.fitstud.backend.dto;

import java.time.Instant;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String email,
        String username,
        String displayName,
        String bio,
        String profilePhotoUrl,
        Instant createdAt
) {}
