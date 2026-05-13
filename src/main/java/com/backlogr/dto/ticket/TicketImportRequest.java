package com.backlogr.dto.ticket;

import jakarta.validation.constraints.NotBlank;

public record TicketImportRequest(
    @NotBlank
    String url
) {}
