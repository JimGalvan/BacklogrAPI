package com.backlogr.dto.ticket;

import com.backlogr.enums.ticket.TicketPriority;
import com.backlogr.enums.ticket.TicketSource;
import com.backlogr.enums.ticket.TicketStatus;

import java.time.Instant;
import java.util.UUID;

public record TicketItemResponse(
    UUID id,
    String externalId,
    String title,
    TicketStatus status,
    TicketPriority priority,
    TicketSource source,
    Instant createdAt
) {}
