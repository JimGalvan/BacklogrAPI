package com.backlogr.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
    @NotBlank
    String refreshToken
) {}
