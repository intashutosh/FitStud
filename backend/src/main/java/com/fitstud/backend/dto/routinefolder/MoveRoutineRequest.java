package com.fitstud.backend.dto.routinefolder;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record MoveRoutineRequest(
        @NotNull(message = "Folder ID is required")
        UUID folderId
) {
}