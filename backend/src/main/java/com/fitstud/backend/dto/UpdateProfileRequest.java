
package com.fitstud.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank
        @Size(max = 80)
        String displayName,

        @Size(max = 300)
        String bio,

        @Size(max = 2048)
        String profilePhotoUrl
) {}
