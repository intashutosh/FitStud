
package com.fitstud.backend.dto;

import java.time.Instant;
import java.util.UUID;

public record PublicProfileResponse(
        UUID id,
        String username,
        String displayName,
        String bio,
        String profilePhotoUrl,
        Instant createdAt
) {}
