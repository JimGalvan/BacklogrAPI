package com.backlogr.domain.dto.ticket;

import jakarta.validation.constraints.NotBlank;

public record TicketImportRequest(
    @NotBlank String url
) {}
