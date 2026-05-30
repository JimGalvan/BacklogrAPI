package com.backlogr.domain.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TokenRefreshRequest(
    @JsonProperty("grant_type")    String grantType,
    @JsonProperty("client_id")     String clientId,
    @JsonProperty("client_secret") String clientSecret,
    @JsonProperty("refresh_token") String refreshToken
) {}
