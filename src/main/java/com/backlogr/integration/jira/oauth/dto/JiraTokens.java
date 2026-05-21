package com.backlogr.integration.jira.oauth.dto;

import java.time.Instant;

public record JiraTokens(
    String accessToken,
    String refreshToken,
    Instant tokenExpiry
) {}
