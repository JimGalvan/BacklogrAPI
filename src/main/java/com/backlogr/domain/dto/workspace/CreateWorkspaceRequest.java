package com.backlogr.domain.dto.workspace;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateWorkspaceRequest(
    @NotBlank
    String name,
    @NotNull
    UUID ownerId
) {}
