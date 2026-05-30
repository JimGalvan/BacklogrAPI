package com.backlogr.domain.dto.workspace;

import java.time.Instant;
import java.util.UUID;

public record WorkspaceMemberResponse(
    UUID userId,
    String email,
    String name,
    Instant joinedAt
) {}
