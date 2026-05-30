package com.backlogr.domain.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TokenExchangeRequest(
    @JsonProperty("grant_type")  String grantType,
    @JsonProperty("client_id")   String clientId,
    @JsonProperty("client_secret") String clientSecret,
    String code,
    @JsonProperty("redirect_uri") String redirectUri
) {}
