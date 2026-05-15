package com.backlogr.integration.jira.oauth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TokenExchangeRequest(
    @JsonProperty("grant_type")  String grantType,
    @JsonProperty("client_id")   String clientId,
    @JsonProperty("client_secret") String clientSecret,
    String code,
    @JsonProperty("redirect_uri") String redirectUri
) {}
