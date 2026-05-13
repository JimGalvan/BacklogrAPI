package com.backlogr.dto.ticket;

import com.backlogr.enums.ticket.TicketPriority;
import com.backlogr.enums.ticket.TicketSource;
import com.backlogr.enums.ticket.TicketStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TicketResponse(
    UUID id,
    String key,
    String url,
    String title,
    String description,
    TicketStatus status,
    TicketPriority priority,
    TicketSource source,
    String assignee,
    Integer storyPoints,
    List<String> tags,
    Instant createdAt,
    Instant lastModifiedAt
) {}
