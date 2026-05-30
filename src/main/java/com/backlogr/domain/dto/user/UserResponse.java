package com.backlogr.domain.dto.user;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String email,
    String name,
    Instant createdAt,
    Instant lastModifiedAt
) {}
