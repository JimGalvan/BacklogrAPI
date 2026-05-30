package com.backlogr.domain.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
    @NotBlank @Email
    String email,
    @NotBlank @Size(min = 8)
    String password,
    String name
) {}
