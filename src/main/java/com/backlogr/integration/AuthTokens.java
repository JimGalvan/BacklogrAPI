package com.backlogr.integration;

import java.time.Instant;

public record AuthTokens(
    String accessToken,
    String refreshToken,
    Instant tokenExpiry
) {}
