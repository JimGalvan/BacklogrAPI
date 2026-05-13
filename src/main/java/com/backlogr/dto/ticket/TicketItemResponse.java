package com.backlogr.dto.ticket;

import com.backlogr.enums.ticket.TicketPriority;
import com.backlogr.enums.ticket.TicketSource;
import com.backlogr.enums.ticket.TicketStatus;

import java.time.Instant;

public record TicketItemResponse(
    Long id,
    String externalId,
    String title,
    TicketStatus status,
    TicketPriority priority,
    TicketSource source,
    Instant createdAt
) {}
