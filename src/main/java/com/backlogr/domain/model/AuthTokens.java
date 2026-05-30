package com.backlogr.domain.model;

import java.time.Instant;

public record AuthTokens(
    String accessToken,
    String refreshToken,
    Instant tokenExpiry
) {}
