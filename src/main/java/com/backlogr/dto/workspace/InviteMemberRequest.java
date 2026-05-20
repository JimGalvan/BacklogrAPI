package com.backlogr.dto.workspace;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record InviteMemberRequest(
    @NotNull
    UUID userId
) {}
