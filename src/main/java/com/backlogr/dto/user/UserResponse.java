package com.backlogr.dto.user;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String email,
    Instant createdAt,
    Instant lastModifiedAt
) {}
