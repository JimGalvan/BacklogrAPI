package com.backlogr.dto.ticket;

import com.backlogr.enums.ticket.TicketSource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TicketImportRequest(
    @NotNull
    TicketSource source,

    @NotNull
    @NotEmpty
    @Size(max = 500, message = "A single import batch may not exceed 500 tickets")
    List<@Valid TicketItemRequest> tickets
) {}
