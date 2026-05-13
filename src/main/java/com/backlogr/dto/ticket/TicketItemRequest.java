package com.backlogr.dto.ticket;

import com.backlogr.enums.ticket.TicketPriority;
import com.backlogr.enums.ticket.TicketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TicketItemRequest(
    @NotBlank
    String externalId,

    @NotBlank
    @Size(max = 255)
    String title,

    @Size(max = 5000)
    String description,

    @NotNull
    TicketStatus status,

    @NotNull
    TicketPriority priority,

    String assignee,

    @Positive
    Integer storyPoints,

    @Size(max = 10)
    List<String> tags
) {}
