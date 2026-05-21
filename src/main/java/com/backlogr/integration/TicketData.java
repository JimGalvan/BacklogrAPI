package com.backlogr.integration;

import com.backlogr.enums.ticket.TicketPriority;
import com.backlogr.enums.ticket.TicketStatus;

import java.time.Instant;
import java.util.List;

public record TicketData(
    String key,
    String title,
    String description,
    TicketStatus status,
    TicketPriority priority,
    String assignee,
    Integer storyPoints,
    List<String> tags,
    Instant externalCreatedAt
) {}
