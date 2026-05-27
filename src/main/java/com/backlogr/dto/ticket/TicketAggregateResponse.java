package com.backlogr.dto.ticket;

import com.backlogr.enums.Provider;
import com.backlogr.enums.ticket.TicketPriority;
import com.backlogr.enums.ticket.TicketStatus;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TicketAggregateResponse(
    UUID id,
    String ticketKey,
    UUID workspaceId,
    UUID importedBy,
    String projectKey,
    String title,
    Provider provider,
    JsonNode description,
    TicketStatus status,
    TicketPriority priority,
    String assignee,
    Integer storyPoints,
    List<String> tags,
    Instant createdAt,
    Instant importedAt
) {}
