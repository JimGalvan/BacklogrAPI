package com.backlogr.domain.dto.jira;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AtlassianTokenResponse(
    @JsonProperty("access_token")  String accessToken,
    @JsonProperty("refresh_token") String refreshToken,
    @JsonProperty("expires_in")    long expiresIn,
    String scope
) {}
