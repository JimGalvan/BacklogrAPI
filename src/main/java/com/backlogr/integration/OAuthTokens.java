package com.backlogr.integration;

import java.time.Instant;

public record OAuthTokens(
    String accessToken,
    String refreshToken,
    Instant tokenExpiry
) {}
