package com.backlogr.dto.ticket;

import java.time.Instant;
import java.util.UUID;

public record TicketResponse(
    UUID id,
    String ticketKey,
    UUID workspaceId,
    UUID importedBy,
    String projectKey,
    String summary,
    Instant createdAt,
    Instant importedAt
) {}
