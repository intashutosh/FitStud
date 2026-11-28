package com.fitstud.backend.dto.routinefolder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateRoutineFolderRequest(
        @NotBlank(message = "Folder name is required")
        @Size(max = 100, message = "Folder name cannot exceed 100 characters")
        String name
) {
}